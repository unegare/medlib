package com.example.medlib;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.shapes.Shape;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class MainActivity extends AppCompatActivity {
    ArrayList<DoctorItem> doctors;
    ArrayList<DoctorItem> docDates;
    VisitDB vdb;
    DoctorAdapter docAd;
    Button btn;
    Button btn2;
    String s_uri_to_open;
    String s_uri_datatype;
    ExtendedFloatingActionButton add_fbtn;
    ExtendedFloatingActionButton gen_new_db_btn;
    ExtendedFloatingActionButton add_new_record_btn;
    ExtendedFloatingActionButton add_new_profile_btn;
    List<ExtendedFloatingActionButton> btn_list;
    boolean FABsShown;

    Toolbar toolbar;

    int menuLvl;

    enum MyIntentResults {
        SELECT_DOC,
        OPEN_DOC,
        OPEN_CONTENT_URL,
        ADD_NEW_RECORD,
    };
    final static MyIntentResults[] MIR_VALUES = MyIntentResults.values();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/pdf");
                startActivityForResult(intent, MyIntentResults.SELECT_DOC.ordinal());
            }
        });
        btn.setVisibility(View.INVISIBLE);

        btn2 = (Button) findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("content://com.example.medlib.MyContentProvider/something"), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivityForResult(intent, MyIntentResults.OPEN_CONTENT_URL.ordinal());
            }
        });
        btn2.setVisibility(View.INVISIBLE);

        add_fbtn = findViewById(R.id.add_fbtn);
        add_fbtn.shrink();
        add_fbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FABsShown) hideFABs();
                else showFABs();
            }
        });

        add_new_record_btn = findViewById(R.id.add_new_record_btn);
        add_new_record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideFABs();
                Intent intent = new Intent(MainActivity.this, RecordSpecificator.class);
                final Cursor allProfiles = vdb.getAllProfiles();
                if (allProfiles.moveToFirst()) {
                    do {
                        intent.putExtra(
                                Integer.toString(allProfiles.getInt(allProfiles.getColumnIndex(VisitDB.VisitDBHelper.COLUMN_PROFILE_ID))),
                                allProfiles.getString(allProfiles.getColumnIndex(VisitDB.VisitDBHelper.COLUMN_PROFILE_NAME)));
                    } while(allProfiles.moveToNext());
                }
                startActivityForResult(intent, MyIntentResults.ADD_NEW_RECORD.ordinal());
            }
        });

        add_new_profile_btn = findViewById(R.id.add_new_profile_btn);
        add_new_profile_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                hideFABs();
                final EditText ed = new EditText(MainActivity.this);
                DialogInterface.OnClickListener ad_onclick = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        switch(id) {
                            case Dialog.BUTTON_POSITIVE:
                                if (ed.getText().toString().trim().equals("")) {
                                    Toast t = Toast.makeText(getApplicationContext(), "entered profile name is empty", Toast.LENGTH_LONG);
                                    t.show();
                                } else {
                                    vdb.addProfile(ed.getText().toString().trim());
                                }
                                break;
                            case Dialog.BUTTON_NEGATIVE:
                                break;
                            case Dialog.BUTTON_NEUTRAL:
                                break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + id);
                        }
                    }
                };
                AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                adb.setTitle(R.string.ad_enter_profile_name);
                adb.setMessage(R.string.ad_enter_profile_name_message);
                adb.setPositiveButton("OK", ad_onclick);
                adb.setNegativeButton("Cancel", ad_onclick);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                ed.setLayoutParams(lp);
                adb.setView(ed);
                adb.show();
            }
        });

        gen_new_db_btn = findViewById(R.id.gen_new_db_btn);
        gen_new_db_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideFABs();
                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        vdb.genNewDB();
                        menuLvl = 0;
                        renderProfileList();
                    }
                });
                th.start();
            }
        });

        btn_list = Arrays.asList(gen_new_db_btn, add_new_profile_btn, add_new_record_btn);
        FABsShown = true;
        hideFABs();

        RecyclerView rvDoctors = (RecyclerView) findViewById(R.id.listOfDoctors);
        if (doctors == null) {
            doctors = new ArrayList<DoctorItem>(Arrays.asList(new DoctorItem("Empty list 1", -1,
                    j -> {Log.i("MainActivity", "Empty list of doctors: onClick (from onCreate)");},
                    j -> {Log.i("MainActivity", "Empty list of doctors: onLongClick (from onCreate)");})));
        }

        docAd = new DoctorAdapter(doctors);
        rvDoctors.setAdapter(docAd);
        rvDoctors.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast toast = Toast.makeText(getApplicationContext(), "C'est un message pour vous.", Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        getWindow().getDecorView().getRootView().setBackgroundColor(Color.argb(255,0,0,0));

        vdb = new VisitDB(getApplicationContext());
        vdb.open();

        menuLvl = 0;

        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                renderProfileList();
            }
        });
        th.start();
    }

    protected void showToast(String s) {
        Toast toast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG);
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
        showToast("onActivityResult: resultCode = " + resultCode + " | RESULT_OK == " + Activity.RESULT_OK + " | " + MIR_VALUES[requestCode] + " | (data == null) == " + (data == null));
        if (resultCode != Activity.RESULT_OK) {
            if  (MIR_VALUES[requestCode] == MyIntentResults.OPEN_CONTENT_URL) {
                if (s_uri_datatype.equals("application/pdf")) {
                    Intent intent = new Intent(MainActivity.this, PdfViewer.class);
                    intent.putExtra("uri", s_uri_to_open);
                    startActivity(intent);
                }
            }
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
            case ADD_NEW_RECORD: {
                if (data == null) {
                    Log.i("onActivityResult: ADD_NEW_RECORD", "data == null");
                } else {
                    Log.i("onActivityResult: ADD_NEW_RECORD", "data received");
                    Bundle bundle = data.getExtras();
                    String s_profile_id = bundle.getString(RecordSpecificator.KEY_PROFILE_ID);
                    String docname = bundle.getString(RecordSpecificator.KEY_DOCNAME);
                    long date = bundle.getLong(RecordSpecificator.KEY_DATE);
                    String attached_file = bundle.getString(RecordSpecificator.KEY_ATTACHED_FILE_URI);
                    Toast.makeText(getApplicationContext(), "attached_file: " + attached_file, Toast.LENGTH_LONG).show();
                    String datatype = getContentResolver().getType(Uri.parse(attached_file));
                    InputStream is = null;
                    try {
                        is = getContentResolver().openInputStream(Uri.parse(attached_file));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast t = Toast.makeText(getApplicationContext(), "bad content uri", Toast.LENGTH_LONG);
                        t.show();
                        break;
                    }
                    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                    final int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];

                    try {
                        int len = 0;
                        while ((len = is.read(buffer)) != -1) {
                            byteBuffer.write(buffer);
                        }
                    } catch (IOException e) {
                        Toast t = Toast.makeText(getApplicationContext(), "io error", Toast.LENGTH_LONG);
                        t.show();
                        e.printStackTrace();
                        return;
                    }
                    Log.i("onActivityResult ADD_NEW_RECORD", "go to addRecord");
                    Log.i("onActivityResult: ADD_NEW_RECORD", datatype);
                    Log.i("onActivityResult: ADD_NEW_RECORD", "bytearr.length = " + byteBuffer.toByteArray().length);
                    vdb.addRecord(docname, Integer.parseInt(s_profile_id), byteBuffer.toByteArray(), datatype);
                }
                break;
            }
        }
        Log.i(MainActivity.class.getName(), "requestCode: " + requestCode);
    }

    @Override
    public void onBackPressed() {
        Toast t = Toast.makeText(getApplicationContext(), "this is a message from onBackPressed", Toast.LENGTH_SHORT);
        t.show();
        if (menuLvl > 0) {
            Log.i("onBackPressed", "doctors.size() = " + doctors.size());
            docAd.setDoctors(doctors);
            docAd.notifyDataSetChanged();
            menuLvl--;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_toolbar_001) {
            Toast t = Toast.makeText(getApplicationContext(), "toolbar action 001 clicked", Toast.LENGTH_LONG);
            t.show();
        }
        return true;
    }

    public void renderProfileList() {
        final Random rnd = new Random();
        final Cursor cur = vdb.getAllUsedProfileNames();
        final ArrayList<DoctorItem> docs = new ArrayList<DoctorItem>();
        if (cur.moveToFirst()) {
            final IntConsumer lambdaOnClick = j -> { onProfileChosen(j); };
            final IntConsumer lambdaOnLongClick = prof_id -> {
                vdb.deleteByProfileID(prof_id);
                Iterator<DoctorItem> itemIterator = docs.iterator();
                int i = 0;
                while(itemIterator.hasNext()) {
                    DoctorItem docitem = itemIterator.next();
                    if (docitem.getID() == prof_id) {
                        Log.i("lambdaOnLongClick", "i == " + i + " | docitem.getProfileID() == " + docitem.getID() + " | prof_id == " + prof_id);
                        itemIterator.remove();
                        docAd.notifyItemRemoved(i);
                    }
                    i++;
                }
            };
            int i = 0;
            do {
                docs.add(new DoctorItem(
                        cur.getString(cur.getColumnIndex(VisitDB.VisitDBHelper.COLUMN_PROFILE_NAME)),
                        cur.getInt(cur.getColumnIndex(VisitDB.VisitDBHelper.COLUMN_PROFILE_ID)),
                        lambdaOnClick,
                        lambdaOnLongClick
                ));
                i++;
            } while (cur.moveToNext());
        } else {
            docs.add(new DoctorItem("Empty list 2", -1,
                    j -> {Log.i("MainActivity", "Empty list of doctors: onClick");},
                    j -> {Log.i("MainActivity", "Empty list of doctors: onLongClick");}));
        }
        doctors = docs;
        final int numOfProfiles = cur.getCount();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast("numOfProfiles: " + Integer.toString(numOfProfiles));
                docAd.setDoctors(docs);
                docAd.notifyDataSetChanged();
            }
        });
    }

    public void onProfileChosen(int profile_id) {
        Log.i("onProfileChosen", "doctors.size() = " + doctors.size());
        Cursor cur = vdb.getAllDocnamesAndDatesByProfileID(profile_id);
        if (docDates == null) {
            docDates = new ArrayList<DoctorItem>();
        } else {
            docDates.clear();
        }
        if (cur.moveToFirst()) {
            final IntConsumer lambdaOnClick = j -> {Log.i("DoctorList", "date #" + j + " clicked"); onDateChosen(j);};
            final IntConsumer lambdaOnLongClick = rec_id -> {
                Log.i("DoctorList", "date #" + rec_id + " longClicked");
                vdb.deleteByRecordID(rec_id);
                Iterator<DoctorItem> itemIterator = docDates.iterator();
                int i = 0;
                while (itemIterator.hasNext()) {
                    DoctorItem doctorItem = itemIterator.next();
                    if (doctorItem.getID() == rec_id) {
                        itemIterator.remove();
                        docAd.notifyItemRemoved(i);
                    }
                    i++;
                }
            };
            do {
                docDates.add(new DoctorItem(
                        cur.getString(cur.getColumnIndex(VisitDB.VisitDBHelper.COLUMN_DOCNAME)),
                        cur.getInt(cur.getColumnIndex(VisitDB.VisitDBHelper.COLUMN_ID)),
                        lambdaOnClick,
                        lambdaOnLongClick));
            } while(cur.moveToNext());
        } else {
            docDates.add(new DoctorItem("Empty list 3", -1,
                    j -> {Log.i("DoctorList", "Empty");},
                    j -> {Log.i("DoctorList", "Empty onLongClick");}));
        }
        menuLvl++;
        docAd.setDoctors(docDates);
        docAd.notifyDataSetChanged();
    }

    public void onDateChosen(int id) {
        Log.i("onDateChosen", "" + id);
        s_uri_datatype = vdb.getDataTypeByID(id);
        s_uri_to_open = "content://com.example.medlib.MyContentProvider/" + id;
        Log.i("onDateChosen", s_uri_datatype);
//        if (s_uri_datatype.equals("application/pdf")) {
//            Intent intent = new Intent(MainActivity.this, PdfViewer.class);
//            intent.putExtra("uri", s_uri_to_open);
//            startActivity(intent);
//        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(s_uri_to_open), s_uri_datatype);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(intent, MyIntentResults.OPEN_CONTENT_URL.ordinal());
//        }
    }

    public void showFABs() {
        if (!FABsShown) {
            btn_list.forEach(j -> {j.show(); j.animate().alpha(1.0f);});
            FABsShown = true;
            ImageView iv = new ImageView(this);
            

            findViewById(R.id.activity_main_constraint_layout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (FABsShown) hideFABs();
                }
            });
        }
    }

    public void hideFABs() {
        if (FABsShown) {
            ListIterator<ExtendedFloatingActionButton> it = btn_list.listIterator(btn_list.size());
            Consumer<ExtendedFloatingActionButton> consumer = j -> {j.animate().alpha(0.0f); j.hide();};
            while (it.hasPrevious()) {
                consumer.accept(it.previous());
            }
            FABsShown = false;
        }
    }
}