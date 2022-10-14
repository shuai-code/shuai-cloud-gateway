package org.shuai.cloud.gateway.handler.predicate;

import org.shuai.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 断言工厂-检查请求Host
 * 解析Host之后会放回到ServerWebExchange中存储
 * @author Yangs
 */
public class HostRoutePredicateFactory extends AbstractRoutePredicateFactory<HostRoutePredicateFactory.Config> {

    private PathMatcher pathMatcher = new AntPathMatcher(".");

    public HostRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange serverWebExchange) {
                String host = serverWebExchange.getRequest().getHeaders().getFirst("Host");
                String match = null;
                for (int i = 0; i < config.getPatterns().size(); i++) {
                    String pattern = config.getPatterns().get(i);
                    if (pathMatcher.match(pattern, host)) {
                        match = pattern;
                        break;
                    }
                }

                if (match != null) {
                    // 模式"/hotels/{hotel}"和路径"/hotels/1", 返回hotel->1的Map
                    Map<String, String> variables = pathMatcher.extractUriTemplateVariables(match, host);
                    ServerWebExchangeUtils.putUriTemplateVariables(serverWebExchange, variables);
                    return true;
                }
                return false;
            }
        };
    }

    public static class Config {
        private List<String> patterns = new ArrayList<>();

        public List<String> getPatterns() {
            return patterns;
        }

        public Config setPatterns(List<String> patterns) {
            this.patterns = patterns;
            return this;
        }

        @Override
        public String toString() {
            return new ToStringCreator(this).append("patterns", patterns).toString();
        }
    }
}
