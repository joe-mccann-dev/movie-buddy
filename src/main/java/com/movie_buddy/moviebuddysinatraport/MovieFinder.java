package com.movie_buddy.moviebuddysinatraport;


import java.util.List;

public class MovieFinder {
  private String title;
  private int releaseYear;
  private List<String> imdbIDs;
  private List<String> movies;
  private boolean apiLimitReached;

  public MovieFinder(String title, int releaseYear) {
    this.title = title.toLowerCase();
    this.releaseYear = releaseYear;
  }



}
