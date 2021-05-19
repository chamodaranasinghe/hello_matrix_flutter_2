package com.hello.hello_matrix_flutter.src.directory;

import android.content.Context;

import androidx.room.Room;

import com.hello.hello_matrix_flutter.src.auth.SessionHolder;

public class DatabaseClient {

    private Context mCtx;
    private static DatabaseClient mInstance;


    private final DirectoryDatabase appDatabase;
    private DatabaseClient(Context mCtx) {
        this.mCtx = mCtx;

        //creating the app database with Room database builder
        appDatabase = Room.databaseBuilder(mCtx, DirectoryDatabase.class, "hello-directory").allowMainThreadQueries().build();
    }

    public static synchronized DatabaseClient getInstance() {
        if (mInstance == null) {
            mInstance = new DatabaseClient(SessionHolder.INSTANCE.getAppContext());
        }
        return mInstance;
    }

    public DirectoryDatabase getAppDatabase() { return appDatabase; }

}