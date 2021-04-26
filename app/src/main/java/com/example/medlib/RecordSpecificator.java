package com.example.medlib;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.SimpleFormatter;

public class RecordSpecificator extends AppCompatActivity {
    Button attach_file_btn;
    Button save_btn;
    Spinner profile_sp;
    EditText docname_ed;
    EditText date_ed;
    TextView selected_uri_tv;

    Bundle inExtras;

    enum MyIntentCodes {
        SEL_DOC
    };

    public static final String KEY_DATE = "date";
    public static final String KEY_DOCNAME = "docname";
    public static final String KEY_PROFILE_ID = "profile_id";
    public static final String KEY_ATTACHED_FILE_URI = "attached_file_uri";

    public final static MyIntentCodes[] myIntentCodes = MyIntentCodes.values();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_specificator_main);

        Intent inIntent = getIntent();
        Bundle inExtras = inIntent.getExtras();

        attach_file_btn = findViewById(R.id.attach_file_btn);
        attach_file_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                startActivityForResult(intent, MyIntentCodes.SEL_DOC.ordinal());
            }
        });

        docname_ed = findViewById(R.id.docname_ed);
        date_ed = findViewById(R.id.appointement_date);
        profile_sp = findViewById(R.id.profile_sp);

        if (inExtras != null) {
            List<String> itemList = new ArrayList<String>();
            for (String key : inExtras.keySet()) {
                itemList.add(inExtras.getString(key));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            profile_sp.setAdapter(adapter);
        }

        selected_uri_tv = findViewById(R.id.selected_uri_tv);
        save_btn = findViewById(R.id.save_btn);
        save_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String docname = docname_ed.getText().toString();
                if (docname == null || docname.trim().equals("")) {
                    Toast t = Toast.makeText(getApplicationContext(), "enter doctor name", Toast.LENGTH_LONG);
                    t.show();
                    return;
                }
                Date date;
                try {
                    date = (new SimpleDateFormat("dd.MM.yyyy")).parse(date_ed.getText().toString().trim());
                } catch (ParseException e) {
                    Toast t = Toast.makeText(getApplicationContext(), "date format: dd.MM.yyyy", Toast.LENGTH_LONG);
                    t.show();
                    e.printStackTrace();
                    return;
                }
                String s_profile_id = null;
                for (String key : inExtras.keySet()) {
                    if (inExtras.getString(key).equals(profile_sp.getSelectedItem().toString())) {
                        s_profile_id = key;
                        break;
                    }
                }
                if (s_profile_id == null) {
                    Toast t = Toast.makeText(getApplicationContext(), "choose profile", Toast.LENGTH_LONG);
                    t.show();
                    return;
                }
                if (selected_uri_tv.getText().toString().trim().equals("")) {
                    Toast t = Toast.makeText(getApplicationContext(), "select a file to attach", Toast.LENGTH_LONG);
                    t.show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(KEY_DOCNAME, docname);
                intent.putExtra(KEY_DATE, date.getTime());
                intent.putExtra(KEY_PROFILE_ID, s_profile_id);
                intent.putExtra(KEY_ATTACHED_FILE_URI, selected_uri_tv.getText().toString().trim());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(myIntentCodes[requestCode]) {
            case SEL_DOC: {
                if (data != null) {
                    selected_uri_tv.setText(data.getDataString());
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
