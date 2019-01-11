package com.zpi;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
import java.util.stream.Collectors;

public class Main extends Application {
	private static final int SCENE_WIDTH = 1000;
	private static final int SCENE_HEIGHT = 600;
	private static final String DEFAULT_CURRENCY = "USD";
	private static final String TITLE = "Kursy walut";

	private static final String SERIES_MEDIAN = "mediana";
	private static final String SERIES_DOMINANT = "dominanta";
	private static final String SERIES_STANDARD_VARIATION = "odchylenie standardowe";

	private CurrencyService currencyService = new CurrencyService(new NbpRestClientImpl());

	private LineChart<String, Double> lineChart;
	private PieChart pieChart;
	private ChoiceBox<Currency> choiceBoxCurrencies;
	private ChoiceBox<TimePeriod> choiceBoxDates;
	private CheckBox checkBoxMedian;
	private CheckBox checkBoxDominant;
	private CheckBox checkBoxStandardVariation;
	private Label labelCoefficientOfVariation;
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
		choiceBoxCurrencies = (ChoiceBox<Currency>) scene.lookup("#choicebox_currencies");
		choiceBoxDates = (ChoiceBox<TimePeriod>) scene.lookup("#choicebox_time_periods");
		checkBoxMedian = (CheckBox) scene.lookup("#checkbox_median");
		checkBoxDominant = (CheckBox)  scene.lookup("#checkbox_dominant");
		checkBoxStandardVariation = (CheckBox) scene.lookup("#checkbox_standard_deviation");
		labelCoefficientOfVariation = (Label) scene.lookup("#label_coefficient_of_variation");
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
		checkBoxStandardVariation.selectedProperty().addListener((observable, oldValue, newValue) -> updateStandardVariation());
	}

	private void initChoiceBoxes() {
		initCurrenciesChoiceBox();
		initDatesChoiceBox();
	}

	private void initCurrenciesChoiceBox() {
		List<Currency> currencies = currencyService.getCurrencies();
		choiceBoxCurrencies.setValue(currencies.stream()
				.filter(currency -> currency.getCode().equalsIgnoreCase(DEFAULT_CURRENCY))
				.findFirst().orElseThrow(() -> new RuntimeException("Currency '" + DEFAULT_CURRENCY + "' not found!")));
		choiceBoxCurrencies.setItems(FXCollections.observableArrayList(currencies));
		choiceBoxCurrencies.getSelectionModel().selectedItemProperty()
				.addListener((observable, oldValue, newValue) -> updateRatesChart());
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
		if (checkBoxMedian.isSelected()) {
			List<Rate> cachedRates = currencyService.getCachedRates();
			double value = currencyService.calculateMedian(cachedRates);
			lineChart.getData().add(createValueMarkerSeries(SERIES_MEDIAN, cachedRates, value));
		} else {
			removeStatisticSeries(SERIES_MEDIAN);
		}
	}

	private void updateDominant() {
		if (checkBoxDominant.isSelected()) {
			List<Rate> cachedRates = currencyService.getCachedRates();
			double value = currencyService.calculateDominant(cachedRates);
			lineChart.getData().add(createValueMarkerSeries(SERIES_DOMINANT, cachedRates, value));
		} else {
			removeStatisticSeries(SERIES_DOMINANT);
		}
	}

	private void updateStandardVariation() {
		if (checkBoxStandardVariation.isSelected()) {
			List<Rate> cachedRates = currencyService.getCachedRates();
			double value = currencyService.calculateStandardVariation(cachedRates);
			lineChart.getData().add(createValueMarkerSeries(SERIES_STANDARD_VARIATION, cachedRates, value));
		} else {
			removeStatisticSeries(SERIES_STANDARD_VARIATION);
		}
	}

	private void updateCoefficientOfVariation() {
		List<Rate> cachedRates = currencyService.getCachedRates();
		double value = currencyService.calculateCoefficientOfVariation(cachedRates);
		polygonCoefficientOfVariation.setRotate(value >= 0 ? 0 : 180);
		polygonCoefficientOfVariation.setFill(value >= 0 ? Paint.valueOf("lime") : Paint.valueOf("red"));
		labelCoefficientOfVariation.setTextFill(value >= 0 ? Paint.valueOf("green") : Paint.valueOf("red"));
		labelCoefficientOfVariation.setText(String.format("%.3f", value));
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
		return currencyService
				.getRatesForTimePeriod(currency.getCode(), timePeriod).stream()
				.map(rate -> new XYChart.Data<>(rate.getEffectiveDate(), rate.getMid()))
				.collect(Collectors.toList());
	}

	public static void main(String[] args) {
		launch(args);
	}
}
