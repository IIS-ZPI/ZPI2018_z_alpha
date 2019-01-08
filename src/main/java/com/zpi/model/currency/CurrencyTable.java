package com.zpi.model.currency;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyTable {

    private String table;

    private String no;

    private String effectiveDate;

    private List<Currency> currencies;

    @JsonProperty("rates")
    public List<Currency> getCurrencies() {
        return currencies;
    }
}
