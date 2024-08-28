package com.movie_buddy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class MoviesControllerTests {

    @Autowired
    private RequestHandler requestHandler;

    @Autowired
    private MockMvc mockMvc;

    @Value(value = "${local.server.port}")
    private int port;

    // testing that visiting root path displays text in layout template
    @Test
    public void rootEndPointShouldDisplayForm() throws Exception {
        String rootURL = "http://localhost:" + port + "/";
        String response = requestHandler.getInitialResponse(rootURL);

        assertThat(response).contains("MovieBuddy");
        assertThat(response).contains("title");
        assertThat(response).contains("release year");
        assertThat(response).contains("Find Movies");
        assertThat(response).contains("Search by title or title contents");
    }

    // testing that visiting movies endpoint without title search param displays an
    // error
    @Test
    void visitingMoviesEndpointWithMissingTitleRendersError() throws Exception {
        String missingTitleParamURL = "http://localhost:" + port + "/movies";
        String response = requestHandler.getInitialResponse(missingTitleParamURL);
        String expectedErrorMessage = "Required request parameter &#39;title&#39; for method parameter type String is not present";
        assertThat(response).contains(expectedErrorMessage);
    }

    // using MockMvc to test that error message attributes are added to model
    @Test
    void moviesEndpointWithNoParamAddsErrorAttributeToModel() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/movies"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attributeExists("errorMessage"))
                .andExpect(MockMvcResultMatchers.view().name("error"));
    }
}