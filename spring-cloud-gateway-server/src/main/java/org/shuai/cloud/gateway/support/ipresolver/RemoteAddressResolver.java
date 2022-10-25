package org.shuai.cloud.gateway.support.ipresolver;

import org.springframework.web.server.ServerWebExchange;

import java.net.InetSocketAddress;

/**
 * @author Yangs
 */
public interface RemoteAddressResolver {

    default InetSocketAddress resolve(ServerWebExchange exchange) {
        return exchange.getRequest().getRemoteAddress();
    }
}
