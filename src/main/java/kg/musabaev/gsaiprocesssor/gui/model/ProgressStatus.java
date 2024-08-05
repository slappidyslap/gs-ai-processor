package kg.musabaev.seogooglesheetshelper.gui.model;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public enum ProgressStatus {

	DEFAULT("Прогресс"), IN_PROGRESS("Обработка..."), ERROR("Ошибка! Проверьте логи");

	private final String status;

	ProgressStatus(String status) {
		this.status = status;
	}
}
