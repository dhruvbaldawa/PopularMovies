package com.dhruvb.popularmovies;

import android.os.Parcelable;
import android.os.Parcel;


/**
 * Created by dhruv on 12/15/15.
 */
public class MovieInfo implements Parcelable {
    public String originalPosterUrl;
    public String title;
    public String releaseDate;
    public String userRating;
    public String overview;

    MovieInfo(Parcel in) {
        originalPosterUrl = in.readString();
        title = in.readString();
        releaseDate = in.readString();
        userRating = in.readString();
        overview = in.readString();
    }

    MovieInfo(String posterUrl, String title, String releaseDate, String userRating, String overview) {
        this.originalPosterUrl = posterUrl;
        this.title = title;
        this.releaseDate = releaseDate;
        this.userRating = userRating;
        this.overview = overview;
    }

    public static String getCompleteImageUrl(String imageUrl, String size) {
        final String BASE_URL = "http://image.tmdb.org/t/p/";
        switch (size) {
            case "w92":
            case "w154":
            case "w185":
            case "w342":
            case "w500":
            case "w780":
            case "original":
                return BASE_URL + size + imageUrl;
        }
        return null;  // @TODO maybe raise an exception instead
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(originalPosterUrl);
        dest.writeString(title);
        dest.writeString(releaseDate);
        dest.writeString(userRating);
        dest.writeString(overview);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public final static Parcelable.Creator<MovieInfo> CREATOR = new Parcelable.Creator<MovieInfo>() {
        @Override
        public MovieInfo createFromParcel(Parcel parcel) {
            return new MovieInfo(parcel);
        }

        @Override
        public MovieInfo[] newArray(int i) {
            return new MovieInfo[i];
        }

    };
}
