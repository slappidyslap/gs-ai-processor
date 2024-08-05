package kg.musabaev.seogooglesheetshelper;

public class Contents {
	public static String ALERT_NOT_SET_OPENAI_API_KEY = "Вы не указали OpenAI API ключ! " +
			"Измените его перейдя в меню \"Настройки\" " +
			"и \"Изменить API ключ у ChatGPT\".";
	public static String ALERT_OPENAI_API_KEY_NOT_VALID = "Текущий OpenAI API ключ не валидный! " +
			"Измените его перейдя в меню \"Настройки\" " +
			"и \"Изменить API ключ у ChatGPT\".";
	public static String OPENAI_API_KEY_REGEX = "^(sk-proj-).{48}$";
}
