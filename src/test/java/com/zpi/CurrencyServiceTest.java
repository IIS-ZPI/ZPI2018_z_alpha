package com.zpi;

import com.zpi.client.NbpRestClient;
import com.zpi.model.currency.Currency;
import com.zpi.model.rate.Rate;
import com.zpi.service.CurrencyService;
import com.zpi.service.TimePeriod;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class CurrencyServiceTest {

    @Mock
    private NbpRestClient nbpRestClient;
    @InjectMocks
    private CurrencyService currencyService;

    @Test
    public void getCurrenciesMethodShouldReturnListOfCurrenciesFetchedFromNbpRestClient() {
        Mockito
                .when(nbpRestClient.getAvailableCurrencies(Mockito.anyString()))
                .thenReturn(getCurrenciesMock());

        List<Currency> result = currencyService.getCurrencies();

        Assert.assertNotNull(result);
    }

    @Test
    public void getRatesForTimePeriodShouldReturnRatesForSpecificTimePeriod() {
        Mockito
                .when(nbpRestClient.getRatesForDatePeriod(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(getRatesForTimePeriodMock());

        List<Rate> result = currencyService.getRatesForTimePeriod("usd", TimePeriod.WEEK);
        Assert.assertNotNull(result);
    }

    private List<Currency> getCurrenciesMock() {
        List<Currency> currencies = new ArrayList<>();
        currencies.add(Currency.builder().name("dolar amerykański").code("USD").mid(3.75).build());
        currencies.add(Currency.builder().name("korona czeska").code("PLN").mid(0.16).build());
        return currencies;
    }

    private List<Rate> getRatesForTimePeriodMock() {
        List<Rate> rates = new ArrayList<>();
        rates.add(Rate.builder().no("2").effectiveDate("2019-01-10").mid(3.75).build());
        rates.add(Rate.builder().no("1").effectiveDate("2019-01-04").mid(0.16).build());
        return rates;
    }
}