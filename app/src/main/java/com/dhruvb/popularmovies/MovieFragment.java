package com.dhruvb.popularmovies;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.content.ContentValues;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.dhruvb.popularmovies.data.MoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by dhruv on 1/26/16.
 */
public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MovieFragment.class.getSimpleName();
    private static final int MOVIE_LOADER = 0;

    private MovieAdapter mMovieAdapter;
    private OkHttpClient httpClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        httpClient = new OkHttpClient();
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_movie, container, false);

        GridView movieGridView = (GridView) rootView.findViewById(R.id.grid_view_movie);
        movieGridView.setAdapter(mMovieAdapter);
        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                long _id = mMovieAdapter.getItemId(position);
                Log.v(LOG_TAG, "movie item position: " + _id);
                Uri uri;
                if (MovieUtilities.isFavoritesEnabled(view.getContext())) {
                    uri = MoviesContract.FavoritesEntry.buildMoviesUri(_id);
                } else {
                    uri = MoviesContract.MoviesEntry.buildMoviesUri(_id);
                }
                Intent intent = new Intent(view.getContext(), MovieDetailActivity.class)
                        .setData(uri);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
//        updateMovies();
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri contentURI;
        if (MovieUtilities.isFavoritesEnabled(getActivity())) {
            contentURI = MoviesContract.FavoritesEntry.CONTENT_URI;
        } else {
            contentURI = MoviesContract.MoviesEntry.CONTENT_URI;
        }

        return new CursorLoader(getActivity(),
                contentURI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }

    public void refreshMovies() {
        if (!MovieUtilities.isFavoritesEnabled(getContext())) {
            updateMovies();
        } else {
            restartLoader();
        }
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    private void clearMoviesInDatabase() {
        Log.v(LOG_TAG, "clearing out the movie database!");
        getActivity().getContentResolver()
                .delete(MoviesContract.MoviesEntry.CONTENT_URI, null, null);
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
        getActivity().getContentResolver()
                .bulkInsert(MoviesContract.MoviesEntry.CONTENT_URI, values);
    }

    private void updateMovies() {
        String sortOrder = MovieUtilities.getSortOrder(getActivity());

        if (MovieUtilities.isFavoritesEnabled(getActivity())) return;

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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        restartLoader();
                    }
                });
            }
        });
    }
}
