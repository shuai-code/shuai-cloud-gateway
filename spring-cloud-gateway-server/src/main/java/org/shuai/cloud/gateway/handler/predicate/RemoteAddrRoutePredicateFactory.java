package org.shuai.cloud.gateway.handler.predicate;

import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shuai.cloud.gateway.support.ipresolver.RemoteAddressResolver;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Yangs
 */
public class RemoteAddrRoutePredicateFactory extends AbstractRoutePredicateFactory<RemoteAddrRoutePredicateFactory.Config> {

    private static final Log log = LogFactory.getLog(RemoteAddrRoutePredicateFactory.class);

    public RemoteAddrRoutePredicateFactory() {
        super(Config.class);
    }

    private List<IpSubnetFilterRule> convert(List<String> values) {
        List<IpSubnetFilterRule> sources = new ArrayList<>();
        for (String arg : values) {
            addSource(sources, arg);
        }
        return sources;
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        List<IpSubnetFilterRule> sources = convert(config.sources);

        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange serverWebExchange) {
                InetSocketAddress remoteAddress = config.remoteAddressResolver.resolve(serverWebExchange);
                if (remoteAddress != null && remoteAddress.getAddress() != null) {
                    String hostAddress = remoteAddress.getAddress().getHostAddress();
                    String host = serverWebExchange.getRequest().getURI().getHost();

                    if (log.isDebugEnabled() && !hostAddress.equals(host)) {
                        log.debug("Remote addresses didn't match " + hostAddress + " != " + host);
                    }

                    for (IpSubnetFilterRule source : sources) {
                        if (source.matches(remoteAddress)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

    private void addSource(List<IpSubnetFilterRule> sources, String source) {
        if (!source.contains("/")) {
            source = source + "/32";
        }
        String[] ipAddressCidrPrefix = source.split("/", 2);
        String ipAddress = ipAddressCidrPrefix[0];
        int cidrPrefix = Integer.parseInt(ipAddressCidrPrefix[1]);

        sources.add(new IpSubnetFilterRule(ipAddress, cidrPrefix, IpFilterRuleType.ACCEPT));
    }

    @Validated
    public static class Config {
        @NotEmpty
        private List<String> sources = new ArrayList<>();

        @NotNull
        private RemoteAddressResolver remoteAddressResolver = new RemoteAddressResolver() {};

        private List<String> getSources() {
            return sources;
        }

        public Config setSources(List<String> sources) {
            this.sources = sources;
            return this;
        }

        public Config setSources(String... sources) {
            this.sources = Arrays.asList(sources);
            return this;
        }

        public Config setRemoteAddressResolver(RemoteAddressResolver remoteAddressResolver) {
            this.remoteAddressResolver = remoteAddressResolver;
            return this;
        }
    }
}
