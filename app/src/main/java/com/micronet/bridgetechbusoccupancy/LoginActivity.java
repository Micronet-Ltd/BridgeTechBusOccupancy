package com.micronet.bridgetechbusoccupancy;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.micronet.bridgetechbusoccupancy.fragment.LoginFragment;
import com.micronet.bridgetechbusoccupancy.viewmodel.LoginViewModel;

import java.util.List;

public class LoginActivity extends AppCompatActivity implements LoginFragment.LogInListener {
    private LoginViewModel mViewModel;
    private Spinner routesSpinner;
    private TextView busNumber;
    private int selectedBreakTypeId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        startService(new Intent(this, UdpService.class));

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        busNumber = findViewById(R.id.bus_number);
        busNumber.setText(String.format("Bus #%s", mViewModel.getBusNumber()));
    }

    @Override
    public void onLogIn(int opsNumber, int route, int odometerReading) {
        mViewModel.onLogin(opsNumber, mViewModel.routeForElement(route), odometerReading);
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public List<String> getRoutes() {
        return mViewModel.routesList();
    }

    @Override
    public int getOpsNumber() {
        return mViewModel.getCurrentOpsNumber();
    }

    @Override
    public int getOdometerReading() {
        return mViewModel.getOdometerReading();
    }
}