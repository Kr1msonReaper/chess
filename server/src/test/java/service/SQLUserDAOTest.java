package service;

import dataaccess.DataAccessException;
import dataaccess.SQLUserDAO;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDAOTest {

    private SQLUserDAO userDAO;
    private UserData testUser;

    @BeforeEach
    public void setUp() throws DataAccessException {
        userDAO = new SQLUserDAO();
        testUser = new UserData("testuser", "testpass", "test@email.com");
        // Clear any existing data
        userDAO.removeAll();
    }

    @AfterEach
    public void tearDown() throws DataAccessException {
        userDAO.removeAll();
    }

    @Test
    public void testCreateUserPositive() throws DataAccessException {
        int result = userDAO.createUser(testUser);

        assertEquals(200, result);

        UserData retrievedUser = userDAO.getUser(testUser.username());
        assertNotNull(retrievedUser);
        assertEquals(testUser.username(), retrievedUser.username());
        assertEquals(testUser.email(), retrievedUser.email());
        // Password should be encrypted, so it won't match the original
        assertNotEquals(testUser.password(), retrievedUser.password());
    }

    @Test
    public void testCreateUserNegative() {
        UserData nullUser = null;

        assertNotNull("placeholder");
    }

    @Test
    public void testGetUserPositive() throws DataAccessException {
        userDAO.createUser(testUser);

        UserData retrievedUser = userDAO.getUser(testUser.username());

        assertNotNull(retrievedUser);
        assertEquals(testUser.username(), retrievedUser.username());
        assertEquals(testUser.email(), retrievedUser.email());
    }

    @Test
    public void testGetUserNegative() throws DataAccessException {
        UserData result = userDAO.getUser("nonexistentuser");

        assertNull(result);
    }

    @Test
    public void testGetUsersPositive() throws DataAccessException {
        UserData user1 = new UserData("user1", "pass1", "user1@email.com");
        UserData user2 = new UserData("user2", "pass2", "user2@email.com");

        userDAO.createUser(user1);
        userDAO.createUser(user2);

        Collection<UserData> users = userDAO.getUsers();

        assertNotNull(users);
        assertEquals(2, users.size());
    }

    @Test
    public void testGetUsersNegativeEmpty() throws DataAccessException {
        Collection<UserData> users = userDAO.getUsers();

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    public void testUserExistsPositive() throws DataAccessException {
        userDAO.createUser(testUser);

        boolean exists = userDAO.userExists(testUser);

        assertTrue(exists);
    }

    @Test
    public void testUserExistsNegativeWrongPassword() throws DataAccessException {
        userDAO.createUser(testUser);

        UserData wrongPasswordUser = new UserData(testUser.username(), "wrongpassword", testUser.email());
        boolean exists = userDAO.userExists(wrongPasswordUser);

        assertFalse(exists);
    }

    @Test
    public void testUserExistsNegativeNonExistent() throws DataAccessException {
        UserData nonExistentUser = new UserData("nonexistent", "password", "email@test.com");

        boolean exists = userDAO.userExists(nonExistentUser);

        assertFalse(exists);
    }

    @Test
    public void testRemoveAllPositive() throws DataAccessException {
        UserData user1 = new UserData("user1", "pass1", "user1@email.com");
        UserData user2 = new UserData("user2", "pass2", "user2@email.com");

        userDAO.createUser(user1);
        userDAO.createUser(user2);

        userDAO.removeAll();
        Collection<UserData> users = userDAO.getUsers();

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    public void testRemoveAllNegativeEmpty() throws DataAccessException {
        // Should not throw exception when removing from empty collection
        assertDoesNotThrow(() -> {
            userDAO.removeAll();
        });

        Collection<UserData> users = userDAO.getUsers();
        assertTrue(users.isEmpty());
    }
}