package com.zpi.client;


import com.zpi.model.currency.Currency;
import com.zpi.model.currency.CurrencyTable;
import com.zpi.model.rate.Rate;
import com.zpi.model.rate.RateTable;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.util.List;

public class NbpRestClient {
    private static final String BASE_URL = "http://api.nbp.pl/api";

    private Client client = ClientBuilder.newClient();

    public List<Currency> getAvailableCurrencies(String table) {
        return client
                .target(BASE_URL)
                .path("exchangerates/tables/" + table)
                .request(MediaType.APPLICATION_JSON)
                .get(CurrencyTable[].class)[0]
                .getCurrencies();
    }

    public List<Rate> getRates(String table, String code, int last) {
        return client
                .target(BASE_URL)
                .path("exchangerates/rates/" + table + "/" + code + "/last/" + last)
                .request(MediaType.APPLICATION_JSON)
                .get(RateTable.class)
                .getRates();
    }
}
