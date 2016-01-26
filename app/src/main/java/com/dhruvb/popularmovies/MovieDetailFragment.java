package com.dhruvb.popularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dhruvb.popularmovies.data.MoviesContract;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Vector;

/**
 * Created by dhruv on 1/26/16.
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    public static final int DETAIL_LOADER = 0;
    private static final String HASHTAG = "#PopularMovies";

    private ShareActionProvider mShareActionProvider;
    private String mMovieTitle;
    private OkHttpClient mHttpClient;
    private long mMovieId;
    private boolean mIsFavorite;

    private ImageView mMoviePosterImageView;
    private ImageView mFavoritesImageView;
    private TextView mOriginalTitleTextView;
    private TextView mReleaseDateTextView;
    private TextView mUserRatingTextView;
    private TextView mOverviewTextView;
    private LinearLayout mTrailorsLayout;
    private LinearLayout mReviewsLayout;

    public MovieDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHttpClient = new OkHttpClient();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        mMoviePosterImageView = (ImageView)rootView.findViewById(R.id.movie_detail_poster_imageview);
        mFavoritesImageView = (ImageView)rootView.findViewById(R.id.movie_detail_favorite_image_view);
        mOriginalTitleTextView = (TextView)rootView.findViewById(R.id.movie_detail_title_textview);
        mReleaseDateTextView = (TextView)rootView.findViewById(R.id.movie_detail_release_date_textview);
        mUserRatingTextView = (TextView)rootView.findViewById(R.id.movie_detail_user_rating_textview);
        mOverviewTextView = (TextView)rootView.findViewById(R.id.movie_detail_overview_textview);
        mTrailorsLayout = (LinearLayout)rootView.findViewById(R.id.movie_detail_trailors_layout);
        mReviewsLayout = (LinearLayout)rootView.findViewById(R.id.movie_detail_reviews_layout);

        showFavoriteIcon();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        fetchAndShowTrailors();
        fetchAndShowReviews();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        if (intent == null || intent.getData() == null) return null;
        mMovieId = ContentUris.parseId(intent.getData());

        Cursor cursor = getActivity().getContentResolver().query(
                MoviesContract.FavoritesEntry.buildMoviesUri(mMovieId),
                null,
                null,
                null,
                null
        );

        mIsFavorite = (cursor != null ? cursor.getCount() : 0) != 0;
        cursor.close();

        return new CursorLoader(getActivity(), intent.getData(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || !cursor.moveToFirst()) return;

        showFavoriteIcon();
        mMovieTitle = cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_TITLE));

        getActivity().setTitle(mMovieTitle);
        Resources res = getResources();

        Picasso.with(getActivity())
                .load(MoviesContract.MoviesEntry.getCompleteImageUrl(
                        cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_POSTER_URL)), "w500"))
                .placeholder(R.drawable.ic_photo_black_48dp)
                .error(R.drawable.ic_broken_image_black_36dp)
                .into(mMoviePosterImageView);

        mFavoritesImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsFavorite) {
                    int deleted = getActivity().getContentResolver().delete(
                            MoviesContract.FavoritesEntry.buildMoviesUri(mMovieId),
                            null,
                            null);
                    if (deleted != 1) {
                        Toast.makeText(v.getContext(), "Sorry, can't unfavorite the movie", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(v.getContext(), "Movie unfavorited successfully", Toast.LENGTH_SHORT).show();
                    mIsFavorite = false;
                } else {
                    ContentValues values = new ContentValues();
                    Cursor copyCursor = getActivity().getContentResolver().query(
                            MoviesContract.MoviesEntry.buildMoviesUri(mMovieId),
                            null,
                            null,
                            null,
                            null
                    );
                    copyCursor.moveToFirst();

                    values.put(
                            MoviesContract.FavoritesEntry._ID,
                            copyCursor.getInt(copyCursor.getColumnIndex(MoviesContract.FavoritesEntry._ID))
                    );
                    values.put(
                            MoviesContract.FavoritesEntry.COLUMN_TITLE,
                            copyCursor.getString(copyCursor.getColumnIndex(MoviesContract.FavoritesEntry.COLUMN_TITLE))
                    );
                    values.put(
                            MoviesContract.FavoritesEntry.COLUMN_OVERVIEW,
                            copyCursor.getString(copyCursor.getColumnIndex(MoviesContract.FavoritesEntry.COLUMN_OVERVIEW))
                    );
                    values.put(
                            MoviesContract.FavoritesEntry.COLUMN_RATING,
                            copyCursor.getString(copyCursor.getColumnIndex(MoviesContract.FavoritesEntry.COLUMN_RATING))
                    );
                    values.put(
                            MoviesContract.FavoritesEntry.COLUMN_RELEASE_DATE,
                            copyCursor.getString(copyCursor.getColumnIndex(MoviesContract.FavoritesEntry.COLUMN_RELEASE_DATE))
                    );
                    values.put(
                            MoviesContract.FavoritesEntry.COLUMN_BACKDROP_URL,
                            copyCursor.getString(copyCursor.getColumnIndex(MoviesContract.FavoritesEntry.COLUMN_BACKDROP_URL))
                    );
                    values.put(
                            MoviesContract.FavoritesEntry.COLUMN_POSTER_URL,
                            copyCursor.getString(copyCursor.getColumnIndex(MoviesContract.FavoritesEntry.COLUMN_POSTER_URL))
                    );
                    copyCursor.close();
                    getActivity().getContentResolver().insert(
                            MoviesContract.FavoritesEntry.buildMoviesUri(mMovieId), values);
                    Toast.makeText(v.getContext(), "Movie favorited successfully", Toast.LENGTH_SHORT).show();
                    mIsFavorite = true;
                }
                showFavoriteIcon();
            }
        });

        mOriginalTitleTextView.setText(
                cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_TITLE)));

        mReleaseDateTextView.setText(String.format(
                res.getString(R.string.movie_detail_label_release_date),
                cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE))));

        mUserRatingTextView.setText(String.format(
                res.getString(R.string.movie_detail_label_rating),
                cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_RATING))));

        mOverviewTextView.setText(cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_OVERVIEW)));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    private Intent createShareIntent(String title, String name, String url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, title + " (" + name + ") | " + url + " " + HASHTAG);
        return shareIntent;
    }

    private void showFavoriteIcon() {
        if (mIsFavorite) {
            mFavoritesImageView.setImageResource(R.drawable.heart);
        } else {
            mFavoritesImageView.setImageResource(R.drawable.heart_outline);
        }

    }

    private void fetchAndShowTrailors() {
        final String TRAILORS_BASE_URL = "http://api.themoviedb.org/3/movie/" + mMovieId + "/videos";
        final String API_KEY = "api_key";

        Uri builtUri = Uri.parse(TRAILORS_BASE_URL).buildUpon()
                .appendQueryParameter(API_KEY, BuildConfig.THEMOVIEDB_API_KEY)
                .build();

        Request request = new Request.Builder()
                .url(builtUri.toString())
                .build();

        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(com.squareup.okhttp.Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Trailor fetching issues: " + response);
                final String TMDB_RESULTS = "results";
                final String TMDB_TRAILOR_NAME_KEY = "name";
                final String TMDB_TRAILOR_VIDEO_KEY = "key";
                final String TMDB_TRAILOR_SITE_KEY = "site";
                final String TMDB_TRAILOR_TYPE_KEY = "type";

                final String TMDB_YOUTUBE_VALUE = "YouTube";
                final String TMDB_TYPE_VALUE = "Trailer";

                final Vector<String> trailorUrls = new Vector();
                final Vector<String> trailors = new Vector();

                try {
                    JSONObject trailorsJSON = new JSONObject(response.body().string());
                    JSONArray trailorsArray = trailorsJSON.getJSONArray(TMDB_RESULTS);

                    for(int i=0; i < trailorsArray.length(); i++) {
                        String siteValue = trailorsArray.getJSONObject(i).getString(TMDB_TRAILOR_SITE_KEY);
                        String typeValue = trailorsArray.getJSONObject(i).getString(TMDB_TRAILOR_TYPE_KEY);

                        // Skip if the video is not from Youtube or is not a trailor
                        if (!typeValue.contentEquals(TMDB_TYPE_VALUE) || !siteValue.contentEquals(TMDB_YOUTUBE_VALUE)) continue;

                        trailors.add(trailorsArray.getJSONObject(i).getString(TMDB_TRAILOR_NAME_KEY));
                        trailorUrls.add(trailorsArray.getJSONObject(i).getString(TMDB_TRAILOR_VIDEO_KEY));
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error parsing trailor json response");
                    e.printStackTrace();
                }

                if (trailors.isEmpty()) return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LayoutInflater inflater = (LayoutInflater)getActivity()
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        mTrailorsLayout.setVisibility(View.VISIBLE);
                        mTrailorsLayout.removeAllViews();

                        // @TODO: better social message here
                        if (mShareActionProvider != null) {
                            mShareActionProvider.setShareIntent(createShareIntent(mMovieTitle,
                                    trailors.get(0), trailorUrls.get(0)));
                        }

                        for (int i = 0; i < trailors.size(); i++) {

                            View view = inflater.inflate(R.layout.list_item_trailor, null);
                            view.setClickable(true);
                            view.setTag(trailorUrls.get(i));
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Uri uri = Uri.parse("https://youtube.com/watch")
                                            .buildUpon()
                                            .appendQueryParameter("v", (String) v.getTag())
                                            .build();
                                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                                }
                            });
                            TextView name = (TextView)view.findViewById(R.id.movie_detail_trailor_textview);
                            name.setText(trailors.get(i));

                            mTrailorsLayout.addView(view);
                        }
                    }
                });
            }
        });

    }

    private void fetchAndShowReviews() {
        final String REVIEWS_BASE_URL = "http://api.themoviedb.org/3/movie/" + mMovieId + "/reviews";
        final String API_KEY = "api_key";

        Uri builtUri = Uri.parse(REVIEWS_BASE_URL).buildUpon()
                .appendQueryParameter(API_KEY, BuildConfig.THEMOVIEDB_API_KEY)
                .build();

        Request request = new Request.Builder()
                .url(builtUri.toString())
                .build();

        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(com.squareup.okhttp.Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Trailor fetching issues: " + response);
                final String TMDB_RESULTS = "results";
                final String TMDB_REVIEW_AUTHOR_KEY = "author";
                final String TMDB_REVIEW_CONTENT_KEY = "content";

                final Vector<String> reviewAuthors = new Vector();
                final Vector<String> reviewContents = new Vector();

                try {
                    JSONObject reviewsJSON = new JSONObject(response.body().string());
                    JSONArray reviewsArray = reviewsJSON.getJSONArray(TMDB_RESULTS);

                    for(int i=0; i < reviewsArray.length(); i++) {
                        reviewAuthors.add(reviewsArray.getJSONObject(i).getString(TMDB_REVIEW_AUTHOR_KEY));
                        reviewContents.add(reviewsArray.getJSONObject(i).getString(TMDB_REVIEW_CONTENT_KEY));
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error parsing reviews json response");
                    e.printStackTrace();
                }

                if (reviewContents.isEmpty()) return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LayoutInflater inflater = (LayoutInflater)getActivity()
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        mReviewsLayout.setVisibility(View.VISIBLE);
                        mReviewsLayout.removeAllViews();

                        for (int i = 0; i < reviewContents.size(); i++) {

                            View view = inflater.inflate(R.layout.list_item_review, null);
                            view.setClickable(true);

                            TextView authorTextView = (TextView)view.findViewById(R.id.movie_detail_review_author_textview);
                            authorTextView.setText(reviewAuthors.get(i));
                            TextView contentTextView = (TextView)view.findViewById(R.id.movie_detail_review_content_textview);
                            contentTextView.setText(reviewContents.get(i));
                            mReviewsLayout.addView(view);
                        }
                    }
                });
            }
        });
    }
}
