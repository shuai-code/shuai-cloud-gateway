package org.shuai.cloud.gateway.handler.predicate;

import org.springframework.web.server.ServerWebExchange;

import java.util.function.Predicate;

/**
 * @author Yangs
 */
public class CloudFoundryRouteServiceRoutePredicateFactory extends AbstractRoutePredicateFactory<Object> {

    /**
     * Forwarded URL header name.
     */
    public static final String X_CF_FORWARDED_URL = "X-CF-Forwarded-Url";

    /**
     * Proxy signature header name.
     */
    public static final String X_CF_PROXY_SIGNATURE = "X-CF-Proxy-Signature";

    /**
     * Proxy metadata header name.
     */
    public static final String X_CF_PROXY_METADATA = "X-CF-Proxy-Metadata";

    private final HeaderRoutePredicateFactory factory = new HeaderRoutePredicateFactory();

    public CloudFoundryRouteServiceRoutePredicateFactory() {
        super(Object.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Object unused) {
        return headerPredicate(X_CF_FORWARDED_URL).and(headerPredicate(X_CF_PROXY_SIGNATURE))
                .and(headerPredicate(X_CF_PROXY_METADATA));
    }

    private Predicate<ServerWebExchange> headerPredicate(String header) {
        HeaderRoutePredicateFactory.Config config = factory.newConfig();
        config.setHeader(header);
        config.setRegexp(".*");
        return factory.apply(config);
    }

}