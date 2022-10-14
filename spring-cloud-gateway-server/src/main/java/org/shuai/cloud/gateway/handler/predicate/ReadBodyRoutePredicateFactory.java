package org.shuai.cloud.gateway.handler.predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reactivestreams.Publisher;
import org.shuai.cloud.gateway.handler.AsyncPredicate;
import org.shuai.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Yangs
 */
public class ReadBodyRoutePredicateFactory extends AbstractRoutePredicateFactory<ReadBodyRoutePredicateFactory.Config> {

    protected static final Log log = LogFactory.getLog(ReadBodyRoutePredicateFactory.class);

    private static final String TEST_ATTRIBUTE = "read_body_predicate_test_attribute";

    private static final String CACHE_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";

    private final List<HttpMessageReader<?>> messageReaders;

    public ReadBodyRoutePredicateFactory() {
        super(Config.class);
        this.messageReaders = HandlerStrategies.withDefaults().messageReaders();
    }

    public ReadBodyRoutePredicateFactory(List<HttpMessageReader<?>> messageReaders) {
        super(Config.class);
        this.messageReaders = messageReaders;
    }

    @Override
    public AsyncPredicate<ServerWebExchange> applyAsync(Config config) {
        return new AsyncPredicate<ServerWebExchange>() {
            @Override
            public Publisher<Boolean> apply(ServerWebExchange serverWebExchange) {
                Class inClass = config.getInClass();

                Object cachedBody = serverWebExchange.getAttribute(CACHE_REQUEST_BODY_OBJECT_KEY);
                Mono<?> modifiedBody;
                if (cachedBody != null) {
                    try {
                        boolean test = config.predicate.test(cachedBody);
                        serverWebExchange.getAttributes().put(TEST_ATTRIBUTE, test);
                        return Mono.just(test);
                    } catch (ClassCastException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Predicate test failed because class in predicate "
                                    + "does not match the cached body object", e);
                        }
                    }
                    return Mono.just(false);
                } else {
                    return ServerWebExchangeUtils.cacheRequestBodyAndRequest(serverWebExchange,
                            (serverHttpRequest) -> ServerRequest
                                    .create(serverWebExchange.mutate().request(serverHttpRequest).build(), messageReaders)
                                    .bodyToMono(inClass).doOnNext(objectValue -> serverWebExchange.getAttributes()
                                            .put(CACHE_REQUEST_BODY_OBJECT_KEY, objectValue))
                                    .map(objectValue -> config.getPredicate().test(objectValue)));
                }
            }
        };
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        throw new UnsupportedOperationException("ReadBodyPredicateFactory is only async.");
    }

    public static class Config {
        private Class inClass;

        private Predicate predicate;

        private Map<String, Object> hints;

        public Class getInClass() {
            return inClass;
        }

        public Config setInClass(Class inClass) {
            this.inClass = inClass;
            return this;
        }

        public Predicate getPredicate() {
            return predicate;
        }

        public Config setPredicate(Predicate predicate) {
            this.predicate = predicate;
            return this;
        }

        public <T> Config setPredicate(Class<T> inClass, Predicate<T> predicate) {
            setInClass(inClass);
            this.predicate = predicate;
            return this;
        }

        public Map<String, Object> getHints() {
            return hints;
        }

        public Config setHints(Map<String, Object> hints) {
            this.hints = hints;
            return this;
        }
    }
}
