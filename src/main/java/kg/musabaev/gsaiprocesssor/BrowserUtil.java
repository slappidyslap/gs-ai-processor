package kg.musabaev.seogooglesheetshelper;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

@Slf4j
public class BrowserUtil {

	public static boolean open(String url) {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop desktop = Desktop.getDesktop();
				if (desktop.isSupported(Desktop.Action.BROWSE)) {
					desktop.browse(URI.create(url));
					return true;
				} else {
					return openByTerminal(url);
				}
			} else {
				return openByTerminal(url);
			}
		} catch (IOException e) {
			log.info("Не удалось открыть в браузере", e);
			return false;
		}
	}

	private static boolean openByTerminal(String url) {
		try {
			Runtime rt = Runtime.getRuntime();
			String os = System.getProperty("os.name").toLowerCase();
			if (os.contains("win")) {
				rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
				return true;
			} else if (os.contains("mac")) {
				rt.exec("open " + url);
				return true;
			} else if (os.contains("nix") || os.contains("nux")) {
				rt.exec("xdg-open " + url);
				return true;
			} else {
				log.info("Не удалось найти операционную систему: " + os);
				return false;
			}
		} catch (IOException e) {
			log.info("Не удалось открыть в браузере", e);
			return false;
		}
	}
}
