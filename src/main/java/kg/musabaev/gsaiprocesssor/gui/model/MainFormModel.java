package kg.musabaev.seogooglesheetshelper.gui.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Data
@Accessors(fluent = true)
public class MainFormModel {
	private final StringProperty systemQuery = new SimpleStringProperty();
	private final StringProperty spreadsheetId = new SimpleStringProperty();
	private final StringProperty sheetName = new SimpleStringProperty();
	private final StringProperty range = new SimpleStringProperty();
	private final BooleanProperty isTitle = new SimpleBooleanProperty();
	private final ObjectProperty<ProgressStatus> progressStatus = new SimpleObjectProperty<>();
	private final StringProperty progressInfo = new SimpleStringProperty();
	private final DoubleProperty progressIndicator = new SimpleDoubleProperty();
	private final ObservableList<CellError> cellsWithError = FXCollections.observableArrayList();

	public void addCellErrorAsEmptyCell(String curCell) {
		this.cellsWithError().add(new CellError(curCell, "Пустая ячейка", getCurrentTime()));
	}

	public void addCellError(String curCell, String desc) {
		this.cellsWithError().add(new CellError(curCell, desc, getCurrentTime()));
	}

	private String getCurrentTime() {
		return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
	}
}
