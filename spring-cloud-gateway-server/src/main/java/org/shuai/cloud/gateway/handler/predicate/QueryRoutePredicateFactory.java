package org.shuai.cloud.gateway.handler.predicate;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.function.Predicate;

/**
 * 断言工厂-检查请求参数
 * @author Yangs
 */
public class QueryRoutePredicateFactory extends AbstractRoutePredicateFactory<QueryRoutePredicateFactory.Config> {

    public QueryRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange serverWebExchange) {
                // 未配置规则, 则只判断是否有指定参数
                if (!StringUtils.hasText(config.regexp)) {
                    return serverWebExchange.getRequest().getQueryParams().containsKey(config.param);
                }
                // 配置了正则规则, 则判断是否有匹配的参数
                List<String> values = serverWebExchange.getRequest().getQueryParams().get(config.param);
                if (values == null) {
                    return false;
                }
                for (String value : values) {
                    if (value != null && value.matches(config.regexp)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static class Config {
        @NotEmpty
        private String param;

        private String regexp;

        public String getParam() {
            return param;
        }

        public Config setParam(String param) {
            this.param = param;
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
