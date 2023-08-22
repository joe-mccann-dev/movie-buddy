package com.movie_buddy.moviebuddysinatraport;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RequestHandler {
  final OkHttpClient client = new OkHttpClient();

  String getAPIResponse(String url) throws IOException {
    Request request = new Request.Builder()
      .url(url)
      .build();

    try (Response response = client.newCall(request).execute()) {
      return response.body().string();
    }
  }
  
}
