package com.blogspot.darwinsapp.distancecalculator;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class MainActivity extends AppCompatActivity implements InputDialog.InputDialogListener{

    private DrawView drawView;
    private FrameLayout preview;
    private Button btn_ok;
    private Button btn_cancel;
    private Button btn_takePicture;
    private File photoFile;
    private double result;

    //static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preview = (FrameLayout) findViewById(R.id.camera_preview);

        btn_takePicture = (Button) findViewById(R.id.button_takePicture);
        btn_ok = (Button) findViewById(R.id.button_calculate);
        btn_cancel = (Button) findViewById(R.id.button_cancel);

        addPictureButton();

        btn_takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new InputDialog().show(getFragmentManager(), "input_dialog");
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.clearCanvas();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
//            case R.id.action_settings:
//                break;
            case R.id.action_cleanStorage:
                cleanPhotoStorage();
                break;
            case R.id.action_choosePhoto:
                dispatchChoosePhotoIntent();
                break;
            case R.id.action_takePicture:
                dispatchTakePictureIntent();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "Error creating image", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/picture.jpg";
                imageFile = new File(imageFilePath); // convert path to Uri
                Uri imageFileUri;
                if(Build.VERSION.SDK_INT>=24){
                   /* try{
                        Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                        m.invoke(null);
                    }catch(Exception e){
                        e.printStackTrace();
                    }*/
                    imageFileUri = FileProvider.getUriForFile(MainActivity.this,"com.blogspot.darwinsapp.distancecalculator.fileprovider",imageFile);

                }
                else {

                    imageFileUri = Uri.fromFile(imageFile);
                }
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageFileUri);

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    File imageFile;

    private void addPictureButton(){
        preview.removeAllViews();

        btn_cancel.setVisibility(View.GONE);
        btn_ok.setVisibility(View.GONE);
        btn_takePicture.setVisibility(View.VISIBLE);
    }

    private void pictureTaken(){
        preview.removeAllViews();

        btn_cancel.setVisibility(View.VISIBLE);
        btn_ok.setVisibility(View.VISIBLE);
        btn_takePicture.setVisibility(View.GONE);

        ImageSurface image = new ImageSurface(this, imageFile);
        preview.addView(image);

        String str = imageFile.getAbsolutePath();
        Bitmap icon = BitmapFactory.decodeFile(str);
        //preview.setBackgroundDrawable(new BitmapDrawable(icon));

        /*try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
            ImageView im=(ImageView)findViewById(R.id.img);
            im.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        ((TextView) findViewById(R.id.info_lbl)).setText(getResources().getString(R.string.setReferencePoints));

        drawView = new DrawView(this,imageFile);
        preview.addView(drawView);
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_SELECT_PHOTO = 2;


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");


        }*/
        switch(requestCode){
            case REQUEST_IMAGE_CAPTURE:
                if(resultCode == RESULT_OK)
                    pictureTaken();
                break;
            case REQUEST_SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    uri = data.getData();
                    String filePath = Utils.getPath(this, uri);
                    assert filePath != null;
                    imageFile = new File(filePath);
                    pictureTaken();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }Uri uri;
    /*private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }*/
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);

        return File.createTempFile(imageFileName,".jpg",storageDir);
    }

    private void cleanPhotoStorage(){
        File storageDir = getExternalFilesDir(null);
        File fList[] = storageDir.listFiles();
        //Search for pictures in the directory
        for(int i = 0; i < fList.length; i++){
            String pes = fList[i].getName();
            if(pes.endsWith(".jpg"))
                new File(fList[i].getAbsolutePath()).delete();
        }
        Toast.makeText(this, getResources().getString(R.string.storageDeleted), Toast.LENGTH_SHORT).show();
    }

    private void dispatchChoosePhotoIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.action_choosePhoto)), REQUEST_SELECT_PHOTO);
    }
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        int inputUnit = ((Spinner)dialog.getDialog().findViewById(R.id.input_unit_chooser)).getSelectedItemPosition();
        int outputUnit = ((Spinner) dialog.getDialog().findViewById(R.id.output_unit_chooser)).getSelectedItemPosition();
        try {
            double reference = Double.parseDouble(((EditText) dialog.getDialog().findViewById(R.id.reference_input)).getText().toString());
            result = drawView.calculate(reference, inputUnit, outputUnit);
            showResult();
        }catch(NumberFormatException ex){
            Toast.makeText(this, getResources().getString(R.string.error_numberFormat), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        //Do absolutely nothing
    }

    private void showResult(){
        if(result != -1) {
            DecimalFormat decimalFormat = new DecimalFormat("#.##");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.result_lbl) + decimalFormat.format(result));
            builder.create().show();
        }
    }
}
