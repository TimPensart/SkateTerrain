package com.example.skateterrain;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class SpotActivity extends AppCompatActivity {

    static Spinner spinner;
    EditText editTextName;
    static String spotName;
    static String _spotType;

    private final int PICK_IMAGE_REQUEST = 71;
    private static Uri filePathUri;

    int takeFlags;

    static Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot);
        spotName = "";
        _spotType = "park";
        spinner = (Spinner) findViewById(R.id.spinner);
        editTextName = (EditText) findViewById(R.id.EditText);
        final ImageView imageViewType = (ImageView) findViewById(R.id.imageView3);


        editTextName.requestFocus();

        bitmap = null;
        filePathUri = null;


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                _spotType = spinner.getSelectedItem().toString();
                imageViewType.setImageResource(getResources().getIdentifier(_spotType,"drawable",getPackageName()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void onTypeButtonClick(View view){
        spinner.performClick();
    }

    public void onCloseButtonClick(View view) {
        MapActivity.newSpotMode = false;
        finish();
    }


    public void onAddSpotButtonClick(View view) {
        spotName = editTextName.getText().toString();
        _spotType = spinner.getSelectedItem().toString();

        if (!spotName.isEmpty() && spotName != null) {
            MapActivity.newSpotMode = true;
            finish();
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(SpotActivity.this).create();
            alertDialog.setTitle("Required");
            alertDialog.setMessage("Please enter a spot name");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    public static String getSpotName() {
        if (spotName != null && !spotName.equals("")) {
            return spotName;
        }
        return "no name";
    }

    public void onClickUpload(View view){
        chooseImage();
    }

    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(Intent.createChooser(intent,"Select picture for spot"),PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePathUri = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePathUri);
                ImageView imageView = (ImageView) findViewById(R.id.imageViewSpot);
                //imageView.setBackgroundResource(0);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setImageURI(filePathUri);
                TextView textViewUpload = (TextView) findViewById(R.id.textViewUpload);
                textViewUpload.setVisibility(View.INVISIBLE);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getFilePath() {
        if (filePathUri != null) {
            return filePathUri.toString();
        }
        return "";
    }

    public static String getSpotType() {
        Log.i("spotType", spinner.getSelectedItem().toString());
        _spotType = spinner.getSelectedItem().toString();

        return _spotType;

    }

}
