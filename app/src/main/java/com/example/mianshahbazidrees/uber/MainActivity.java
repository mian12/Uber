package com.example.mianshahbazidrees.uber;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.mianshahbazidrees.uber.Customer.CustomerLoginActivity;
import com.example.mianshahbazidrees.uber.Driver.DriverLoginActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mDriverButton,mCustomerButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mDriverButton=findViewById(R.id.driver);
        mCustomerButton=findViewById(R.id.customer);


        mDriverButton.setOnClickListener(this);
        mCustomerButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {

        Intent intent;
        switch (view.getId())
        {
            case R.id.driver:
                intent=new Intent(MainActivity.this, DriverLoginActivity.class);
                startActivity(intent);
                finish();

                break;
            case R.id.customer:
                intent=new Intent(MainActivity.this, CustomerLoginActivity.class);
                startActivity(intent);
                finish();

                break;

        }
    }
}
