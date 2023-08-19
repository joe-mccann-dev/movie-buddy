package com.movie_buddy.moviebuddysinatraport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MovieService {

  @Autowired
  private Environment environment;

  public List<Movie> getMoviesWithIds(String title, String releaseYear) {
    String externalRequestURL = getRequestURL(title, releaseYear);

    RestTemplate restTemplate = new RestTemplate();
    String response = restTemplate.getForObject(externalRequestURL, String.class);

    ObjectMapper objectMapper = new ObjectMapper();
    List<Movie> movies = null;

    try {
      JsonNode rootNode = objectMapper.readTree(response);
      JsonNode searchNode = rootNode.get("Search");

      if (searchNode != null && searchNode.isArray())
        movies = objectMapper.convertValue(searchNode, new TypeReference<List<Movie>>() {
        });

    } catch (IOException e) {
      e.printStackTrace();
    }

    return movies;
  }

  public List<Object> getMovieDetails(List<Movie> movies) {
    List<Object> movieDetailsObjects = new ArrayList<>();
    List<String> movieIDs = new ArrayList<>();
    for (Movie movie : movies)
      movieIDs.add(movie.getImdbID());

    for (String movieID : movieIDs) {
      Object movieDetailsObject = getDetailsRequestResponse(movieID);
      movieDetailsObjects.add(movieDetailsObject);
    }

    return movieDetailsObjects;
  }

  private Object getDetailsRequestResponse(String movieID) {
    String detailsURL = "https://www.omdbapi.com/?apikey=" +
        environment.getProperty("omdb.api.key") +
        "&i=" + movieID;

    RestTemplate restTemplate = new RestTemplate();
    Object response = restTemplate.getForObject(detailsURL, Object.class);

    return response;

  }

  private String getRequestURL(String title, String releaseYear) {
    String baseURI = "https://www.omdbapi.com/?apikey=" + environment.getProperty("omdb.api.key");
    String searchParams = "&s=" + title + "&type=movie&y=" + releaseYear;
    String externalRequestURL = baseURI + searchParams;

    return externalRequestURL;
  }
}
