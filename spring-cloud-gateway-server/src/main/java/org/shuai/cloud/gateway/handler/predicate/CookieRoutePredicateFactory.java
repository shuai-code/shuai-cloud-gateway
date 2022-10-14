package org.shuai.cloud.gateway.handler.predicate;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpCookie;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.function.Predicate;

/**
 * 断言工厂-检查请求cookie
 * @author Yangs
 */
public class CookieRoutePredicateFactory extends AbstractRoutePredicateFactory<CookieRoutePredicateFactory.Config> {

    public CookieRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange serverWebExchange) {
                // 从请求中获取需要检查得cookie名字
                List<HttpCookie> cookies = serverWebExchange.getRequest().getCookies().get(config.name);
                // 没有则不通过
                if (cookies == null) {
                    return false;
                }
                // 存在符合正则规则得cookie则通过
                for (HttpCookie cookie : cookies) {
                    if (cookie.getValue().matches(config.getRegexp())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static class Config {
        @NotEmpty
        private String name;

        @NotEmpty
        private String regexp;

        public String getName() {
            return name;
        }

        public Config setName(String name) {
            this.name = name;
            return this;
        }

        public String getRegexp() {
            return regexp;
        }

        public Config setRegexp(String regexp) {
            this.regexp = regexp;
            return this;
        }
    }
}
