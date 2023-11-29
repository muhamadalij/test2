package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import defines.Errors;
import exceptions.NotExistentUser;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import service.Baloot;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes={UserController.class})
@AutoConfigureMockMvc
@EnableWebMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserController userController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Baloot baloot;

    public Map<String,String> createAddCreditInput(String credit) {
        return Map.of(
                "credit", credit
        );
    }

    @BeforeEach
    void init() {
        baloot = mock(Baloot.class);
        userController.setBaloot(baloot);
    }

    @Test
    public void When_NoUser_Expect_NotExistentUserThrow() throws Exception {
        when(baloot.getUserById("1")).thenThrow(NotExistentUser.class);
        mockMvc.perform(get("/users/{id}", 1)).andExpect(status().isNotFound());
    }

    @Test
    public void When_UserExist_Expect_UserObj() throws Exception {
        when(baloot.getUserById("1")).thenReturn(new User("1", "1", "1", "1", "1"));
        mockMvc.perform(get("/users/{id}", 1)).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("1"));
    }

    @Test
    public void When_InvalidRangeAddCredit_Expect_BadRequest() throws Exception {
        var creditInput = createAddCreditInput("-1");
        when(baloot.getUserById("1")).thenReturn(new User("1", "1", "1", "1", "1"));
        mockMvc.perform(post("/users/{id}/credit", "1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creditInput))).andExpect(status().isBadRequest()).andExpect(content().string(Errors.INVALID_CREDIT_RANGE));
    }

    @Test
    public void When_BadFormatAddCredit_Expect_BadRequest() throws Exception {
        var creditInput = createAddCreditInput("324dsfgd");
        when(baloot.getUserById("1")).thenReturn(new User("1", "1", "1", "1", "1"));
        mockMvc.perform(post("/users/{id}/credit", "1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creditInput))).andExpect(status().isBadRequest()).andExpect(content().string("Please enter a valid number for the credit amount."));
    }

    @Test
    public void When_AddCreditForNotExistentUser_Expect_NotFound() throws Exception {
        var creditInput = createAddCreditInput("35");
        when(baloot.getUserById("1")).thenThrow(new NotExistentUser());
        mockMvc.perform(post("/users/{id}/credit", "1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creditInput))).andExpect(status().isNotFound()).andExpect(content().string(Errors.NOT_EXISTENT_USER));
    }

    @Test
    public void When_ValidAddCredit_Expect_SuccessfulMessage() throws Exception {
        var creditInput = createAddCreditInput("32");
        when(baloot.getUserById("1")).thenReturn(new User("1", "1", "1", "1", "1"));
        mockMvc.perform(post("/users/{id}/credit", "1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creditInput))).andExpect(status().isOk()).andExpect(content().string("credit added successfully!"));
    }
}
