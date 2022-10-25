package org.shuai.cloud.gateway.support.ipresolver;

import org.springframework.util.Assert;

/**
 * @author Yangs
 */
public class XForwardedRemoteAddressResolver implements RemoteAddressResolver {

    /**
     * Forwarded-For 请求头名字
     * */
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private final int maxTrustedIndex;

    private XForwardedRemoteAddressResolver(int maxTrustedIndex) {
        this.maxTrustedIndex = maxTrustedIndex;
    }

    public static XForwardedRemoteAddressResolver maxTrustedIndex(int maxTrustedIndex) {
        Assert.isTrue(maxTrustedIndex > 0, "An index greater than 0 is required");
        return new XForwardedRemoteAddressResolver(maxTrustedIndex);
    }
}
