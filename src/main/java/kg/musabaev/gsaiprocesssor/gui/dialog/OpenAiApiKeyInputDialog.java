package kg.musabaev.seogooglesheetshelper.gui.dialog;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import kg.musabaev.seogooglesheetshelper.Config;
import kg.musabaev.seogooglesheetshelper.Contents;
import kg.musabaev.seogooglesheetshelper.Main;
import kg.musabaev.seogooglesheetshelper.gui.component.SimpleHyperlink;
import net.synedra.validatorfx.Validator;

public class OpenAiApiKeyInputDialog extends Dialog<String> {

	private final Validator validator = new Validator();

	private static final String titleTextTemplate = "свой OpenAI API ключ.";

	public OpenAiApiKeyInputDialog(Action action) {
		super();
		this.setTitle(action.title);

		ButtonType submitButton = new ButtonType("Принять", ButtonBar.ButtonData.OK_DONE);
		this.getDialogPane().getButtonTypes().add(submitButton);
		this.getDialogPane().lookupButton(submitButton).setDisable(true);

		var openAiApiKeyInput = new TextField();
		openAiApiKeyInput.setPromptText("OpenAI API ключ");
		validator.createCheck()
				.dependsOn("openAiApiKeyInput", openAiApiKeyInput.textProperty())
				.withMethod(c -> {
					String key = c.get("openAiApiKeyInput");
					if (!key.matches(Contents.OPENAI_API_KEY_REGEX))
						c.error("Не валидный ключ!");
				})
				.decorates(openAiApiKeyInput);
		openAiApiKeyInput.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!validator.validate())
				this.getDialogPane().lookupButton(submitButton).setDisable(true);
			else {
				validator.clear();
				this.getDialogPane().lookupButton(submitButton).setDisable(false);
			}
		});

		TextFlow header = new TextFlow(
				new Text("Перейдите на "),
				new SimpleHyperlink("страницу API ключей", "https://platform.openai.com/api-keys"),
				new Text("и нажмите \"Create new secret key\". Укажите имя ключа. " +
						"Затем выберите ограниченные (Restricted) права " +
						"и отметьте пункт \"Model capabilities\" как \"Write\". " +
						"Выданный ключ вставьте в поле ниже."));

		var vbox = new VBox(header, openAiApiKeyInput);
		vbox.setSpacing(7);
		this.getDialogPane().setContent(vbox);

		this.getDialogPane().setPrefWidth(400);
		((Stage) this.getDialogPane().getScene().getWindow())
				.getIcons()
				.add(new Image(Main.class.getResourceAsStream("icon.png")));
		((Button) this.getDialogPane().lookupButton(submitButton)).setOnAction(e -> {
			Config.getConfig().setProperty("OPENAI_API_KEY", openAiApiKeyInput.getText());
			Config.storeAll();
		});
	}

	public enum Action {
		CREATE("Ввести " + titleTextTemplate),
		CHANGE("Изменить " + titleTextTemplate);


		private final String title;

		Action(String title) {
			this.title = title;
		}
	}
}
