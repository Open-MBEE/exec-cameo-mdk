package gov.nasa.jpl.mbee.mdk;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;

public class MDKApplication extends Application {
    private static final CountDownLatch latch = new CountDownLatch(1);
    private static MDKApplication INSTANCE;

    public MDKApplication() {
        INSTANCE = this;
        latch.countDown();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Platform.setImplicitExit(false);
    }

    protected static MDKApplication getInstance() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return INSTANCE;
    }

    public static void main(String... args) {
        javafx.application.Application.launch(MDKApplication.class);
    }
}