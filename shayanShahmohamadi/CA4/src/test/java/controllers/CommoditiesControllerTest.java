package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import defines.Errors;
import exceptions.NotExistentCommodity;
import exceptions.NotExistentUser;
import model.Comment;
import model.Commodity;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import service.Baloot;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes={CommoditiesController.class})
@AutoConfigureMockMvc
@EnableWebMvc
public class CommoditiesControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CommoditiesController commoditiesController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    Baloot baloot;

    @BeforeEach
    public void init() {
        baloot = mock(Baloot.class);
        commoditiesController.setBaloot(baloot);
    }

    public Map<String,String> createRateCommodityInput(String rate) {
        return Map.of(
                "username", "user1",
                "rate", rate
        );
    }

    public ArrayList<Commodity> createCommoditiesList() {
        Commodity commodity1 = new Commodity();
        Commodity commodity2 = new Commodity();
        Commodity commodity3 = new Commodity();

        return new ArrayList<Commodity>() {
            {
                add(commodity1);
                add(commodity2);
                add(commodity3);
            }
        };
    }

    @Test
    public void When_GetCommoditiesSuccessful_Expect_ListOfCommodities() throws Exception {
        when(baloot.getCommodities()).thenReturn(createCommoditiesList());
        mockMvc.perform(get("/commodities")).andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(3));
    }

    @Test
    public void When_DontHaveCommodity_Expect_NotFoundCommodity() throws Exception {
        when(baloot.getCommodityById(anyString())).thenThrow(NotExistentCommodity.class);
        mockMvc.perform(get("/commodities/{id}", 1)).andExpect(status().isNotFound()).andExpect(content().string(""));
    }

    @Test
    public void When_GetExistCommodity_Expect_TheCommodity() throws Exception {
        var commodity = new Commodity();
        commodity.setId("1");
        when(baloot.getCommodityById("1")).thenReturn(commodity);
        mockMvc.perform(get("/commodities/{id}", 1)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    public void When_RateCommodityWithNotExistentCommodity_Expect_CommodityNotFound() throws Exception {
        var rateInput = createRateCommodityInput("2");
        when(baloot.getCommodityById(anyString())).thenThrow(new NotExistentCommodity());
        mockMvc.perform(post("/commodities/{id}/rate", 1).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rateInput))).andExpect(status().isNotFound()).andExpect(content().string(Errors.NOT_EXISTENT_COMMODITY));
    }

    @Test
    public void When_RateWithBadNumberFormat_Expect_() throws Exception {
        var rateInput = createRateCommodityInput("2asdf");
        when(baloot.getCommodityById("1")).thenReturn(new Commodity());
        mockMvc.perform(post("/commodities/{id}/rate", 1).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rateInput))).andExpect(status().isBadRequest());
    }

    @Test
    public void When_SuccessfullyRate_Expect_SuccessMessage() throws Exception {
        var rateInput = createRateCommodityInput("2");
        when(baloot.getCommodityById("1")).thenReturn(new Commodity());
        mockMvc.perform(post("/commodities/{id}/rate", 1).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rateInput))).andExpect(status().isOk()).andExpect(content().string("rate added successfully!"));
    }

    @Test
    public void When_NotExistentUserAddComment_Expect_NotFoundUser() throws Exception {
        var commentInput = Map.of(
                "username", "user1",
                "comment", "comment1"
        );
        when(baloot.getUserById("user1")).thenThrow(new NotExistentUser());
        mockMvc.perform(post("/commodities/{id}/comment", 1).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentInput))).andExpect(status().isNotFound()).andExpect(content().string(Errors.NOT_EXISTENT_USER));
    }

    @Test
    public void When_SuccessfullyAddComment_Expect_SuccessMessage() throws Exception {
        var commentInput = Map.of(
                "username", "user1",
                "comment", "comment1"
        );
        when(baloot.getUserById("user1")).thenReturn(new User());

        mockMvc.perform(post("/commodities/{id}/comment", 1).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentInput))).andExpect(status().isOk()).andExpect(content().string("comment added successfully!"));
    }

    @Test
    public void getCommodityComment_success() throws Exception {
        Comment comment1 = new Comment();
        Comment comment2 = new Comment();
        Comment comment3 = new Comment();

        when(baloot.getCommentsForCommodity(1)).thenReturn(new ArrayList<Comment>() {
            {
                add(comment1);
                add(comment2);
                add(comment3);
            }
        });

        mockMvc.perform(get("/commodities/{id}/comment", 1)).andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(3));
    }

    @Test
    public void When_SearchCommoditiesByName_Expect_TheCommoditiesList() throws Exception {
        var searchInput = Map.of(
                "searchOption", "name",
                "searchValue", "value"
        );
        when(baloot.filterCommoditiesByName(anyString())).thenReturn(createCommoditiesList());

        mockMvc.perform(post("/commodities/search").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchInput))).andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(3));
    }

    @Test
    public void When_SearchCommoditiesByCategory_Expect_TheCommoditiesList() throws Exception{
        var searchInput = Map.of(
                "searchOption", "category",
                "searchValue", "value"
        );
        when(baloot.filterCommoditiesByCategory(anyString())).thenReturn(createCommoditiesList());

        mockMvc.perform(post("/commodities/search").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchInput))).andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(3));
    }

    @Test
    public void When_SearchCommoditiesByProvider_Expect_TheCommoditiesList() throws Exception{
        var searchInput = Map.of(
                "searchOption", "provider",
                "searchValue", "value"
        );
        when(baloot.filterCommoditiesByProviderName(anyString())).thenReturn(createCommoditiesList());

        mockMvc.perform(post("/commodities/search").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchInput))).andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(3));
    }

    @Test
    public void When_GetSuggestedCommoditiesNotExistentCommodity_Expect_CommodityNotFound() throws Exception {
        when(baloot.getCommodityById(anyString())).thenThrow(new NotExistentCommodity());
        mockMvc.perform(get("/commodities/{id}/suggested", 1)).andExpect(status().isNotFound()).andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    public void When_GetSuggestedCommodities_Expect_CommoditiesList() throws Exception {
        Commodity commodity = new Commodity();
        when(baloot.getCommodityById("1")).thenReturn(commodity);
        when(baloot.suggestSimilarCommodities(commodity)).thenReturn(createCommoditiesList());
        mockMvc.perform(get("/commodities/{id}/suggested", 1)).andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(3));
    }
}
