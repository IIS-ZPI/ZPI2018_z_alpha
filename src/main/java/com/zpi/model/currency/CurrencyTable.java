package com.zpi.model.currency;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class CurrencyTable {@Getter
    private String table;
    private String no;
    private String effectiveDate;
    private List<Currency> currencies;

    @JsonProperty("rates")
    public List<Currency> getCurrencies() {
        return currencies;
    }
}
