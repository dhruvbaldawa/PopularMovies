package com.dhruvb.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    private MovieAdapter mMovieAdapter;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Popular Movies");

        mMovieAdapter = new MovieAdapter(this);
//        mMovieAdapter.setNotifyOnChange(true);

        GridView movieGridView = (GridView) findViewById(R.id.grid_view_movie);
        movieGridView.setAdapter(mMovieAdapter);
        movieGridView.setNumColumns(getWindowManager().getDefaultDisplay().getWidth() / 184);
        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                MovieInfo movieInfo = mMovieAdapter.getItem(position);
                Intent intent = new Intent(view.getContext(), MovieDetailActivity.class)
                        .putExtra("com.dhruvb.popularmovies.MovieInfo", movieInfo);
                startActivity(intent);
            }
        });

        FetchMoviesTask movieTask = new FetchMoviesTask();
        movieTask.execute("test");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.dhruvb.popularmovies/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.dhruvb.popularmovies/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, MovieInfo[]> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private MovieInfo[] getPostersFromJson(String moviesJsonStr) throws JSONException {
            final String TMDB_RESULTS = "results";
            final String TMDB_POSTER_KEY = "poster_path";
            final String TMDB_TITLE_KEY = "title";
            final String TMDB_RELEASE_DATE_KEY = "release_date";
            final String TMDB_USER_RATING_KEY = "vote_average";
            final String TMDB_OVERVIEW_KEY = "overview";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray resultArray = moviesJson.getJSONArray(TMDB_RESULTS);

            MovieInfo[] result = new MovieInfo[resultArray.length()];
            for (int i = 0; i < resultArray.length(); i++) {
                String posterUrl = resultArray.getJSONObject(i).getString(TMDB_POSTER_KEY);
                String title = resultArray.getJSONObject(i).getString(TMDB_TITLE_KEY);
                String releaseDate = resultArray.getJSONObject(i).getString(TMDB_RELEASE_DATE_KEY);
                String userRating = resultArray.getJSONObject(i).getString(TMDB_USER_RATING_KEY);
                String overview = resultArray.getJSONObject(i).getString(TMDB_OVERVIEW_KEY);
                result[i] = new MovieInfo(posterUrl, title, releaseDate, userRating, overview);
            }
            return result;
        }

        @Override
        protected MovieInfo[] doInBackground(String... params) {
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
                return getPostersFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(MovieInfo[] result) {
            if (result != null) {
                mMovieAdapter.clear();
                for (MovieInfo mMovieInfo : result) {
                    mMovieAdapter.add(mMovieInfo);
                }
            }
        }
    }
}
