package org.shuai.cloud.gateway.route.builder;

import org.shuai.cloud.gateway.handler.AsyncPredicate;
import org.shuai.cloud.gateway.handler.predicate.*;
import org.shuai.cloud.gateway.route.Route;
import org.shuai.cloud.gateway.support.ipresolver.RemoteAddressResolver;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Arrays.stream;
import static org.shuai.cloud.gateway.support.ServerWebExchangeUtils.toAsyncPredicate;

/**
 * 规定所有可用于URL路由的断言类型
 * @author Yangs
 */
public class PredicateSpec extends UriSpec {

    PredicateSpec(Route.AsyncBuilder routeBuilder, RouteLocatorBuilder.Builder builder) {
        super(routeBuilder, builder);
    }

    /**
     * Predicate是Java1.8提供的断言函数式接口, 接收一个入参, 返回一个boolean值
     * ServerWebExchange是Spring的请求整合, 做为断言的入参
     * 将断言转换成异步断言类
     * */
    public BooleanSpec predicate(Predicate<ServerWebExchange> predicate) {
        return asyncPredicate(toAsyncPredicate(predicate));
    }

    public BooleanSpec asyncPredicate(AsyncPredicate<ServerWebExchange> predicate) {
        this.routeBuilder.asyncPredicate(predicate);
        return new BooleanSpec(this.routeBuilder, this.builder);
    }

    /**
     * 断言, 检查请求是否在指定时间之后. 只当请求在此时间之后才会被路由
     * */
    public BooleanSpec after(ZonedDateTime dateTime) {
        // 使用getBean获取断言工厂
        // 将时间参数传入工厂配置对象中
        return asyncPredicate(getBean(AfterRoutePredicateFactory.class).applyAsync(config -> config.setDatetime(dateTime)));
    }

    /**
     * 断言, 检查请求是否在指定时间之前. 只当请求在此时间之前才会被路由
     * */
    public BooleanSpec before(ZonedDateTime dateTime) {
        return asyncPredicate(getBean(BeforeRoutePredicateFactory.class).applyAsync(config -> config.setDatetime(dateTime)));
    }

    /**
     * 断言, 检查请求是否在两个时间之间. 只当请求在这两个时间之间才会被路由
     * */
    public BooleanSpec between(ZonedDateTime dateTime1, ZonedDateTime dateTime2) {
        return asyncPredicate(getBean(BetweenRoutePredicateFactory.class).applyAsync(config -> config.setDatetime1(dateTime1).setDatetime2(dateTime2)));
    }

    /**
     * 断言, 检查cookie是否与给定的正则匹配, 只有cookie与给定正则匹配时才被路由
     * */
    public BooleanSpec cookie(String name, String regex) {
        return asyncPredicate(getBean(CookieRoutePredicateFactory.class).applyAsync(config -> config.setName(name).setRegexp(regex)));
    }

    /**
     * 断言, 检查请求是否存在给定的请求头, 只有请求有给定的header才会路由
     * */
    public BooleanSpec header(String header) {
        return asyncPredicate(getBean(HeaderRoutePredicateFactory.class).applyAsync(config -> config.setHeader(header)));
    }

    /**
     * 断言, 检查请求是否存在与给定正则匹配的请求头, 存在与给定正则匹配的请求头时被路由
     * */
    public BooleanSpec header(String header, String regex) {
        return asyncPredicate(getBean(HeaderRoutePredicateFactory.class).applyAsync(config -> config.setHeader(header).setRegexp(regex)));
    }

    /**
     * 断言, 检查请求的host是否与给定正则匹配, 匹配的host会被路由
     * */
    public BooleanSpec host(String... pattern) {
        return asyncPredicate(getBean(HostRoutePredicateFactory.class).applyAsync(config -> config.setPatterns(Arrays.asList(pattern))));
    }

    /**
     * 断言, 检查HTTP请求方法(POST,PUT)是否匹配, 匹配则路由
     * 接收HTTP请求方法名
     * */
    public BooleanSpec method(String... methods) {
        return asyncPredicate(getBean(MethodRoutePredicateFactory.class).applyAsync(config -> {
            // 将字符串类型的请求方法名转成方法对象
            HttpMethod[] httpMethods = stream(methods).map(HttpMethod::valueOf).toArray(HttpMethod[]::new);
            config.setMethods(httpMethods);
        }));
    }

    /**
     * 断言, 检查HTTP请求方法(POST,PUT)是否匹配, 匹配则路由
     * 接收HTTP请求方法对象
     * */
    public BooleanSpec method(HttpMethod... methods) {
        return asyncPredicate(getBean(MethodRoutePredicateFactory.class).applyAsync(config -> config.setMethods(methods)));
    }

    /**
     * 断言, 请求路径与给定正则匹配, 匹配则路由
     * */
    public BooleanSpec path(String... patterns) {
        return asyncPredicate(getBean(PathRoutePredicateFactory.class).applyAsync(config -> config.setPatterns(Arrays.asList(patterns))));
    }

    /**
     * 断言, 请求路径与给定正则匹配, 匹配则路由
     * matchTrailingSlash处理路由后尾随的斜杠("/")
     * 如果不希望结尾有/时匹配此路径,则设置为false
     * */
    public BooleanSpec path(boolean matchTrailingSlash, String... patterns) {
        return asyncPredicate(getBean(PathRoutePredicateFactory.class).applyAsync(c -> c.setPatterns(Arrays.asList(patterns)).setMatchTrailingSlash(matchTrailingSlash)));
    }

    /**
     * 测试断言, 检查请求主体
     * */
    public <T> BooleanSpec readBody(Class<T> inClass, Predicate<T> predicate) {
        return asyncPredicate(getBean(ReadBodyRoutePredicateFactory.class).applyAsync(c -> c.setPredicate(inClass, predicate)));
    }

    /**
     * 断言, 检查查询参数是否与指定的正则匹配, 匹配则路由
     * */
    public BooleanSpec query(String param, String regex) {
        return asyncPredicate(getBean(QueryRoutePredicateFactory.class).applyAsync(c -> c.setParam(param).setRegexp(regex)));
    }

    /**
     * 断言, 检查请求的URL中是否有指定查询参数, 存在则路由
     * */
    public BooleanSpec query(String param) {
        return asyncPredicate(getBean(QueryRoutePredicateFactory.class).applyAsync(c -> c.setParam(param)));
    }

    /**
     * 断言, 检查远程地址
     * */
    public BooleanSpec remoteAddr(String... addrs) {
        return remoteAddr(null, addrs);
    }

    /**
     * 断言, 检查远程地址
     * */
    public BooleanSpec remoteAddr(RemoteAddressResolver resolver, String... addrs) {
        return asyncPredicate(getBean(RemoteAddrRoutePredicateFactory.class).applyAsync(c -> {
            c.setSources(addrs);
            if (resolver != null) {
                c.setRemoteAddressResolver(resolver);
            }
        }));
    }

    /**
     * 断言, 检查远程地址
     * */
    public BooleanSpec xForwardedRemoteAddr(String... addrs) {
        return asyncPredicate(getBean(XForwardedRemoteAddrRoutePredicateFactory.class).applyAsync(c -> c.setSources(addrs)));
    }

    /**
     * 断言, 根据指定的权重选择路由
     * */
    public BooleanSpec weight(String group, int weight) {
        return asyncPredicate(getBean(WeightRoutePredicateFactory.class).applyAsync(c -> c.setGroup(group).setRouteId(routeBuilder.getId()).setWeight(weight)));
    }

    public BooleanSpec cloudFoundryRouteService() {
        return predicate(getBean(CloudFoundryRouteServiceRoutePredicateFactory.class).apply(c -> {
        }));
    }

    /**
     * 断言, 一个始终为真的断言, 用于添加逻辑运算符
     * */
    public BooleanSpec alwaysTrue() {
        return predicate(serverWebExchange -> true);
    }

    public BooleanSpec not(Function<PredicateSpec, BooleanSpec> fn) {
        return alwaysTrue().and().not(fn);
    }
}
