package com.chrissetiana.notekeeper;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import static com.chrissetiana.notekeeper.NoteActivity.LOADER_NOTES;
import static com.chrissetiana.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private NoteRecyclerAdapter noteAdapter;
    private RecyclerView recyclerItems;
    private LinearLayoutManager noteLayoutManager;
    private CourseRecyclerAdapter courseAdapter;
    private GridLayoutManager courseLayoutManager;
    private NoteKeeperDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    @Override
    protected void onResume() {
        super.onResume();

        getLoaderManager().initLoader(LOADER_NOTES, null, this);

        updateNaveHeader();
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

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
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

    private void displayCourses() {
        recyclerItems.setLayoutManager(courseLayoutManager);
        recyclerItems.setAdapter(courseAdapter);
        selectNavigationMenuItem(R.id.nav_courses);
    }

    private void selectNavigationMenuItem(int id) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(id).setChecked(true);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            handleNotes(R.string.nav_send);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void handleShare() {
        View view = findViewById(R.id.list_items);
        final String userFavoriteSocial = PreferenceManager.getDefaultSharedPreferences(this).getString("user_favorite_social", "");
        Snackbar.make(view, "Share to " + userFavoriteSocial, Snackbar.LENGTH_LONG).show();
    }

    private void handleNotes(int messageId) {
        View view = findViewById(R.id.list_items);
        Snackbar.make(view, messageId, Snackbar.LENGTH_LONG).show();
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;

        if (id == LOADER_NOTES) {
            loader = new CursorLoader(this) {
                @Override
                public Cursor loadInBackground() {
                    SQLiteDatabase db = databaseHelper.getReadableDatabase();

                    final String[] noteColumns = {NoteInfoEntry._ID, NoteInfoEntry.COLUMN_NOTE_TITLE, NoteInfoEntry.COLUMN_COURSE_ID};
                    final String noteOrderBy = NoteInfoEntry.COLUMN_COURSE_ID + ", " + NoteInfoEntry.COLUMN_NOTE_TITLE;

                    return db.query(NoteInfoEntry.TABLE_NAME, noteColumns, null, null, null, null, noteOrderBy);
                }
            };
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
