package com.proxima.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.SaveCallback;
import com.proxima.R;
import com.proxima.utils.PhotoUtils;

//
// Created by Andrew Clissold, Rachel Glomski, Jon Wong on 10/16/14.
// Upload Activity for the app, displays a preview of a taken photo, asks user to add comment,
// and then allows user to post to Parse or cancel the post.
//
// Recent Version: 11/25/14
public class ConfirmationActivity extends ActionBarActivity {

    // variables for UI interface
    Button mPostButton;
    Button mCancelButton;
    ImageView mImageView;
    EditText mDescriptionEditText;

    private final String TAG = getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set UI
        setContentView(R.layout.activity_confirmation);

        mPostButton = (Button) findViewById(R.id.button_post);
        mCancelButton = (Button) findViewById(R.id.button_cancel);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_text);
        mImageView = (ImageView) findViewById(R.id.imageView);

        // get extras containing photo data and rotate flag
        Bundle extras = getIntent().getExtras();
        byte[] photo = extras.getByteArray("data");
        Boolean rotateFlag = extras.getBoolean("rotateFlag");
        Boolean selfFlag = extras.getBoolean("selfFlag");

        // convert photo data to bitmap rotate it and display it
        Bitmap previewPhoto = BitmapFactory.decodeByteArray(photo, 0, photo.length);
        previewPhoto = PhotoUtils.getInstance().rotatePreview(previewPhoto, rotateFlag, selfFlag);
        mImageView.setImageBitmap(previewPhoto);

        // click listener for cancel button, exits the confirmation activity
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // click listener for the post button, posts the photo to Parse
        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPostButton.setEnabled(false);
                // Get the entered description.
                String description = mDescriptionEditText.getText().toString();

                // Get the photo components sent over in extras.
                Bundle extras = getIntent().getExtras();
                Location location = (Location) extras.get("location");
                Boolean rotateFlag = extras.getBoolean("rotateFlag");
                Boolean selfFlag = extras.getBoolean("selfFlag");
                byte[] data = extras.getByteArray("data");
                ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

                // Upload the photo
                PhotoUtils.getInstance().uploadPhoto(data, geoPoint, description, rotateFlag, selfFlag,
                        new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    // Upload succeeded; dismiss activity.
                                    PhotoUtils.getInstance().downloadUserPhotos();
                                    finish();
                                } else {
                                    // Error occurred; display it and don't dismiss.
                                    CharSequence message = getString(R.string.error_photo_upload_failed);
                                    Context context = getApplicationContext();
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                                    Log.e(TAG, message.toString());
                                }
                            }
                        });
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.confirmation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
