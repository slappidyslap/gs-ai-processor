package kg.musabaev.seogooglesheetshelper.cellprocessor;

import com.google.api.services.sheets.v4.model.ValueRange;
import javafx.application.Platform;
import kg.musabaev.seogooglesheetshelper.chatgpt.ChatGptService;
import kg.musabaev.seogooglesheetshelper.google.GoogleSheetsService;
import kg.musabaev.seogooglesheetshelper.google.GoogleSheetsUtil;
import kg.musabaev.seogooglesheetshelper.gui.model.CellError;
import kg.musabaev.seogooglesheetshelper.gui.model.MainFormModel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class SimpleCellProcessor {

	private final MainFormModel mainFormModel;
	private final ChatGptService chatGpt;
	private final GoogleSheetsService google;

	public SimpleCellProcessor(MainFormModel model) {
		mainFormModel = model;
		chatGpt = new ChatGptService();
		try {
			google = new GoogleSheetsService(mainFormModel);
		} catch (GeneralSecurityException | IOException e) {
			log.error("", e);
			throw new RuntimeException(e);
		}
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
					};
				} catch (Exception e) {
					log.info("{} пустая ячейка", currentCell);
					mainFormModel.addCellErrorAsEmptyCell(currentCell);
					continue;
				}

				log.info("Обработка ячейки {}...", currentCell);
				String content = (String) values.get(i).get(0);
				log.info("Содержимое ячейки {}:\n{}", currentCell, content);

//				String newContent = chatGpt.execute(mainFormModel.systemQuery().get(), content, mainFormModel.isTitle().get());
				String newContent = "test";
				log.info("Контент сгенерированный от ChatGPT:\n{}", newContent);

				try {
					google.updateValues(
							google.getSheets(),
							mainFormModel.spreadsheetId().get(),
							currentCell,
							List.of(List.of(newContent)));
				} catch (IOException e) {
					log.info("Ошибка при обновлении ячейки {}: {}", currentCell, e.getMessage());
					mainFormModel.addCellError(currentCell, e.getMessage());
				}

				log.info("Ячейка {} была обработана", currentCell);

//				try {
//					TimeUnit.SECONDS.sleep(20);
//				} catch (InterruptedException e) {
//					log.error("Поток был прерван при ожидании следующей ячейки в Google Sheets", e);
//				}
			}
		}).start();
	}
}
