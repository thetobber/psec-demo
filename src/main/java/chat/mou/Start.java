package chat.mou;

import chat.mou.events.ConnectEvent;
import chat.mou.events.ViewEvent;
import chat.mou.network.SocketConnectionManager;
import chat.mou.views.ConnectView;
import chat.mou.views.HostView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.CompletableFuture;

public class Start extends Application
{
    private ConfigurableApplicationContext applicationContext;
    private Scene rootScene;

    @Override
    public void init()
    {
        applicationContext = new SpringApplicationBuilder(Main.class).run();
    }

    @Override
    public void start(Stage stage)
    {
        stage.setTitle("Mou");
        rootScene = new Scene(applicationContext.getBean(ConnectView.class), 840, 680);

        applicationContext.addApplicationListener(event -> {
            if (event instanceof ViewEvent) {
                onViewEvent((ViewEvent) event);
            }
        });

        rootScene.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ALT)) {
                applicationContext.publishEvent(new ViewEvent(this, HostView.class));
            }
            else if (event.getCode().equals(KeyCode.CONTROL)) {
                applicationContext.publishEvent(new ViewEvent(this, ConnectView.class));
            }
        });

        stage.setScene(rootScene);
        stage.show();
    }

    @Override
    public void stop()
    {
        CompletableFuture.runAsync(() -> {
            final var connectionManager = applicationContext.getBean(SocketConnectionManager.class);
            connectionManager.closeSocketConnection();
        }).thenRun(() -> {
            applicationContext.close();
            Platform.exit();
        });
    }

    private void onViewEvent(ViewEvent event)
    {
        rootScene.setRoot(applicationContext.getBean(event.getView()));
    }
}
