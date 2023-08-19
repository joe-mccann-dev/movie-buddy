package com.movie_buddy.moviebuddysinatraport;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Movie {

  @JsonProperty("Title")
  private String title;

  @JsonProperty("Year")
  private String year;

  @JsonProperty("imdbID")
  private String imdbID;

  @JsonProperty("Type")
  private String type;

  @JsonProperty("Poster")
  private String poster;

  public String getImdbID() {
    return imdbID;
  }
}
