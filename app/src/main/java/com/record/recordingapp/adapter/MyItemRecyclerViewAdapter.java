package com.record.recordingapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.record.recordingapp.DetailActivity;
import com.record.recordingapp.R;
import com.record.recordingapp.dummy.DummyContent.DummyItem;

import java.util.List;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<DummyItem> mValues;
    private Context con = null;

    public MyItemRecyclerViewAdapter(Context context, List<DummyItem> items) {
        con = context;
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        if (!holder.mItem.contentUrl.equals("")) {
            holder.mIcon.setImageResource(R.drawable.phone);
            holder.mTitle.setText("Call from ");
            SpannableString content = new SpannableString(holder.mItem.phone);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            holder.mPhoneNumber.setText(content);
        } else {
            holder.mIcon.setImageResource(R.drawable.message);
            holder.mTitle.setText(holder.mItem.content);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(con, DetailActivity.class);
                intent.putExtra("index", position); // call
                con.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public LinearLayout ll_item;
        public ImageView mIcon;
        public TextView mTitle;
        public TextView mPhoneNumber;
        public DummyItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            ll_item = (LinearLayout) view.findViewById(R.id.ll_item);
            mIcon = (ImageView) view.findViewById(R.id.list_icon);
            mTitle = (TextView) view.findViewById(R.id.list_title);
            mPhoneNumber= (TextView) view.findViewById(R.id.list_phone);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mPhoneNumber.getText() + "'";
        }
    }
}
