package com.movie_buddy.moviebuddysinatraport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class MovieBuddySinatraPortApplicationTests {

	@Autowired
	private Environment environment;

	@Autowired
	private MoviesController moviesController;

	@Autowired
	private MovieService movieService;

	@Autowired
	private RequestHandler requestHandler;

	@Autowired
	private MockMvc mockMvc;

	private MockWebServer mockWebServer;

	private OkHttpClient client;

	@Value(value = "${local.server.port}")
	private int port;

	@BeforeEach
	void setup() throws IOException {
		this.mockWebServer = new MockWebServer();
		this.mockWebServer.start();
		this.client = new OkHttpClient();
	}

	@Test
	void contextLoads() throws Exception {
		assertThat(movieService).isNotNull();
		assertThat(moviesController).isNotNull();
		assertThat(requestHandler).isNotNull();
	}

	// MoviesController
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

	// RequestHandler

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
