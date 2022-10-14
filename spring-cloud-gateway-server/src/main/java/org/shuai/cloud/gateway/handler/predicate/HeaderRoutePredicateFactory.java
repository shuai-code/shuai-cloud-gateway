package org.shuai.cloud.gateway.handler.predicate;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * 断言工厂-检查请求Header
 * @author Yangs
 */
public class HeaderRoutePredicateFactory extends AbstractRoutePredicateFactory<HeaderRoutePredicateFactory.Config> {

    public HeaderRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        boolean hasRegex = !ObjectUtils.isEmpty(config.regexp);

        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange serverWebExchange) {
                List<String> values = serverWebExchange.getRequest().getHeaders().getOrDefault(config.getHeader(), Collections.emptyList());
                // 检查的header不存在, 不通过
                if (values == null) {
                    return false;
                }
                // 如果指定了检查规则
                if (hasRegex) {
                    // 有满足规则的header, 通过
                    for (int i = 0; i < values.size(); i++) {
                        String value = values.get(i);
                        if (value.matches(config.regexp)) {
                            return true;
                        }
                    }
                    // 没有满足规则的header, 不通过
                    return false;
                }
                // 未指定检查规则, header存在, 通过
                return true;
            }
        };
    }

    public static class Config {
        @NotEmpty
        private String header;

        private String regexp;

        public String getHeader() {
            return header;
        }

        public Config setHeader(String header) {
            this.header = header;
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
