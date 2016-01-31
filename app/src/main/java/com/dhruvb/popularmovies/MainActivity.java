package com.dhruvb.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.stetho.Stetho;


public class MainActivity extends AppCompatActivity implements MovieFragment.SelectCallback {
    public static final String MOVIE_FRAGMENT_TAG = "MOVIE_DETAIL_FRAGMENT";
    private boolean mTwoPane;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private String mSortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);
        mSortOrder = MovieUtilities.getSortOrder(this);

        if (findViewById(R.id.fragment_movie_detail_view) != null) {
            mTwoPane = true;
            if (savedInstanceState != null) {
                MovieDetailFragment movieDetailFragment = (MovieDetailFragment)getSupportFragmentManager().findFragmentByTag(MOVIE_FRAGMENT_TAG);

                if (movieDetailFragment == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_movie_detail_view, new MovieDetailFragment(), MOVIE_FRAGMENT_TAG)
                            .commit();
                }
            }
        } else {
            mTwoPane = false;
        }

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build()
        );
    }

    protected void onResume() {
        super.onResume();
        // @TODO: only notify on sort order changes
        MovieFragment movieFragment = (MovieFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_movie);

        if (movieFragment != null) {
            movieFragment.refreshMovies();
        }
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
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(MovieDetailFragment.DETAIL_URI, contentUri);

            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_movie_detail_view, fragment, MOVIE_FRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, MovieDetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }

    }
}
