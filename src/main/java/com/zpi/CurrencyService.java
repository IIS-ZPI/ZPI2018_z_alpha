package com.zpi;

import com.zpi.client.NbpRestClient;
import com.zpi.model.currency.Currency;
import com.zpi.model.rate.Rate;
import com.zpi.model.rate.TimePeriod;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CurrencyService {

    public static final long WEEK_TIME_MILLIS = 604800000L;
    public static final long MONTH_TIME_MILLIS = 2592000000L;
    public static final long YEAR_TIME_MILLIS = 31556952000L;
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    private NbpRestClient nbpRestClient = new NbpRestClient();

    public List<Currency> getCurrencies() {
        List<Currency> currencies = nbpRestClient.getAvailableCurrencies("a");
        return currencies;
    }

    private List<Rate> getRatesForTimePeriod(String code, TimePeriod timePeriod) {
        List<Rate> rates = new ArrayList<>();
        switch (timePeriod) {
            case WEEK:
                rates = nbpRestClient.getRatesForDatePeriod("a", code, prepareDate(new Date()), prepareDate(new Date(new Date().getTime() - WEEK_TIME_MILLIS)));
                break;
            case TWO_WEEKS:
                rates = nbpRestClient.getRatesForDatePeriod("a", code, prepareDate(new Date()), prepareDate(new Date(new Date().getTime() - WEEK_TIME_MILLIS*2)));
                break;
            case MONTH:
                rates = nbpRestClient.getRatesForDatePeriod("a", code, prepareDate(new Date()), prepareDate(new Date(new Date().getTime() - MONTH_TIME_MILLIS)));
                break;
            case QUARTER:
                rates = nbpRestClient.getRatesForDatePeriod("a", code, prepareDate(new Date()), prepareDate(new Date(new Date().getTime() - MONTH_TIME_MILLIS*3)));
                break;
            case HALF_A_YEAR:
                rates = nbpRestClient.getRatesForDatePeriod("a", code, prepareDate(new Date()), prepareDate(new Date(new Date().getTime() - MONTH_TIME_MILLIS*6)));
                break;
            case YEAR:
                rates = nbpRestClient.getRatesForDatePeriod("a", code, prepareDate(new Date()), prepareDate(new Date(new Date().getTime() - YEAR_TIME_MILLIS)));
                break;
            default : break;
        }
        return rates;
    }

    private String prepareDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
        return format.format(date);
    }

    public double getMeanRateOfCurrencyForTimePeriod(String code, TimePeriod timePeriod) {
        List<Rate> rates = getRatesForTimePeriod(code, timePeriod);
        double mean = 0.0;
        int count = 0;
        for (Rate rate: rates) {
            count ++;
            mean = +rate.getMid();
        }
        return mean/count;
    }

}
