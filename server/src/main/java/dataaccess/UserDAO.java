// declares what methods exist
package dataaccess;
import model.userData;

publid interface UserDAO {
    void createUser(UserData userData) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void clear() throws DataAccessException;
}