package org.shuai.cloud.gateway.handler.predicate;

import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Predicate;

import static java.util.Arrays.stream;

/**
 * 断言工厂-检查URL请求方法
 * @author Yangs
 */
public class MethodRoutePredicateFactory extends AbstractRoutePredicateFactory<MethodRoutePredicateFactory.Config> {

    public MethodRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange serverWebExchange) {
                HttpMethod requestMethod = serverWebExchange.getRequest().getMethod();
                return stream(config.getMethods()).anyMatch(httpMethod -> httpMethod == requestMethod);
            }
        };
    }

    public static class Config {
        private HttpMethod[] methods;

        public HttpMethod[] getMethods() {
            return methods;
        }

        public void setMethods(HttpMethod... methods) {
            this.methods = methods;
        }
    }
}
