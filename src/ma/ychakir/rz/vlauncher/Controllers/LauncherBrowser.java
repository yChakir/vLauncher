package ma.ychakir.rz.vlauncher.Controllers;

import com.sun.javafx.webkit.Accessor;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.control.Alert;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.apache.log4j.Logger;

/**
 * @author Yassine
 */
public class LauncherBrowser extends Region {
    private static final Logger logger = Logger.getLogger(LauncherBrowser.class);
    final WebView browser = new WebView();
    final WebEngine engine = browser.getEngine();
    private double xOffset;
    private double yOffset;

    public LauncherBrowser(Stage stage, String url) {
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

        final LauncherController ja = new LauncherController(stage, engine);
        engine.getLoadWorker().stateProperty().addListener((e, o, n) -> {
            if (n == Worker.State.SUCCEEDED) {
                logger.debug("Loading succeeded");
                ((JSObject) engine.executeScript("window")).setMember("launcher", ja);
                getChildren().add(browser);
                Accessor.getPageFor(browser.getEngine()).setBackgroundColor(0);
                engine.executeScript("js.init();");
            } else if (n == Worker.State.FAILED) {
                logger.fatal("Loading failed: " + engine.getLoadWorker().getException().getMessage());
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("ERROR");
                alert.setHeaderText("حدث خطأ أثناء الإتصال بموقع اللانشر.");
                alert.setContentText(engine.getLoadWorker().getException().getMessage());
                alert.showAndWait();
                Platform.exit();
                System.exit(0);
            }
        });

        engine.load(url);
        logger.debug("Loading: " + url);
    }
}
