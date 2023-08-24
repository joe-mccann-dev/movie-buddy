package com.movie_buddy.moviebuddysinatraport;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;

import static org.mockito.Mockito.*;

// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// TODO mock API responses
// TODO add test for raised exceptions.
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

	// @Autowired
	// private MockMvc mockMvc;

	private MockWebServer mockWebServer;

	private MockResponse mockResponse;

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

	// Testing MoviesController
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

	@Test
	void visitingMoviesEndpointWithMissingTitleRendersError() throws Exception {
		String missingTitleParamURL = "http://localhost:" + port + "/movies";
		String response = requestHandler.getInitialResponse(missingTitleParamURL);
		String expectedErrorMessage = "Required request parameter &#39;title&#39; for method parameter type String is not present";

		assertThat(response).contains(expectedErrorMessage);
	}

	// Testing RequestHandler
	// mock
	@Test
	void getInitialResponseShouldReturnSearchKeyAndKnownTitle() throws Exception {

		mockWebServer.enqueue(new MockResponse()
				.setResponseCode(200)
				.setBody(
						"{\"Search\":[{\"Title\":\"Casablanca\",\"Year\":\"1942\",\"imdbID\":\"tt0034583\",\"Type\":\"movie\",\"Poster\":\"https://m.media-amazon.com/images/M/MV5BY2IzZGY2YmEtYzljNS00NTM5LTgwMzUtMzM1NjQ4NGI0OTk0XkEyXkFqcGdeQXVyNDYyMDk5MTU@._V1_SX300.jpg\"}],\"totalResults\":\"1\",\"Response\":\"True\"}"));

		Request request = new Request.Builder()
				.url(mockWebServer.url("/"))
				.header("Accept", "text/plain")
				.build();

		Response response = client.newCall(request).execute();
		String responseBody = response.body().string();

		assertThat(response.code()).isEqualTo(200);
		assertThat(responseBody.contains("Search"));
		assertThat(responseBody.contains("Title"));
	}

	// TODO write this method
	// @Test
	// void getMoviesWithIdsShouldThrowExceptionWhenRequestLimitExceeded() throws IOException {
	// 	mockWebServer.enqueue(new MockResponse()
	// 			.setResponseCode(400)
	// 			.setBody("{\"Response\":\"False\",\"Error\":\"Request limit reached!\"}"));

	// 	RequestHandler mockRequestHandler = mock(RequestHandler.class);
	// 	when(mockRequestHandler.getInitialResponse(anyString()))
	// 			.thenReturn("{\"Response\":\"False\",\"Error\":\"Request limit reached!\"}");

	// 	MovieService movieService = new MovieService(environment, mockRequestHandler);

	// }

	// mock
	@Test
	void getDetailedResponseShouldReturnACompletableFuture() throws Exception {
		String sampleMovieID = "tt0034583";
		String detailsURL = "https://www.omdbapi.com/?apikey=" +
				environment.getProperty("omdb.api.key") +
				"&i=" + sampleMovieID;

		CompletableFuture<String> responseFuture = this.requestHandler.getDetailedResponse(detailsURL);
		assertThat(responseFuture).isInstanceOf(CompletableFuture.class);

		// complete future
		String responseBody = responseFuture.get();
		assertThat(responseBody).isInstanceOf(String.class);
		assertThat(responseBody).contains("Actors");
	}

	@Test
	void getDetailedResponseShouldContainExternalLinkToImdb() throws InterruptedException, ExecutionException {
		String sampleTitle = "Casablanca";
		String releaseYear = null;
		String baseURI = "https://www.omdbapi.com/?apikey=" + environment.getProperty("omdb.api.key");
		String searchParams = "&s=" + sampleTitle + "&type=movie&y=" + releaseYear;
		String externalRequestURL = baseURI + searchParams;

		CompletableFuture<String> futureBody = requestHandler.getDetailedResponse(externalRequestURL);
		String body = futureBody.get();
		String linkText = "Find more results at IMDb.";

		assertThat(body.contains(linkText));

	}

	// Testing MovieService
	// mock
	@Test
	void getMoviesWithIdsReturnsNullWhenCalledWithNonsenseTitle() throws IOException {
		String nonseneseTitle = "ajf239499vjvjzzzzawfdkj";
		List<String> results = movieService.getMoviesWithIds(nonseneseTitle, null);
		assertThat(results).isNull();
	}

	// mock
	@Test
	void getMoviesWithIdsReturnsAListWhenCalledWithAnExistingTitleAndYear() throws IOException {
		String sampleMovieTitle = "casablanca";
		String sampleReleaseYear = "1942";
		List<String> results = movieService.getMoviesWithIds(sampleMovieTitle, sampleReleaseYear);

		assertThat(results).isNotEmpty();
	}

	// mock
	@Test
	void getMoviesWithDetailsReturnsAListOfMovieObjects() throws IOException, InterruptedException, ExecutionException {
		String sampleMovieTitle = "Blue";
		String sampleReleaseYear = null;

		List<String> movieIDs = movieService.getMoviesWithIds(sampleMovieTitle, sampleReleaseYear);
		List<Movie> result = movieService.getMoviesWithDetails(movieIDs);

		assertThat(result).isNotEmpty();
	}

}
