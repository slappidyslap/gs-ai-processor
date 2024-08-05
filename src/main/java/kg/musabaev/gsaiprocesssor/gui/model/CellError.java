package kg.musabaev.seogooglesheetshelper.gui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CellError {
	private final StringProperty rowId = new SimpleStringProperty();
	private final StringProperty desc = new SimpleStringProperty();
	private final StringProperty time = new SimpleStringProperty();

	public CellError(String rowId, String desc, String time) {
		this.rowId.set(rowId);
		this.desc.set(desc);
		this.time.set(time);
	}

	public String getRowId() {
		return rowId.get();
	}

	public StringProperty rowIdProperty() {
		return rowId;
	}

	public void setRowId(String rowId) {
		this.rowId.set(rowId);
	}

	public String getDesc() {
		return desc.get();
	}

	public StringProperty descProperty() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc.set(desc);
	}

	public String getTime() {
		return time.get();
	}

	public StringProperty timeProperty() {
		return time;
	}

	public void setTime(String time) {
		this.time.set(time);
	}
}
