package com.movie_buddy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class MovieServiceTests {

    @Autowired
    private Environment environment;

    // use mocking and dependency injection to simulate an api key that has maxed
    // out its request limit
    @Test
    void getMoviesWithIdsShouldThrowExceptionWhenRequestLimitExceeded() throws IOException {
        RequestHandler mockRequestHandler = mock(RequestHandler.class);
        // prevent depending on external request
        when(mockRequestHandler.getInitialResponse(anyString()))
                .thenReturn("{\"Response\":\"False\",\"Error\":\"Request limit reached!\"}");

        MovieService movieService = new MovieService(environment);
        movieService.setRequestHandler(mockRequestHandler);

        // since a mocked RequestHandler has been injected,
        // mocked response contains node of "Error" with value "Request Limit reached!"
        // therefore, RequestLimitExceededException is raised
        assertThrows(RequestLimitExceededException.class, () -> {
            movieService.getMoviesWithIds("any title", null);
        });
    }

    // Testing MovieService using dependency injection.
    // Movie service is injected with the mockRequestHandler
    // MovieService#getMoviesWithIds then calls RequestHandler#getInitialResponse
    // using the injected mockRequestHandler,
    // whose response has been predetermined by the when/thenReturn mocking sequence
    @Test
    void getMoviesWithIdsReturnsNullWhenCalledWithNonsenseTitle() throws IOException {
        MovieService movieService = new MovieService(environment);
        RequestHandler mockRequestHandler = mock(RequestHandler.class);

        movieService.setRequestHandler(mockRequestHandler);

        when(mockRequestHandler.getInitialResponse(anyString()))
                .thenReturn("{\"Response\":\"False\",\"Error\":\"Movie not found!\"}");

        String noResultsTitle = "ajf239499vjvjzzzzawfdkj";
        // testing internal behavior of MovieService#getMoviesWithIds
        List<String> movieIDs = movieService.getMoviesWithIds(noResultsTitle, noResultsTitle);

        assertThat(movieIDs).isNull();
    }

    // ensure a valid request returns a non-empty list of string ids as a response
    @Test
    void getMoviesWithIdsReturnsAListWhenCalledWithAnExistingTitleAndYear() throws IOException {
        MovieService movieService = new MovieService(environment);
        RequestHandler mockRequestHandler = mock(RequestHandler.class);
        movieService.setRequestHandler(mockRequestHandler);

        String expectedResponse = "{\"Search\":[{\"Title\":\"Casablanca\",\"Year\":\"1942\",\"imdbID\":\"tt0034583\",\"Type\":\"movie\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BY2IzZGY2YmEtYzljNS00NTM5LTgwMzUtMzM1NjQ4NGI0OTk0XkEyXkFqcGdeQXVyNDYyMDk5MTU@._V1_SX300.jpg\"}],\"totalResults\":\"1\",\"Response\":\"True\"}";

        when(mockRequestHandler.getInitialResponse(anyString()))
                .thenReturn(expectedResponse);

        String sampleMovieTitle = "casablanca";
        String sampleReleaseYear = "1942";

        List<String> results = movieService.getMoviesWithIds(sampleMovieTitle, sampleReleaseYear);
        assertThat(results).isNotEmpty();
    }

    // ensure MovieService#getMoviesWithDetails returns a list of movie objects
    // whose attributes can be later parsed in movies template
    @Test
    void getMoviesWithDetailsReturnsAListOfMovieObjects() throws IOException, InterruptedException, ExecutionException {
        MovieService movieService = new MovieService(environment);
        RequestHandler mockRequestHandler = mock(RequestHandler.class);

        String expectedInitialResponse = "{\"Search\":[{\"Title\":\"Citizen Kane\",\"Year\":\"1941\",\"imdbID\":\"tt0033467\",\"Type\":\"movie\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BYjBiOTYxZWItMzdiZi00NjlkLWIzZTYtYmFhZjhiMTljOTdkXkEyXkFqcGdeQXVyNzkwMjQ5NzM@._V1_SX300.jpg\"}],\"totalResults\":\"1\",\"Response\":\"True\"}";
        when(mockRequestHandler.getInitialResponse(anyString())).thenReturn(expectedInitialResponse);

        String detailedResponseBody = "{\"Title\":\"Citizen Kane\",\"Year\":\"1941\",\"Rated\":\"PG\",\"Released\":\"05 Sep 1941\",\"Runtime\":\"119 min\",\"Genre\":\"Drama, Mystery\",\"Director\":\"Orson Welles\",\"Writer\":\"Herman J. Mankiewicz, Orson Welles, John Houseman\",\"Actors\":\"Orson Welles, Joseph Cotten, Dorothy Comingore\",\"Plot\":\"Following the death of publishing tycoon Charles Foster Kane, reporters scramble to uncover the meaning of his final utterance: 'Rosebud.'\",\"Language\":\"English, Italian\",\"Country\":\"United States\",\"Awards\":\"Won 1 Oscar. 11 wins & 13 nominations total\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BYjBiOTYxZWItMzdiZi00NjlkLWIzZTYtYmFhZjhiMTljOTdkXkEyXkFqcGdeQXVyNzkwMjQ5NzM@._V1_SX300.jpg\",\"Ratings\":[{\"Source\":\"Internet Movie Database\",\"Value\":\"8.3/10\"},{\"Source\":\"Rotten Tomatoes\",\"Value\":\"99%\"},{\"Source\":\"Metacritic\",\"Value\":\"100/100\"}],\"Metascore\":\"100\",\"imdbRating\":\"8.3\",\"imdbVotes\":\"454,103\",\"imdbID\":\"tt0033467\",\"Type\":\"movie\",\"DVD\":\"28 Jun 2016\",\"BoxOffice\":\"$1,627,530\",\"Production\":\"N/A\",\"Website\":\"N/A\",\"Response\":\"True\"}";
        CompletableFuture<String> detailedResponseFuture = CompletableFuture.completedFuture(detailedResponseBody);
        when(mockRequestHandler.getDetailedResponse(anyString())).thenReturn(detailedResponseFuture);

        movieService.setRequestHandler(mockRequestHandler);

        String sampleTitle = "Citizen Kane";
        String sampleReleaseYear = "1941";
        List<String> movieIDs = movieService.getMoviesWithIds(sampleTitle, sampleReleaseYear);
        List<Movie> result = movieService.getMoviesWithDetails(movieIDs);

        assertThat(result).isNotEmpty();
    }
}
