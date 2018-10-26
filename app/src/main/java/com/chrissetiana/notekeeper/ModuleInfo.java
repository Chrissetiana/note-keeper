package com.chrissetiana.notekeeper;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

class ModuleInfo implements Parcelable {
    public static final Creator<ModuleInfo> CREATOR = new Creator<ModuleInfo>() {
        @Override
        public ModuleInfo createFromParcel(Parcel in) {
            return new ModuleInfo(in);
        }

        @Override
        public ModuleInfo[] newArray(int size) {
            return new ModuleInfo[size];
        }
    };
    private final String moduleId;
    private final String moduleTitle;
    private boolean isComplete = false;

    public ModuleInfo(String moduleId, String moduleTitle) {
        this(moduleId, moduleTitle, false);
    }

    private ModuleInfo(String moduleId, String moduleTitle, boolean isComplete) {
        this.moduleId = moduleId;
        this.moduleTitle = moduleTitle;
        this.isComplete = isComplete;
    }

    private ModuleInfo(Parcel source) {
        moduleId = source.readString();
        moduleTitle = source.readString();
        isComplete = source.readByte() == 1;
    }

    public static Creator<ModuleInfo> getCREATOR() {
        return CREATOR;
    }

    public String getModuleTitle() {
        return moduleTitle;
    }

    String getModuleId() {
        return moduleId;
    }

    boolean isComplete() {
        return isComplete;
    }

    void setComplete(boolean complete) {
        isComplete = complete;
    }

    @NonNull
    @Override
    public String toString() {
        return moduleTitle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
