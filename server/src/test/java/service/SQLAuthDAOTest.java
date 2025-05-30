package service;
import dataaccess.DataAccessException;
import dataaccess.SQLAuthDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class SQLAuthDAOTest {

    private SQLAuthDAO authDAO;
    private UserData testUser;

    @BeforeEach
    public void setUp() throws DataAccessException {
        authDAO = new SQLAuthDAO();
        testUser = new UserData("testuser", "testpass", "test@email.com");
        // Clear any existing data
        authDAO.removeAll();
    }

    @AfterEach
    public void tearDown() throws DataAccessException {
        authDAO.removeAll();
    }

    @Test
    public void testCreateAuthPositive() throws DataAccessException {
        AuthData authData = authDAO.createAuth(testUser);

        assertNotNull(authData);
        assertNotNull(authData.authToken());
        assertEquals(testUser.username(), authData.username());
        assertFalse(authData.authToken().isEmpty());
    }

    @Test
    public void testCreateAuthNegative() {
        UserData nullUser = null;

        assertThrows(DataAccessException.class, () -> {
            authDAO.createAuth(nullUser);
        });
    }

    @Test
    public void testGetAuthPositive() throws DataAccessException {
        AuthData createdAuth = authDAO.createAuth(testUser);
        AuthData retrievedAuth = authDAO.getAuth(createdAuth.authToken());

        assertNotNull(retrievedAuth);
        assertEquals(createdAuth.authToken(), retrievedAuth.authToken());
        assertEquals(createdAuth.username(), retrievedAuth.username());
    }

    @Test
    public void testGetAuthNegative() throws DataAccessException {
        AuthData result = authDAO.getAuth("nonexistent-token");

        assertNull(result);
    }

    @Test
    public void testGetAllPositive() throws DataAccessException {
        UserData user1 = new UserData("user1", "pass1", "user1@email.com");
        UserData user2 = new UserData("user2", "pass2", "user2@email.com");

        authDAO.createAuth(user1);
        authDAO.createAuth(user2);

        Collection<AuthData> allAuth = authDAO.getAll();

        assertNotNull(allAuth);
        assertEquals(2, allAuth.size());
    }

    @Test
    public void testGetAllNegativeEmpty() throws DataAccessException {
        Collection<AuthData> allAuth = authDAO.getAll();

        assertNotNull(allAuth);
        assertTrue(allAuth.isEmpty());
    }

    @Test
    public void testRemoveAuthPositive() throws DataAccessException {
        AuthData createdAuth = authDAO.createAuth(testUser);

        authDAO.removeAuth(createdAuth.authToken());
        AuthData retrievedAuth = authDAO.getAuth(createdAuth.authToken());

        assertNull(retrievedAuth);
    }

    @Test
    public void testRemoveAuthNegative() throws DataAccessException {
        // Should not throw exception when removing non-existent token
        assertDoesNotThrow(() -> {
            authDAO.removeAuth("nonexistent-token");
        });
    }

    @Test
    public void testRemoveAllPositive() throws DataAccessException {
        UserData user1 = new UserData("user1", "pass1", "user1@email.com");
        UserData user2 = new UserData("user2", "pass2", "user2@email.com");

        authDAO.createAuth(user1);
        authDAO.createAuth(user2);

        authDAO.removeAll();
        Collection<AuthData> allAuth = authDAO.getAll();

        assertNotNull(allAuth);
        assertTrue(allAuth.isEmpty());
    }

    @Test
    public void testRemoveAllNegativeEmpty() throws DataAccessException {
        // Should not throw exception when removing from empty collection
        assertDoesNotThrow(() -> {
            authDAO.removeAll();
        });

        Collection<AuthData> allAuth = authDAO.getAll();
        assertTrue(allAuth.isEmpty());
    }
}