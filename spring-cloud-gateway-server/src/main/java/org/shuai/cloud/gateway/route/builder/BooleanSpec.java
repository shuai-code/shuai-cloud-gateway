package org.shuai.cloud.gateway.route.builder;

import org.shuai.cloud.gateway.route.Route;

/**
 * @author Yangs
 */
public class BooleanSpec extends UriSpec {
    public BooleanSpec(Route.AsyncBuilder routeBuilder, RouteLocatorBuilder.Builder builder) {
        super(routeBuilder, builder);
    }
}
