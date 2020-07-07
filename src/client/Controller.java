package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextArea textArea;
    @FXML
    public TextField textField;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox authPanel;
    @FXML
    public HBox msgPanel;


    private final int PORT = 8189;
    private final String IP_ADDRESS = "localhost";
    private final String CHAT_TITLE_EMPTY = "Chat july 2020";

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private Stage stageAbout;

    private boolean authenticated;
    private String nick;

    private Stage stage;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        if (!authenticated) {
            nick = "";
        }
        setTitle(nick);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            stage = (Stage) textField.getScene().getWindow();
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    System.out.println("bye");
                    if (socket != null && !socket.isClosed()) {
                        try {
                            out.writeUTF(Server.getEND());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        });

        setAuthenticated(false);

    }

    private void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith(Server.getAuthOk())) {
                            nick = str.split("\\s")[1];
                            setAuthenticated(true);
                            break;
                        }
                        textArea.appendText(str + "\n");
                    }


                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.equals(Server.getEND())) {
                            setAuthenticated(false);
                            break;
                        }
                        textArea.appendText(str + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendMsg(ActionEvent actionEvent) {
        try {
            out.writeUTF(textField.getText());
            textField.requestFocus();
            textField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            out.writeUTF(Server.getAUTH() + loginField.getText().trim() + " " + passwordField.getText().trim());
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String nick) {
        Platform.runLater(() -> {
            stage.setTitle(CHAT_TITLE_EMPTY + " : " + nick);
        });
    }

    public void close(ActionEvent actionEvent) {
        Stage stage = (Stage) textField.getScene().getWindow();
        stage.close();
    }

    public void openAbout(ActionEvent actionEvent) {
        if (stageAbout==null) {
            stageAbout=newAbout();
        }
        stageAbout.show();
    }

    private Stage newAbout() {
        Stage stage=null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("About.fxml"));
            Parent root =fxmlLoader.load();
            stage=new Stage();
            stage.setTitle("About");
            stage.setScene(new Scene(root, 300, 43));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stage;
    }
}
