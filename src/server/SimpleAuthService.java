package server;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {
    private class UserData {
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    private List<UserData> users;

    public SimpleAuthService() {
        users = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            users.add(new UserData("l" + i, "p" + i, "nick" + i));
        }
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        for (UserData user : users) {
            if (user.login.equals(login) && user.password.equals(password)) {
                return user.nickname;
            }
        }

        return null;
    }

    @Override
    public Boolean nickIsExist(String nick) {
        for (UserData user : users) {
            if (user.nickname.equals(nick)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean nickIsOnLine(Server server, String nick) {
        for (ClientHandler client : server.getClients()) {
            if (client.getNick().equals(nick))  {
                return true;
            }
        }
        return false;
    }
}
