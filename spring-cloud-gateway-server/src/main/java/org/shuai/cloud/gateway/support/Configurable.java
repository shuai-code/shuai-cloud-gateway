package org.shuai.cloud.gateway.support;

/**
 * @author Yangs
 */
public interface Configurable<C> {

    Class<C> getConfigClass();

    C newConfig();
}
