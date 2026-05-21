// register, login, logout
package service;

import dataAccess.DataAccess;
import dataAccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(String username, String password, String email) throws ServiceException {
        if (username == null || username.isBlank() ||
                password == null || password.isBlank() ||
                email == null || email.isBlank()) {
            throw new ServiceException(400, "bad request");
        }
        try {
            if (dataAccess.getUser(username) != null) {
                throw new ServiceException(403, "already taken");
            }
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            dataAccess.createUser(new UserData(username, hashedPassword, email));
            return dataAccess.createAuth(username);
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }

    public AuthData login(String username, String password) throws ServiceException {
        if (username == null || password == null) {
            throw new ServiceException(400, "bad request");
        }
        try {
            UserData user = dataAccess.getUser(username);
            if (user == null || !BCrypt.checkpw(password, user.password())) {
                throw new ServiceException(401, "unauthorized");
            }
            return dataAccess.createAuth(username);
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }

    public void logout(String authToken) throws ServiceException {
        try {
            AuthData auth = dataAccess.getAuth(authToken);
            if (auth == null) {
                throw new ServiceException(401, "unauthorized");
            }
            dataAccess.deleteAuth(authToken);
        } catch (DataAccessException e) {
            throw new ServiceException(500, e.getMessage());
        }
    }
}