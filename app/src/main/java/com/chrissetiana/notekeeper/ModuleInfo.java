package com.chrissetiana.notekeeper;

import android.os.Parcel;
import android.os.Parcelable;

class ModuleInfo {
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

    String getModuleId() {
        return moduleId;
    }

    public String getModuleTitle() {
        return moduleTitle;
    }

    boolean isComplete() {
        return isComplete;
    }

    void setComplete(boolean complete) {
        isComplete = complete;
    }

    @Override
    public String toString() {
        return moduleTitle;
    }
}
