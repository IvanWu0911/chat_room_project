package chat.client;

import chat.model.MessagePacket;
import chat.sound.SoundManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;

public class ChatClient extends Application {
    private static String HOST = System.getenv().getOrDefault("SERVER_HOST", "acela.proxy.rlwy.net");
    private static int PORT = Integer.parseInt(System.getenv().getOrDefault("SERVER_PORT", "42808"));
    private Socket socket;
    private PrintWriter out;
    private LoginController loginCtrl;
    private ChatController chatCtrl;
    private Stage stage;
    private String user;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        try {
            socket = new Socket(HOST, PORT);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            startReceiver();
        } catch (IOException e) {
            showErr("Connection failed: " + e.getMessage());
            return;
        }
        showLogin();
        stage.setTitle("Chat Room");
        stage.show();
        stage.setOnCloseRequest(e -> System.exit(0));
    }

    public void showLogin() throws Exception {
        var l = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        stage.setScene(new Scene(l.load()));
        loginCtrl = l.getController();
        loginCtrl.setClient(this);
    }

    public void showChat(String u) throws Exception {
        this.user = u;
        var l = new FXMLLoader(getClass().getResource("/fxml/Chat.fxml"));
        stage.setScene(new Scene(l.load(), 500, 700));
        stage.centerOnScreen();
        chatCtrl = l.getController();
        chatCtrl.setClient(this, u);
    }

    private void startReceiver() {
        new Thread(() -> {
            try (var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = in.readLine()) != null) {
                    var p = MessagePacket.fromJson(line);
                    Platform.runLater(() -> dispatch(p));
                }
            } catch (IOException e) {
                Platform.runLater(() -> showErr("Lost connection to server"));
            }
        }).start();
    }

    private void dispatch(MessagePacket p) {
        switch (p.type()) {
            case AUTH_OK -> { try { showChat(loginCtrl.getUsername()); } catch (Exception ignored) {} }
            case AUTH_FAIL -> { loginCtrl.showError(p.content()); SoundManager.playError(); }
            case CHAT -> { 
                chatCtrl.appendChat(p); 
                if (!p.sender().equals(user)) SoundManager.playReceive(); 
            }
            case HISTORY -> chatCtrl.appendHistory(p);
            case SYSTEM -> {
                chatCtrl.appendSystem(p);
                chatCtrl.updateOnlineCount(p.onlineCount());
                if (p.content().contains("joined")) SoundManager.playJoin(); else SoundManager.playLeave();
            }
            case ONLINE -> chatCtrl.updateOnlineCount(p.onlineCount());
        }
    }

    public void send(MessagePacket p) { out.println(p.toJson()); }

    private void showErr(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
        Platform.exit();
    }

    public static void main(String[] args) {
        if (args.length > 0) HOST = args[0];
        if (args.length > 1) PORT = Integer.parseInt(args[1]);
        launch(args);
    }
}
