package com.chrissetiana.notekeeper;

import android.app.LoaderManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.chrissetiana.notekeeper.NoteKeeperProviderContract.Notes;

import java.util.List;

import static com.chrissetiana.notekeeper.NoteActivity.LOADER_NOTES;
import static com.chrissetiana.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int NOTE_UPLOADER_JOB_ID = 1;
    private NoteRecyclerAdapter noteAdapter;
    private RecyclerView recyclerItems;
    private LinearLayoutManager noteLayoutManager;
    private CourseRecyclerAdapter courseAdapter;
    private GridLayoutManager courseLayoutManager;
    private NoteKeeperDatabaseHelper databaseHelper;

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        enableStrictMode();

        databaseHelper = new NoteKeeperDatabaseHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NoteActivity.class));
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initializeDisplayContent();
    }

    private void enableStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(LOADER_NOTES, null, this);
        updateNaveHeader();
        openDrawer();
    }

    private void openDrawer() {
        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.openDrawer(Gravity.START);
            }
        }, 1000);
    }

    private void loadNotes() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        final String[] columnsNotes = {
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry._ID};

        String orderBy = NoteInfoEntry.COLUMN_COURSE_ID + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;

        final Cursor cursorNotes = db.query(NoteInfoEntry.TABLE_NAME, columnsNotes, null, null, null, null, orderBy);

        noteAdapter.changeCursor(cursorNotes);
    }

    private void updateNaveHeader() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        TextView userName = headerView.findViewById(R.id.text_user_name);
        TextView userEmail = headerView.findViewById(R.id.text_user_email);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sharedPreferences.getString("user_name", "");
        String email = sharedPreferences.getString("user_email", "");

        userName.setText(name);
        userEmail.setText(email);
    }

    private void initializeDisplayContent() {
        DataManager.loadFromDatabase(databaseHelper);

        recyclerItems = findViewById(R.id.list_items);

        noteLayoutManager = new LinearLayoutManager(this);
        courseLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.layout_manager));

        noteAdapter = new NoteRecyclerAdapter(this, null);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        courseAdapter = new CourseRecyclerAdapter(this, courses);

        displayNotes();
        displayCourses();
    }

    private void displayNotes() {
        recyclerItems.setLayoutManager(noteLayoutManager);
        recyclerItems.setAdapter(noteAdapter);
        selectNavigationMenuItem(R.id.nav_notes);
    }

    private void selectNavigationMenuItem(int id) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(id).setChecked(true);
    }

    private void displayCourses() {
        recyclerItems.setLayoutManager(courseLayoutManager);
        recyclerItems.setAdapter(courseAdapter);
        selectNavigationMenuItem(R.id.nav_courses);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_backup) {
            backupNotes();
        } else if (id == R.id.action_upload) {
            uploadNotes();
        }

        return super.onOptionsItemSelected(item);
    }

    private void uploadNotes() {
        PersistableBundle extras = new PersistableBundle();
        extras.putString(NoteUploadService.EXTRA_DATA_URI, Notes.CONTENT_URI.toString());

        ComponentName componentName = new ComponentName(this, NoteUploadService.class);

        JobInfo jobInfo = new JobInfo.Builder(NOTE_UPLOADER_JOB_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setExtras(extras)
                .build();

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
    }

    private void backupNotes() {
        Intent intent = new Intent(this, NoteBackupService.class);
        intent.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);
        startService(intent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_notes) {
            displayNotes();
        } else if (id == R.id.nav_courses) {
            displayCourses();
        } else if (id == R.id.nav_share) {
            handleShare();
        } else if (id == R.id.nav_send) {
            handleSelection(R.string.nav_send);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void handleShare() {
        View view = findViewById(R.id.list_items);

        final String userFavoriteSocial = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString("user_favorite_social", "");

        Snackbar.make(view, "Share to " + userFavoriteSocial, Snackbar.LENGTH_LONG).show();
    }

    private void handleSelection(int messageId) {
        View view = findViewById(R.id.list_items);
        Snackbar.make(view, messageId, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;

        if (id == LOADER_NOTES) {
            final String[] noteColumns = {
                    Notes._ID,
                    Notes.COLUMN_NOTE_TITLE,
                    Notes.COLUMN_COURSE_TITLE};
            final String noteOrderBy = Notes.COLUMN_COURSE_TITLE + ", " + Notes.COLUMN_NOTE_TITLE;

            loader = new CursorLoader(this, Notes.CONTENT_EXPANDED_URI, noteColumns, null, null, noteOrderBy);
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_NOTES) {
            noteAdapter.changeCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LOADER_NOTES) {
            noteAdapter.changeCursor(null);
        }
    }
}
