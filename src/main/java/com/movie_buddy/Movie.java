package com.movie_buddy;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Movie {

    // properties I care about
    @JsonProperty("imdbID")
    private String imdbID;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Year")
    private String year;

    @JsonProperty("Poster")
    private String poster;

    @JsonProperty("Runtime")
    private String runtime;

    @JsonProperty("Actors")
    private String actors;

    @JsonProperty("imdbRating")
    private String imdbRating;

    @JsonProperty("Plot")
    private String plot;

    // getters
    public String getImdbID() {
        return this.imdbID;
    }

    public String getTitle() {
        return this.title;
    }

    public String getYear() {
        return this.year;
    }

    public String getRuntime() {
        return this.runtime;
    }

    public String getActors() {
        return this.actors;
    }

    public String getImdbRating() {
        return this.imdbRating;
    }

    public String getPlot() {
        return this.plot;
    }

    public String getPoster() {
        return this.poster;
    }

    public String getImdbPage() {
        return "https://www.imdb.com/title/" + getImdbID();
    }

    // properties I don't care about, included for simplicity when working with
    // objectMapper
    @JsonProperty("Rated")
    private String rated;

    @JsonProperty("Released")
    private String released;

    @JsonProperty("Genre")
    private String genre;

    @JsonProperty("Director")
    private String director;

    @JsonProperty("Writer")
    private String writer;

    @JsonProperty("Language")
    private String language;

    @JsonProperty("Country")
    private String country;

    @JsonProperty("Awards")
    private String awards;

    @JsonProperty("Ratings")
    private List<Object> ratings;

    @JsonProperty("Metascore")
    private String metascore;

    @JsonProperty("imdbVotes")
    private String imdbVotes;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("DVD")
    private String dvd;

    @JsonProperty("BoxOffice")
    private String boxOffice;

    @JsonProperty("Production")
    private String production;

    @JsonProperty("Website")
    private String website;

    @JsonProperty("Response")
    private String response;
}
