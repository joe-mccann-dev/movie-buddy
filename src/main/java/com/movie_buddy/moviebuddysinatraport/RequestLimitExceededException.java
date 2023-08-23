package com.movie_buddy.moviebuddysinatraport;

public class RequestLimitExceededException extends RuntimeException {
  public RequestLimitExceededException() {
    super("API request limit for this server's API key has been reached");
  }
}
