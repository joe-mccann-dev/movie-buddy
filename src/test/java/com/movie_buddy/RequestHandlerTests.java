package com.movie_buddy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;

import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class RequestHandlerTests {

    private MockWebServer mockWebServer;

    private OkHttpClient client;

    @Autowired
    private Environment environment;

    @BeforeEach
    void setup() throws IOException {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();
        this.client = new OkHttpClient();
    }

    // ensure that initial response has a search key for further processing of
    // individual movies
    @Test
    void getInitialResponseShouldReturnSearchKeyAndKnownTitle() throws Exception {

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(
                        "{\"Search\":[{\"Title\":\"Casablanca\",\"Year\":\"1942\",\"imdbID\":\"tt0034583\",\"Type\":\"movie\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BY2IzZGY2YmEtYzljNS00NTM5LTgwMzUtMzM1NjQ4NGI0OTk0XkEyXkFqcGdeQXVyNDYyMDk5MTU@._V1_SX300.jpg\"}],\"totalResults\":\"1\",\"Response\":\"True\"}"));

        Request request = new Request.Builder()
                .url(mockWebServer.url("/movies"))
                .header("Accept", "text/plain")
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();

        assertThat(response.code()).isEqualTo(200);
        assertThat(responseBody.contains("Search"));
        assertThat(responseBody.contains("Title"));
    }

    // ensure RequestHandler#getDetailedResponse returns a completable future object
    // that can be used later by private method MovieService#getMovieWithDetails,
    // called from the public MovieService#getMoviesWithDetails (plural).
    @Test
    void getDetailedResponseShouldReturnACompletableFuture() throws Exception {
        String sampleMovieID = "tt0034583";
        String sampleURL = "https://www.omdbapi.com/?apikey=" +
                environment.getProperty("omdb.api.key") +
                "&i=" + sampleMovieID;

        String expectedBodyResponse = "{\"Title\":\"Casablanca\",\"Year\":\"1942\",\"Rated\":\"PG\",\"Released\":\"23 Jan 1943\",\"Runtime\":\"102 min\",\"Genre\":\"Drama, Romance, War\",\"Director\":\"Michael Curtiz\",\"Writer\":\"Julius J. Epstein, Philip G. Epstein, Howard Koch\",\"Actors\":\"Humphrey Bogart, Ingrid Bergman, Paul Henreid\",\"Plot\":\"A cynical expatriate American cafe owner struggles to decide whether or not to help his former lover and her fugitive husband escape the Nazis in French Morocco.\",\"Language\":\"English, French, German, Italian\",\"Country\":\"United States\",\"Awards\":\"Won 3 Oscars. 13 wins & 9 nominations total\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BY2IzZGY2YmEtYzljNS00NTM5LTgwMzUtMzM1NjQ4NGI0OTk0XkEyXkFqcGdeQXVyNDYyMDk5MTU@._V1_SX300.jpg\",\"Ratings\":[{\"Source\":\"Internet Movie Database\",\"Value\":\"8.5/10\"},{\"Source\":\"Rotten Tomatoes\",\"Value\":\"99%\"},{\"Source\":\"Metacritic\",\"Value\":\"100/100\"}],\"Metascore\":\"100\",\"imdbRating\":\"8.5\",\"imdbVotes\":\"587,983\",\"imdbID\":\"tt0034583\",\"Type\":\"movie\",\"DVD\":\"15 Aug 2008\",\"BoxOffice\":\"$4,219,709\",\"Production\":\"N/A\",\"Website\":\"N/A\",\"Response\":\"True\"}";
        mockWebServer.enqueue(new MockResponse().setBody(expectedBodyResponse));

        RequestHandler mockRequestHandler = mock(RequestHandler.class);
        CompletableFuture<String> expectedResponseFuture = CompletableFuture.completedFuture(expectedBodyResponse);

        when(mockRequestHandler.getDetailedResponse(anyString()))
                .thenReturn(expectedResponseFuture);

        CompletableFuture<String> actualResponseFuture = mockRequestHandler
                .getDetailedResponse(sampleURL);
        // complete future
        String responseBody = actualResponseFuture.get();

        assertThat(actualResponseFuture).isInstanceOf(CompletableFuture.class);
        assertThat(expectedResponseFuture).isEqualTo(actualResponseFuture);
        assertThat(responseBody).contains("Title");
    }
}
