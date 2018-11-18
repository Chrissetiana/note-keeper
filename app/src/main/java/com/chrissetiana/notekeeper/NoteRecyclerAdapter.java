package com.chrissetiana.notekeeper;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.chrissetiana.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder> {

    private final Context context;
    private final LayoutInflater layoutInflater;
    private Cursor cursor;
    private int coursePos;
    private int courseTitle;
    private int idPos;

    public NoteRecyclerAdapter(Context context, Cursor cursor) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.cursor = cursor;
        populateColumnPositions();
    }

    private void populateColumnPositions() {
        if (cursor == null) {
            return;
        }

        coursePos = cursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        courseTitle = cursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        idPos = cursor.getColumnIndex(NoteInfoEntry._ID);
    }

    private void changeCursor(Cursor c) {
        if (cursor != null) {
            cursor.close();
        }

        cursor = c;
        populateColumnPositions();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = layoutInflater.inflate(R.layout.item_note_list, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursor.moveToPosition(position);

        String course = cursor.getString(coursePos);
        String title = cursor.getString(courseTitle);
        int id = cursor.getInt(idPos);

        holder.textCourse.setText(course);
        holder.textTitle.setText(title);
        holder.id = id;
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView textCourse;
        public final TextView textTitle;
        public int id;

        public ViewHolder(View itemView) {
            super(itemView);

            textCourse = itemView.findViewById(R.id.text_course);
            textTitle = itemView.findViewById(R.id.text_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, NoteActivity.class);
                    intent.putExtra(NoteActivity.NOTE_ID, id);
                    context.startActivity(intent);
                }
            });
        }
    }

}
