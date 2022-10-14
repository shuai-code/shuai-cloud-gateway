package org.shuai.cloud.gateway.handler.predicate;

import org.springframework.web.server.ServerWebExchange;

import java.util.function.Predicate;

/**
 * @author Yangs
 */
public interface GatewayPredicate extends Predicate<ServerWebExchange> {
}
