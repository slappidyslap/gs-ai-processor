package kg.musabaev.seogooglesheetshelper.gui.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import kg.musabaev.seogooglesheetshelper.Main;
import kg.musabaev.seogooglesheetshelper.gui.model.CellError;
import kg.musabaev.seogooglesheetshelper.gui.model.MainFormModel;

public class CellsWithErrorDialog extends Alert {

	private final MainFormModel model;

	public CellsWithErrorDialog(MainFormModel model) {
		super(AlertType.ERROR);
		this.model = model;

		this.setTitle("Ошибки");
		this.setHeaderText("Ошибки при обработке ячеек");
		this.getDialogPane().setPrefWidth(400);
		((Stage) this.getDialogPane().getScene().getWindow())
				.getIcons()
				.add(new Image(Main.class.getResourceAsStream("icon.png")));

		var table = new TableView<>(model.cellsWithError());
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		table.setPlaceholder(new Label("Ошибок нет, пока..."));

		var rowIdColumn = new TableColumn<CellError, String>("Ячейка");
		rowIdColumn.setCellValueFactory(c -> c.getValue().rowIdProperty());
		table.getColumns().add(rowIdColumn);

		var descColumn = new TableColumn<CellError, String>("Описание");
		descColumn.setCellValueFactory(c -> c.getValue().descProperty());
		descColumn.minWidthProperty().bind(table.widthProperty().multiply(0.5));
		table.getColumns().add(descColumn);

		var timeColumn = new TableColumn<CellError, String>("Время");
		timeColumn.setCellValueFactory(c -> c.getValue().timeProperty());
		table.getColumns().add(timeColumn);

		this.getDialogPane().setContent(table);
	}
}
