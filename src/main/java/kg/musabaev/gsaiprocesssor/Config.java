package kg.musabaev.seogooglesheetshelper;

import io.avaje.config.Configuration;
import kg.musabaev.seogooglesheetshelper.gui.dialog.OpenAiApiKeyInputDialog;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class Config {

	private static volatile Configuration instance;
	private static File settingsFile;

	private Config() {
	}

	public static Configuration getConfig() {
		if (instance != null) return instance;
		synchronized(Config.class) {
			if (instance == null) {
				instance = load();
				storeAll();
			}
			return instance;
		}
	}

	private static Configuration load() {
		File folder = new File(
				System.getProperty("user.home") +
						File.separator +
						".seo-google-sheets-helper");
		if (!folder.exists()) {
			boolean isMkdired = folder.mkdir();
			if (!isMkdired) {
				String e = "Не удалось создать папку в домашней папке пользователя";
				log.error(e);
				throw new RuntimeException(e);
			}
		} // TODO dialog openapi key
		settingsFile = new File(folder, "settings.properties");
		try {
			if (!settingsFile.exists()) {
				log.info("Файл конфигурации не найден. Создаем файл и записываем данные...");
				settingsFile.createNewFile();
				return preconfigure(Configuration.builder().load(settingsFile).build());
			} else if (settingsFile.length() == 0) {
				log.info("Файл конфигурации пустой. Записываем данные...");
				return preconfigure(Configuration.builder().load(settingsFile).build());
			} else {
				log.info("Файл конфигурации найден");
				return Configuration.builder().load(settingsFile).build();
			}
//			Config.onChange("OPENAI_API_KEY", s -> {}); // TODO hot reload
		} catch (IOException e) {
			log.error("Не удалось создать файл в домашней папке пользователя", e);
			throw new RuntimeException(e);
		}
	}

	public static void storeAll() {
		log.info("Сохранение конфигурации...");
		AtomicReference<String> s = new AtomicReference<>("");
		instance.asProperties().entrySet().forEach(e -> s.set(s.get() + e.toString() + "\n"));
		try {
			Files.writeString(settingsFile.toPath(), s.get(), UTF_8);
			log.info("Конфигурация сохранена!");
		} catch (IOException e) {
			log.error("Не удалось сохранить конфигурацию", e);
			throw new RuntimeException(e);
		}
	}

	public static void storeTo(File file) {
		log.info("Сохранение файла конфигурации...");
		AtomicReference<String> s = new AtomicReference<>("");
		instance.asProperties().entrySet().forEach(e -> s.set(s.get() + e.toString() + "\n"));
		try {
			Files.writeString(file.toPath(), s.get(), UTF_8);
			log.info("Файл конфигурации сохранен!");
		} catch (IOException e) {
			log.error("Не удалось сохранить файл конфигурации", e);
			throw new RuntimeException(e);
		}
	}

	private static Configuration preconfigure(Configuration config) {
		config.eventBuilder("first")
				.put("OPENAI_API_KEY", "")
				.put("SYSTEM_QUERY", "")
				.put("SPREADSHEET_ID", "")
				.put("SHEET_NAME", "")
				.put("RANGE", "")
				.put("IS_TITLE", "true")
				.publish();
		return config;
	}
}
