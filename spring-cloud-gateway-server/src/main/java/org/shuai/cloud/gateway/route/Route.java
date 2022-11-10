package org.shuai.cloud.gateway.route;

import org.shuai.cloud.gateway.filter.GatewayFilter;
import org.shuai.cloud.gateway.handler.AsyncPredicate;
import org.shuai.cloud.gateway.route.builder.Buildable;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.shuai.cloud.gateway.support.ServerWebExchangeUtils.toAsyncPredicate;

/**
 * @author Yangs
 */
public class Route implements Ordered {

    private final String id;

    private final URI uri;

    private final List<GatewayFilter> gatewayFilters;

    private final AsyncPredicate<ServerWebExchange> predicate;

    private final int order;

    private final Map<String, Object> metadata;

    public Route(String id, URI uri, List<GatewayFilter> gatewayFilters, AsyncPredicate<ServerWebExchange> predicate, int order, Map<String, Object> metadata) {
        this.id = id;
        this.uri = uri;
        this.gatewayFilters = gatewayFilters;
        this.predicate = predicate;
        this.order = order;
        this.metadata = metadata;
    }
    public String getId() {
        return this.id;
    }

    public URI getUri() {
        return this.uri;
    }

    public int getOrder() {
        return order;
    }

    public static AsyncBuilder async() {
        return new AsyncBuilder();
    }

    public abstract static class AbstractBuilder<B extends AbstractBuilder<B>> implements Buildable<Route> {

        protected String id;

        protected URI uri;

        protected int order = 0;

        public AbstractBuilder() {

        }

        protected abstract B getThis();

        public B id(String id) {
            this.id = id;
            return getThis();
        }

        public String getId() {
            return id;
        }

        public Route build() {

        }
    }

    public static class AsyncBuilder extends AbstractBuilder<AsyncBuilder> {

        protected AsyncPredicate<ServerWebExchange> predicate;

        public AsyncBuilder predicate(Predicate<ServerWebExchange> predicate) {
            asyncPredicate(toAsyncPredicate(predicate));
            return this;
        }

        public AsyncBuilder asyncPredicate(AsyncPredicate<ServerWebExchange> predicate) {
            this.predicate = predicate;
            return this;
        }

        @Override
        protected AsyncBuilder getThis() {
            return this;
        }
    }
}
