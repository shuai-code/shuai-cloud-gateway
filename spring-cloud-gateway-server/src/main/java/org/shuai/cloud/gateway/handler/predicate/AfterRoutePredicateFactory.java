package org.shuai.cloud.gateway.handler.predicate;

import com.sun.istack.internal.NotNull;
import org.springframework.web.server.ServerWebExchange;

import java.time.ZonedDateTime;
import java.util.function.Predicate;

/**
 * 断言工厂-在指定时间之后
 * @author Yangs
 */
public class AfterRoutePredicateFactory extends AbstractRoutePredicateFactory<AfterRoutePredicateFactory.Config> {

    public AfterRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange serverWebExchange) {
                final ZonedDateTime now = ZonedDateTime.now();
                return now.isAfter(config.getDatetime());
            }
        };
    }

    public static class Config {
        @NotNull
        private ZonedDateTime datetime;

        public ZonedDateTime getDatetime() {
            return datetime;
        }

        public void setDatetime(ZonedDateTime datetime) {
            this.datetime = datetime;
        }
    }
}
