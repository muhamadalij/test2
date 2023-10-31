package controllers;

import exceptions.NotExistentCommodity;
import exceptions.NotExistentUser;
import model.Comment;
import model.Commodity;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import service.Baloot;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommoditiesControllerTest {
    CommoditiesController commoditiesController;

    @Mock
    Baloot baloot;

    @BeforeEach
    public void init() {
        baloot = mock(Baloot.class);
        commoditiesController = new CommoditiesController();
        commoditiesController.setBaloot(baloot);
    }

    public Map<String,String> createRateCommodityInput(String rate) {
        return Map.of(
                "username", "user1",
                "rate", rate
        );
    }

    public ArrayList<Commodity> createCommoditiesList() {
        Commodity commodity1 = mock(Commodity.class);
        Commodity commodity2 = mock(Commodity.class);
        Commodity commodity3 = mock(Commodity.class);

        return new ArrayList<Commodity>() {
            {
                add(commodity1);
                add(commodity2);
                add(commodity3);
            }
        };
    }

    @Test
    public void getCommodities_success() {
        when(baloot.getCommodities()).thenReturn(createCommoditiesList());

        var res = commoditiesController.getCommodities();
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(3, res.getBody().size());
    }

    @Test
    public void getCommodity_notFound() throws NotExistentCommodity {
        when(baloot.getCommodityById(anyString())).thenThrow(NotExistentCommodity.class);
        var statusCode = commoditiesController.getCommodity("1").getStatusCode();
        assertEquals(HttpStatus.NOT_FOUND, statusCode);
    }

    @Test
    public void getCommodity_success() throws NotExistentCommodity {
        when(baloot.getCommodityById("1")).thenReturn(new Commodity());
        var statusCode = commoditiesController.getCommodity("1").getStatusCode();
        assertEquals(HttpStatus.OK, statusCode);
    }

    @Test
    public void rateCommodity_notFound() throws NotExistentCommodity {
        var rateInput = createRateCommodityInput("2");
        when(baloot.getCommodityById(anyString())).thenThrow(NotExistentCommodity.class);
        var statusCode = commoditiesController.rateCommodity("1", rateInput).getStatusCode();
        assertEquals(HttpStatus.NOT_FOUND, statusCode);
    }

    @Test
    public void rateCommodity_badNumberFormat() throws NotExistentCommodity {
        var rateInput = createRateCommodityInput("2asdf");
        when(baloot.getCommodityById("1")).thenReturn(new Commodity());
        var res = commoditiesController.rateCommodity("1", rateInput);
        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    }

    @Test
    public void rateCommodity_success() throws NotExistentCommodity {
        var rateInput = createRateCommodityInput("2");
        when(baloot.getCommodityById("1")).thenReturn(new Commodity());
        var res = commoditiesController.rateCommodity("1", rateInput);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("rate added successfully!", res.getBody());
    }

    @Test
    public void addCommodityComment_notFound() throws NotExistentUser {
        var commentInput = Map.of(
                "username", "user1",
                "comment", "comment1"
        );
        when(baloot.getUserById("user1")).thenThrow(NotExistentUser.class);
        var statusCode = commoditiesController.addCommodityComment("1", commentInput).getStatusCode();
        assertEquals(HttpStatus.NOT_FOUND, statusCode);
    }

    @Test
    public void addCommodityComment_success() throws NotExistentUser {
        var commentInput = Map.of(
                "username", "user1",
                "comment", "comment1"
        );
        when(baloot.getUserById("user1")).thenReturn(new User());
        var statusCode = commoditiesController.addCommodityComment("1", commentInput).getStatusCode();
        assertEquals(HttpStatus.OK, statusCode);
    }

    @Test
    public void getCommodityComment_success() {
        Comment comment1 = mock(Comment.class);
        Comment comment2 = mock(Comment.class);
        Comment comment3 = mock(Comment.class);

        when(baloot.getCommentsForCommodity(1)).thenReturn(new ArrayList<Comment>() {
            {
                add(comment1);
                add(comment2);
                add(comment3);
            }
        });

        var res = commoditiesController.getCommodityComment("1");
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(3, res.getBody().size());
    }

    @Test
    public void searchCommodities_byName_success() {
        var searchInput = Map.of(
                "searchOption", "name",
                "searchValue", "value"
        );
        when(baloot.filterCommoditiesByName(anyString())).thenReturn(createCommoditiesList());

        var res = commoditiesController.searchCommodities(searchInput);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(3, res.getBody().size());
    }

    @Test
    public void searchCommodities_byCategory_success() {
        var searchInput = Map.of(
                "searchOption", "category",
                "searchValue", "value"
        );
        when(baloot.filterCommoditiesByCategory(anyString())).thenReturn(createCommoditiesList());

        var res = commoditiesController.searchCommodities(searchInput);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(3, res.getBody().size());
    }

    @Test
    public void searchCommodities_byProvider_success() {
        var searchInput = Map.of(
                "searchOption", "provider",
                "searchValue", "value"
        );
        when(baloot.filterCommoditiesByProviderName(anyString())).thenReturn(createCommoditiesList());

        var res = commoditiesController.searchCommodities(searchInput);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(3, res.getBody().size());
    }

    @Test
    public void getSuggestedCommodities_notFound() throws NotExistentCommodity {
        when(baloot.getCommodityById(anyString())).thenThrow(NotExistentCommodity.class);
        var statusCode = commoditiesController.getSuggestedCommodities("1").getStatusCode();
        assertEquals(HttpStatus.NOT_FOUND, statusCode);
    }

    @Test
    public void getSuggestedCommodities_success() throws NotExistentCommodity {
        Commodity commodity = new Commodity();
        when(baloot.getCommodityById("1")).thenReturn(commodity);
        when(baloot.suggestSimilarCommodities(commodity)).thenReturn(createCommoditiesList());
        var res = commoditiesController.getSuggestedCommodities("1");
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(3, res.getBody().size());
    }
}
