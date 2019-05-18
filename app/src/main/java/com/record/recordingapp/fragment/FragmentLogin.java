package com.record.recordingapp.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.record.recordingapp.MainActivity;
import com.record.recordingapp.R;
import com.record.recordingapp.util.Constant;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FragmentLogin extends Fragment {

    private UserLoginTask mAuthTask = null;
    private AutoCompleteTextView mEmail;
    private EditText mPassword;

    public static FragmentLogin newInstance() {
        FragmentLogin fragment = new FragmentLogin();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (!Constant.userEmail.equals("") && !Constant.userPassword.equals("")) {
            mAuthTask = new UserLoginTask(Constant.userEmail, Constant.userPassword);
            mAuthTask.execute((Void) null);
        } else {
            View view = inflater.inflate(R.layout.fragment_login, container, false);
            mEmail = (AutoCompleteTextView) view.findViewById(R.id.email);
            mPassword = (EditText) view.findViewById(R.id.password);
            view.findViewById(R.id.email_sign_in_button).setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (mEmail.getText().toString().equals("")) {
                        Toast.makeText(getActivity(),"Enter Email", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (mPassword.getText().toString().equals("")){
                        Toast.makeText(getActivity(),"Enter Password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mAuthTask = new UserLoginTask(mEmail.getText().toString(), mPassword.getText().toString());
                    mAuthTask.execute((Void) null);
                }
            });
            return view;
        }
        return null;
    }

    public class UserLoginTask extends AsyncTask<Void, Void, String> {
        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected String doInBackground(Void... params) {
            String url = "http://www.cloudansweringmachine.com/ajax/LoginApp.aspx";
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url);

                try {
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("email", this.mEmail));
                    nameValuePairs.add(new BasicNameValuePair("password", this.mPassword));
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
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    return "";
                } catch (IOException e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    return "";
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                return "";
            }
        }

        @Override
        protected void onPostExecute(final String result) {
            mAuthTask = null;
            if (result.equals("") || result.equalsIgnoreCase("fail")) {
                Toast.makeText(getActivity(), "Login Fail", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONObject object = new JSONObject(result);
                    String userName = object.getString("id");
                    if (!userName.equalsIgnoreCase("")) {
                        Constant.userEmail = object.getString("email");
                        Constant.userPassword = object.getString("password");

                        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
                        Menu menuNav = navigationView.getMenu();
                        MenuItem dashboardItem = menuNav.findItem(R.id.nav_dashboard);
                        dashboardItem.setVisible(true);

                        View navHeaderLayout = (View) navigationView.getHeaderView(0);
                        ((TextView)navHeaderLayout.findViewById(R.id.email)).setText(object.getString("email"));

                        Constant.userData = object;

                        ((MainActivity)getActivity()).getSupportActionBar().setTitle("Dashboard");
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_layout, FragmentDashboard.newInstance());
                        transaction.commit();
                    }
                } catch (Exception e) {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}