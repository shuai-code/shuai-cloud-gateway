package org.shuai.cloud.gateway.support;

import io.netty.buffer.Unpooled;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shuai.cloud.gateway.handler.AsyncPredicate;
import org.springframework.core.io.buffer.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Yangs
 */
public class ServerWebExchangeUtils {

    private static final Log log = LogFactory.getLog(ServerWebExchangeUtils.class);

    public static final String URI_TEMPLATE_VARIABLES_ATTRIBUTE = qualify("uriTemplateVariables");

    public static final String GATEWAY_PREDICATE_MATCHED_PATH_ATTR = qualify("gatewayPredicateMatchedPathAttr");

    public static final String GATEWAY_PREDICATE_MATCHED_PATH_ROUTE_ID_ATTR = qualify("gatewayPredicateMatchedPathRouteIdAttr");

    public static final String GATEWAY_PREDICATE_ROUTE_ATTR = qualify("gatewayPredicateRouteAttr");

    public static final String CACHED_REQUEST_BODY_ATTR = "cachedRequestBody";

    public static final String CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR = "cachedServerHttpRequestDecorator";

    private static final byte[] EMPTY_BYTES = {};

    public static AsyncPredicate<ServerWebExchange> toAsyncPredicate(Predicate<? super ServerWebExchange> predicate) {
        Assert.notNull(predicate, "predicate must not be null");
        return AsyncPredicate.from(predicate);
    }

    @SuppressWarnings("unchecked")
    public static void putUriTemplateVariables(ServerWebExchange exchange, Map<String, String> uriVariables) {
        if (exchange.getAttributes().containsKey(URI_TEMPLATE_VARIABLES_ATTRIBUTE)) {
            Map<String, Object> existingVariables = (Map<String, Object>) exchange.getAttributes().get(URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            HashMap<String, Object> newVariables = new HashMap<>();
            newVariables.putAll(existingVariables);
            newVariables.putAll(uriVariables);
            exchange.getAttributes().put(URI_TEMPLATE_VARIABLES_ATTRIBUTE, newVariables);
        } else {
            exchange.getAttributes().put(URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriVariables);
        }
    }

    public static <T> Mono<T> cacheRequestBodyAndRequest(ServerWebExchange exchange, Function<ServerHttpRequest, Mono<T>> function) {
        return cacheRequestBody(exchange, true, function);
    }

    private static <T> Mono<T> cacheRequestBody(ServerWebExchange exchange, boolean cacheDecoratedRequest, Function<ServerHttpRequest, Mono<T>> function) {
        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory factory = response.bufferFactory();
        // Join all the DataBuffers so we have a single DataBuffer for the body
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .defaultIfEmpty(factory.wrap(EMPTY_BYTES))
                .map(dataBuffer -> decorate(exchange, dataBuffer, cacheDecoratedRequest))
                .switchIfEmpty(Mono.just(exchange.getRequest()))
                .flatMap(function);
    }

    private static ServerHttpRequest decorate(ServerWebExchange exchange, DataBuffer dataBuffer, boolean cacheDecoratedRequest) {
        if (dataBuffer.readableByteCount() > 0) {
            if (log.isTraceEnabled()) {
                log.trace("retaining body in exchange attribute");
            }
            exchange.getAttributes().put(CACHED_REQUEST_BODY_ATTR, dataBuffer);
        }

        ServerHttpRequest decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public Flux<DataBuffer> getBody() {
                return Mono.fromSupplier(() -> {
                    if (exchange.getAttributeOrDefault(CACHED_REQUEST_BODY_ATTR, null) == null) {
                        // probably == downstream closed or no body
                        return null;
                    }
                    if (dataBuffer instanceof NettyDataBuffer) {
                        NettyDataBuffer pdb = (NettyDataBuffer) dataBuffer;
                        return pdb.factory().wrap(pdb.getNativeBuffer().retainedSlice());
                    } else if (dataBuffer instanceof DefaultDataBuffer) {
                        DefaultDataBuffer ddf = (DefaultDataBuffer) dataBuffer;
                        return ddf.factory().wrap(Unpooled.wrappedBuffer(ddf.getNativeBuffer()).nioBuffer());
                    } else {
                        throw new IllegalArgumentException("Unable to handle DataBuffer of type " + dataBuffer.getClass());
                    }
                }).flux();
            }
        };
        if (cacheDecoratedRequest) {
            exchange.getAttributes().put(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR, decorator);
        }
        return decorator;
    }

    private static String qualify(String attr) {
        return ServerWebExchangeUtils.class.getName() + "." + attr;
    }
}
