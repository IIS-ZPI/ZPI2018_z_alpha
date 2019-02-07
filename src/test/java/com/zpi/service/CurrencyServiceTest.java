package com.zpi.service;

import com.zpi.client.NbpRestClient;
import com.zpi.model.currency.Currency;
import com.zpi.model.rate.Rate;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class CurrencyServiceTest {

    @Mock
    private NbpRestClient nbpRestClient;

    @InjectMocks
    private CurrencyService currencyService;

    @Test
    public void getCurrenciesMethodMockTest() {
        Mockito
                .when(nbpRestClient.getAvailableCurrencies(Mockito.anyString()))
                .thenReturn(getCurrenciesMock());

        List<Currency> result = currencyService.getCurrencies();

        Assert.assertNotNull(result);
    }

    @Test
    public void getCurrenciesMethodPerformance(){
        long t0 = System.currentTimeMillis();
        getCurrenciesMethodMockTest();
        long t1 = System.currentTimeMillis() - t0;

        Assert.assertTrue(t1 <= 10);
    }

    @Test
    public void getRatesForTimePeriodMockTest() {
        Mockito
                .when(nbpRestClient.getRatesForDatePeriod(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getRatesForTimePeriodMock());

        List<Rate> result = currencyService.getRatesForTimePeriod("usd", TimePeriod.WEEK);
        Assert.assertNotNull(result);
    }

    @Test
    public void getRatesForTimePeriodPerformance(){
        long t0 = System.currentTimeMillis();
        getRatesForTimePeriodMockTest();
        long t1 = System.currentTimeMillis() - t0;

        Assert.assertTrue(t1 <= 100);
    }

    @Test
    public void getRatesOfTwoCurrenciesForTimePeriodMockTest(){
        Mockito
                .when(nbpRestClient.getRatesForDatePeriod(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getRatesForTimePeriodMock());
        currencyService.setCachedRates(getSecondRatesForTimePeriodMock());
        List<Rate> result = currencyService.getRatesOfTwoCurrenciesForTimePeriod( "eur", TimePeriod.WEEK);
        Assert.assertNotNull(result);
    }

    @Test
    public void getRatesOfTwoCurrenciesForTimePeriodPerformance(){
        long t0 = System.currentTimeMillis();
        getRatesOfTwoCurrenciesForTimePeriodMockTest();
        long t1 = System.currentTimeMillis() - t0;

        Assert.assertTrue(t1 <= 100);
    }

    @Test
    public void getDistributionChangesForTwoCurrenciesTest(){
        Mockito
                .when(nbpRestClient.getRatesForDatePeriod(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getRatesForTimePeriodMock());
        currencyService.setCachedRates(getSecondRatesForTimePeriodMock());
        Map<String, Integer> result = currencyService.getDistributionChangesForTwoCurrencies( "eur", TimePeriod.WEEK);

        Assert.assertNotNull(result);
    }

    @Test
    public void getDistributionChangesForTwoCurrenciesPerformance(){
        long t0 = System.currentTimeMillis();
        getDistributionChangesForTwoCurrenciesTest();
        long t1 = System.currentTimeMillis() - t0;

        Assert.assertTrue(t1 <= 10);
    }

    @Test
    public void calculateSessionsTest() {
        SessionsDataPack sessionsDataPack = currencyService.calculateSessions(getRatesMock());
        Assert.assertEquals(3, sessionsDataPack.riseSessions);
        Assert.assertEquals(2, sessionsDataPack.fallSessions);
        Assert.assertEquals(2, sessionsDataPack.unchangedSessions);
    }

    @Test
    public void calculateSessionsPerformance(){
        long t0 = System.currentTimeMillis();
        calculateSessionsTest();
        long t1 = System.currentTimeMillis() - t0;

        Assert.assertTrue(t1 <= 10);
    }

    @Test
    public void calculateMedianTest(){
        List<Rate> median = getRatesMock();
        double result = currencyService.calculateMedian(median);
        Assert.assertEquals(3.0, result, 0.1);
    }

    @Test
    public void calculateMedianPerformance(){
        long t0 = System.currentTimeMillis();
        calculateMedianTest();
        long t1 = System.currentTimeMillis() - t0;

        Assert.assertTrue(t1 <= 10);
    }

    @Test
    public void calculateDominantTest(){
        List<Rate> dominant = getRatesMock();
        double result = currencyService.calculateDominant(dominant);
        Assert.assertEquals(1.0, result, 0.1);
    }

    @Test
    public void calculateDominantPerformance(){
        long t0 = System.currentTimeMillis();
        calculateDominantTest();
        long t1 = System.currentTimeMillis() - t0;

        Assert.assertTrue(t1 <= 10);
    }

    @Test
    public void calculateMeanTest(){
        List<Rate> dominant = getRatesMock();
        double result = currencyService.calculateMean(dominant);
        Assert.assertEquals(3.0, result, 0.1);
    }

    @Test
    public void calculateMeanPerformance(){
        long t0 = System.currentTimeMillis();
        calculateMeanTest();
        long t1 = System.currentTimeMillis() - t0;

        Assert.assertTrue(t1 <= 10);
    }

    @Test
    public void calculateStandardDeviationTest(){
        List<Rate> dominant = getRatesMock();
        double result = currencyService.calculateStandardDeviation(dominant);
        Assert.assertEquals(1.5, result, 0.1);
    }

    @Test
    public void calculateStandardDeviationPerformance(){
        long t0 = System.currentTimeMillis();
        calculateStandardDeviationTest();
        long t1 = System.currentTimeMillis() - t0;

        Assert.assertTrue(t1 <= 10);
    }

    @Test
    public void calculateCoefficientOfVariationTest(){
        List<Rate> dominant = getRatesMock();
        double result = currencyService.calculateCoefficientOfVariation(dominant);
        Assert.assertEquals(50, result, 0.1);
    }

    @Test
    public void calculateCoefficientOfVariationPerformance(){
        long t0 = System.currentTimeMillis();
        calculateCoefficientOfVariationTest();
        long t1 = System.currentTimeMillis() - t0;

        Assert.assertTrue(t1 <= 10);
    }

    private List<Currency> getCurrenciesMock() {
        List<Currency> currencies = new ArrayList<>();
        currencies.add(Currency.builder().name("dolar amerykański").code("USD").mid(3.75).build());
        currencies.add(Currency.builder().name("euro").code("EUR").mid(0.16).build());
        return currencies;
    }

    private List<Rate> getRatesMock() {
        List<Rate> rates = new ArrayList<>();
        rates.add(Rate.builder().mid(1.0).build());
        rates.add(Rate.builder().mid(2.0).build());
        rates.add(Rate.builder().mid(5.0).build());
        rates.add(Rate.builder().mid(3.0).build());
        rates.add(Rate.builder().mid(4.0).build());
        rates.add(Rate.builder().mid(4.0).build());
        rates.add(Rate.builder().mid(2.0).build());
        rates.add(Rate.builder().mid(3.0).build());
        rates.add(Rate.builder().mid(3.0).build());
        return rates;
    }

    private List<Rate> getRatesForTimePeriodMock() {
        List<Rate> rates = new ArrayList<>();
        rates.add(Rate.builder().no("2").effectiveDate("2019-01-10").mid(3.75).build());
        rates.add(Rate.builder().no("1").effectiveDate("2019-01-04").mid(0.16).build());
        return rates;
    }
    private List<Rate> getSecondRatesForTimePeriodMock() {
        List<Rate> rates = new ArrayList<>();
        rates.add(Rate.builder().no("2").effectiveDate("2019-01-10").mid(2.75).build());
        rates.add(Rate.builder().no("1").effectiveDate("2019-01-04").mid(0.76).build());
        return rates;
    }
}