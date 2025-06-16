package com.hortifacil.controller;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.animation.PauseTransition;
import javafx.util.Duration;


public class SplashController extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Carrega a view do splash (pode ser só uma imagem)
        Parent root = FXMLLoader.load(getClass().getResource("/view/Splash.fxml"));
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        // Espera 3 segundos e abre login
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> {
            try {
                openLogin(stage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        delay.play();
    }

    private void openLogin(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle("HortiFácil - Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
