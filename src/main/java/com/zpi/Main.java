package com.zpi;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import com.zpi.client.NbpRestClientImpl;
import com.zpi.model.currency.Currency;
import com.zpi.model.rate.Rate;
import com.zpi.service.CurrencyService;
import com.zpi.service.SessionsDataPack;
import com.zpi.service.TimePeriod;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main extends Application {
	private static final int SCENE_WIDTH = 1000;
	private static final int SCENE_HEIGHT = 600;
	private static final String DEFAULT_CURRENCY = "USD";
	private static final String DEFAULT_SECOND_CURRENCY = "EUR";
	private static final String TITLE = "Kursy walut";

	private static final String SERIES_MEDIAN = "mediana";
	private static final String SERIES_DOMINANT = "dominanta";

	private CurrencyService currencyService = new CurrencyService(new NbpRestClientImpl());

	private LineChart<String, Double> lineChart;
	private PieChart pieChart;
	private BarChart<String, Integer> barchartDistributions;
	private ChoiceBox<Currency> choiceBoxCurrencies;
	private ChoiceBox<Currency> choiceBoxSecondCurrencies;
	private ChoiceBox<TimePeriod> choiceBoxDates;
	private CheckBox checkBoxMedian;
	private CheckBox checkBoxDominant;
	private Label labelCoefficientOfVariation;
	private Label labelMedian;
	private Label labelDominant;
	private Label labelStandardVariation;
	private Polygon polygonCoefficientOfVariation;
	private Label labelRiseSessions;
	private Label labelFallSessions;
	private Label labelUnchangedSessions;

	@Override
	public void start(Stage stage) throws IOException {
		Scene scene = prepareScene(SCENE_WIDTH, SCENE_HEIGHT);
		bindControls(scene);
		initControls();
		initStage(stage, scene);
	}

	private Scene prepareScene(int width, int heigt) throws IOException {
		URL resource = getClass().getClassLoader().getResource("layout.fxml");
		return new Scene(FXMLLoader.load(resource), width, heigt);
	}

	private void initStage(Stage stage, Scene scene) {
		stage.setScene(scene);
		stage.setResizable(false);
		stage.setTitle(TITLE);
		stage.show();
	}

	private void bindControls(Scene scene) {
		lineChart = (LineChart<String, Double>) scene.lookup("#linechart_currency_rates");
		pieChart = (PieChart) scene.lookup("#piechart_sessions");
		barchartDistributions = (BarChart<String, Integer>) scene.lookup("#barchart_distributions");
		choiceBoxCurrencies = (ChoiceBox<Currency>) scene.lookup("#choicebox_currencies");
		choiceBoxSecondCurrencies = (ChoiceBox<Currency>) scene.lookup("#choicebox_second_currencies");
		choiceBoxDates = (ChoiceBox<TimePeriod>) scene.lookup("#choicebox_time_periods");
		checkBoxMedian = (CheckBox) scene.lookup("#checkbox_median");
		checkBoxDominant = (CheckBox)  scene.lookup("#checkbox_dominant");
		labelCoefficientOfVariation = (Label) scene.lookup("#label_coefficient_of_variation");
		labelMedian = (Label) scene.lookup("#label_median");
		labelDominant = (Label) scene.lookup("#label_dominant");
		labelStandardVariation = (Label) scene.lookup("#label_standard_variation");
		polygonCoefficientOfVariation = (Polygon) scene.lookup("#polygon_coefficient_of_variation");
		labelRiseSessions = (Label) scene.lookup("#label_rise_sessions");
		labelFallSessions = (Label) scene.lookup("#label_fall_sessions");
		labelUnchangedSessions = (Label) scene.lookup("#label_unchanged_sessions");
	}

	private void initControls() {
		initChoiceBoxes();
		initCheckBoxes();
	}

	private void initCheckBoxes() {
		checkBoxMedian.selectedProperty().addListener((observable, oldValue, newValue) -> updateMedian());
		checkBoxDominant.selectedProperty().addListener((observable, oldValue, newValue) ->  updateDominant());
	}

	private void initChoiceBoxes() {
		List<Currency> currencies = currencyService.getCurrencies();
		initCurrenciesChoiceBox(currencies);
		initSecondCurrenciesChoiceBox(currencies);
		initDatesChoiceBox();
	}

	private void initCurrenciesChoiceBox(List<Currency> currencies) {
		choiceBoxCurrencies.setValue(currencies.stream()
				.filter(currency -> currency.getCode().equalsIgnoreCase(DEFAULT_CURRENCY))
				.findFirst().orElseThrow(() -> new RuntimeException("Currency '" + DEFAULT_CURRENCY + "' not found!")));
		choiceBoxCurrencies.setItems(FXCollections.observableArrayList(currencies));
		choiceBoxCurrencies.getSelectionModel().selectedItemProperty()
				.addListener((observable, oldValue, newValue) -> updateRatesChart());
	}

	private void initSecondCurrenciesChoiceBox(List<Currency> currencies) {
		choiceBoxSecondCurrencies.setValue(currencies.stream()
				.filter(currency -> currency.getCode().equalsIgnoreCase(DEFAULT_SECOND_CURRENCY))
				.findFirst().orElseThrow(() -> new RuntimeException("Currency '" + DEFAULT_SECOND_CURRENCY + "' not found!")));
		choiceBoxSecondCurrencies.setItems(FXCollections.observableArrayList(currencies));
		choiceBoxSecondCurrencies.getSelectionModel().selectedItemProperty()
				.addListener((observable, oldValue, newValue) -> updateDistributionChanges());
	}

	private void initDatesChoiceBox() {
		choiceBoxDates.getSelectionModel().selectedItemProperty()
				.addListener((observable, oldValue, newValue) -> updateRatesChart());
		choiceBoxDates.setItems(FXCollections.observableArrayList(TimePeriod.values()));
		choiceBoxDates.setValue(TimePeriod.WEEK);
	}

	private void updateRatesChart() {
		lineChart.getData().clear();
		Currency currency = choiceBoxCurrencies.getValue();
		TimePeriod timePeriod = choiceBoxDates.getValue();
		lineChart.getData().add(createSeries(currency.getName(), fetchData(currency, timePeriod)));
		updateStatisticSeries();
		updateCoefficientOfVariation();
		updateDistributionChanges();
	}
	private XYChart.Series<String, Double> createSeries(String name, List<XYChart.Data<String, Double>> ratesData) {
		XYChart.Series<String, Double> series = new XYChart.Series<>(FXCollections.observableArrayList(ratesData));
		series.setName(name);
		return series;
	}

	private void updateStatisticSeries() {
		updateMedian();
		updateDominant();
		updateStandardVariation();
		updateSessions();
	}

	private void updateSessions() {
		List<Rate> cachedRates = currencyService.getCachedRates();
		pieChart.getData().clear();
		SessionsDataPack sessionsDataPack = currencyService.calculateSessions(cachedRates);
		pieChart.getData().add(new PieChart.Data("Sesje wzrostowe", sessionsDataPack.getRiseSessions()));
		pieChart.getData().add(new PieChart.Data("Sesje spadkowe", sessionsDataPack.getFallSessions()));
		pieChart.getData().add(new PieChart.Data("Sesje bez zmian", sessionsDataPack.getUnchangedSessions()));
		labelRiseSessions.setText(sessionsDataPack.getRiseSessions() + "");
		labelFallSessions.setText(sessionsDataPack.getFallSessions() + "");
		labelUnchangedSessions.setText(sessionsDataPack.getUnchangedSessions() + "");
	}

	private void updateMedian() {
		List<Rate> cachedRates = currencyService.getCachedRates();
		double value = currencyService.calculateMedian(cachedRates);
		labelMedian.setText(String.format("%.4f", value));
		if (checkBoxMedian.isSelected()) {
			lineChart.getData().add(createValueMarkerSeries(SERIES_MEDIAN, cachedRates, value));
		} else {
			removeStatisticSeries(SERIES_MEDIAN);
		}
	}

	private void updateDominant() {
		List<Rate> cachedRates = currencyService.getCachedRates();
		double value = currencyService.calculateDominant(cachedRates);
		labelDominant.setText(String.format("%.4f", value));
		if (checkBoxDominant.isSelected()) {
			lineChart.getData().add(createValueMarkerSeries(SERIES_DOMINANT, cachedRates, value));
		} else {
			removeStatisticSeries(SERIES_DOMINANT);
		}
	}

	private void updateStandardVariation() {
		List<Rate> cachedRates = currencyService.getCachedRates();
		double value = currencyService.calculateStandardDeviation(cachedRates);
		labelStandardVariation.setText(String.format("%.4f", value));
	}

	private void updateCoefficientOfVariation() {
		List<Rate> cachedRates = currencyService.getCachedRates();
		double value = currencyService.calculateCoefficientOfVariation(cachedRates);
		polygonCoefficientOfVariation.setRotate(value >= 0 ? 0 : 180);
		polygonCoefficientOfVariation.setFill(value >= 0 ? Paint.valueOf("lime") : Paint.valueOf("red"));
		labelCoefficientOfVariation.setTextFill(value >= 0 ? Paint.valueOf("green") : Paint.valueOf("red"));
		labelCoefficientOfVariation.setText(String.format("%.4f", value) + "%");
	}

	private void updateDistributionChanges() {
		barchartDistributions.getData().clear();
		List<XYChart.Data<String, Integer>> data = currencyService
				.getDistributionChangesForTwoCurrencies(choiceBoxSecondCurrencies.getValue().getCode(), choiceBoxDates.getValue()).entrySet().stream()
				.map(entry -> new XYChart.Data<>(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
		XYChart.Series<String, Integer> series = new XYChart.Series<>(FXCollections.observableArrayList(data));
		series.setName(choiceBoxCurrencies.getValue().getCode() + " / " + choiceBoxSecondCurrencies.getValue().getCode());
		barchartDistributions.getData().add(series);


	}

	private XYChart.Series<String, Double> createValueMarkerSeries(String name, List<Rate> cachedRates, double value) {
		XYChart.Data<String, Double> start = new XYChart.Data<>(cachedRates.get(0).getEffectiveDate(), value);
		XYChart.Data<String, Double> end = new XYChart.Data<>(cachedRates.get(cachedRates.size() - 1).getEffectiveDate(), value);
		XYChart.Series<String, Double> series = new XYChart.Series<>(FXCollections.observableArrayList(start, end));
		series.setName(name);
		return series;
	}

	private boolean removeStatisticSeries(String name) {
		return lineChart.getData().removeIf(series -> series.getName().equals(name));
	}

	private List<XYChart.Data<String, Double>> fetchData(Currency currency, TimePeriod timePeriod) {
		List<Rate> rates = currencyService.getRatesForTimePeriod(currency.getCode(), timePeriod);
		currencyService.setCachedRates(rates);
		return rates.stream()
				.map(rate -> new XYChart.Data<>(rate.getEffectiveDate(), rate.getMid()))
				.collect(Collectors.toList());
	}

	public static void main(String[] args) {
		launch(args);
	}
}
