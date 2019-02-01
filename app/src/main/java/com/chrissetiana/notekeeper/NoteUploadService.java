package com.chrissetiana.notekeeper;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.net.Uri;
import android.os.AsyncTask;

public class NoteUploadService extends JobService {
    public static final String EXTRA_DATA_URI = "com.chrissetiana.notekeeper.extra.DATA_URI";
    private NoteUpload noteUpload;

    public NoteUploadService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        AsyncTask<JobParameters, Void, Void> task = new AsyncTask<JobParameters, Void, Void>() {
            @Override
            protected Void doInBackground(JobParameters... args) {
                JobParameters jobParameters = args[0];

                String stringDataUri = jobParameters.getExtras().getString(EXTRA_DATA_URI);
                Uri dataUri = Uri.parse(stringDataUri);

                noteUpload.doUpload(dataUri);

                if (!noteUpload.isCancelled()) {
                    jobFinished(jobParameters, false);
                }

                return null;
            }
        };

        noteUpload = new NoteUpload(this);
        task.execute(params);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        noteUpload.cancel();
        return true;
    }
}
