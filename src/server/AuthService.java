package server;

public interface AuthService {
    String getNicknameByLoginAndPassword(String login, String password);
    Boolean nickIsExist(String nick);
    Boolean nickIsOnLine(Server server, String nick);
}
