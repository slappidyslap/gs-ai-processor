package kg.musabaev.seogooglesheetshelper.gui.component;

import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.Border;
import kg.musabaev.seogooglesheetshelper.BrowserUtil;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class SimpleHyperlink extends Hyperlink {

	public SimpleHyperlink(String content, String url) {
		super(((Character) content.charAt(content.length() - 1)).equals(" ") ? content : content + " ");
		this.setOnAction(e -> BrowserUtil.open(url));
		this.setBorder(Border.EMPTY);
		this.setPadding(Insets.EMPTY);
	}
}
