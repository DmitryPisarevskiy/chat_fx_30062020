package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

public class Server {
    private List<ClientHandler> clients;
    private AuthService authService;
    final static String AUTH_OK="/authok ";
    final static String AUTH="/auth ";
    final static String END="/end";
    final static String SEND_EXACT_USERS="/w ";

    public static String getAuthOk() {
        return AUTH_OK;
    }

    public static String getAUTH() {
        return AUTH;
    }

    public static String getEND() {
        return END;
    }

    public static String getSendExactUsers() {
        return SEND_EXACT_USERS;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public Server() {
        clients = new Vector<>();
        authService = new SimpleAuthService();

        ServerSocket server = null;
        Socket socket;

        final int PORT = 8189;

        try {
            server = new ServerSocket(PORT);
            System.out.println("Сервер запущен!");

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился");
                System.out.println("socket.getRemoteSocketAddress(): "+socket.getRemoteSocketAddress());
                System.out.println("socket.getLocalSocketAddress() "+socket.getLocalSocketAddress());
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void broadcastMsg(String sender, ArrayList<String> receivers, String msg){
        for (ClientHandler client : clients) {
            if (receivers.contains(client.getNick()) || sender.equals(client.getNick()))  {
                client.sendMsg(sender + "->" +receivers + ": " +msg);
            }
        }
    }

    void broadcastMsg(String sender, String msg){
        for (ClientHandler client : clients) {
            client.sendMsg(sender + "->everyone: " + msg);
        }
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    public List<ClientHandler> getClients() {
        return clients;
    }
}
