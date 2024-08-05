package kg.musabaev.seogooglesheetshelper.gui.controller;

import ch.qos.logback.classic.LoggerContext;
import io.avaje.config.Configuration;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import kg.musabaev.seogooglesheetshelper.Config;
import kg.musabaev.seogooglesheetshelper.Contents;
import kg.musabaev.seogooglesheetshelper.Main;
import kg.musabaev.seogooglesheetshelper.cellprocessor.CellProcessor2;
import kg.musabaev.seogooglesheetshelper.cellprocessor.CellProcessor3;
import kg.musabaev.seogooglesheetshelper.cellprocessor.SimpleCellProcessor;
import kg.musabaev.seogooglesheetshelper.gui.dialog.CellsWithErrorDialog;
import kg.musabaev.seogooglesheetshelper.gui.dialog.OpenAiApiKeyInputDialog;
import kg.musabaev.seogooglesheetshelper.gui.dialog.WarningAlert;
import kg.musabaev.seogooglesheetshelper.gui.model.MainFormModel;
import kg.musabaev.seogooglesheetshelper.gui.model.ProgressStatus;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Properties;
import java.util.ResourceBundle;

@Slf4j
public class MainFormController implements Initializable {

	@FXML
	private TextArea queryInput;
	@FXML
	private TextField spreadsheetIdInput, sheetNameInput, rangeInput;
	@FXML
	private RadioButton metaTitle, metaDesc;
	@FXML
	private ToggleGroup selectedMetaType;
	@FXML
	private Button startButton;
	@FXML
	private ProgressBar progressBar;
	@FXML
	private Label progressStatus, progressInfo;

	private MainFormModel model;
	private CellProcessor3 cellProcessor;
	private Configuration config;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		model = new MainFormModel();
		cellProcessor = new CellProcessor3(model);
		config = Config.getConfig();

		// System query
		model.systemQuery().addListener((observableValue, o, n) -> {
			final String newNewValue = n.strip();
			queryInput.setText(newNewValue);
			config.setProperty("SYSTEM_QUERY", newNewValue);
		});
		queryInput.textProperty().addListener((observable, oldValue, newValue) -> {
			final String newNewValue = newValue.strip();
			model.systemQuery().set(newNewValue);
			config.setProperty("SYSTEM_QUERY", newNewValue);
		});
		queryInput.setText(config.get("SYSTEM_QUERY", ""));

		// Spreadsheet id
		model.spreadsheetId().addListener((observableValue, o, n) -> {
			final String newNewValue = n.strip();
			spreadsheetIdInput.setText(newNewValue);
			config.setProperty("SPREADSHEET_ID", newNewValue);
		});
		spreadsheetIdInput.textProperty().addListener((observable, oldValue, newValue) -> {
			final String newNewValue = newValue.strip();
			model.spreadsheetId().set(newNewValue);
			config.setProperty("SPREADSHEET_ID", newNewValue);
		});
		spreadsheetIdInput.setText(config.get("SPREADSHEET_ID", ""));

		// Sheet name
		model.sheetName().addListener((observableValue, o, n) -> {
			final String newNewValue = n.strip();
			sheetNameInput.setText(newNewValue);
			config.setProperty("SHEET_NAME", newNewValue);
		});
		sheetNameInput.textProperty().addListener((observable, oldValue, newValue) -> {
			final String newNewValue = newValue.strip();
			model.sheetName().set(newNewValue);
			config.setProperty("SHEET_NAME", newNewValue);
		});
		sheetNameInput.setText(config.get("SHEET_NAME", ""));

		// Range
		model.range().addListener((observableValue, o, n) -> {
			final String newNewValue = n.strip();
			rangeInput.setText(newNewValue);
			config.setProperty("RANGE", newNewValue);
		});
		rangeInput.textProperty().addListener((observable, oldValue, newValue) -> {
			final String newNewValue = newValue.strip();
			model.range().set(newNewValue);
			config.setProperty("RANGE", newNewValue);
		});
		rangeInput.setText(config.get("RANGE", ""));

		// Selected meta type
		model.isTitle().set(config.getBool("IS_TITLE", true));
		model.isTitle().addListener((observable, oldValue, newValue) -> {
			config.setProperty("IS_TITLE", String.valueOf(newValue));
			selectedMetaType.selectToggle(newValue ? metaTitle : metaDesc);
		});
		selectedMetaType.selectToggle(config.getBool("IS_TITLE", true) ? metaTitle : metaDesc);
		selectedMetaType.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			boolean isTitle = ((RadioButton) newValue).getText().contains("title");
			model.isTitle().set(isTitle);
			config.setProperty("IS_TITLE", String.valueOf(isTitle));
		});

		model.progressStatus().addListener((observable, oldValue, newValue) -> {
			progressStatus.setText(newValue.status());
			if (newValue != ProgressStatus.IN_PROGRESS) progressInfo.setText("");
		});
		model.progressInfo().bindBidirectional(progressInfo.textProperty());
		model.progressIndicator().bindBidirectional(progressBar.progressProperty());
	}

	@FXML
	void onClickStartButton(MouseEvent event) {
		String key = Config.getConfig().get("OPENAI_API_KEY", "");
		if (key.isBlank()) {
			log.info("Нет установлен OpenAI API ключ, вызываю диалоговое окно...");
			var alert = new WarningAlert(Contents.ALERT_NOT_SET_OPENAI_API_KEY);
			alert.showAndWait();
			return;
		} else if (!key.matches(Contents.OPENAI_API_KEY_REGEX)) {
			log.info("OpenAI API ключ не валидный, вызываю диалоговое окно...");
			var alert = new WarningAlert(Contents.ALERT_OPENAI_API_KEY_NOT_VALID);
			alert.showAndWait();
		}

		log.info("Starting processing...");
		startButton.setDisable(true);
		try {
			cellProcessor.startProcessing();
		} catch (Exception e) {
			log.error("", e);
			throw new RuntimeException(e);
		} finally {
			startButton.setDisable(false);
		}
	}

	@FXML
	void onOpenCurrentLogs(ActionEvent event) throws IOException {
		String curTimestamp = getCurTimestampByLoggerContext();
		Desktop.getDesktop().open(Path.of(
				System.getProperty("user.home"),
				".seo-google-sheets-helper",
				"logs",
				"logfile-%s.txt".formatted(curTimestamp)).toFile());
	}

	private static String getCurTimestampByLoggerContext() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        return context.getProperty("currentTimestamp");
	}

	@FXML
	public void showCellErrors() {
		new CellsWithErrorDialog(model).show();
	}

	@FXML
	public void onChangeApiKey(ActionEvent event) {
		new OpenAiApiKeyInputDialog(OpenAiApiKeyInputDialog.Action.CREATE).show();
	}

	@FXML
	public void saveFile(ActionEvent actionEvent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Сохранить конфигурацию");
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Файлы конфигурации", "*.properties"));
		fileChooser.setInitialFileName("config-" + getCurTimestampByLoggerContext() + ".properties");
		File file = fileChooser.showSaveDialog(Main.primaryStage());
		Config.storeTo(file);
	}

	@FXML
	public void loadFile(ActionEvent actionEvent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Загрузить конфигурацию");
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Файлы конфигурации", "*.properties"));
		File file = fileChooser.showOpenDialog(Main.primaryStage());
		var props = new Properties();
        try {
            props.load(new FileInputStream(file));
        } catch (IOException e) {
			log.error("Ошибка при чтении файла конфигурации");
            throw new RuntimeException(e);
        }

		model.systemQuery().set(props.getProperty("SYSTEM_QUERY", model.systemQuery().get()));
		model.spreadsheetId().set(props.getProperty("SPREADSHEET_ID", model.spreadsheetId().get()));
		model.isTitle().set(Boolean.parseBoolean(props.getProperty("IS_TITLE", model.isTitle().toString())));
		Config.getConfig().setProperty("OPENAI_API_KEY", props.getProperty("OPENAI_API_KEY", ""));
		model.sheetName().set(props.getProperty("SHEET_NAME", model.sheetName().get()));
		model.range().set(props.getProperty("RANGE", model.range().get()));
		Config.storeAll();
	}
}
