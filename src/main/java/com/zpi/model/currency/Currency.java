package com.zpi.model.currency;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Currency {
    private String name;
    private String code;
    private double mid;

    @JsonProperty("currency")
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " [" + code + "]";
    }
}
