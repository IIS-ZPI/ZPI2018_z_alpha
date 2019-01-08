package com.zpi;

import com.zpi.client.NbpRestClient;
import com.zpi.client.NbpRestClientImpl;
import com.zpi.model.currency.Currency;
import com.zpi.model.rate.Rate;
import com.zpi.model.rate.TimePeriod;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CurrencyService {

    public static final long WEEK_TIME_MILLIS = 604800000L;
    public static final long MONTH_TIME_MILLIS = 2592000000L;
    public static final long YEAR_TIME_MILLIS = 31556952000L;
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    private NbpRestClient nbpRestClient;

    public CurrencyService(NbpRestClient nbpRestClient) {
        this.nbpRestClient = nbpRestClient;
    }

    public List<Currency> getCurrencies() {
        List<Currency> currencies = nbpRestClient.getAvailableCurrencies("a");
        return currencies;
    }

    public List<Rate> getRatesForTimePeriod(String code, TimePeriod timePeriod) {
        List<Rate> rates = new ArrayList<>();
        switch (timePeriod) {
            case WEEK:
                rates = nbpRestClient.getRatesForDatePeriod("a", code, prepareDate(new Date(new Date().getTime() - WEEK_TIME_MILLIS)), prepareDate(new Date()));
                break;
            case TWO_WEEKS:
                rates = nbpRestClient.getRatesForDatePeriod("a", code, prepareDate(new Date(new Date().getTime() - WEEK_TIME_MILLIS*2)), prepareDate(new Date()));
                break;
            case MONTH:
                rates = nbpRestClient.getRatesForDatePeriod("a", code, prepareDate(new Date(new Date().getTime() - MONTH_TIME_MILLIS)), prepareDate(new Date()));
                break;
            case QUARTER:
                rates = nbpRestClient.getRatesForDatePeriod("a", code, prepareDate(new Date(new Date().getTime() - MONTH_TIME_MILLIS*3)), prepareDate(new Date()));
                break;
            case HALF_A_YEAR:
                rates = nbpRestClient.getRatesForDatePeriod("a", code, prepareDate(new Date(new Date().getTime() - MONTH_TIME_MILLIS*6)), prepareDate(new Date()));
                break;
            case YEAR:
                rates = nbpRestClient.getRatesForDatePeriod("a", code, prepareDate(new Date(new Date().getTime() - YEAR_TIME_MILLIS)), prepareDate(new Date()));
                break;
            default : break;
        }
        return rates;
    }

    private String prepareDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
        return format.format(date);
    }

    public double getMedianRateOfCurrencyForTimePeriod(String code, TimePeriod timePeriod) {
        List<Rate> rates = getRatesForTimePeriod(code, timePeriod);
        List<Double> rateValues = new ArrayList<>();
        int count = 0;
        for (Rate rate: rates) {
            count ++;
            rateValues.add(rate.getMid());
        }
        Collections.sort(rateValues);
        return count % 2 > 0 ? rateValues.get((count /2) + 1) : rateValues.get(((count /2) + ((count/2)+1)) / 2);
    }
}
