import org.shuai.cloud.gateway.route.builder.RouteLocatorBuilder;

/**
 * @author Yangs
 */
public class Simple {

    public void test(RouteLocatorBuilder builder) {
        builder.routes().route(r -> System.out.println(r.toString()));
    }
}
