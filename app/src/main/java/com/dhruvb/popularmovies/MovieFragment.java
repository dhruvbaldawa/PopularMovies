package com.dhruvb.popularmovies;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
    private static final String STATE_SELECTED_MOVIE_KEY = "mPosition";

    private MovieAdapter mMovieAdapter;
    private OkHttpClient mHttpClient;
    private GridView mGridView;
    private int mPosition = GridView.INVALID_POSITION;

    public interface SelectCallback {
        void onItemSelected(Uri dateUri);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mHttpClient = new OkHttpClient();
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_movie, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.grid_view_movie);
        mGridView.setAdapter(mMovieAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                long _id = mMovieAdapter.getItemId(position);
                mPosition = position;
                Log.v(LOG_TAG, "movie item position: " + _id);
                Uri uri;
                if (MovieUtilities.isFavoritesEnabled(getActivity())) {
                    uri = MoviesContract.FavoritesEntry.buildMoviesUri(_id);
                } else {
                    uri = MoviesContract.MoviesEntry.buildMoviesUri(_id);
                }
                ((SelectCallback) getActivity()).onItemSelected(uri);
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_SELECTED_MOVIE_KEY)) {
            mPosition = savedInstanceState.getInt(STATE_SELECTED_MOVIE_KEY);
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(STATE_SELECTED_MOVIE_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
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

        if (mPosition != GridView.INVALID_POSITION) {
            mGridView.setSelection(mPosition);
        }

//        } else {
//            // select the first item in the list by default
//            new Handler().post(new Runnable() {
//                @Override
//                public void run() {
//                    mPosition = 0;
//                    mGridView.setSelection(mPosition);
//                    mGridView.performItemClick(
//                            mGridView.getChildAt(mPosition),
//                            mPosition,
//                            mMovieAdapter.getItemId(mPosition)
//                    );
//                }
//            });
//        }
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

        // Using the other endpoint because it does not change as often the discover endpoint
//        final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/discover/movie";
        final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/" + sortOrder;
        final String API_KEY = "api_key";
//        final String SORT_BY_KEY = "sort_by";

        Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
//                .appendQueryParameter(SORT_BY_KEY, sortOrder)
                .appendQueryParameter(API_KEY, BuildConfig.THEMOVIEDB_API_KEY)
                .build();

        Request request = new Request.Builder()
                .url(builtUri.toString())
                .build();

        mHttpClient.newCall(request).enqueue(new Callback() {
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
