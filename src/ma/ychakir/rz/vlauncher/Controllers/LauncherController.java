/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose vLauncher.Tools | Templates
 * and open the template in the editor.
 */
package ma.ychakir.rz.vlauncher.Controllers;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import ma.ychakir.rz.vlauncher.Launcher;
import ma.ychakir.rz.vlauncher.Runnables.UpdateRunnable;
import ma.ychakir.rz.vlauncher.Utils.GameStarter;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Yassine
 */
public class LauncherController {
    private static final Logger logger = Launcher.getLogger();
    private final Stage stage;
    private final WebEngine engine;

    public LauncherController(Stage stage, WebEngine engine) {
        this.stage = stage;
        this.engine = engine;
    }

    /**
     * close the launcher
     */
    public void close() {
        logger.debug("Launcher closed by user.");
        Platform.exit();
        System.exit(0);
    }

    /**
     * Minimize the launcher window
     */
    public void minimize() {
        this.stage.setIconified(true);
    }

    /**
     * Open the url in new tab of the default browser
     */
    public void open(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException ex) {
            logger.error("Failed to open: " + url + " " + ex.getMessage());
        }
    }

    /**
     * Start the game
     */
    public void start(String sframe, String args) {
        try {
            logger.debug("Statting the game with arguments: " + sframe + " " + args);
            if (new GameStarter(sframe, args).start())
                engine.executeScript("js.gameStarted();");
        } catch (Exception ex) {
            logger.error("Failed to start the game " + ex.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("حدث خطأ أثناء تشغيل اللعبة.");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Set window width
     */
    public void setWidth(int width) {
        stage.setWidth(width);
    }

    /**
     * Set window Height
     */
    public void setHeight(int height) {
        stage.setHeight(height);
    }

    /**
     * Starting update
     */
    public void startUpdate(String version) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        final UpdateRunnable updateRunnable = new UpdateRunnable(version);
        logger.debug("Starting update worker.");
        executor.execute(updateRunnable);
        logger.debug("Starting progress worker.");
        executor.execute(() -> {
            logger.debug("Progress worker started.");
            UpdateRunnable.Status status;
            do {
                status = updateRunnable.getStatus();
                switch (status) {
                    case SEARSHING:
                        Platform.runLater(() -> engine.executeScript("js.searching();"));
                        break;
                    case CLEANING:
                        Platform.runLater(() -> engine.executeScript("js.cleaning();"));
                        break;
                    case ALREADY_UPDATED:
                        Platform.runLater(() -> engine.executeScript("js.alreadyUpdated();"));
                        break;
                    case FINISHED:
                        Platform.runLater(() -> engine.executeScript("js.finished();"));
                        break;
                    case UPDATING:
                        Platform.runLater(() -> engine.executeScript("js.totalCount = " + updateRunnable.getTotalCount() + ";" +
                                "js.currentCount = " + updateRunnable.getCurrentCount() + ";" +
                                "js.totalProgress(" + updateRunnable.getTotalProgress() + ");" +
                                "js.currentName(\"" + updateRunnable.getCurrentName() + "\");" +
                                "js.currentProgress(" + updateRunnable.getCurrentProgress() + ");"));
                        break;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }
            } while (status != UpdateRunnable.Status.FINISHED &&
                    status != UpdateRunnable.Status.ALREADY_UPDATED);
        });

        executor.shutdown();
    }
}
