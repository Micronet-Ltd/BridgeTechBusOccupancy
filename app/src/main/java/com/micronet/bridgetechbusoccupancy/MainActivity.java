package com.micronet.bridgetechbusoccupancy;

import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidchils.odometer.Odometer;
import com.micronet.bridgetechbusoccupancy.fragment.OdometerReadingFragment;
import com.micronet.bridgetechbusoccupancy.fragment.PcResetFragment;
import com.micronet.bridgetechbusoccupancy.repository.Bus;
import com.micronet.bridgetechbusoccupancy.repository.BusDriver;
import com.micronet.bridgetechbusoccupancy.repository.Settings;
import com.micronet.bridgetechbusoccupancy.utils.OutgoingMessage;
import com.micronet.bridgetechbusoccupancy.viewmodel.MainViewModel;

import java.security.Provider;

public class MainActivity extends AppCompatActivity implements PcResetFragment.PcResetListener {

    Spinner breakSpinner;
    MainViewModel mainViewModel;
    TextView occupancy; // Don't be fooled. This is not the odometer reading.
    int currentBreakItemSelected;
    LinearLayout logoutLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        breakSpinner = findViewById(R.id.break_select_spinner);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.spinner_item, mainViewModel.breakTypes());
        arrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        breakSpinner.setAdapter(arrayAdapter);
        breakSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentBreakItemSelected = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        logoutLayout = (LinearLayout) findViewById(R.id.logout_layout);
        if(!mainViewModel.canLogOut()) {
            logoutLayout.setVisibility(View.GONE);
        }
        final Button clockInButton = findViewById(R.id.clock_in_status_button);
        clockInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean onBreak = "resume route".equals(clockInButton.getText().toString().toLowerCase());
                updateClockInStatus(!onBreak);
                clockInButton.setText(onBreak ? "Go on break" : "Resume route" );
            }
        });
        updateClockInStatus(true);
        occupancy = findViewById(R.id.occupancy_view);
        mainViewModel.observeOccupancy(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                occupancy.setText(integer.toString());
            }
        });
    }

    private void updateClockInStatus(boolean onBreak) {
        TextView view = findViewById(R.id.clock_in_status);
        view.setText(String.format("Status: %s", onBreak ? "On break" : "On shift"));
        if(onBreak) {
            mainViewModel.setBreakType(currentBreakItemSelected);
        }
        else {
            mainViewModel.clockIn();
        }
        OutgoingMessage.sendData();
    }

    @Override
    public void onBackPressed() {
        if(mainViewModel.canLogOut()) {
            super.onBackPressed();
        }
        else {
            Toast.makeText(this, "Cannot log out", Toast.LENGTH_LONG).show();
        }
    }

    public void onClickLogout(View view) {
        android.support.v4.app.DialogFragment fragment = OdometerReadingFragment.newInstance();
        Dialog dialog = fragment.getDialog();
        fragment.show(getSupportFragmentManager(), "Odometer");
    }

    public void onClickPcReset(View view) {
        android.support.v4.app.DialogFragment fragment = PcResetFragment.newInstance(this);
        Dialog dialog = fragment.getDialog();
        fragment.show(getSupportFragmentManager(), "pc-reset");
    }

    @Override
    public void onPcReset(String busNumber, String opsNumber, String occupancy) {
        if(!"".equals(busNumber)) {
            Bus.getInstance().busNumber.setValue(busNumber);
        }
        if(!"".equals(opsNumber)) {
            BusDriver.getInstance().opsNumber.setValue(opsNumber);
        }
        if(!"".equals(occupancy)) {
            Bus.getInstance().currentOccupancy.setValue(Integer.parseInt(occupancy));
        }
        OutgoingMessage.sendData();
    }
}
