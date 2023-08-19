package com.movie_buddy.moviebuddysinatraport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MoviesController {

  @Autowired
  private MovieService movieService;

  @GetMapping("/")
  @ResponseBody
  public List<Object> getSearchedForMovies(@RequestParam(required = false) String title,
      @RequestParam(required = false) String releaseYear) {

    List<Movie> moviesWithIds = movieService.getMoviesWithIds(title, releaseYear);
    List<Object> movieDetails = movieService.getMovieDetails(moviesWithIds);

    return movieDetails;
  }

}
