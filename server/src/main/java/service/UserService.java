// register, login, logout
package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

/** handles logic for user registration, login, and logout. */

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    /** registers a new user to chess game; returns auth token.
     * service exception 400 if any field is left blank; 403 if username is taken. */

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

    /** logs in an existing user; returns a new auth token.
     * 401 if credentials are invalid. */

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

    /** logs out an existing user by invalidating their token.
     * 401 if the token is invalid. */

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