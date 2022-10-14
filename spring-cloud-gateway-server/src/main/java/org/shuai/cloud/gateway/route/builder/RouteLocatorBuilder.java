package org.shuai.cloud.gateway.route.builder;

import org.shuai.cloud.gateway.route.Route;
import org.shuai.cloud.gateway.route.RouteLocator;
import org.springframework.context.ConfigurableApplicationContext;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * 用于构建路由规则
 *
 * @author Yangs
 */
public class RouteLocatorBuilder {

    private ConfigurableApplicationContext context;

    public RouteLocatorBuilder(ConfigurableApplicationContext context) {
        this.context = context;
    }

    /**
     * 创建一个构造器
     * 构造一个路由的开始, 返回一个Builder, 使用内部的route方法添加一个路由, 添加路由完成后返回当前Builder对象, 用于继续调用, 建造者模式
     * routes().route().route().build();
     */
    public Builder routes() {
        return new Builder(context);
    }

    /**
     * 基于建造者模式的路由构建工具
     * 每个route接收一个路由配置, 最后使用build将所有路由配置打包
     */
    public static class Builder {

        /**
         * 保存每个路由配置的构建信息, 用于在最后阶段集中构建
         */
        private List<Buildable<Route>> routes = new ArrayList<>();

        private ConfigurableApplicationContext context;

        public Builder(ConfigurableApplicationContext context) {
            this.context = context;
        }

        /**
         * 使用自定义ID 和 路由规则 构建一个路由
         *
         * @param id       自定义ID
         * @param function 传入的Function是一个函数式接口, 第一个参数规定入参类型Buildable<Route>, 第二个参数规定返回值类型PredicateSpec
         *                 在使用的时候会传入一个函数, 传入的函数使用new RouteSpec(this).id(id)做为入参, 执行函数后返回一个Buildable<Route>类型的结果
         * @return Builder 返回一个Builder构造器, 用于多个route串联构建 route().route().route();
         */
        public Builder route(String id, Function<PredicateSpec, Buildable<Route>> function) {
            // 应用这个函数方法, 返回一个Buildable<Route>
            Buildable<Route> routeBuilder = function.apply(new RouteSpec(this).id(id));
            // 把创建好的路由放入列表中
            add(routeBuilder);
            return this;
        }

        /**
         * 使用随机ID 和 路由规则 构建一个路由
         */
        public Builder route(Function<PredicateSpec, Buildable<Route>> function) {
            Buildable<Route> routeBuilder = function.apply(new RouteSpec(this).randomId());
            add(routeBuilder);
            return this;
        }

        /**
         * 收尾方法, 标志建造者模式结束, 所有路由添加完后的收尾操作
         * 循环调用每个路由的构造器build方法, 生成最终的路由Route对象
         */
        public RouteLocator build() {
            return () -> Flux.fromIterable(this.routes).map(Buildable::build);
        }

        ConfigurableApplicationContext getContext() {
            return context;
        }

        public void add(Buildable<Route> routeBuilder) {
            routes.add(routeBuilder);
        }
    }

    public static class RouteSpec {

        /**
         * 路由构建对象, 使用此对象构建一个路由, 初始化一个异步构建器对象
         */
        private final Route.AsyncBuilder routeBuilder = Route.async();

        private final Builder builder;

        public RouteSpec(Builder builder) {
            this.builder = builder;
        }

        /**
         * 路由指定ID, 初始化一个断言构造器
         */
        public PredicateSpec id(String id) {
            // 将路由ID设置到路由构造器中
            this.routeBuilder.id(id);
            return predicateBuilder();
        }

        /**
         * 路由随机ID, 初始化一个断言构造器
         */
        public PredicateSpec randomId() {
            return id(UUID.randomUUID().toString());
        }

        /**
         * 创建一个断言构造器PredicateSpec, 使用此构造器构造断言规则
         */
        public PredicateSpec predicateBuilder() {
            return new PredicateSpec(this.routeBuilder, this.builder);
        }
    }
}
