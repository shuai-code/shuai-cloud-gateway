package org.shuai.cloud.gateway.route;

import reactor.core.publisher.Flux;

/**
 * @author Yangs
 */
public interface RouteLocator {

    Flux<Route> getRoutes();
}
