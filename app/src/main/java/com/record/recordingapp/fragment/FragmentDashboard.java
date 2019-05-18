package com.record.recordingapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.record.recordingapp.AccountActivity;
import com.record.recordingapp.R;
import com.record.recordingapp.adapter.MyItemRecyclerViewAdapter;
import com.record.recordingapp.dummy.*;
import com.record.recordingapp.util.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FragmentDashboard extends Fragment {
    private TextView account_info;

    public static FragmentDashboard newInstance() {
        FragmentDashboard fragment = new FragmentDashboard();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        account_info = (TextView) view.findViewById(R.id.account_info);

        if (Constant.userData != null) {
            try {
                account_info.setText("Account: "+Constant.userData.getString("number"));
            } catch (JSONException e) {
                Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext(),LinearLayoutManager.VERTICAL,false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(mLayoutManager);

        // Add some sample items.
        DummyContent.ITEMS.clear();
        if (Constant.userData != null) {
            try {
                JSONArray messageArray = Constant.userData.getJSONArray("messages");
                for (int i=0;i<messageArray.length();i++) {
                    JSONObject message = messageArray.getJSONObject(i);
                    DummyContent.ITEMS.add(
                            new DummyContent.DummyItem(message.getInt("Id"), message.getString("Sender"), message.getString("MessageText"), message.getString("MessageUrl")));
                    DummyContent.objects.add(message);
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        }

        recyclerView.setAdapter(new MyItemRecyclerViewAdapter(getActivity(), DummyContent.ITEMS));

        LinearLayout ll_account = (LinearLayout) view.findViewById(R.id.ll_account);
        ll_account.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AccountActivity.class));
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
