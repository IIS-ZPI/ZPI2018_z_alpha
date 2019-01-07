package com.zpi.client;


import com.zpi.model.currency.Currency;
import com.zpi.model.currency.CurrencyTable;
import com.zpi.model.rate.Rate;
import com.zpi.model.rate.RateTable;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.util.List;

public class NbpRestClientImpl implements NbpRestClient {
    private static final String BASE_URL = "http://api.nbp.pl/api";

    private Client client = ClientBuilder.newClient();

    @Override
    public List<Currency> getAvailableCurrencies(String table) {
        return client
                .target(BASE_URL)
                .path("exchangerates/tables/" + table)
                .request(MediaType.APPLICATION_JSON)
                .get(CurrencyTable[].class)[0]
                .getCurrencies();
    }

    @Override
    public List<Rate> getRates(String table, String code, int last) {
        return client
                .target(BASE_URL)
                .path("exchangerates/rates/" + table + "/" + code + "/last/" + last)
                .request(MediaType.APPLICATION_JSON)
                .get(RateTable.class)
                .getRates();
    }

    /**
     *
     * @param table table from which data would be fetched, mostly a, could be b and c
     * @param code currency code, mostly a 3-letter one like USD
     * @param dateFrom as a String in form "YYYY-MM-DD"
     * @param dateTo as a String in form "YYYY-MM-DD"
     */
    @Override
    public List<Rate> getRatesForDatePeriod(String table, String code, String dateFrom, String dateTo) {
        return client
                .target(BASE_URL)
                .path("exchangerates/rates/" + table + "/" + code + "/" + dateFrom + "/" + dateTo)
                .request(MediaType.APPLICATION_JSON)
                .get(RateTable.class)
                .getRates();
    }
}
