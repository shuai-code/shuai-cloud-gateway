package org.shuai.cloud.gateway.handler.predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shuai.cloud.gateway.support.ipresolver.XForwardedRemoteAddressResolver;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Yangs
 */
public class XForwardedRemoteAddrRoutePredicateFactory extends AbstractRoutePredicateFactory<XForwardedRemoteAddrRoutePredicateFactory.Config> {

    private static final Log log = LogFactory.getLog(XForwardedRemoteAddrRoutePredicateFactory.class);

    public XForwardedRemoteAddrRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        if (log.isDebugEnabled()) {
            log.debug("Applying XForwardedRemoteAddr route predicate with maxTrustedIndex of " + config.getMaxTrustedIndex() + " for " + config.getSources().size() + " source(s)");
        }
        // 重用标准的RemoteAddrRoutePredicateFactory, 但不是使用默认的RemoteAddressResolver来确定客户端IP地址, 而是使用XForwardedRemoteAddressResolver
        RemoteAddrRoutePredicateFactory.Config wrappedConfig = new RemoteAddrRoutePredicateFactory.Config();
        wrappedConfig.setSources(config.getSources());
        wrappedConfig.setRemoteAddressResolver(XForwardedRemoteAddressResolver.maxTrustedIndex(config.getMaxTrustedIndex()));

        RemoteAddrRoutePredicateFactory remoteAddrRoutePredicateFactory = new RemoteAddrRoutePredicateFactory();
        Predicate<ServerWebExchange> wrappedPredicate = remoteAddrRoutePredicateFactory.apply(wrappedConfig);

        return serverWebExchange -> {
            boolean isAllowed = wrappedPredicate.test(serverWebExchange);
            if (log.isDebugEnabled()) {
                ServerHttpRequest request = serverWebExchange.getRequest();
                log.debug("Request for \"" + request.getURI() + "\" from client \""
                        + request.getRemoteAddress().getAddress().getHostAddress() + "\" with \""
                        + XForwardedRemoteAddressResolver.X_FORWARDED_FOR + "\" header value of \""
                        + request.getHeaders().get(XForwardedRemoteAddressResolver.X_FORWARDED_FOR) + "\" is "
                        + (isAllowed ? "ALLOWED" : "NOT ALLOWED"));
            }
            return isAllowed;
        };
    }

    public static class Config {
        // 默认情况下, 信任"x-forward-for"报头中的最后一个值, 该值表示调用网关时使用的最后一个反向代理
        private int maxTrustedIndex = 1;

        private List<String> sources = new ArrayList<>();

        public int getMaxTrustedIndex() {
            return this.maxTrustedIndex;
        }

        public Config setMaxTrustedIndex(int maxTrustedIndex) {
            this.maxTrustedIndex = maxTrustedIndex;
            return this;
        }

        public List<String> getSources() {
            return this.sources;
        }

        public Config setSources(List<String> sources) {
            this.sources = sources;
            return this;
        }

        public Config setSources(String... sources) {
            this.sources = Arrays.asList(sources);
            return this;
        }
    }
}
