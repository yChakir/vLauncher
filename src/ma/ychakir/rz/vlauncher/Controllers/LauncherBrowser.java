package ma.ychakir.rz.vlauncher.Controllers;

import com.sun.javafx.webkit.Accessor;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import ma.ychakir.rz.vlauncher.Utils.GameStarter;
import netscape.javascript.JSObject;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

/**
 * @author Yassine
 */
public class LauncherBrowser extends Region {
    private static final Logger logger = Logger.getLogger(LauncherBrowser.class);
    final WebView browser = new WebView();
    final WebEngine engine = browser.getEngine();
    private double xOffset;
    private double yOffset;

    public LauncherBrowser(Stage stage, String[] args) {
        setBackground(Background.EMPTY);
        //mouse drag to move window
        browser.setOnMousePressed(event -> {
            xOffset = stage.getX() - event.getScreenX();
            yOffset = stage.getY() - event.getScreenY();
        });

        browser.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() + xOffset);
            stage.setY(event.getScreenY() + yOffset);
        });

        //disable right clicks
        browser.setContextMenuEnabled(false);

        //enable javascript
        engine.setJavaScriptEnabled(true);

        final LauncherController js = new LauncherController(stage, engine);
        engine.getLoadWorker().stateProperty().addListener((e, o, n) -> {
            if (n == Worker.State.SUCCEEDED) {
                logger.debug("Loading succeeded");
                ((JSObject) engine.executeScript("window")).setMember("launcher", js);
                getChildren().add(browser);
                Accessor.getPageFor(browser.getEngine()).setBackgroundColor(0);
                engine.executeScript("js.init();");
            } else if (n == Worker.State.FAILED) {
                logger.fatal("Loading failed: " + engine.getLoadWorker().getException().getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR);

                alert.getButtonTypes().clear();
                alert.getButtonTypes().add(ButtonType.YES);
                alert.getButtonTypes().add(ButtonType.NO);
                alert.setTitle("ERROR");
                alert.setHeaderText("حدث خطأ أثناء الإتصال بموقع اللانشر.");
                alert.setContentText(engine.getLoadWorker().getException().getMessage() + "\nهل تريد تشغيل اللعبة ؟");

                Optional<ButtonType> choice = alert.showAndWait();
                if (choice.isPresent() && choice.get() == ButtonType.YES) {
                    String sframe = args[1];
                    String arguments = String.format("/auth_ip:%s /auth_port:%s /use_nprotect:0 /allow_double_exec:1 /locale:Windows-1256 /country:ME", args[2], args[3]);
                    startGame(sframe, arguments);
                }
                Platform.exit();
                System.exit(0);
            }
        });

        engine.load(args[0]);
        logger.debug("Loading: " + args[0]);
    }

    private void startGame(String sframe, String args) {
        try {
            new GameStarter(sframe, args).start();
        } catch (IOException ex) {
            logger.error("Failed to start the game, args: " + sframe + " " + args);
            logger.error("Failed to start the game " + ex.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("حدث خطأ أثناء تشغيل اللعبة.");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }
}
