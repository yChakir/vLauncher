package ma.ychakir.rz.vlauncher;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ma.ychakir.rz.vlauncher.Controllers.LauncherBrowser;
import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * @author Yassine
 */
public class Launcher extends Application {

    private static String[] arguments;
    private static final Logger logger = Logger.getLogger(Launcher.class);

    public static Logger getLogger() {
        return logger;
    }

    public static void main(String[] args) {
        logger.debug("Launcher started with arguments: " + Arrays.toString(args));
        arguments = args;
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(new LauncherBrowser(primaryStage, arguments[0]));
        primaryStage.setTitle("vLauncher");
        scene.setFill(Color.TRANSPARENT);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
