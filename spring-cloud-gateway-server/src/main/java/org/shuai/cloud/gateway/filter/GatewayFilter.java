package org.shuai.cloud.gateway.filter;

import org.shuai.cloud.gateway.support.ShortcutConfigurable;

/**
 * @author Yangs
 */
public interface GatewayFilter extends ShortcutConfigurable {
    /**
     * Name key.
     */
    String NAME_KEY = "name";

    /**
     * Value key.
     */
    String VALUE_KEY = "value";
}
