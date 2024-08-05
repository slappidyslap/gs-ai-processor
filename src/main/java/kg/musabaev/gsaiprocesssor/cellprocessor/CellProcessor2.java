package kg.musabaev.seogooglesheetshelper.cellprocessor;

import com.google.api.services.sheets.v4.model.ValueRange;
import io.github.bonigarcia.wdm.WebDriverManager;
import javafx.application.Platform;
import kg.musabaev.seogooglesheetshelper.chatgpt.ChatGptService;
import kg.musabaev.seogooglesheetshelper.google.GoogleSheetsService;
import kg.musabaev.seogooglesheetshelper.google.GoogleSheetsUtil;
import kg.musabaev.seogooglesheetshelper.gui.model.MainFormModel;
import lombok.extern.slf4j.Slf4j;
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
public class CellProcessor2 {

	private final MainFormModel mainFormModel;
	private final ChatGptService chatGpt;
	private final GoogleSheetsService google;
	private final WebDriver web;

	public CellProcessor2(MainFormModel model) {
		WebDriverManager.chromedriver().setup();
		web = new ChromeDriver();
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
				final String newTitleCell = "E" + currentRowId;
				final String newDescCell = "G" + currentRowId;
				String url = (String) values.get(i).get(0);
				String textContent;

                try {
					textContent = getTextContent(url);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
				log.info(textContent);

                String[] metas = null;
                try {
                    metas = retrieveMetasFromChatGpt(textContent, i + 3);
                } catch (InterruptedException e) {
                    web.findElement(By.xpath("//*[@id=\"__next\"]/div[1]/div/main/div[1]/div[2]/div[1]/div[2]/button")).click();
					continue;
                }
                final String metaTitle = metas[0];
				final String metaDesc = metas[1];
				log.info(metaTitle);
				log.info(metaDesc);

                try {
					google.updateValues(
							google.getSheets(),
							mainFormModel.spreadsheetId().get(),
							newTitleCell,
							List.of(List.of(metaTitle)));
					google.updateValues(
							google.getSheets(),
							mainFormModel.spreadsheetId().get(),
							newDescCell,
							List.of(List.of(metaDesc)));
				} catch (IOException e) {
					log.info("Ошибка при обновлении ячейки {}: {}", currentCell, e.getMessage());
					mainFormModel.addCellError(currentCell, e.getMessage());
				}

				log.info("Ячейка {} была обработана", currentCell);

				try {
					TimeUnit.SECONDS.sleep(20);
				} catch (InterruptedException e) {
					log.error("Поток был прерван при ожидании следующей ячейки в Google Sheets", e);
				}
			}
		}).start();
	}

	private String[] retrieveMetasFromChatGpt(String textContent, int i) throws InterruptedException {
		web.get("https://auth0.openai.com/authorize?client_id=TdJIcbe16WoTHtN95nyywh5E4yOo6ItG&scope=openid%20email%20profile%20offline_access%20model.request%20model.read%20organization.read%20organization.write&response_type=code&redirect_uri=https%3A%2F%2Fchatgpt.com%2Fapi%2Fauth%2Fcallback%2Fauth0&audience=https%3A%2F%2Fapi.openai.com%2Fv1&device_id=503b48c9-ab36-4a36-bec0-a50a7bc36a5c&prompt=login&ext-statsig-tier=production&ext-oai-did=503b48c9-ab36-4a36-bec0-a50a7bc36a5c&state=rUIZu7-EM_EEnTJ3fESm9QS6aV40NWuHQEkO1GugjZc&code_challenge=xyO-PhexswydVqmKru25tv1hwwcKoU-JIpj605gLJiI&code_challenge_method=S256");
		sleep(1000);
		WebDriverWait wait5min = new WebDriverWait(web, Duration.ofMinutes(5));
		WebElement promptInEl = wait5min.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("prompt-textarea"))).get(0);
		String systemMsg = "Смотри ты мне будешь SEO помощником и ты мне будешь помогать " +
				"составлять meta title и meta description. Естественно соблюдай общую длину и старайся не превышать длину. " +
				"Используй больше ключевых слов, а не чисел. Я тебе дам контент страницы, а ты должен мне только в одном " +
				"формате без лишних пробелов, объяснений, описаний, и тому подобное. Плюс в конец title добавляй \"| Статьи Kompanion\". " +
				"Как итог ты должен сперва записать meta title и только потом meta description и между ними обязательно должен быть символ /, то есть должен быть слеш. " +
				"Это обязательно, я потом собираюсь спецальным образом отформатировать твой выданнный результат. Вот содержание страницы\n" + textContent;
		promptInEl.sendKeys(systemMsg);
		sleep(3000);
		WebElement sendBtn = promptInEl.findElement(By.xpath("./../following-sibling::button[1]"));
		sleep(3000);
		sendBtn.click();
		WebDriverWait wait1min = new WebDriverWait(web, Duration.ofMinutes(1));
		// когда нажимает на кнопку она меняет иконку, как иконка вернетеся в исходное это значит, что ответ завершен
		wait1min.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.tagName("button[data-testid='fruitjuice-send-button']"))).get(0);
		WebElement gptResp = web.findElement(By.tagName("div[data-testid='conversation-turn-%s']".formatted(i)));
		WebElement respContent = gptResp.findElement(By.xpath("./div/div/div[2]/div/div[1]/div/div/div/p"));
		return respContent.getText().split("/");
	}

	public String getTextContent(String url) throws IOException {
		try {
			web.get(url);
			WebDriverWait wait = new WebDriverWait(web, Duration.ofMinutes(5));
			WebElement main = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.tagName("main"))).get(0);
			return main.getText();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
