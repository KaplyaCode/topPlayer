package com.example.topplayer;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoFiles implements Parcelable {
    private String id;
    private String title;
    private String display_name;
    private String size;
    private String duration;
    private String path;
    private String dateAdded;

    public VideoFiles(String id, String title, String display_name, String size, String duration, String path, String dateAdded) {
        this.id = id;
        this.title = title;
        this.display_name = display_name;
        this.size = size;
        this.duration = duration;
        this.path = path;
        this.dateAdded = dateAdded;
    }

    protected VideoFiles(Parcel in) {
        id = in.readString();
        title = in.readString();
        display_name = in.readString();
        size = in.readString();
        duration = in.readString();
        path = in.readString();
        dateAdded = in.readString();
    }

    public static final Creator<VideoFiles> CREATOR = new Creator<VideoFiles>() {
        @Override
        public VideoFiles createFromParcel(Parcel in) {
            return new VideoFiles(in);
        }

        @Override
        public VideoFiles[] newArray(int size) {
            return new VideoFiles[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayName() {
        return display_name;
    }

    public void setDisplayName(String name) {
        this.display_name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(display_name);
        dest.writeString(size);
        dest.writeString(duration);
        dest.writeString(path);
        dest.writeString(dateAdded);
    }
}
