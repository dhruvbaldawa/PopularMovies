package com.dhruvb.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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
        List<String> sampleList = new ArrayList<String>(Arrays.asList(sampleData));

        ArrayAdapter<String> movieAdapter;
        movieAdapter = new ArrayAdapter<String>(
                this,
                R.layout.list_item_movie,
                R.id.list_item_movie_textview,
                sampleList
        );

        GridView movieGridView = (GridView) findViewById(R.id.grid_view_movie);
        movieGridView.setAdapter(movieAdapter);

    }
}
