package org.verwaltung.verwaltung_kunden_ma;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Entry point of the JavaFX management application.
 * <p>
 * This class is responsible only for bootstrapping the JavaFX runtime.
 * It loads the main FXML file ({@code MainView.fxml}) and shows the primary
 * stage (window). All application logic is delegated to the controllers and DAOs.
 */
public class ManagementApplication extends Application
{

    /**
     * Initializes and shows the primary stage of the application.
     * <p>
     * Loads the main FXML file, creates a {@link Scene} with the default size
     * of 1100x500, sets the window title to {@code "Verwaltung"}, and displays
     * the stage.
     *
     * @param stage the primary stage provided by the JavaFX runtime
     * @throws IOException if the {@code MainView.fxml} file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(ManagementApplication.class.getResource("MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 500);
        stage.setTitle("Verwaltung");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args optional command-line arguments
     */
    public static void main(String[] args)
    {
        launch();
    }
}