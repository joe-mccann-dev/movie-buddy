package com.movie_buddy.moviebuddysinatraport;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

// enables an initial synchronous response to get movie ids,
// enables asynchronous responses via futures
public class RequestHandler {
  final OkHttpClient client = new OkHttpClient();

  public String getInitialResponse(String url) throws IOException {
    Request request = new Request.Builder()
      .url(url)
      .build();

    try (Response response = client.newCall(request).execute()) {
      return response.body().string();
    }
  }

  public CompletableFuture<String> getDetailedResponse(String url) {
    CompletableFuture<String> future = new CompletableFuture<>();

    Request request = new Request.Builder()
      .url(url)
      .build();

    client.newCall(request).enqueue(new Callback() {
      @Override public void onFailure(Call call, IOException e) {
        future.completeExceptionally(e);
      }
      
      @Override public void onResponse(Call call, Response response) throws IOException {
        try (ResponseBody responseBody = response.body()) {
          if (!response.isSuccessful())
            future.completeExceptionally(new IOException("Unexpected code " + response));
          else
            future.complete(responseBody.string());
        }
      }
    });

    return future;
  }
}
