package com.filiaiev.mzkit;

import com.filiaiev.mzkit.image.Detector;
import com.filiaiev.mzkit.image.ImageFilter;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

public class Main extends Application {

    private ImageView imageView;
    private BufferedImage img;

    private BorderPane root;
    private StackPane webCamPane;

    private HBox inputBox;

    private Webcam webCam;

    private ObjectProperty<javafx.scene.image.Image> imageProperty = new SimpleObjectProperty<Image>();

    @Override
    public void init() {
        webCam = Webcam.getDefault();
        imageView = new ImageView();
        initWebCam();

        Button getImageButton = new Button("Capture");
        getImageButton.setOnAction(event -> {
            BufferedImage detecting = SwingFXUtils.fromFXImage(imageView.getImage(), null);
            int[][] binarizedMatrix = ImageFilter.getBinarizedMatrix(detecting);

            webCam.close();
            BufferedImage bufferedImage = Detector.detectObjects(detecting, binarizedMatrix);

            imageView.imageProperty().unbind();
            imageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
        });

        Button startWebCamButton = new Button("Cam");
        startWebCamButton.setOnAction(event -> {
            if(webCam.isOpen())
                webCam.close();
            else {
                webCam.open();
                imageView.imageProperty().bind(imageProperty);
            }
        });

        inputBox = new HBox(getImageButton, startWebCamButton);
        inputBox.setSpacing(25);
        inputBox.setPadding(new Insets(10, 0, 10, 0));
        inputBox.setAlignment(Pos.CENTER);

        root = new BorderPane();
        webCamPane = new StackPane(imageView);
        root.setCenter(webCamPane);
        root.setBottom(inputBox);
    }

    private void initWebCam() {
        Task<Void> webCamTask = new Task<Void>() {

            @Override
            protected Void call() {
                webCam.setViewSize(WebcamResolution.VGA.getSize());
                webCam.open();

                startWebCamStream();
                return null;
            }
        };

        Thread webCamThread = new Thread(webCamTask);
        webCamThread.setDaemon(true);
        webCamThread.start();
    }

    protected void startWebCamStream() {
        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() {
                final AtomicReference<WritableImage> ref = new AtomicReference<>();
                while (true) {
                    try {
                        if ((img = webCam.getImage()) != null) {
                            int[][] binarizedMatrix = ImageFilter.getBinarizedMatrix(img);
                            BufferedImage bufferedImage = Detector.detectObjects(img, binarizedMatrix);
                            ref.set(SwingFXUtils.toFXImage(bufferedImage, ref.get()));
                            img.flush();

                            Platform.runLater(() -> imageProperty.set(ref.get()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();

        imageView.imageProperty().bind(imageProperty);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(root, 700, 520));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
