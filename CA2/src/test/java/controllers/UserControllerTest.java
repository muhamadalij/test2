package controllers;

import exceptions.NotExistentUser;
import model.Comment;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import service.Baloot;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserControllerTest {
    UserController userController;

    @Mock
    Baloot baloot;

    @BeforeEach
    public void initialize() {
        baloot = mock(Baloot.class);
        userController = new UserController();
        userController.setBaloot(baloot);
    }

    public Map<String,String> createAddCreditInput(String credit) {
        return Map.of(
                "credit", credit
        );
    }

    @Test
    public void getUser_notFound() throws NotExistentUser {
        when(baloot.getUserById(anyString())).thenThrow(NotExistentUser.class);
        var statusCode = userController.getUser("1").getStatusCode();
        assertEquals(HttpStatus.NOT_FOUND, statusCode);
    }

    @Test
    public void getUser_found() throws NotExistentUser {
        when(baloot.getUserById("1")).thenReturn(new User());
        var statusCode = userController.getUser("1").getStatusCode();
        assertEquals(HttpStatus.OK, statusCode);
    }

    @Test
    public void addCredit_invalidRange() throws NotExistentUser {
        var creditInput = createAddCreditInput("-1");
        when(baloot.getUserById("1")).thenReturn(new User());
        var statusCode = userController.addCredit("1", creditInput).getStatusCode();
        assertEquals(HttpStatus.BAD_REQUEST, statusCode);
    }

    @Test
    public void addCredit_badNumberFormat() throws NotExistentUser {
        var creditInput = createAddCreditInput("324dsfgd");
        when(baloot.getUserById("1")).thenReturn(new User());
        var res = userController.addCredit("1", creditInput);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals("Please enter a valid number for the credit amount.", res.getBody());
    }

    @Test
    public void addCredit_userNotFound() throws NotExistentUser {
        var creditInput = createAddCreditInput("35");
        when(baloot.getUserById(anyString())).thenThrow(NotExistentUser.class);
        var statusCode = userController.addCredit("1", creditInput).getStatusCode();
        assertEquals(HttpStatus.NOT_FOUND, statusCode);
    }

    @Test
    public void addCredit_success() throws NotExistentUser {
        var creditInput = createAddCreditInput("32");
        when(baloot.getUserById("1")).thenReturn(new User());
        var res = userController.addCredit("1", creditInput);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("credit added successfully!", res.getBody());
    }
}
