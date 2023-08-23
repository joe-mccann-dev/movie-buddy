package com.movie_buddy.moviebuddysinatraport;

import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class SearchExceptionHandler {

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public String handleMissingParametersException(Exception ex, Model model) {
    model.addAttribute("errorMessage", ex.getMessage());
    return "error";
  }

  @ExceptionHandler(RequestLimitExceededException.class)
  public String handleRequestLimitExceededException(RequestLimitExceededException ex, Model model) {
    model.addAttribute("errorMessage", ex.getMessage());
    return "error";
  }
}
