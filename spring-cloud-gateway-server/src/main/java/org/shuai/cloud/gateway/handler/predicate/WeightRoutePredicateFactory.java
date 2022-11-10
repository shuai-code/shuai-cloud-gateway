package org.shuai.cloud.gateway.handler.predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.shuai.cloud.gateway.support.WeightConfig;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;

import static org.shuai.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_PREDICATE_ROUTE_ATTR;
import static org.shuai.cloud.gateway.support.ServerWebExchangeUtils.WEIGHT_ATTR;

/**
 * 断言工厂-权重判断
 * @author Yangs
 */
public class WeightRoutePredicateFactory extends AbstractRoutePredicateFactory<WeightConfig> implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher publisher;

    private static final Log log = LogFactory.getLog(WeightRoutePredicateFactory.class);

    public WeightRoutePredicateFactory() {
        super(WeightConfig.class);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public Predicate<ServerWebExchange> apply(WeightConfig config) {
        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange serverWebExchange) {
                // 获取权重配置
                Map<String, String> weights = serverWebExchange.getAttributeOrDefault(WEIGHT_ATTR, Collections.emptyMap());
                // 获取路由ID
                String routeId = serverWebExchange.getAttribute(GATEWAY_PREDICATE_ROUTE_ATTR);
                // 获取组
                String group = config.getGroup();
                // 有匹配组的权重
                if (weights.containsKey(group)) {
                    // 获取组对应的权重
                    String chosenRoute = weights.get(group);
                    if (log.isTraceEnabled()) {
                        log.trace("in group weight: " + group + ", current route: " + routeId + ", chosen route: " + chosenRoute);
                    }
                    return routeId.equals(chosenRoute);
                } else {
                    log.trace("no weights found for group: " + group + ", current route: " + routeId);
                }
                return false;
            }
        };
    }
}
