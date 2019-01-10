package com.zpi;

import com.zpi.client.NbpRestClient;
import com.zpi.model.currency.Currency;
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

        Assert.assertNotNull(null);
    }

    private List<Currency> getCurrenciesMock() {
        List<Currency> currencies = new ArrayList<>();
        currencies.add(Currency.builder().name("dolar amerykański").code("USD").mid(3.75).build());
        currencies.add(Currency.builder().name("korona czeska").code("PLN").mid(0.16).build());
        return currencies;
    }
}