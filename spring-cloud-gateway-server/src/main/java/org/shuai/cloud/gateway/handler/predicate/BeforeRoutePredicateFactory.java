package org.shuai.cloud.gateway.handler.predicate;

import org.springframework.web.server.ServerWebExchange;

import java.time.ZonedDateTime;
import java.util.function.Predicate;

/**
 * 断言工厂-在指定时间之前
 * @author Yangs
 */
public class BeforeRoutePredicateFactory extends AbstractRoutePredicateFactory<BeforeRoutePredicateFactory.Config> {

    public BeforeRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange serverWebExchange) {
                final ZonedDateTime now = ZonedDateTime.now();
                return now.isBefore(config.getDatetime());
            }
        };
    }

    public static class Config {

        private ZonedDateTime datetime;

        public ZonedDateTime getDatetime() {
            return datetime;
        }

        public void setDatetime(ZonedDateTime datetime) {
            this.datetime = datetime;
        }
    }
}
