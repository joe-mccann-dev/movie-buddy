package com.movie_buddy.moviebuddysinatraport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class MoviesController {

  @Autowired
  private MovieService movieService;

  @GetMapping("/")
  @ResponseBody
  public ResponseEntity<List<Movie>> getSearchedForMovies(@RequestParam(required = false) String title,
      @RequestParam(required = false) String releaseYear) {


    List<Movie> moviesWithIds = movieService.getMoviesWithIds(title, releaseYear);
    List<Movie> movieDetails = movieService.getMoviesWithDetails(moviesWithIds);
    
    
    return ResponseEntity.ok(movieDetails);
  }

}
