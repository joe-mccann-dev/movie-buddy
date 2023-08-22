package com.movie_buddy.moviebuddysinatraport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

  // initial method to gather list of imdbIDs returned from initial get request to
  // omdb
  public List<Movie> getMoviesWithIds(String title, String releaseYear) throws IOException {
    String externalRequestURL = getRequestURL(title, releaseYear);

    RequestHandler requestHandler = new RequestHandler();
    String response = requestHandler.getInitialResponse(externalRequestURL);

    ObjectMapper objectMapper = new ObjectMapper();
    List<Movie> movies = null;

    try {
      JsonNode rootNode = objectMapper.readTree(response);
      JsonNode searchNode = rootNode.get("Search");

      movies = objectMapper.convertValue(searchNode, new TypeReference<List<Movie>>() {
      });

    } catch (IOException e) {
      e.printStackTrace();
    }

    return movies;
  }

  // calls getMovieWithDetails to assemble a collection of movies
  public List<Movie> getMoviesWithDetails(List<Movie> movies)
      throws IOException, InterruptedException, ExecutionException {
    
    List<String> movieIDs = new ArrayList<>();
    List<CompletableFuture<String>> responseFutures = new ArrayList<>();
    RequestHandler requestHandler = new RequestHandler();
    Movie movieWithDetails = null;
    List<Movie> moviesWithDetails = new ArrayList<>();
    
    // accumulate ids for processing futures
    for (Movie movie : movies)
      movieIDs.add(movie.getImdbID());

    for (String movieID : movieIDs) {
      String detailsURL = getDetailsURL(movieID);
      responseFutures.add(requestHandler.getDetailedResponse(detailsURL));
    }

    // use futures to build movie objects
    for (CompletableFuture<String> responseFuture : responseFutures) {
      // waits for response to be available
      String responseBody = responseFuture.get();
      movieWithDetails = getMovieWithDetails(responseBody);
      moviesWithDetails.add(movieWithDetails);
    }

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
