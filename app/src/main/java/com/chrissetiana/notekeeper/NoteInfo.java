package com.chrissetiana.notekeeper;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

class NoteInfo implements Parcelable {
    public final static Parcelable.Creator<NoteInfo> CREATOR = new Parcelable.Creator<NoteInfo>() {

        @Override
        public NoteInfo createFromParcel(Parcel source) {
            return new NoteInfo(source);
        }

        @Override
        public NoteInfo[] newArray(int size) {
            return new NoteInfo[size];
        }
    };
    private CourseInfo courseInfo;
    private String title;
    private String text;

    NoteInfo(CourseInfo courseInfo, String title, String text) {
        this.courseInfo = courseInfo;
        this.title = title;
        this.text = text;
    }

    private NoteInfo(Parcel source) {
        courseInfo = source.readParcelable(CourseInfo.class.getClassLoader());
        title = source.readString();
        text = source.readString();
    }

    CourseInfo getCourse() {
        return courseInfo;
    }

    void setCourse(CourseInfo courseInfo) {
        this.courseInfo = courseInfo;
    }

    public String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    void setText(String text) {
        this.text = text;
    }

    private String getCompareKey() {
        return courseInfo.getCourseId() + "|" + title + "|" + text;
    }

    @Override
    public int hashCode() {
        return getCompareKey().hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return getCompareKey();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NoteInfo that = (NoteInfo) o;
        return getCompareKey().equals(that.getCompareKey());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(courseInfo, 0);
        dest.writeString(title);
        dest.writeString(text);
    }
}
