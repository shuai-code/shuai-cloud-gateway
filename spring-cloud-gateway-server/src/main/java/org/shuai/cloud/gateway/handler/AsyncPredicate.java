package org.shuai.cloud.gateway.handler;

import org.reactivestreams.Publisher;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 异步断言函数式接口, 扩展自Java的Function
 * 接收一个参数, 返回一个Publisher<Boolean>
 * 可以构建一个断言规则集, 以与或非的形式串联
 * @author Yangs
 */
public interface AsyncPredicate<T> extends Function<T, Publisher<Boolean>> {
    /**
     * 逻辑与操作
     * @param other 另一个断言
     * @return 组合断言
     * */
    default AsyncPredicate<T> and(AsyncPredicate<? super T> other) {
        return new AndAsyncPredicate<>(this, other);
    }

    /**
     * 逻辑非操作
     * @return 组合断言
     * */
    default AsyncPredicate<T> negate() {
        return new NegateAsyncPredicate<>(this);
    }

    /**
     * 逻辑非操作
     * @param other 另一个断言
     * @return 组合断言
     * */
    default AsyncPredicate<T> not(AsyncPredicate<? super T> other) {
        return new NegateAsyncPredicate<>(other);
    }

    /**
     * 逻辑或操作
     * @param other 另一个断言
     * @return 组合断言
     * */
    default AsyncPredicate<T> or(AsyncPredicate<? super T> other) {
        return new OrAsyncPredicate<>(this, other);
    }

    static AsyncPredicate<ServerWebExchange> from(Predicate<? super ServerWebExchange> predicate) {
        return new DefaultAsyncPredicate<>(predicate);
    }

    class DefaultAsyncPredicate<T> implements AsyncPredicate<T> {

        private final Predicate<? super T> delegate;

        public DefaultAsyncPredicate(Predicate<? super T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Publisher<Boolean> apply(T t) {
            return Mono.just(delegate.test(t));
        }
    }

    /**
     * 与断言
     * */
    class AndAsyncPredicate<T> implements AsyncPredicate<T> {

        private final AsyncPredicate<? super T> left;

        private final AsyncPredicate<? super T> right;

        public AndAsyncPredicate(AsyncPredicate<? super T> left, AsyncPredicate<? super T> right) {
            Assert.notNull(left, "Left AsyncPredicate must not be null");
            Assert.notNull(right, "Right AsyncPredicate must not be null");
            this.left = left;
            this.right = right;
        }

        @Override
        public Publisher<Boolean> apply(T t) {
            // 左右断言都为true返回true
            return Mono.from(left.apply(t)).flatMap(result -> !result ? Mono.just(false) : Mono.from(right.apply(t)));
        }
    }

    /**
     * 非断言
     * */
    class NegateAsyncPredicate<T> implements AsyncPredicate<T> {

        private final AsyncPredicate<? super T> predicate;

        public NegateAsyncPredicate(AsyncPredicate<? super T> predicate) {
            Assert.notNull(predicate, "predicate AsyncPredicate must not be null");
            this.predicate = predicate;
        }

        @Override
        public Publisher<Boolean> apply(T t) {
            // 取反, 断言为true返回false, 断言为false返回true
            return Mono.from(predicate.apply(t)).map(b -> !b);
        }
    }

    /**
     * 或断言
     * */
    class OrAsyncPredicate<T> implements AsyncPredicate<T> {

        private final AsyncPredicate<? super T> left;

        private final AsyncPredicate<? super T> right;

        public OrAsyncPredicate(AsyncPredicate<? super T> left, AsyncPredicate<? super T> right) {
            Assert.notNull(left, "Left AsyncPredicate must not be null");
            Assert.notNull(right, "Right AsyncPredicate must not be null");
            this.left = left;
            this.right = right;
        }

        @Override
        public Publisher<Boolean> apply(T t) {
            // 两个断言任一为true, 返回true
            return Mono.from(left.apply(t)).flatMap(result -> result ? Mono.just(true) : Mono.from(right.apply(t)));
        }
    }
}
