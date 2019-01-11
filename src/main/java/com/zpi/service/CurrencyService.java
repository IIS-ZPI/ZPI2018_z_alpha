package com.zpi.service;

import com.zpi.client.NbpRestClient;
import com.zpi.model.currency.Currency;
import com.zpi.model.rate.Rate;
import lombok.Getter;

import java.text.SimpleDateFormat;
import java.util.*;

public class CurrencyService {

    public static final long WEEK_TIME_MILLIS = 604800000L;
    public static final long MONTH_TIME_MILLIS = 2592000000L;
    public static final long YEAR_TIME_MILLIS = 31556952000L;
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    private NbpRestClient nbpRestClient;
    @Getter
    private List<Rate> cachedRates;

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
        cachedRates = rates;
        return rates;
    }

    private String prepareDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
        return format.format(date);
    }

    public double getMedianRateOfCurrencyForTimePeriod(String code, TimePeriod timePeriod) {
        List<Rate> rates = getRatesForTimePeriod(code, timePeriod);
        return calculateMedian(rates);
    }

    public double calculateMedian(List<Rate> rates) {
        List<Double> rateValues = new ArrayList<>();
        int count = 0;
        for (Rate rate: rates) {
            count ++;
            rateValues.add(rate.getMid());
        }
        Collections.sort(rateValues);
        return count % 2 > 0 ? rateValues.get((count /2) + 1) : rateValues.get(((count /2) + ((count/2)+1)) / 2);
    }

    public double calculateDominant(List<Rate> rates) {
        // TODO Jerry's implementation here
        return calculateMedian(rates) - 0.1;
    }

    public double calculateStandardVariation(List<Rate> rates) {
        // TODO Jerry's implementation here
        return calculateMedian(rates) - 0.2;
    }

    public double calculateCoefficientOfVariation(List<Rate> rates) {
        // TODO Jerry's implementation
        return new Random().nextDouble() - 0.5;
    }

    public SessionsDataPack calculateSessions(List<Rate> rates) {
        SessionsDataPack sessionsDataPack = new SessionsDataPack();

        double lastDiff = Double.MAX_VALUE;
        Rate prev = rates.get(0);
        for (int i = 1; i < rates.size(); i++) {
            Rate rate = rates.get(i);

            double diff = rate.getMid() - prev.getMid();
            if (diff > 0) {
                if (lastDiff <= 0 || lastDiff == Double.MAX_VALUE) {
                    sessionsDataPack.riseSessions++;
                }
            } else if (diff < 0) {
                if (lastDiff >= 0 || lastDiff == Double.MAX_VALUE) {
                    sessionsDataPack.fallSessions++;
                }
            } else {
                if (lastDiff != 0 || lastDiff == Double.MAX_VALUE) {
                    sessionsDataPack.unchangedSessions++;
                }
            }

            lastDiff = diff;
            prev = rate;
        }
        return sessionsDataPack;
    }
}
