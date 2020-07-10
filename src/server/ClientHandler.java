package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class ClientHandler {
    Server server;
    Socket socket = null;
    DataInputStream in;
    DataOutputStream out;

    private String nick;
    private String login;
    private boolean clientIsAuth;
    private final int SOCKET_TIME_OUT = 120000;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            clientIsAuth = false;

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    try {
                        socket.setSoTimeout(SOCKET_TIME_OUT);
                        while (true) {
                            String str = in.readUTF();

                            if (str.startsWith(Server.REG)) {
                                String[] token = str.split("\\s");
                                Boolean b = server.getAuthService().registration(token[1], token[2], token[3]);
                                if (b) {
                                    System.out.println("Прошла регистрация нового пользователя " + token[3] + "\n");
                                    sendMsg(Server.REG_RESULT + "ok");
                                } else {
                                    System.out.println("Была неудачная попытка регистрации");
                                    sendMsg(Server.REG_RESULT + "failed");
                                }
                            } else if (str.startsWith(Server.AUTH)) {
                                String[] token = str.split("\\s");
                                if (token.length < 3) {
                                    continue;
                                }
                                String newNick = server
                                        .getAuthService()
                                        .getNicknameByLoginAndPassword(token[1], token[2]);
                                Boolean nickIsOnLine = server.nickIsOnLine(newNick);
                                if (newNick == null) {
                                    sendMsg("Неверный логин / пароль");
                                } else if (!nickIsOnLine) {
                                    sendMsg(Server.AUTH_OK + newNick);
                                    nick = newNick;
                                    login = token[1];
                                    server.subscribe(this);
                                    System.out.printf("Клиент %s подключился \n", nick);
                                    server.broadcastMsg(Server.WHO_LOGGED_IN + nick);
                                    clientIsAuth = true;
                                    break;
                                } else {
                                    sendMsg("Пользователь с данным логином уже зашел в чат");
                                }
                            }
                        }
                    } catch (SocketTimeoutException e) {
                        sendMsg(Server.END);
                    } finally {
                        socket.setSoTimeout(0);
                    }

                    //цикл работы
                    while (clientIsAuth) {
                        String str = in.readUTF();

                        if (str.equals(Server.END)) {
                            out.writeUTF(Server.END);
                            break;

                        } else if (str.startsWith(Server.SEND_EXACT_USERS)) {
                            String[] token = str.split("\\s");
                            ArrayList<String> nicknames = new ArrayList<>();
                            int i = 1;
                            boolean stop;
                            while (i < token.length) {
                                stop = true;
                                for (ClientHandler clientHandler : server.getClients()) {
                                    if (clientHandler.getNick().equals(token[i])) {
                                        nicknames.add(token[i]);
                                        stop = false;
                                    }
                                }
                                if (stop) {
                                    break;
                                }
                                i++;
                            }
                            if (nicknames.size() != 0) {
                                server.broadcastMsg(this.nick, nicknames, str.substring(str.indexOf(nicknames.get(nicknames.size() - 1)) + nicknames.get(nicknames.size() - 1).length() + 1));
                            }

                        } else {
                            server.broadcastMsg(this.nick, str);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Клиент отключился");
                    server.unsubscribe(this);
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

    void sendMsg(String str) {
        try {
            out.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }
}
