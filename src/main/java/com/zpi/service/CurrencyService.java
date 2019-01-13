package com.zpi.service;

import com.zpi.client.NbpRestClient;
import com.zpi.model.currency.Currency;
import com.zpi.model.rate.Rate;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.*;

public class CurrencyService {
    private static final long WEEK_TIME_MILLIS = 604800000L;
    private static final long MONTH_TIME_MILLIS = 2592000000L;
    private static final long YEAR_TIME_MILLIS = 31556952000L;
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private NbpRestClient nbpRestClient;

    @Getter
    @Setter
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
        return rates;
    }

    List<Rate> getRatesOfTwoCurrenciesForTimePeriod(String code, TimePeriod timePeriod){
        List<Rate> resultRates = new ArrayList<>();
        List<Rate> rates = getRatesForTimePeriod(code, timePeriod);
        for( int i = 0 ; i < cachedRates.size(); i++) {
            Rate rate = new Rate("No", cachedRates.get(i).getEffectiveDate(), cachedRates.get(i).getMid()/rates.get(i).getMid());
            resultRates.add(rate);
        }
        return resultRates;
    }

    public Map<String, Integer> getDistributionChangesForTwoCurrencies(String code, TimePeriod timeperiod) {
        List<Rate> rates = getRatesOfTwoCurrenciesForTimePeriod(code, timeperiod);
        Map<String, Integer> resultMap = new HashMap<>();
        for(int i = 1; i < rates.size();i++ ) {
            Double actualRate = rates.get(i).getMid();
            Double pastRate = rates.get(i-1).getMid();
            resultMap.put(rates.get(i).getEffectiveDate(), (int) Math.round((actualRate-pastRate)*10000));
        }
        return resultMap;
    }

    private String prepareDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
        return format.format(date);
    }

    public double calculateMedian(List<Rate> rates) {
        List<Double> rateValues = new ArrayList<>();
        int arraySize = rates.size()-1;
        for (Rate rate: rates) {
            rateValues.add(rate.getMid());
        }
        Collections.sort(rateValues);
        return arraySize % 2 > 0 ? rateValues.get((arraySize /2)) : (rateValues.get((arraySize /2)) + rateValues.get((arraySize/2)-1)) / 2;
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

        list.sort(Comparator.comparing(o -> (o.getValue())));

        Map<Double, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Double, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    double calculateMean(List<Rate> rates) {
        double sum = 0.0;
        for (Rate rate: rates) {
            sum += rate.getMid();
        }
        return sum / rates.size();
    }

    public double calculateStandardDeviation(List<Rate> rates) {
        double mean = calculateMean(rates);
        double temp = 0.0;
        for (Rate rate : rates) {
            temp += (rate.getMid()-mean)*(rate.getMid()-mean);
        }
        return temp/(rates.size()-1);
    }


    public Double calculateCoefficientOfVariation(List<Rate> rates) {
        return calculateStandardDeviation(rates) / calculateMean(rates) * 100;
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
