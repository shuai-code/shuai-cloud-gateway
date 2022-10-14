package org.shuai.cloud.gateway.handler.predicate;

import org.shuai.cloud.gateway.handler.AsyncPredicate;
import org.shuai.cloud.gateway.support.Configurable;
import org.shuai.cloud.gateway.support.ShortcutConfigurable;
import org.springframework.web.server.ServerWebExchange;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.shuai.cloud.gateway.support.ServerWebExchangeUtils.toAsyncPredicate;

/**
 * 路由断言工厂
 * @author Yangs
 */
public interface RoutePredicateFactory<C> extends ShortcutConfigurable, Configurable<C> {

    default Predicate<ServerWebExchange> apply(Consumer<C> consumer) {
        C config = newConfig();
        consumer.accept(config);
        beforeApply(config);
        return apply(config);
    }

    /**
     * 接收一个Consumer函数, 进行配置初始化
     * */
    default AsyncPredicate<ServerWebExchange> applyAsync(Consumer<C> consumer) {
        // 常见断言工厂对应的配置对象
        C config = newConfig();
        // 给配置对象赋值
        consumer.accept(config);
        beforeApply(config);
        return applyAsync(config);
    }

    @Override
    default Class<C> getConfigClass() {
        throw new UnsupportedOperationException("getConfigClass() not implemented");
    }

    @Override
    default C newConfig() {
        throw new UnsupportedOperationException("newConfig() not implemented");
    }

    default void beforeApply(C config) {
    }

    Predicate<ServerWebExchange> apply(C config);

    default AsyncPredicate<ServerWebExchange> applyAsync(C config) {
        return toAsyncPredicate(apply(config));
    }
}
