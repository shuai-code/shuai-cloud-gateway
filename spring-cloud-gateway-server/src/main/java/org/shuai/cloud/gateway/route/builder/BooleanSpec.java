package org.shuai.cloud.gateway.route.builder;

import org.shuai.cloud.gateway.route.Route;
import org.springframework.util.Assert;

import static org.shuai.cloud.gateway.route.builder.BooleanSpec.Operator.AND;

/**
 * @author Yangs
 */
public class BooleanSpec extends UriSpec {
    public BooleanSpec(Route.AsyncBuilder routeBuilder, RouteLocatorBuilder.Builder builder) {
        super(routeBuilder, builder);
    }

    public BooleanOpSpec and() {
        return new BooleanOpSpec(routeBuilder, builder, AND);
    }

    enum Operator {
        /**
         * 与
         * */
        AND,
        OR,
        NEGATE
    }

    public static class BooleanOpSpec extends PredicateSpec {

        private Operator operator;

        BooleanOpSpec(Route.AsyncBuilder routeBuilder, RouteLocatorBuilder.Builder builder, Operator operator) {
            super(routeBuilder, builder);
            Assert.notNull(operator, "operator may not be null");
            this.operator = operator;
        }
    }
}
