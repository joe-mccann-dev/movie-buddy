package com.movie_buddy.moviebuddysinatraport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MovieService {

  @Autowired
  private Environment environment;

  @Autowired
  private RequestHandler requestHandler;

  // for 
  public MovieService(Environment environment) {
    this.environment = environment;
  }

  // for injecting a mocked request handler
  public void setRequestHandler(RequestHandler requestHandler) {
    this.requestHandler = requestHandler;
  }

  // initial method to gather list of imdbIDs returned from initial get request to
  // omdb
  public List<String> getMoviesWithIds(String title, String releaseYear) throws IOException {
    String externalRequestURL = getRequestURL(title, releaseYear);
    String response = requestHandler.getInitialResponse(externalRequestURL);

    ObjectMapper objectMapper = new ObjectMapper();
    List<Movie> movies = null;
    List<String> movieIDs = null;

    try {
      JsonNode rootNode = objectMapper.readTree(response);
      JsonNode searchNode = rootNode.get("Search");

      if (rootNode.has("Error") && rootNode.get("Error").asText().contains("Request limit reached!"))
        throw new RequestLimitExceededException();

      movies = objectMapper.convertValue(searchNode, new TypeReference<List<Movie>>() {
      });

      if (movies != null)
        movieIDs = movies.stream().map(movie -> movie.getImdbID()).collect(Collectors.toList());

    } catch (IOException e) {
      e.printStackTrace();
    }

    return movieIDs;
  }

  // calls getMovieWithDetails to assemble a collection of movies
  public List<Movie> getMoviesWithDetails(List<String> movieIDs)
      throws IOException, InterruptedException, ExecutionException {

    List<CompletableFuture<String>> responseFutures = new ArrayList<>();
    RequestHandler requestHandler = new RequestHandler();

    responseFutures = movieIDs.stream()
        .map(movieID -> {
          String detailsURL = getDetailsURL(movieID);
          return requestHandler.getDetailedResponse(detailsURL);
        })
        .collect(Collectors.toList());

    List<Movie> moviesWithDetails = responseFutures.stream()
        .map(responseFuture -> {
          try {
            String responseBody = responseFuture.get();
            return getMovieWithDetails(responseBody);
          } catch (InterruptedException | ExecutionException | IOException e) {
            return null;
          }

        }).filter(Objects::nonNull)
        .collect(Collectors.toList());

    return moviesWithDetails;
  }

  // map response body to a movie object
  private Movie getMovieWithDetails(String responseBody) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    Movie movie = null;

    try {
      JsonNode rootNode = objectMapper.readTree(responseBody);
      movie = objectMapper.convertValue(rootNode, Movie.class);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return movie;
  }

  private String getRequestURL(String title, String releaseYear) {
    String baseURI = "https://www.omdbapi.com/?apikey=" + environment.getProperty("omdb.api.key");
    String searchParams = "&s=" + title + "&type=movie&y=" + releaseYear;
    String externalRequestURL = baseURI + searchParams;

    return externalRequestURL;
  }

  private String getDetailsURL(String movieID) {
    return "https://www.omdbapi.com/?apikey=" +
        environment.getProperty("omdb.api.key") +
        "&i=" + movieID;
  }
}
