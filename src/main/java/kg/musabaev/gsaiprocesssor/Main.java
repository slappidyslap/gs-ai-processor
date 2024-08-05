package kg.musabaev.seogooglesheetshelper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Main extends Application {

	@Getter
	@Accessors(fluent = true)
	private static Stage primaryStage;

	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("main-view.fxml"));
		Scene scene = new Scene(fxmlLoader.load());
		stage.setTitle("SEO Google Sheet Helper");
		stage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
		stage.setScene(scene);
		stage.setOnCloseRequest($ -> Config.storeAll());
		this.primaryStage = stage;
		stage.show();
		log.info("Программа запущена");
	}

	public static void main(String[] args) {
		launch();
	}
}
