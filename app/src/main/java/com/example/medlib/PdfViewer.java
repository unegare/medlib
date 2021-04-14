package com.example.medlib;

import android.app.AppComponentFactory;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.FileNotFoundException;

public class PdfViewer extends AppCompatActivity {
    PDFView pdfView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdf_viewer_main);

        Intent intent = getIntent();
        Uri uri = Uri.parse(intent.getStringExtra("uri"));
        Log.i("PdfViewer:onCreate", "uri = \"" + uri.toString() + "\"");

        pdfView = findViewById(R.id.pdfView);
        try {
            pdfView.fromStream(getContentResolver().openInputStream(uri)).load();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
