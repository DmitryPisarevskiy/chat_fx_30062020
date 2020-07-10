package server;

public interface AuthService {
    String getNicknameByLoginAndPassword(String login, String password);
    Boolean nickIsExist(String nick);
    Boolean registration(String login, String password, String nickname);
}
