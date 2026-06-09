package chat.client;

import chat.model.MessagePacket.Type;
import chat.model.MessagePacket;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel, errorLabel;
    @FXML private Button loginBtn, registerBtn;
    private ChatClient client;

    public void setClient(ChatClient client) { this.client = client; }

    @FXML private void onLogin() { send(Type.LOGIN); }
    @FXML private void onRegister() { send(Type.REGISTER); }

    private void send(Type t) {
        var u = usernameField.getText().trim();
        var p = passwordField.getText();
        if (u.isEmpty() || p.isEmpty()) { showError("Empty field"); return; }
        client.send(new MessagePacket(t, u, p));
        setDisabled(true); statusLabel.setText("Processing...");
    }

    public void showError(String m) {
        errorLabel.setText(m); errorLabel.setVisible(true);
        setDisabled(false); statusLabel.setText("Connected");
    }

    private void setDisabled(boolean d) {
        usernameField.setDisable(d); passwordField.setDisable(d);
        loginBtn.setDisable(d); registerBtn.setDisable(d);
    }

    public String getUsername() { return usernameField.getText().trim(); }
}
