package org.shuai.cloud.gateway.support;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

/**
 * @author Yangs
 */
public class WeightConfig {

    @NotEmpty
    private String group;

    private String routeId;

    @Min(0)
    private int weight;

    public String getGroup() {
        return group;
    }

    public WeightConfig setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getRouteId() {
        return routeId;
    }

    public WeightConfig setRouteId(String routeId) {
        this.routeId = routeId;
        return this;
    }

    public int getWeight() {
        return weight;
    }

    public WeightConfig setWeight(int weight) {
        this.weight = weight;
        return this;
    }
}
