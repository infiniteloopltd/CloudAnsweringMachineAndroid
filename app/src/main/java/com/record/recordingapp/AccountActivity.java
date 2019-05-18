package com.record.recordingapp;

import android.Manifest;
import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class AccountActivity extends AppCompatActivity {
    private int mActive = 0;
    private MediaRecorder recorder = null;
    private String fileName = "record.3gp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_account));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView tv_phone = (TextView) findViewById(R.id.tv_phone);
        TextView tv_email = (TextView) findViewById(R.id.tv_email);
        LinearLayout ll_personalise = (LinearLayout) findViewById(R.id.ll_personalise);
        LinearLayout ll_greeting = (LinearLayout) findViewById(R.id.ll_greeting);
        final TextView tv_active = (TextView) findViewById(R.id.tv_active);
        final ImageView iv_active = (ImageView) findViewById(R.id.iv_active);
        iv_active.setImageResource(R.drawable.switch_on);

        try {
            tv_phone.setText(Constant.userData.getString("number"));
            tv_email.setText("Email: " + Constant.userData.getString("email"));
            tv_active.setText("Active: true");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ll_personalise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord();
            }
        });

        ll_greeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String file_path = getApplicationContext().getFilesDir().getPath();

                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(file_path + "/" + fileName);
                    mediaPlayer.prepare(); // must call prepare first
                    mediaPlayer.start(); // then start
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        iv_active.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActive % 2 == 0) {
                    iv_active.setImageResource(R.drawable.switch_off);
                    tv_active.setText("Active: false");
                } else {
                    iv_active.setImageResource(R.drawable.switch_on);
                    tv_active.setText("Active: true");
                }
                mActive++;
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onRecord() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 2);
        } else {
            try {
//                if (new File(getFilename()).exists())
//                    new File(getFilename()).delete();
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setOutputFile(getFilename());
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

                try {
                    recorder.prepare();
                } catch (IOException e) {
                }

                recorder.start();

                final ProgressDialog mProgressDialog = new ProgressDialog(AccountActivity.this);
                mProgressDialog.setTitle("recording");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setButton("Stop recording", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mProgressDialog.dismiss();
                        stopRecording();
                    }
                });

                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface p1) {
                        stopRecording();
                    }
                });
                mProgressDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecording() {
        UploadTask uploadTask = null;
        try {
            uploadTask = new UploadTask(Constant.userData.getString("email"), Constant.userData.getString("password"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        uploadTask.execute();
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
                recorder = null;
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
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

    private String getFilename() {
        String file_path=getApplicationContext().getFilesDir().getPath();
        File file= new File(file_path);
        if (!file.exists()){
            file.mkdirs();
        }

        return file_path + "/" + fileName;
    }

    public class UploadTask extends AsyncTask<Void, Void, String> {
        private final String mEmail;
        private final String mPassword;

        UploadTask(String email, String password) {
            this.mEmail = email;
            this.mPassword = password;
        }

        @Override
        protected String doInBackground(Void... params) {
            String url = "http://www.cloudansweringmachine.com/ajax/UploadAndroid.aspx";
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url);

                Thread.sleep(1000);

                String file_path=getApplicationContext().getFilesDir().getPath();
                FileInputStream fis = new FileInputStream(file_path + "/" + fileName);
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                String base64 = Base64.encodeToString(buffer, Base64.DEFAULT);

                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("Email", this.mEmail));
                    nameValuePairs.add(new BasicNameValuePair("Password", this.mPassword));
                    nameValuePairs.add(new BasicNameValuePair("filedata", (base64)));
                    nameValuePairs.add(new BasicNameValuePair("fileformat", "3gp"));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));


                    // Execute HTTP Post Request
                    HttpResponse response = httpclient.execute(httppost);
                    StatusLine statusLine = response.getStatusLine();
                    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                        String result = EntityUtils.toString(response.getEntity());
                        return result;
                    } else {
                        response.getEntity().getContent().close();
                        throw new IOException(statusLine.getReasonPhrase());
                    }
                } catch (ClientProtocolException e) {
                    return "";
                } catch (IOException e) {
                    return "";
                }
            } catch (Exception e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(final String result) {
            if (result.equals("") || result.equalsIgnoreCase("fail")) {
                Toast.makeText(AccountActivity.this, "Register Fail", Toast.LENGTH_SHORT).show();
            } else {
//                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(result));
//                startActivity(browserIntent
                Toast.makeText(AccountActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
            }
        }
    }
}