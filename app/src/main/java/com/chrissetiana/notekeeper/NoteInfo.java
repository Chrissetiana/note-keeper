package com.chrissetiana.notekeeper;

class NoteInfo {
    private CourseInfo courseInfo;
    private String title;
    private String text;

    NoteInfo(CourseInfo courseInfo, String title, String text) {
        this.courseInfo = courseInfo;
        this.title = title;
        this.text = text;
    }

    public CourseInfo getCourseInfo() {
        return courseInfo;
    }

    private void setCourseInfo(CourseInfo courseInfo) {
        this.courseInfo = courseInfo;
    }

    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    private void setText(String text) {
        this.text = text;
    }

    private String getCompareKey() {
        return courseInfo.getNoteId() + "|" + title + "|" + text;
    }

    @Override
    public boolean equals( Object o) {
        if(this == o) {
            return true;
        }

        if(o == null || getClass() != o.getClass()); {
            return false;
        }

        NoteInfo that = (NoteInfo) o;
        return getCompareKey().equals(that.getCompareKey());
    }

    @Override
    public int hashCode() {
        return getCompareKey().hashCode();
    }

    @Override
    public String toString() {
        return getCompareKey();
    }
}
