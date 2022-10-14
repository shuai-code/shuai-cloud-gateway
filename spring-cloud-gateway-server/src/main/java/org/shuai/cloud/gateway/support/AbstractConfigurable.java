package org.shuai.cloud.gateway.support;

import org.springframework.beans.BeanUtils;

/**
 * @author Yangs
 */
public abstract class AbstractConfigurable<C> implements Configurable<C> {

    private Class<C> configClass;

    protected AbstractConfigurable(Class<C> configClass) {
        this.configClass = configClass;
    }

    @Override
    public Class<C> getConfigClass() {
        return configClass;
    }

    @Override
    public C newConfig() {
        return BeanUtils.instantiateClass(this.configClass);
    }
}
