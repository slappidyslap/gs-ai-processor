package kg.musabaev.seogooglesheetshelper.gui.dialog;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import kg.musabaev.seogooglesheetshelper.Main;

public class WarningAlert extends Alert {
	public WarningAlert(Node... nodes) {
		super(AlertType.WARNING);
		this.setTitle("Внимание!");
		this.getDialogPane().setPrefWidth(400);
		((Stage) this.getDialogPane().getScene().getWindow())
				.getIcons()
				.add(new Image(Main.class.getResourceAsStream("icon.png")));
		var textFlow = new TextFlow(nodes);
		textFlow.setPadding(new Insets(20, 10, 0, 10));
		this.getDialogPane().setHeader(textFlow);
	}

	public WarningAlert(String content) {
		this(new Text(content));
	}
}
