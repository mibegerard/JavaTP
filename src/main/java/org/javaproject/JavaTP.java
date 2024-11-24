package org.javaproject;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class JavaTP extends Application {

    private TargetDataLine line;
    private AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    private ByteArrayOutputStream byteArrayOutputStream;
    private byte[] audioData;

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Prêt à enregistrer !");
        Button btnStartRecording = new Button("Démarrer l'enregistrement");
        btnStartRecording.setOnAction(e -> startRecording(label));
        Button btnStopRecording = new Button("Arrêter l'enregistrement");
        btnStopRecording.setOnAction(e -> stopRecording(label));
        Button btnListen = new Button("Écouter");
        btnListen.setOnAction(e -> listenToRecording(label));
        Button btnShowPosition = new Button("Afficher la position");
        btnShowPosition.setOnAction(e -> showPosition(label));

        VBox vbox = new VBox(10, btnStartRecording, btnStopRecording, btnListen, btnShowPosition);
        vbox.setAlignment(javafx.geometry.Pos.CENTER);
        vbox.setStyle("-fx-padding: 20px;");

        StackPane root = new StackPane();
        root.getChildren().addAll(vbox, label);
        StackPane.setAlignment(label, javafx.geometry.Pos.TOP_CENTER);
        StackPane.setAlignment(vbox, javafx.geometry.Pos.CENTER);

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("Enregistrement audio et position");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startRecording(Label label) {
        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                label.setText("Ligne non supportée !");
                return;
            }

            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            byteArrayOutputStream = new ByteArrayOutputStream();

            Thread captureThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while (line.isOpen()) {
                    bytesRead = line.read(buffer, 0, buffer.length);
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
                audioData = byteArrayOutputStream.toByteArray();
            });

            captureThread.start();
            label.setText("Enregistrement démarré...");
        } catch (LineUnavailableException ex) {
            label.setText("Erreur lors du démarrage de l'enregistrement.");
            ex.printStackTrace();
        }
    }

    private void stopRecording(Label label) {
        if (line != null && line.isOpen()) {
            line.stop();
            line.close();
            label.setText("Enregistrement arrêté.");
        }
    }

    private void listenToRecording(Label label) {
        if (audioData != null && audioData.length > 0) {
            try {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioData);
                AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
                AudioInputStream audioStream = new AudioInputStream(byteArrayInputStream, format, audioData.length);

                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();

                label.setText("Écoute de l'enregistrement...");
            } catch (LineUnavailableException | IOException ex) {
                label.setText("Erreur lors de la lecture de l'enregistrement.");
                ex.printStackTrace();
            }
        } else {
            label.setText("Aucun enregistrement à écouter.");
        }
    }

    private void showPosition(Label label) {
        try {
            URL url = new URL("https://ipinfo.io/json?token=425360a4a00515");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();
            if (status != 200) {
                label.setText("Erreur : impossible de récupérer la position.");
                return;
            }

            Scanner scanner = new Scanner(connection.getInputStream());
            String response = scanner.useDelimiter("\\A").next();
            scanner.close();

            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            String city = jsonResponse.get("city").getAsString();
            String region = jsonResponse.get("region").getAsString();
            String country = jsonResponse.get("country").getAsString();
            String loc = jsonResponse.get("loc").getAsString();

            label.setText(String.format("Ville: %s\nRégion: %s\nPays: %s\nPosition: %s", city, region, country, loc));
        } catch (Exception e) {
            e.printStackTrace();
            label.setText("Erreur lors de la récupération de la position.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
