package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.Priority;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract.TodoNote;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.debug.DebugActivity;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;

    private TodoDbHelper todoDbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        todoDbHelper = new TodoDbHelper(this);
        db = todoDbHelper.getWritableDatabase();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
            }
        });
        recyclerView.setAdapter(notesAdapter);

        notesAdapter.refresh(loadNotesFromDatabase());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
            db = null;
        }
        if (todoDbHelper != null) {
            todoDbHelper.close();
            todoDbHelper = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private List<Note> loadNotesFromDatabase() {

        // TODO 从数据库中查询数据，并转换成 JavaBeans

        List<Note> listNote = new LinkedList<>();
        if (db == null) {
            Log.d(TAG, "loadNotesFromDatabase: db == null");
        }
        if (db != null) {
            Cursor cursor = null;
            try {
                cursor = db.query(TodoNote.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        TodoNote.COLUMN_DATE + " DESC");
                while (cursor.moveToNext()) {
                    Log.d(TAG, "loadNotesFromDatabase: Next");
                    long id = cursor.getLong(cursor.getColumnIndex(TodoNote._ID));
                    Log.d(TAG, "loadNotesFromDatabase: ID");
                    long longDate = cursor.getLong(cursor.getColumnIndex(TodoNote.COLUMN_DATE));
                    Log.d(TAG, "loadNotesFromDatabase: Date");
                    int intState = cursor.getInt(cursor.getColumnIndex(TodoNote.COLUMN_STATE));
                    Log.d(TAG, "loadNotesFromDatabase: State");
                    String content = cursor.getString(cursor.getColumnIndex(TodoNote.COLUMN_CONTENT));
                    Log.d(TAG, "loadNotesFromDatabase: Content");
                    //int intPriority = cursor.getInt(cursor.getColumnIndex(TodoNote.COLUMN_PRIORITY));
                    //Log.d(TAG, "loadNotesFromDatabase: Priority");

                    Note note = new Note(id);
                    note.setContent(content);
                    note.setDate(new Date(longDate));
                    note.setState(State.from(intState));
                    //note.setPriority(Priority.from(intPriority));

                    listNote.add(note);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return listNote;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据

        if (db != null) {
            int raw = db.delete(TodoNote.TABLE_NAME,
                    TodoNote._ID + " =? ",
                    new String[]{String.valueOf(note.id)});
            if (raw > 0) {
                notesAdapter.refresh(loadNotesFromDatabase());
            }
        }
    }

    private void updateNode(Note note) {
        // 更新数据

        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(TodoNote.COLUMN_STATE, note.getState().intValue);

            int rows = db.update(TodoNote.TABLE_NAME,
                    values,
                    TodoNote._ID + " =? ",
                    new String[]{String.valueOf(note.id)});
            if (rows > 0) {
                notesAdapter.refresh(loadNotesFromDatabase());
            }
        }
    }

}
