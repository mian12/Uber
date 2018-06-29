package com.example.mianshahbazidrees.uber.History;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.mianshahbazidrees.uber.Model.HistoryModel;
import com.example.mianshahbazidrees.uber.R;
import com.example.mianshahbazidrees.uber.adapter.HistoryRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    HistoryRecyclerAdapter mHistoryAdapter;
    private String customerOrDriver = "", userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mRecyclerView = findViewById(R.id.recyclerView);


        customerOrDriver = getIntent().getExtras().getString("CustomerOrDriver");

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        getUserHistoryIds();


        mRecyclerView.setLayoutManager(new LinearLayoutManager(HistoryActivity.this));
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mHistoryAdapter = new HistoryRecyclerAdapter(HistoryActivity.this, getDataSetHistory());
        mRecyclerView.setAdapter(mHistoryAdapter);




    }

    private void getUserHistoryIds() {
        DatabaseReference userDataBaseref = FirebaseDatabase.getInstance().getReference().child("Users").child(customerOrDriver).child(userId).child("history");
        userDataBaseref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot history : dataSnapshot.getChildren()) {

                        fecthRideIdInformation(history.getKey());

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void fecthRideIdInformation(String rideKey) {


        DatabaseReference historyDatabaseRef = FirebaseDatabase.getInstance().getReference().child("History").child(rideKey);
        historyDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                   // for (DataSnapshot childRideId:)

                    String rideId = dataSnapshot.getKey();

                    Long timeStamp=0L;
                    for (DataSnapshot child: dataSnapshot.getChildren())
                    {
                        if (child.getKey().equals("timeStamp"))
                        {
                            timeStamp=Long.valueOf(child.getValue().toString());
                        }

                    }


                    HistoryModel object = new HistoryModel();
                    object.setRideId(rideId);
                    object.setTimeStamp(getDate(timeStamp));

                    historyResults.add(object);
                    mHistoryAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private String getDate(Long timeStamp)
        {
            Calendar cal=Calendar.getInstance(Locale.getDefault());
            cal.setTimeInMillis(timeStamp*1000);
           String date= android.text.format.DateFormat.format("dd-MM-yyyy hh:mm",cal).toString();
           return  date;
    }

    private ArrayList<HistoryModel> historyResults = new ArrayList<>();

    private ArrayList<HistoryModel> getDataSetHistory() {


        return historyResults;

    }
}
