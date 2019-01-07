package com.zpi.model.currency;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
