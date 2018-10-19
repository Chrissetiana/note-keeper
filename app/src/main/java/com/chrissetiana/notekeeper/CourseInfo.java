package com.chrissetiana.notekeeper;

import android.support.annotation.NonNull;

import java.util.List;

public final class CourseInfo {
    private final String noteId;
    private final String noteTitle;
    private final List<ModuleInfo> modules;

    public CourseInfo(String noteId, String noteTitle, List<ModuleInfo> modules) {
        this.noteId = noteId;
        this.noteTitle = noteTitle;
        this.modules = modules;
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
}
