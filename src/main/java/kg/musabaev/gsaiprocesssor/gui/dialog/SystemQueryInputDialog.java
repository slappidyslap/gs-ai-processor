package kg.musabaev.seogooglesheetshelper.gui.dialog;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import kg.musabaev.seogooglesheetshelper.Config;
import kg.musabaev.seogooglesheetshelper.Main;
import net.synedra.validatorfx.Validator;

public class SystemQueryInputDialog extends Dialog<String> {

    private final Validator validator = new Validator();
    public SystemQueryInputDialog() {
        super();

        ButtonType submitButton = new ButtonType("Принять", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().add(submitButton);
        this.getDialogPane().lookupButton(submitButton).setDisable(true);

        var systemQueryInput = new TextField();
        systemQueryInput.setPromptText("Запрос для ChatGPT");
        validator.createCheck()
                .dependsOn("systemQueryInput", systemQueryInput.textProperty())
                .withMethod(c -> {
                    String key = c.get("systemQueryInput");
                    if (key.isBlank())
                        c.error("Не валидный ключ!");
                })
                .decorates(systemQueryInput);
        systemQueryInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!validator.validate())
                this.getDialogPane().lookupButton(submitButton).setDisable(true);
            else {
                validator.clear();
                this.getDialogPane().lookupButton(submitButton).setDisable(false);
            }
        });

        var header = new Text("Системный запрос для ChatGPT - запрос ");

        var vbox = new VBox(header, systemQueryInput);
        vbox.setSpacing(7);
        this.getDialogPane().setContent(vbox);

        this.getDialogPane().setPrefWidth(400);
        ((Stage) this.getDialogPane().getScene().getWindow())
                .getIcons()
                .add(new Image(Main.class.getResourceAsStream("icon.png")));
        ((javafx.scene.control.Button) this.getDialogPane().lookupButton(submitButton)).setOnAction(e -> {
            Config.getConfig().setProperty("SYSTEM_QUERY", systemQueryInput.getText());
            Config.storeAll();
        });
    }
}
