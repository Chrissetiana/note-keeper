package com.chrissetiana.notekeeper;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class CourseInfo implements Parcelable {
    private final String noteId;
    private final String noteTitle;
    private final List<ModuleInfo> modules;

    public CourseInfo(String noteId, String noteTitle, List<ModuleInfo> modules) {
        this.noteId = noteId;
        this.noteTitle = noteTitle;
        this.modules = modules;
    }

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

    private CourseInfo(Parcel source) {
        noteId = source.readString();
        noteTitle = source.readString();
        modules = new ArrayList<>();
        source.readTypedList(modules, ModuleInfo.CREATOR);
    }

    String getNoteId() {
        return noteId;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public List<ModuleInfo> getModules() {
        return modules;
    }

    public boolean[] getModulesCompletionStatus() {
        boolean[] status = new boolean[modules.size()];

        for(int i=0; i<modules.size(); i++) {
            status[i] = modules.get(i).isComplete();
        }

        return status;
    }

    public void setModulesCompletionStatus(boolean[] status) {
        for(int i=0; i<modules.size(); i++) {
            modules.get(i).setComplete(status[i]);
        }
    }

    public ModuleInfo getModule(String moduleId) {
        for(ModuleInfo moduleInfo: modules) {
            if(moduleId.equals(moduleInfo.getModuleId())) {
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

       return noteId.equals(that.noteId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeTypedList(modules);
        dest.writeString(noteId);
        dest.writeString(noteTitle);
    }
}
