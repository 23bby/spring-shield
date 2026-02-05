package com.cryptoproject.spring_shield;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringShieldApplication extends Application {

	private ConfigurableApplicationContext springContext;

	@Override
	public void init() {
		// Avvia il motore Spring in background
		this.springContext = SpringApplication.run(SpringShieldApplication.class);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// Carica la grafica dal file fxml che hai creato
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));

		// Collega i due mondi: permette a Spring di gestire i futuri bottoni
		loader.setControllerFactory(springContext::getBean);

		Parent root = loader.load();
		primaryStage.setTitle("SpringShield - Crypto Tool");
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}

	@Override
	public void stop() {
		// Chiude Spring quando chiudi la finestra con la X
		springContext.close();
	}

	public static void main(String[] args) {
		// Avvia JavaFX
		launch(args);
	}
}