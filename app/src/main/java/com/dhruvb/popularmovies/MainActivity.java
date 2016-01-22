package com.dhruvb.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.dhruvb.popularmovies.data.MoviesContract;
import com.facebook.stetho.Stetho;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    private MovieAdapter mMovieAdapter;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private OkHttpClient httpClient;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);
// @TODO: fix this thing after moving to content providers
//
//        if (savedInstanceState == null || !savedInstanceState.containsKey(
//                getString(R.string.main_activity_movie_detail_key))) {
//            mMovieInfoList = new ArrayList<>();
//        } else {
//            mMovieInfoList = savedInstanceState.getParcelableArrayList(
//                    getString(R.string.main_activity_movie_detail_key));

//        }
        Uri contentURI;
        // @TODO: move to utilities
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String sortOrder = settings.getString(getString(R.string.pref_sort_by_key),
                getString(R.string.pref_sort_by_value_most_popular));
        if (sortOrder.contentEquals(getString(R.string.pref_sort_by_value_favorites))) {
            contentURI = MoviesContract.FavoritesEntry.CONTENT_URI;
        } else {
            contentURI = MoviesContract.MoviesEntry.CONTENT_URI;
        }
        Cursor movieCursor = getContentResolver().query(contentURI,
                null,
                null,
                null,
                null);

        mMovieAdapter = new MovieAdapter(this, movieCursor, 0);

        GridView movieGridView = (GridView) findViewById(R.id.grid_view_movie);
        movieGridView.setAdapter(mMovieAdapter);
        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                long _id = mMovieAdapter.getItemId(position);
                Log.v(LOG_TAG, "movie item position: " + _id);
                Uri uri = MoviesContract.MoviesEntry.buildMoviesUri(_id);
                Intent intent = new Intent(view.getContext(), MovieDetailActivity.class)
                        .setData(uri);
                startActivity(intent);
            }
        });

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build()
        );

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        httpClient = new OkHttpClient();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        updateMovies();
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

    private void clearMoviesInDatabase() {
        Log.v(LOG_TAG, "clearing out the movie database!");
        getContentResolver().delete(MoviesContract.MoviesEntry.CONTENT_URI, null, null);
    }

    private void insertMoviesFromJson(String moviesJsonStr) throws JSONException {
        final String TMDB_ID_KEY = "id";
        final String TMDB_RESULTS = "results";
        final String TMDB_POSTER_KEY = "poster_path";
        final String TMDB_TITLE_KEY = "title";
        final String TMDB_RELEASE_DATE_KEY = "release_date";
        final String TMDB_USER_RATING_KEY = "vote_average";
        final String TMDB_OVERVIEW_KEY = "overview";
        final String TMDB_BACKDROP__KEY = "backdrop_path";

        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        JSONArray resultArray = moviesJson.getJSONArray(TMDB_RESULTS);

        ContentValues[] values = new ContentValues[resultArray.length()];
        for (int i = 0; i < resultArray.length(); i++) {
            values[i] = new ContentValues();
            values[i].put(MoviesContract.MoviesEntry._ID,
                    resultArray.getJSONObject(i).getInt(TMDB_ID_KEY));
            values[i].put(MoviesContract.MoviesEntry.COLUMN_TITLE,
                    resultArray.getJSONObject(i).getString(TMDB_TITLE_KEY));
            values[i].put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW,
                    resultArray.getJSONObject(i).getString(TMDB_OVERVIEW_KEY));
            values[i].put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE,
                    resultArray.getJSONObject(i).getString(TMDB_RELEASE_DATE_KEY));
            values[i].put(MoviesContract.MoviesEntry.COLUMN_POSTER_URL,
                    resultArray.getJSONObject(i).getString(TMDB_POSTER_KEY));
            values[i].put(MoviesContract.MoviesEntry.COLUMN_BACKDROP_URL,
                    resultArray.getJSONObject(i).getString(TMDB_BACKDROP__KEY));
            values[i].put(MoviesContract.MoviesEntry.COLUMN_RATING,
                    resultArray.getJSONObject(i).getString(TMDB_USER_RATING_KEY));
        }
        getContentResolver().bulkInsert(MoviesContract.MoviesEntry.CONTENT_URI, values);
    }

    private void updateMovies() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String sortOrder = settings.getString(getString(R.string.pref_sort_by_key),
                getString(R.string.pref_sort_by_value_most_popular));

        if (sortOrder.contentEquals(getString(R.string.pref_sort_by_value_favorites))) return;

        final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/" + sortOrder;
        final String API_KEY = "api_key";

        Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                .appendQueryParameter(API_KEY, BuildConfig.THEMOVIEDB_API_KEY)
                .build();

        Request request = new Request.Builder()
                .url(builtUri.toString())
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Movie fetching issues: " + response);

                try {
                    clearMoviesInDatabase();
                    insertMoviesFromJson(response.body().string());

                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Cursor cursor = getContentResolver()
                                .query(MoviesContract.MoviesEntry.CONTENT_URI, null, null, null, null);
                        mMovieAdapter.changeCursor(cursor);
                    }
                });
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!mMovieAdapter.getCursor().isClosed()) mMovieAdapter.getCursor().close();
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
}
