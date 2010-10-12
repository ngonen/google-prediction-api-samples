/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.predict.movierecommenderdemo;

import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.googleapis.auth.clientlogin.ClientLogin;
import com.google.api.client.googleapis.json.JsonCContent;
import com.google.api.client.googleapis.json.JsonCParser;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.predict.movierecommenderdemo.model.InputData;
import com.google.api.predict.movierecommenderdemo.model.OutputData;
import com.google.api.predict.movierecommenderdemo.model.PredictionUrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Yaniv Inbar
 * @author Max Lin
 */
@SuppressWarnings("serial")
public class MovieRecommenderDemoServlet extends HttpServlet {

  private static List<String> movieNames = new ArrayList<String>();
  private static List<List<Integer>> movieSignals = new ArrayList<List<Integer>>();

  // There are three movies we want to predict.
  private static final int numMovies = 3;
  // The number of signals we use for making prediction. There are 19 genres.
  private static final int numSignals = 19;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    loadMovieData();

    // Set up Json parser and authentication before making Prediction API calls
    HttpTransport transport = GoogleTransport.create();
    transport.addParser(new JsonCParser());
    authenticateWithClientLogin(transport);

    final Float[] predictedRatings = new Float[numMovies];
    for (int i = 0; i < numMovies; i++) {
      /*
       * For each movie, load its signals, that is, genre information, to an
       * array of 19 elements. An element is 1.0 if a movie belongs to the
       * genre, and 0.0 otherwise.
       */
      float[] signals = new float[numSignals];
      Arrays.fill(signals, 0.0f);
      for (int j = 0; j < movieSignals.get(i).size(); j++) {
        signals[movieSignals.get(i).get(j)] = 1.0f;
      }

      // Send a movie's signals (genre) to the Prediction API and obtain a
      // predicted rating
      predictedRatings[i] = predict(transport, signals);
    }

    // Sort movies by predicted ratings. We want to show preferred movies first.
    Integer[] sortedIndexes = {0, 1, 2};
    sortByPredictedRatings(sortedIndexes, predictedRatings);

    showRecommendedMovies(resp, sortedIndexes);
  }

  private void sortByPredictedRatings(Integer[] sortedIndexes, final Float[] predictedRatings) {
    Arrays.sort(sortedIndexes, new Comparator<Integer>() {
      @Override
      public int compare(final Integer a, final Integer b) {
        return Float.compare(predictedRatings[b], predictedRatings[a]);
      }
    });
  }

  private static void authenticateWithClientLogin(HttpTransport transport) throws IOException {
    ClientLogin authenticator = new ClientLogin();
    authenticator.authTokenType = "xapi";
    authenticator.username = ClientLoginCredentials.ENTER_USERNAME;
    authenticator.password = ClientLoginCredentials.ENTER_PASSWORD;
    authenticator.authenticate().setAuthorizationHeader(transport);
  }

  /* Makes a prediction call to the Google Prediction API */
  private static float predict(HttpTransport transport, float[] signals) throws IOException {
    HttpRequest request = transport.buildPostRequest();
    request.url = PredictionUrl.forPrediction(ClientLoginCredentials.OBJECT_PATH);
    JsonCContent content = new JsonCContent();
    InputData inputData = new InputData();

    // inputData holds the signals to be sent to the Prediction API.
    for (int i = 0; i < signals.length; i++) {
      inputData.input.numeric.add(signals[i]);
    }

    content.data = inputData;
    request.content = content;
    OutputData outputData = request.execute().parseAs(OutputData.class);

    return outputData.outputValue;
  }

  /*
   * Loads movies' metadata and signals for predicting their ratings using the
   * Google Prediction API.
   */
  private static void loadMovieData() {
    /*
     * To make the sample demo easier to follow, metadata (e.g., title) and
     * signals (e.g., genre) are in the source codes. In practice, metadata and
     * signals may be loaded from data storage such as the Datastore on the
     * Google App Engine.
     */
    List<Integer> signal1 = new ArrayList<Integer>();
    movieNames.add("Pulp Fiction, The (1994");
    signal1.add(6);
    signal1.add(8);
    movieSignals.add(signal1);

    List<Integer> signal2 = new ArrayList<Integer>();
    movieNames.add("Swan Princess, The (1994)");
    signal2.add(2);
    signal2.add(3);
    movieSignals.add(signal2);

    List<Integer> signal3 = new ArrayList<Integer>();
    movieNames.add("Unstrung Heroes (1995)");
    signal3.add(5);
    signal3.add(8);
    movieSignals.add(signal3);
  }

  private static void showRecommendedMovies(HttpServletResponse resp, Integer[] sortedIndexes)
      throws IOException {
    resp.setContentType("text/html");
    resp.getWriter().println(
        "<link href=\"movierecommenderdemo.css\" type=\"text/css\" rel=\"stylesheet\"/>");
    resp.getWriter().println("<p>Based on User 405's previous ratings on 727 movies, "
        + "the Google Prediction API estimates that the user would prefer</p>");

    for (int i = 0; i < numMovies; i++) {
      resp.getWriter().println(
          "<div class=\"movie\">" + movieNames.get(sortedIndexes[i]) + "</div>");
      if (i < numMovies - 1) {
        resp.getWriter().println("<div class=\"comparer\">" + "&gt;" + "</div>");
      }
    }
  }
}
