package org.shuai.cloud.gateway.handler.predicate;

import com.sun.istack.internal.NotNull;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;

import java.time.ZonedDateTime;
import java.util.function.Predicate;

/**
 * 断言工厂-在时间区间内
 * @author Yangs
 */
public class BetweenRoutePredicateFactory extends AbstractRoutePredicateFactory<BetweenRoutePredicateFactory.Config> {

    public BetweenRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        Assert.isTrue(config.getDatetime1().isBefore(config.getDatetime2()), config.getDatetime1() + " must be before " + config.getDatetime2());
        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange serverWebExchange) {
                final ZonedDateTime now = ZonedDateTime.now();
                return now.isAfter(config.getDatetime1()) && now.isBefore(config.getDatetime2());
            }
        };
    }

    public static class Config {
        @NotNull
        private ZonedDateTime datetime1;

        @NotNull
        private ZonedDateTime datetime2;

        public ZonedDateTime getDatetime1() {
            return datetime1;
        }

        public Config setDatetime1(ZonedDateTime datetime1) {
            this.datetime1 = datetime1;
            return this;
        }

        public ZonedDateTime getDatetime2() {
            return datetime2;
        }

        public Config setDatetime2(ZonedDateTime datetime2) {
            this.datetime2 = datetime2;
            return this;
        }
    }
}
