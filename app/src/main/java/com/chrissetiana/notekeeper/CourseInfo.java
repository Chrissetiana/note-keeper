package com.chrissetiana.notekeeper;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class CourseInfo implements Parcelable {
    public static final Creator<CourseInfo> CREATOR = new Creator<CourseInfo>() {
        @Override
        public CourseInfo createFromParcel(Parcel in) {
            return new CourseInfo(in);
        }

        @Override
        public CourseInfo[] newArray(int size) {
            return new CourseInfo[size];
        }
    };
    private final String courseId;
    private final String noteTitle;
    private final List<ModuleInfo> modules;

    public CourseInfo(String courseId, String noteTitle, List<ModuleInfo> modules) {
        this.courseId = courseId;
        this.noteTitle = noteTitle;
        this.modules = modules;
    }

    private CourseInfo(Parcel source) {
        courseId = source.readString();
        noteTitle = source.readString();
        modules = new ArrayList<>();
        source.readTypedList(modules, ModuleInfo.CREATOR);
    }

    String getCourseId() {
        return courseId;
    }

    public String getTitle() {
        return noteTitle;
    }

    public List<ModuleInfo> getModules() {
        return modules;
    }

    public boolean[] getModulesCompletionStatus() {
        boolean[] status = new boolean[modules.size()];

        for (int i = 0; i < modules.size(); i++) {
            status[i] = modules.get(i).isComplete();
        }

        return status;
    }

    public void setModulesCompletionStatus(boolean[] status) {
        for (int i = 0; i < modules.size(); i++) {
            modules.get(i).setComplete(status[i]);
        }
    }

    public ModuleInfo getModule(String moduleId) {
        for (ModuleInfo moduleInfo : modules) {
            if (moduleId.equals(moduleInfo.getModuleId())) {
                return moduleInfo;
            }
        }

        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return noteTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CourseInfo that = (CourseInfo) o;

        return courseId.equals(that.courseId);
    }

    @Override
    public int hashCode() {
        return courseId.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(courseId);
        dest.writeString(noteTitle);
        dest.writeTypedList(modules);
    }
}
