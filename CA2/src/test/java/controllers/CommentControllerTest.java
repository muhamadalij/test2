package controllers;

import exceptions.NotExistentComment;
import model.Comment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import service.Baloot;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommentControllerTest {
    CommentController commentController;
    @Mock
    Baloot baloot;

    @BeforeEach
    public void initialize() {
        baloot = mock(Baloot.class);
        commentController = new CommentController();
        commentController.setBaloot(baloot);
    }

    public Map<String,String> createCommentInput() {
        return Map.of(
                "username", "user1"
        );
    }

    @Test
    public void likeComment_notFound() throws NotExistentComment {
        var input = createCommentInput();
        when(baloot.getCommentById(anyInt())).thenThrow(NotExistentComment.class);
        var statusCode = commentController.likeComment("1", input).getStatusCode();
        assertEquals(HttpStatus.NOT_FOUND, statusCode);
    }

    @Test
    public void likeComment_found() throws NotExistentComment {
        var input = createCommentInput();
        when(baloot.getCommentById(1)).thenReturn(new Comment());
        var res = commentController.likeComment("1", input);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("The comment was successfully liked!", res.getBody());
    }

    @Test
    public void dislikeComment_notFound() throws NotExistentComment {
        var input = createCommentInput();
        when(baloot.getCommentById(anyInt())).thenThrow(NotExistentComment.class);
        var statusCode = commentController.dislikeComment("1", input).getStatusCode();
        assertEquals(HttpStatus.NOT_FOUND, statusCode);
    }

    @Test
    public void dislikeComment_found() throws NotExistentComment {
        var input = createCommentInput();
        when(baloot.getCommentById(1)).thenReturn(new Comment());
        var res = commentController.dislikeComment("1", input);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("The comment was successfully disliked!", res.getBody());
    }
}
