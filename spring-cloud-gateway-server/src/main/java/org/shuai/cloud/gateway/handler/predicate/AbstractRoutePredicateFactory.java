package org.shuai.cloud.gateway.handler.predicate;

import org.shuai.cloud.gateway.support.AbstractConfigurable;

/**
 * @author Yangs
 */
public abstract class AbstractRoutePredicateFactory<C> extends AbstractConfigurable<C> implements RoutePredicateFactory<C> {

    public AbstractRoutePredicateFactory(Class<C> configClass) {
        super(configClass);
    }
}
