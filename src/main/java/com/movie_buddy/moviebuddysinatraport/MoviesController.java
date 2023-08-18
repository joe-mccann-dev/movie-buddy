package com.movie_buddy.moviebuddysinatraport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class MoviesController {

  @Autowired
  private Environment environment;

  @RequestMapping("/hello")
  public String hello() {
    return "Hello world";
  }

  @GetMapping(value = "/callclienthello")
  private String getHelloClient() {
    String uri = "http://localhost:8080/hello";
    RestTemplate restTemplate = new RestTemplate();
    String result = restTemplate.getForObject(uri, String.class);
    return result;
  }

  @GetMapping("/")
  @ResponseBody
  public String getBaseURI(@RequestParam(required = false) String title, @RequestParam(required = false) String releaseYear) {
    String baseURI = "https://www.omdbapi.com/?apikey=" + environment.getProperty("omdb.api.key");
    String searchParams = "&s=" + title + "&type=movie&y=" + releaseYear;
    String externalRequestURL = baseURI + searchParams;

    RestTemplate restTemplate = new RestTemplate();
    String result = restTemplate.getForObject(externalRequestURL, String.class);
    return result;
  }

}


