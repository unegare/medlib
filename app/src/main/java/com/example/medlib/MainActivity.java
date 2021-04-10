package com.example.medlib;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    ArrayList<DoctorItem> doctors;
    VisitDB vdb;
    Button btn;
    Button btn2;

    enum MyIntentResults {
        SELECT_DOC,
        OPEN_DOC,
        OPEN_CONTENT_URL,
    };
    final static MyIntentResults[] MIR_VALUES = MyIntentResults.values();

//    public static final int SELECT_DOC = 1;
//    public static final int OPEN_DOC = 2;
//    public static final int OPEN_CONTENT_URL = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                    f = File.createTempFile("temp", "txt");
//                    f.deleteOnExit();
//                    FileOutputStream fout = new FileOutputStream(f, false);
//                    OutputStreamWriter osw = new OutputStreamWriter(fout);
//                    osw.append("Hello there!");
//                    osw.close();
//                    fout.flush();
//                    fout.close();
//                } catch (java.io.IOException ex) {
//                    Log.i("BUTTON LISTENER", "ex is caught: " + ex.getMessage());
//                    return;
//                }
//                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//
//                intent.setDataAndType(Uri.fromFile(f), "application/pdf");
//                startActivity(intent);
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/pdf");
                startActivityForResult(intent, MyIntentResults.SELECT_DOC.ordinal());
            }
        });

        btn2 = (Button) findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("content://com.example.medlib.MyContentProvider/something"), "text/plain");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivityForResult(intent, MyIntentResults.OPEN_CONTENT_URL.ordinal());
            }
        });

        RecyclerView rvDoctors = (RecyclerView) findViewById(R.id.listOfDoctors);
        if (doctors == null) {
//            DoctorItem.lastDoctorId = 0;
//            doctors = DoctorItem.createDoctorList(5);
            doctors = new ArrayList<DoctorItem>(Arrays.asList(new DoctorItem("Empty list", -1)));
        }

        DoctorAdapter docAd = new DoctorAdapter(doctors);
        rvDoctors.setAdapter(docAd);
        rvDoctors.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast toast = Toast.makeText(getApplicationContext(), "C'est un message pour vous.", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().getRootView().setBackgroundColor(Color.argb(255,0,0,0));

        vdb = new VisitDB(getApplicationContext());
        vdb.open();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Random rnd = new Random();
                Cursor cur = vdb.getAllUsedProfilesNames();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(Integer.toString(cur.getCount()));
                        if (cur.moveToFirst()) {
                            doctors.clear();
                            do {
                                doctors.add(new DoctorItem(
                                        cur.getString(cur.getColumnIndex(VisitDB.VisitDBHelper.COLUMN_PROFILE_NAME)),
                                        cur.getInt(cur.getColumnIndex(VisitDB.VisitDBHelper.COLUMN_PROFILE_ID))));
//                                rvDoctors.getAdapter().notifyItemInserted(doctors.size() - 1);
                            } while (cur.moveToNext());
                            RecyclerView rvDoctors = (RecyclerView) findViewById(R.id.listOfDoctors);
                            rvDoctors.getAdapter().notifyDataSetChanged();
                        }
                    }
                });
            }
        });
        t.start();
    }

    protected void showToast(String s) {
        Toast toast = Toast.makeText(getApplicationContext(), Integer.toString(vdb.getNumOfRecords()), Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        vdb.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
//            Log.i(Thread.currentThread().getStackTrace()[1].getMethodName(), "resultCode = " + resultCode);
            Log.i("onActivityResult", "resultCode = " + resultCode);
            return;
        }
        switch (MIR_VALUES[requestCode]) {
            case SELECT_DOC: {
                Uri uri = data.getData();
                Toast t = Toast.makeText(getApplicationContext(), uri.getPath(), Toast.LENGTH_LONG);
                t.show();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivityForResult(intent, MyIntentResults.OPEN_DOC.ordinal());
                break;
            }
            case OPEN_DOC: {
                Toast t = Toast.makeText(getApplicationContext(), "Returned from OPEN_DOC", Toast.LENGTH_LONG);
                t.show();
                break;
            }
            case OPEN_CONTENT_URL: {
                Toast t = Toast.makeText(getApplicationContext(), "Returned from OPEN_CONTENT_URL", Toast.LENGTH_LONG);
                t.show();
                break;
            }
        }
        Log.i(MainActivity.class.getName(), "requestCode: " + requestCode);
    }

    @Override
    public void onBackPressed() {
        Toast t = Toast.makeText(getApplicationContext(), "this is a message from onBackPressed", Toast.LENGTH_SHORT);
        t.show();
    }

    public void renderProfileList(Cursor cur) {

    }
}