package com.dhruvb.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private ArrayAdapter<String> mMovieAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Popular Movies");

        String[] sampleData = {
                "Test 1",
                "Test 2",
                "Test 3",
                "Test 4",
                "Test 5",
        };
        ArrayList<String> sampleList = new ArrayList<String>(Arrays.asList(sampleData));

        mMovieAdapter = new ArrayAdapter<String>(
                this,
                R.layout.list_item_movie,
                R.id.list_item_movie_textview
        );
//        mMovieAdapter.setNotifyOnChange(true);

        GridView movieGridView = (GridView) findViewById(R.id.grid_view_movie);
        movieGridView.setAdapter(mMovieAdapter);

        FetchMoviesTask movieTask = new FetchMoviesTask();
        movieTask.execute("test");
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private String[] getMovieTitlesFromJson(String moviesJsonStr) throws JSONException {
            final String TMDB_RESULTS = "results";
            final String TMDB_TITLE = "title";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray resultArray = moviesJson.getJSONArray(TMDB_RESULTS);

            String[] result = new String[resultArray.length()];
            for (int i = 0; i < resultArray.length(); i++) {
                result[i] = resultArray.getJSONObject(i).getString(TMDB_TITLE);
            }
            return result;
        }

        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;
            try {

                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/discover/movie";
                final String MOVIES_SORT_BY_KEY = "sort_by";
                final String API_KEY = "api_key";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY, BuildConfig.THEMOVIEDB_API_KEY)
                        .appendQueryParameter(MOVIES_SORT_BY_KEY, "popularity.desc")
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    moviesJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    moviesJsonStr = null;
                }
                moviesJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                moviesJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieTitlesFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mMovieAdapter.clear();
                for (String mMovieStr : result) {
                    mMovieAdapter.add(mMovieStr);
                }
            }
        }
    }
}
