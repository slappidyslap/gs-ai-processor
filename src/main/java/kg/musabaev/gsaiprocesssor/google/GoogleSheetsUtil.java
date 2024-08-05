package kg.musabaev.seogooglesheetshelper.google;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class GoogleSheetsUtil {

	public static String getStartColumnId(String range) {
		if (range.contains("!")) {
			return range.split("!")[1].split(":")[0].substring(0, 1);
		} else {
			return range.split(":")[0].substring(0, 1);
		}
	}

	public static String getEndColumnId(String range) {
		if (range.contains("!")) {
			return range.split("!")[1].split(":")[1].substring(0, 1);
		} else {
			return range.split(":")[1].substring(0, 1);
		}
	}

	public static int getStartRowId(String range) {
		if (range.contains("!")) {
			return parseInt(range.split("!")[1].split(":")[0].substring(1));
		} else {
			return parseInt(range.split(":")[0].substring(1));
		}
	}

	public static int getEndRowId(String range) {
		if (range.contains("!")) {
			return parseInt(range.split("!")[1].split(":")[1].substring(1));
		} else {
			return parseInt(range.split(":")[1].substring(1));
		}
	}

	public static int getRowCount(String range) {
		String[] matches = Pattern
				.compile("\\d+")
				.matcher(range.contains("!") ? range.split("!")[1] : range)
				.results()
				.map(MatchResult::group)
				.toArray(String[]::new);
		return parseInt(matches[1]) - parseInt(matches[0]) + 1;
	}
}
