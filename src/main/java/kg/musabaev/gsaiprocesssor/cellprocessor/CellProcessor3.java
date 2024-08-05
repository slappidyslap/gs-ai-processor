package kg.musabaev.seogooglesheetshelper.cellprocessor;

import com.google.api.services.sheets.v4.model.ValueRange;
import io.github.bonigarcia.wdm.WebDriverManager;
import javafx.application.Platform;
import kg.musabaev.seogooglesheetshelper.ai.Gemini;
import kg.musabaev.seogooglesheetshelper.chatgpt.ChatGptService;
import kg.musabaev.seogooglesheetshelper.google.GoogleSheetsService;
import kg.musabaev.seogooglesheetshelper.google.GoogleSheetsUtil;
import kg.musabaev.seogooglesheetshelper.gui.model.MainFormModel;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

@Slf4j
public class CellProcessor3 {

	private final MainFormModel mainFormModel;
	private final Gemini gemini;
	private final GoogleSheetsService google;
	private final WebDriver web;

	public CellProcessor3(MainFormModel model) {
        this.gemini = new Gemini();
		mainFormModel = model;
		try {
			google = new GoogleSheetsService(mainFormModel);
		} catch (GeneralSecurityException | IOException e) {
			log.error("", e);
			throw new RuntimeException(e);
		}
		WebDriverManager.chromedriver().setup();
		web = new ChromeDriver();
	}

	public void startProcessing() {
		final String fullRange = mainFormModel.sheetName().get() + "!" + mainFormModel.range().get();
		final String rangeColumnId = GoogleSheetsUtil.getEndColumnId(fullRange);
		new Thread(() -> {
			final List<List<Object>> values;
			try {
				ValueRange response = google.getSheets().spreadsheets().values()
						.get(mainFormModel.spreadsheetId().get(), fullRange)
						.execute();
				values = response.getValues();
				log.info("Все излеченные данные из Google Sheets:\n{}", Arrays.deepToString(values.toArray()));
			} catch (IOException e) {
				log.error("Ошибка при извлечении данных из Google Sheets", e);
				throw new RuntimeException(e);
			}

			if (values.isEmpty()) {
				log.info("Отсутствует данные из Google Sheets");
				return;
			}

			int rowCount = GoogleSheetsUtil.getRowCount(fullRange);
			for (int i = 0; i < rowCount; i++) {
				try {
					int currentRowId = GoogleSheetsUtil.getStartRowId(fullRange) + i;
					String currentCell = rangeColumnId + currentRowId;

					int finalI = i;
					Platform.runLater(() -> {
						mainFormModel.progressInfo().set("%s из %s".formatted((finalI + 1), rowCount));
						mainFormModel.progressIndicator().set((double) (finalI + 1) / rowCount);
					});

					// Проверка на то, что ячейка не пуста
					try {
						Object value = values.get(i).get(0);
						if (value == null) {
							log.info("{} пустая ячейка", currentCell);
							mainFormModel.addCellErrorAsEmptyCell(currentCell);
							continue;
						}
						if (((String) value).isBlank()) {
							log.info("{} пустая ячейка", currentCell);
							mainFormModel.addCellErrorAsEmptyCell(currentCell);
							continue;
						}
						;
					} catch (Exception e) {
						log.info("{} пустая ячейка", currentCell);
						mainFormModel.addCellErrorAsEmptyCell(currentCell);
						continue;
					}

					final String newTitleCell = "G" + currentRowId;
					final String newDescCell = "D" + currentRowId;
					String url = (String) values.get(i).get(0);
					String textContent;


					try {
						textContent = getTextContent(url);
					} catch (IOException e) {
						setErrorToCells(e, newTitleCell, newDescCell);
						continue;
					}
                    log.info("textContent:\n{}", textContent);

					String[] metas = null;
					try {
						metas = retrieveMetasFromChatGpt(textContent, i + 3);
					} catch (Exception e) {
						setErrorToCells(e, newTitleCell, newDescCell);
						continue;
					}
					String metaTitle;
					String metaDesc;
					try {
						metaTitle = metas[0].trim();
						metaDesc = metas[1].trim();
					} catch (Exception e) {
						setErrorToCells(e, newTitleCell, newDescCell);
						continue;
					}

					try {
						google.updateValues(
								google.getSheets(),
								mainFormModel.spreadsheetId().get(),
								mainFormModel.sheetName().get() + "!" + newTitleCell,
								List.of(List.of(metaTitle)));
						google.updateValues(
								google.getSheets(),
								mainFormModel.spreadsheetId().get(),
								mainFormModel.sheetName().get() + "!" + newDescCell,
								List.of(List.of(metaDesc)));
					} catch (IOException e) {
						log.info("Ошибка при обновлении ячейки {}: {}", currentCell, e.getMessage());
						setErrorToCells(e, newTitleCell, newDescCell);
						mainFormModel.addCellError(currentCell, e.getMessage());
						continue;
					}

					log.info("Ячейка {} была обработана", currentCell);

					try {
						TimeUnit.SECONDS.sleep(13);
					} catch (InterruptedException e) {
						log.error("Поток был прерван при ожидании следующей ячейки в Google Sheets", e);
						setErrorToCells(e, newTitleCell, newDescCell);
					}
				} catch (Exception e) {
					continue;
				}
			}
		}).start();
	}

	private String[] retrieveMetasFromChatGpt(String textContent, int i) throws InterruptedException {
		var metas = gemini.execute(textContent);
		var a = metas.split("\\[mid\\]");
		if (a.length == 2) return a;
		a = metas.split("/");
		if (a.length == 2) return a;
		a = metas.split("\\n");
		if (a.length == 2) return a;
		a = metas.split("\\|");
		if (a.length == 2) return a;
		return null;
	}

	public String getTextContent(String url) throws IOException, InterruptedException {
		web.get(url);
		WebDriverWait wait = new WebDriverWait(web, Duration.ofMinutes(1));
		var main = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.tagName("main"))).get(0);
        var a = main.getText();
		if (a.isBlank() || a.length() < 50) return web.findElement(By.tagName("body")).getText();
		return a;
	}

	public void setErrorToCells(Exception e, String cell1, String cell2) {
        try {
            google.updateValues(
                    google.getSheets(),
                    mainFormModel.spreadsheetId().get(),
                    mainFormModel.sheetName().get() + "!" + cell1,
                    List.of(List.of(e.getMessage())));
        google.updateValues(
				google.getSheets(),
				mainFormModel.spreadsheetId().get(),
				mainFormModel.sheetName().get() + "!" + cell2,
				List.of(List.of(e.getMessage())));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
