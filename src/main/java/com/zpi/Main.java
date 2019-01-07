package com.zpi;

import com.zpi.client.NbpRestClient;
import com.zpi.client.NbpRestClientImpl;
import com.zpi.model.currency.Currency;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends Application {
	private static final String DEFAULT_TABLE = "a";
	private static final String DEFAULT_CURRENCY = "USD";

	private NbpRestClient nbpRestClient = new NbpRestClientImpl();
	private Scene scene;

	@Override
	public void start(Stage stage) throws IOException {
		URL resource = getClass().getClassLoader().getResource("layout.fxml");
		this.scene = new Scene(FXMLLoader.load(resource));
		initCurrenciesChoiceBox();
		stage.setScene(this.scene);
		stage.show();
	}

	private void initCurrenciesChoiceBox() {
		List<Currency> currencies = nbpRestClient.getAvailableCurrencies(DEFAULT_TABLE);
		ChoiceBox<Currency> choiceBox = (ChoiceBox<Currency>) scene.lookup("#choice_currencies");
		choiceBox.getSelectionModel().selectedItemProperty()
				.addListener((observable, oldValue, newValue) -> updateRatesChart(newValue));
		choiceBox.setItems(FXCollections.observableArrayList(currencies));
		choiceBox.setValue(currencies.stream()
				.filter(currency -> currency.getCode().equalsIgnoreCase(DEFAULT_CURRENCY))
				.findFirst().orElseThrow(() -> new RuntimeException("Currency '" + DEFAULT_CURRENCY + "' not found!")));
	}

	private void updateRatesChart(Currency currency) {
		LineChart<String, Double> chart = (LineChart) scene.lookup("#chart_currency_rates");
		List<XYChart.Data<String, Double>> ratesData = nbpRestClient.getRates(DEFAULT_TABLE, currency.getCode(), 10)
				.stream()
				.map(rate -> new XYChart.Data<>(rate.getEffectiveDate(), rate.getMid()))
				.collect(Collectors.toList());
		XYChart.Series<String, Double> serie = new XYChart.Series<>(FXCollections.observableArrayList(ratesData));
		serie.setName(currency.getName());
		chart.getData().clear();
		chart.getData().add(serie);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
