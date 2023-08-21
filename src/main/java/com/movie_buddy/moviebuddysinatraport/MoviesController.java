package com.movie_buddy.moviebuddysinatraport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class MoviesController {

  @Autowired
  private MovieService movieService;

  @GetMapping("/")
  public String home() {
    return "layout";
  }

  @GetMapping("/movies")
  public String getSearchedForMovies(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String releaseYear,
      Model model) {

    List<Movie> moviesWithIds = movieService.getMoviesWithIds(title, releaseYear);
    List<Movie> movieDetails;

    if (moviesWithIds != null) {
      movieDetails = movieService.getMoviesWithDetails(moviesWithIds);
      model.addAttribute("moviesFound", !movieDetails.isEmpty());
      model.addAttribute("movieDetails", movieDetails);
    }

    return "layout";
  }
}
