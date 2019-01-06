package com.zpi.model.rate;

import lombok.Getter;

import java.util.List;

@Getter
public class RateTable {
    private String table;
    private String currency;
    private String code;
    private List<Rate> rates;
}
