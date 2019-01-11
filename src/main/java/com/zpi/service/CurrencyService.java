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
        return count % 2 > 0 ? rateValues.get((count /2)) : (rateValues.get((count /2)) + rateValues.get((count/2)-1)) / 2;
    }

    public Double calculateDominant(List<Rate> rates) {
        Map<Double, Integer> rateValues = new HashMap<>();
        for (Rate rate: rates) {
            if(!rateValues.isEmpty() && rateValues.containsKey(rate.getMid())){
                int count = rateValues.get(rate.getMid());
                rateValues.replace(rate.getMid(),count + 1);
            } else {
                rateValues.put(rate.getMid(), 0);
            }
        }
        Map<Double, Integer> sortedValues = sortByValue(rateValues);
        Map.Entry<Double, Integer> entry = sortedValues.entrySet().iterator().next();
        return entry.getKey();
    }

    private static Map<Double, Integer> sortByValue(Map<Double, Integer> unsortMap) {

        List<Map.Entry<Double, Integer>> list =
                new LinkedList<>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Double, Integer>>() {
            public int compare(Map.Entry<Double, Integer> o1,
                               Map.Entry<Double, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<Double, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Double, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
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
