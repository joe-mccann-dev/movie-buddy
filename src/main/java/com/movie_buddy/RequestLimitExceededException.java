package com.movie_buddy;

public class RequestLimitExceededException extends RuntimeException {
    public RequestLimitExceededException() {
        super("API request limit for this server's API key has been reached");
    }
}
