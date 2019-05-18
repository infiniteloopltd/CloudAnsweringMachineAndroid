package com.record.recordingapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.record.recordingapp.dummy.DummyContent;
import com.record.recordingapp.util.Constant;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private String phoneNumber = "";
    private String messageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_detail));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        LinearLayout detail_call = (LinearLayout) findViewById(R.id.detail_call);
        LinearLayout detail_message = (LinearLayout) findViewById(R.id.detail_message);
        LinearLayout detail_callback = (LinearLayout) findViewById(R.id.detail_callback);
        LinearLayout detail_voicemail = (LinearLayout) findViewById(R.id.detail_voicemail);

        int index = getIntent().getExtras().getInt("index");
        if (index >= 0) {
            try {
                JSONObject object = DummyContent.objects.get(index);
                phoneNumber = object.getString("Sender");
                messageUrl = object.getString("MessageUrl");
                if (messageUrl.equalsIgnoreCase("")) {   // message
                    detail_message.setVisibility(View.VISIBLE);
                    detail_call.setVisibility(View.GONE);

                    TextView message_phone_0 = (TextView) findViewById(R.id.message_phone_0);
                    TextView message_content = (TextView) findViewById(R.id.message_content);
                    TextView message_time = (TextView) findViewById(R.id.message_time);
                    message_phone_0.setText(phoneNumber);
                    message_content.setText(object.getString("MessageText"));
                    message_time.setText(getTime(object.getString("DateArrived")));
                    detail_voicemail.setVisibility(View.GONE);
                } else { // call
                    detail_message.setVisibility(View.GONE);
                    detail_call.setVisibility(View.VISIBLE);

                    TextView call_phone_0 = (TextView) findViewById(R.id.call_phone_0);
                    TextView call_location = (TextView) findViewById(R.id.call_location);
                    TextView call_time = (TextView) findViewById(R.id.call_time);
                    TextView call_message_content = (TextView) findViewById(R.id.call_message_content);
                    call_phone_0.setText(phoneNumber);
                    call_location.setText("Location: " + object.getString("MessageText"));
                    call_time.setText(getTime(object.getString("DateArrived")));
                    call_message_content.setText("");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        detail_callback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneNumber != null && !phoneNumber.equals("")) {
                    onCall();
                } else {
                    Toast.makeText(DetailActivity.this, "Phone Number is not correct.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        detail_voicemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MediaPlayer player = new MediaPlayer();
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.setDataSource(messageUrl);
                    player.prepare();
                    player.start();
                } catch (Exception e) {
                    Toast.makeText(DetailActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
                //onRecord();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private String getTime(String strTime) {
        String strLongTime = strTime.substring(strTime.indexOf("(") + 1, strTime.indexOf(")"));
        long longTime = Long.parseLong(strLongTime);
        Date date = new Date(longTime);
        return (date.getMonth() + 1) + "/" + date.getDate() + "/" + (1900 + date.getYear()) + " " + date.getHours() + ":" + date.getMinutes();
    }

    public void onRecord() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},2);
        } else {
            final MediaRecorder rec;
            String file_path=getApplicationContext().getFilesDir().getPath();
            File file= new File(file_path);

            rec=new MediaRecorder();

            rec.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            rec.setAudioChannels(1);
            rec.setAudioSamplingRate(8000);
            rec.setAudioEncodingBitRate(44100);
            rec.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            rec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            if (!file.exists()){
                file.mkdirs();
            }

            rec.setOutputFile(file_path + "/" + Constant.fileName);

            try {
                rec.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(DetailActivity.this,"Sorry! file creation failed!"+e.getMessage(),Toast.LENGTH_SHORT).show();
                return;
            }

            final ProgressDialog mProgressDialog = new ProgressDialog(DetailActivity.this);
            mProgressDialog.setTitle("recording");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setButton("Stop recording", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    mProgressDialog.dismiss();
                    rec.stop();
                    rec.release();

//                    try {
//                        UploadTask uploadTask = new UploadTask(Constant.userData.getString("email"), Constant.userData.getString("password"));
//                        uploadTask.execute();
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                }
            });

            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
                public void onCancel(DialogInterface p1) {
                    rec.stop();
                    rec.release();
                }
            });
            rec.start();
            mProgressDialog.show();
        }
    }

    public void onCall() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE},1);
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    onCall();
                } else {
                    Log.d("TAG", "Call Permission Not Granted");
                }
                break;
            case 2:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    onRecord();
                } else {
                    Log.d("TAG", "Call Permission Not Granted");
                }
                break;
            default:
                break;
        }
    }
}
