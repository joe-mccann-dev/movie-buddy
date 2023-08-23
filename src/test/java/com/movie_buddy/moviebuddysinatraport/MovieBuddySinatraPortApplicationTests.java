package com.movie_buddy.moviebuddysinatraport;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.env.Environment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class MovieBuddySinatraPortApplicationTests {

	@Autowired
	private Environment environment;

	@Autowired
	private MoviesController moviesController;

	@Autowired
	private MovieService movieService;

	@Autowired
	private RequestHandler requestHandler;

	@Value(value = "${local.server.port}")
	private int port;

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
		String invalidURL = "http://localhost:" + port + "/movies";
		String response = requestHandler.getInitialResponse(invalidURL);
		String expectedErrorMessage = "Required request parameter &#39;title&#39; for method parameter type String is not present";
		
		assertThat(response).contains(expectedErrorMessage);
	}

	// Testing RequestHandler
	@Test
	void getInitialResponseShouldReturnSearchKeyAndKnownTitle() throws Exception {
		String sampleTitle = "Casablanca";
		String releaseYear = null;
		String baseURI = "https://www.omdbapi.com/?apikey=" + environment.getProperty("omdb.api.key");
		String searchParams = "&s=" + sampleTitle + "&type=movie&y=" + releaseYear;
		String externalRequestURL = baseURI + searchParams;

		assertThat(this.requestHandler.getInitialResponse(externalRequestURL)).contains("Search");
		assertThat(this.requestHandler.getInitialResponse(externalRequestURL)).contains("Casablanca");
	}

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

	// Testing MovieService
	@Test
	void getMoviesWithIdsReturnsNullWhenCalledWithNonsenseTitle() throws IOException {
		String nonseneseTitle = "ajf239499vjvjzzzzawfdkj";
		List<Movie> results = movieService.getMoviesWithIds(nonseneseTitle, null);
		assertThat(results).isNull();
	}

	@Test
	void getMoviesWithIdsReturnsAListWhenCalledWithAnExistingTitleAndYear() throws IOException {
		String sampleMovieTitle = "casablanca";
		String sampleReleaseYear = "1942";
		List<Movie> results = movieService.getMoviesWithIds(sampleMovieTitle, sampleReleaseYear);

		assertThat(results).isNotEmpty();
	}

	@Test
	void getMoviesWithDetailsReturnsAListOfMovieObjects() throws IOException, InterruptedException, ExecutionException {
		String sampleMovieTitle = "Blue";
		String sampleReleaseYear = null;

		List<Movie> movieListFromIDs = movieService.getMoviesWithIds(sampleMovieTitle, sampleReleaseYear);
		List<Movie> result = movieService.getMoviesWithDetails(movieListFromIDs);

		assertThat(result).isNotEmpty();
	}

}
