package com.zpi.model.rate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateTable {

    private String table;

    private String currency;

    private String code;

    private List<Rate> rates;
}
