package com.onevn.client;

import com.onevn.client.ui.ChatView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        ChatView chatView = new ChatView();

        Scene scene = new Scene(
                chatView.createView(),
                1100,
                700
        );

        scene.getStylesheets().add(
                getClass()
                        .getResource("/styles/chat.css")
                        .toExternalForm()
        );

        stage.setTitle("1VN Chat");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}