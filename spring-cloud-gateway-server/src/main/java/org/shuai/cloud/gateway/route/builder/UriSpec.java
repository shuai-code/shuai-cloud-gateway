package org.shuai.cloud.gateway.route.builder;

import org.shuai.cloud.gateway.route.Route;

/**
 * @author Yangs
 */
public class UriSpec {

    final Route.AsyncBuilder routeBuilder;

    final RouteLocatorBuilder.Builder builder;

    UriSpec(Route.AsyncBuilder routeBuilder, RouteLocatorBuilder.Builder builder) {
        this.routeBuilder = routeBuilder;
        this.builder = builder;
    }

    <T> T getBean(Class<T> type) {
        return this.builder.getContext().getBean(type);
    }
}
