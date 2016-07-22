package com.evansappwriter.sharkfeed.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by markevans on 7/21/16.
 */
public class Photo implements Parcelable {
    private String mId;
    private String mTitle;
    private String mUrlThumb;
    private String mUrlMedium;
    private String mUrlLarge;
    private String mUrlOriginal;

    public Photo() {

    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getUrlThumb() {
        return mUrlThumb;
    }

    public void setUrlThumb(String urlThumb) {
        mUrlThumb = urlThumb;
    }

    public String getUrlMedium() {
        return mUrlMedium;
    }

    public void setUrlMedium(String urlMedium) {
        mUrlMedium = urlMedium;
    }

    public String getUrlLarge() {
        return mUrlLarge;
    }

    public void setUrlLarge(String urlLarge) {
        mUrlLarge = urlLarge;
    }

    public String getUrlOriginal() {
        return mUrlOriginal;
    }

    public void setUrlOriginal(String urlOriginal) {
        mUrlOriginal = urlOriginal;
    }

    private Photo(Parcel in) {
        mId = in.readString();
        mTitle = in.readString();
        mUrlThumb = in.readString();
        mUrlMedium = in.readString();
        mUrlLarge = in.readString();
        mUrlOriginal = in.readString();
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mTitle);
        dest.writeString(mUrlThumb);
        dest.writeString(mUrlMedium);
        dest.writeString(mUrlLarge);
        dest.writeString(mUrlOriginal);
    }
}
