package org.shuai.cloud.gateway.handler.predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.server.PathContainer;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.shuai.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_PREDICATE_MATCHED_PATH_ATTR;
import static org.shuai.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_PREDICATE_MATCHED_PATH_ROUTE_ID_ATTR;
import static org.shuai.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_PREDICATE_ROUTE_ATTR;
import static org.springframework.http.server.PathContainer.parsePath;
import static org.shuai.cloud.gateway.support.ServerWebExchangeUtils.putUriTemplateVariables;

/**
 * 断言工厂-检查请求路径
 * @author Yangs
 */
public class PathRoutePredicateFactory extends AbstractRoutePredicateFactory<PathRoutePredicateFactory.Config> {

    private static final Log log = LogFactory.getLog(PathRoutePredicateFactory.class);

    private PathPatternParser pathPatternParser = new PathPatternParser();

    public PathRoutePredicateFactory() {
        super(Config.class);
    }

    private static void traceMatch(String prefix, Object desired, Object actual, boolean match) {
        if (log.isTraceEnabled()) {
            String message = String.format("%s \"%s\" %s against value \"%s\"", prefix, desired, match ? "matches" : "does not match", actual);
            log.trace(message);
        }
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        final ArrayList<PathPattern> pathPatterns = new ArrayList<>();
        synchronized (this.pathPatternParser) {
            pathPatternParser.setMatchOptionalTrailingSeparator(config.isMatchTrailingSlash());
            config.getPatterns().forEach(pattern -> {
                PathPattern pathPattern = this.pathPatternParser.parse(pattern);
                pathPatterns.add(pathPattern);
            });
        }
        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange serverWebExchange) {
                // 解析路径, 根据/分隔符
                PathContainer path = parsePath(serverWebExchange.getRequest().getURI().getRawPath());
                // 找到第一个匹配的路径
                PathPattern match = null;
                for (int i = 0; i < pathPatterns.size(); i++) {
                    PathPattern pathPattern = pathPatterns.get(i);
                    if (pathPattern.matches(path)) {
                        match = pathPattern;
                        break;
                    }
                }
                if (match != null) {
                    // 日志记录
                    traceMatch("Pattern", match.getPatternString(), path, true);
                    // 提取URL模板变量和参数
                    PathPattern.PathMatchInfo pathMatchInfo = match.matchAndExtract(path);
                    putUriTemplateVariables(serverWebExchange, pathMatchInfo.getUriVariables());
                    serverWebExchange.getAttributes().put(GATEWAY_PREDICATE_MATCHED_PATH_ATTR, match.getPatternString());
                    String routeId = (String) serverWebExchange.getAttributes().get(GATEWAY_PREDICATE_ROUTE_ATTR);
                    if (routeId != null) {
                        serverWebExchange.getAttributes().put(GATEWAY_PREDICATE_MATCHED_PATH_ROUTE_ID_ATTR, routeId);
                    }
                    return true;
                } else {
                    traceMatch("Pattern", config.getPatterns(), path, false);
                    return false;
                }
            }
        };
    }

    @Validated
    public static class Config {

        private List<String> patterns = new ArrayList<>();

        private boolean matchTrailingSlash = true;

        public List<String> getPatterns() {
            return patterns;
        }

        public Config setPatterns(List<String> patterns) {
            this.patterns = patterns;
            return this;
        }

        public Config setMatchTrailingSlash(boolean matchTrailingSlash) {
            this.matchTrailingSlash = matchTrailingSlash;
            return this;
        }

        public boolean isMatchTrailingSlash() {
            return matchTrailingSlash;
        }
    }
}
