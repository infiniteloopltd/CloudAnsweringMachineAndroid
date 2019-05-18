package com.record.recordingapp.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.record.recordingapp.R;
import com.record.recordingapp.dummy.Phones;
import com.record.recordingapp.dummy.Country;
import com.record.recordingapp.util.Constant;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FragmentRegister extends Fragment {
  private AutoCompleteTextView email;
  private EditText password;
  private Spinner country;
  private Spinner phone;
  private Button sign_up;
  private CountryTask mCountryTask = null;
  private AvaiablePhoneTask mPhoneNumberTask = null;
  private RegisterTask mRegisterTask = null;
  private int nUSSelected = -1;

  public static FragmentRegister newInstance() {
    FragmentRegister fragment = new FragmentRegister();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_register, container, false);
    email = (AutoCompleteTextView) view.findViewById(R.id.email);
    password = (EditText) view.findViewById(R.id.password);
    country = (Spinner) view.findViewById(R.id.country);
    phone = (Spinner) view.findViewById(R.id.phone);
    sign_up = (Button) view.findViewById(R.id.sign_up);

    mCountryTask = new CountryTask();
    mCountryTask.execute();

    country.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mPhoneNumberTask = new AvaiablePhoneTask(Constant.countries.get(position).iso);
        mPhoneNumberTask.execute();
      }
      @Override
      public void onNothingSelected(AdapterView<?> parent) {}
    });

    sign_up.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (email.getText().toString().equals("")) {
          Toast.makeText(getActivity(), "Please Enter Email.", Toast.LENGTH_SHORT).show();
        } else if (password.getText().toString().equals("")) {
          Toast.makeText(getActivity(), "Please Enter Password.", Toast.LENGTH_SHORT).show();
        } else if (country.getSelectedItemPosition() == -1) {
          Toast.makeText(getActivity(), "Please Select Country.", Toast.LENGTH_SHORT).show();
        } else if (phone.getSelectedItemPosition() == -1) {
          Toast.makeText(getActivity(), "Please Select Phone Number.", Toast.LENGTH_SHORT).show();
        } else {
          mRegisterTask = new RegisterTask(email.getText().toString(), password.getText().toString(), Constant.countries.get(country.getSelectedItemPosition()).iso,
                  Constant.phones.get(phone.getSelectedItemPosition()).name);
          mRegisterTask.execute();
        }
      }
    });
    return view;
  }

  public class RegisterTask extends AsyncTask<Void, Void, String> {
    private final String mEmail;
    private final String mPassword;
    private final String country_iso;
    private final String phone;

    RegisterTask(String email, String password, String country_iso, String phone) {
      this.mEmail = email;
      this.mPassword = password;
      this.country_iso = country_iso;
      this.phone = phone;
    }

    @Override
    protected String doInBackground(Void... params) {
      String url = "http://www.cloudansweringmachine.com/ajax/Register.aspx";
      try {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        try {
          List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
          nameValuePairs.add(new BasicNameValuePair("Email", this.mEmail));
          nameValuePairs.add(new BasicNameValuePair("Password", this.mPassword));
          nameValuePairs.add(new BasicNameValuePair("Country", this.country_iso));
          nameValuePairs.add(new BasicNameValuePair("Number", this.phone));
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
      if (result.equals("") || result.equalsIgnoreCase("fail")) {
        Toast.makeText(getActivity(), "Register Fail", Toast.LENGTH_SHORT).show();
      } else {
        try {
          Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(result));
          startActivity(browserIntent);
        } catch (Exception e) {
          if (getActivity() != null)
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        }
      }
    }
  }

  public class AvaiablePhoneTask extends AsyncTask<Void, Void, String> {
    private final String iso;

    AvaiablePhoneTask(String iso) {
      this.iso = iso;
    }

    @Override
    protected String doInBackground(Void... params) {
      String url = "http://www.cloudansweringmachine.com/ajax/available.aspx?Country=" + this.iso;
      try {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;

        response = httpclient.execute(new HttpGet(url));
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() == HttpStatus.SC_OK){
          String result = EntityUtils.toString(response.getEntity());
          return result;
        } else {
          response.getEntity().getContent().close();
          throw new IOException(statusLine.getReasonPhrase());
        }
      } catch (Exception e) {
        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        return "fail";
      }
    }

    @Override
    protected void onPostExecute(final String result) {
      if (result.equalsIgnoreCase("fail")) {
        Toast.makeText(getActivity(), "Getting Phone Number fail", Toast.LENGTH_SHORT).show();
      } else if (result.equals("")) {
        Toast.makeText(getActivity(), "no Available Phone Number", Toast.LENGTH_SHORT).show();
      } else {
        try {
          JSONObject object = new JSONObject(result);
          JSONArray phoneArr = object.getJSONArray("available_phone_numbers");
          ArrayList<String> phones = new ArrayList<String>();
          Constant.phones.clear();
          for (int i = 0;i < phoneArr.length();i++) {
            JSONObject phone = phoneArr.getJSONObject(i);
            Constant.phones.add(new Phones(i, phone.getString("phone_number")));
            phones.add(phone.getString("phone_number"));
          }

          ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,phones); //selected item will look like a spinner set from XML
          spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          phone.setAdapter(spinnerArrayAdapter);
        } catch (Exception e) {
          if (getActivity() != null)
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        }
      }
    }
  }


  public class CountryTask extends AsyncTask<Void, Void, String> {

    @Override
    protected String doInBackground(Void... params) {
      String url = "http://www.cloudansweringmachine.com/ajax/countries.aspx";
      try {
        HttpClient httpclient = new DefaultHttpClient();

        HttpResponse response;
//
        response = httpclient.execute(new HttpGet(url));
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() == HttpStatus.SC_OK){
          String result = EntityUtils.toString(response.getEntity());
          return result;
        } else {
          response.getEntity().getContent().close();
          throw new IOException(statusLine.getReasonPhrase());
        }
      } catch (Exception e) {
        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        return "";
      }
    }

    @Override
    protected void onPostExecute(final String result) {
      if (result.equals("") || result.equalsIgnoreCase("fail")) {
        Toast.makeText(getActivity(), "Getting Country List Fail", Toast.LENGTH_SHORT).show();
      } else {
        try {
          JSONObject object = new JSONObject(result);
          JSONArray countryArr = object.getJSONArray("countries");
          ArrayList<String> countres = new ArrayList<String>();
          for (int i = 0;i < countryArr.length();i++) {
            JSONObject country = countryArr.getJSONObject(i);
            Constant.countries.add(new Country(i, country.getString("url"), country.getString("country"), country.getString("iso_country")));
            countres.add(country.getString("country"));
            if (country.getString("iso_country").equals("US")) {
              nUSSelected = i;
            }
          }

          ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,countres); //selected item will look like a spinner set from XML
          spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          country.setAdapter(spinnerArrayAdapter);

          country.setSelection(nUSSelected);
          mPhoneNumberTask = new AvaiablePhoneTask(Constant.countries.get(nUSSelected).iso);
          mPhoneNumberTask.execute();
        } catch (Exception e) {
          if (getActivity() != null)
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        }
      }
    }
  }
}