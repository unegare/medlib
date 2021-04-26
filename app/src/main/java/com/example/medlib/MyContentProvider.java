package com.example.medlib;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MyContentProvider extends ContentProvider {
    VisitDB vdb;

    @Override
    public boolean onCreate() {
        Log.i(MyContentProvider.class.getName(), "onCreate");
        vdb = new VisitDB(getContext());
        vdb.open();
        return true;
    }

    public Cursor query(Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal) {
        Log.i(MyContentProvider.class.getName(), "query 1");
        return null;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.i(MyContentProvider.class.getName(), "query 2");
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        Log.i(MyContentProvider.class.getName(), "insert");
        return null;
    }

    public int update(Uri uri, ContentValues values, Bundle extras) {
        Log.i(MyContentProvider.class.getName(), "update");
        return 0;
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.i(MyContentProvider.class.getName(), "update");
        return 0;
    }

    public int delete(Uri uri, Bundle extras) {
        Log.i(MyContentProvider.class.getName(), "delete");
        return 0;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.i(MyContentProvider.class.getName(), "delete");
        return 0;
    }

    public String getType(Uri uri) {
        Log.i(MyContentProvider.class.getName(), "getType(" + uri.toString() + ")");
        return vdb.getDataTypeByID(Integer.parseInt(uri.getLastPathSegment()));
    }

    public ParcelFileDescriptor openFile(Uri uri, String mode, CancellationSignal signal) {
        Log.i(MyContentProvider.class.getName(), "openFile(" + uri.toString() + ") 1");
        return null;
    }

    public ParcelFileDescriptor openFile(Uri uri, String mode) {
        Log.i(MyContentProvider.class.getName(), "openFile(" + uri.toString() + ") 2");
        try {
            byte[] bytearr = vdb.getDataByID(Integer.parseInt(uri.getLastPathSegment()));
            ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
            ParcelFileDescriptor.AutoCloseOutputStream outputStream = new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1]);
            outputStream.write(bytearr);
            outputStream.flush();
            outputStream.close();
            return pipe[0];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
