package com.movie_buddy.moviebuddy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MoviesController {

  @Autowired
  private MovieService movieService;

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("initialSearch", true);
    return "layout";
  }

  @GetMapping("/movies")
  public String getSearchedForMovies(
      @RequestParam(required = true) String title,
      @RequestParam(required = false) String releaseYear,
      Model model) throws IOException, InterruptedException, ExecutionException {

    List<String> movieIDs = movieService.getMoviesWithIds(title, releaseYear);
    List<Movie> movieDetails = new ArrayList<>();

    if (movieIDs != null) 
      movieDetails = movieService.getMoviesWithDetails(movieIDs);
    
    model.addAttribute("moviesFound", !movieDetails.isEmpty());
    model.addAttribute("movieDetails", movieDetails);
    return "layout";
  }
}
