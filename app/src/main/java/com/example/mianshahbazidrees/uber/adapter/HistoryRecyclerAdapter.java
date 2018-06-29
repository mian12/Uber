package com.example.mianshahbazidrees.uber.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mianshahbazidrees.uber.History.HistorySingleActivity;
import com.example.mianshahbazidrees.uber.Model.HistoryModel;
import com.example.mianshahbazidrees.uber.R;

import java.util.ArrayList;


/**
 * Created by Engr Shahbaz on 6/28/2016.
 */

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.MyViewHolder>
{


    private LayoutInflater inflater;
    private Context context;
    ArrayList<HistoryModel> dataList;


    public HistoryRecyclerAdapter(Context context, ArrayList<HistoryModel> dataList) {
        this.context = context;
        this.dataList = dataList;
        inflater = LayoutInflater.from(context);

    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.row_history, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;


    }



    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        String id = dataList.get(position).getRideId();
        String timeStamp = dataList.get(position).getTimeStamp();


        holder.rideId.setText(id);

        holder.timeStamp.setText(timeStamp);


    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{



        TextView rideId,timeStamp;


        public MyViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            rideId = itemView.findViewById(R.id.rideId);
            timeStamp = itemView.findViewById(R.id.timeStamp);


        }

        @Override
        public void onClick(View v) {

            Intent intent=new Intent(v.getContext(), HistorySingleActivity.class);
            intent.putExtra("rideId",rideId.getText().toString());
            v.getContext().startActivity(intent);



        }
    }


}
