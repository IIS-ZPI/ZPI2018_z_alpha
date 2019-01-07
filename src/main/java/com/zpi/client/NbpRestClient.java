package com.zpi.client;

import com.zpi.model.currency.Currency;
import com.zpi.model.rate.Rate;

import java.util.List;

public interface NbpRestClient {

    List<Currency> getAvailableCurrencies(String table);

    List<Rate> getRates(String table, String code, int last);

    List<Rate> getRatesForDatePeriod(String table, String code, String dateFrom, String dateTo);
}
