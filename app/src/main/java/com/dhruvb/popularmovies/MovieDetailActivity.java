package com.dhruvb.popularmovies;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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


public class MovieDetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = MovieDetailActivity.class.getSimpleName();
    private OkHttpClient httpClient;
    private long mMovieId;

    private void updateView() {
        ImageView moviePosterImageView = (ImageView)findViewById(R.id.movie_detail_poster_imageview);
        TextView originalTitleTextView = (TextView)findViewById(R.id.movie_detail_title_textview);
        TextView releaseDateTextView = (TextView)findViewById(R.id.movie_detail_release_date_textview);
        TextView userRatingTextView = (TextView)findViewById(R.id.movie_detail_user_rating_textview);
        TextView overviewTextView = (TextView)findViewById(R.id.movie_detail_overview_textview);

        Uri movieURI = getIntent().getData();
        mMovieId = ContentUris.parseId(movieURI);
        Cursor cursor = getContentResolver().query(movieURI, null, null, null, null);
        if (!cursor.moveToFirst()) return;

        setTitle(cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_TITLE)));
        Resources res = getResources();

        Picasso.with(this)
                .load(MoviesContract.MoviesEntry.getCompleteImageUrl(
                        cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_POSTER_URL)), "w500"))
                .placeholder(R.drawable.ic_photo_black_48dp)
                .error(R.drawable.ic_broken_image_black_36dp)
                .into(moviePosterImageView);
        originalTitleTextView.setText(
                cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_TITLE)));
        releaseDateTextView.setText(String.format(
                res.getString(R.string.movie_detail_label_release_date),
                cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE))));
        userRatingTextView.setText(String.format(
                res.getString(R.string.movie_detail_label_rating),
                cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_RATING))));
        overviewTextView.setText(cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_OVERVIEW)));
        cursor.close();
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

        httpClient.newCall(request).enqueue(new Callback() {
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

                MovieDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LinearLayout trailorsLayout = (LinearLayout)findViewById(R.id.movie_detail_trailors_layout);
                        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        trailorsLayout.setVisibility(View.VISIBLE);
                        trailorsLayout.removeAllViews();

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

                            trailorsLayout.addView(view);
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

        httpClient.newCall(request).enqueue(new Callback() {
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

                MovieDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LinearLayout reviewsLayout = (LinearLayout)findViewById(R.id.movie_detail_reviews_layout);
                        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        reviewsLayout.setVisibility(View.VISIBLE);
                        reviewsLayout.removeAllViews();

                        for (int i = 0; i < reviewContents.size(); i++) {

                            View view = inflater.inflate(R.layout.list_item_review, null);
                            view.setClickable(true);

                            TextView authorTextView = (TextView)view.findViewById(R.id.movie_detail_review_author_textview);
                            authorTextView.setText(reviewAuthors.get(i));
                            TextView contentTextView = (TextView)view.findViewById(R.id.movie_detail_review_content_textview);
                            contentTextView.setText(reviewContents.get(i));
                            reviewsLayout.addView(view);
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "detail activity started");
        setContentView(R.layout.activity_movie_detail);
        httpClient = new OkHttpClient();
    }

    @Override
    protected void onStart() {
        updateView();
        fetchAndShowTrailors();
        fetchAndShowReviews();
        super.onStart();
    }
}
