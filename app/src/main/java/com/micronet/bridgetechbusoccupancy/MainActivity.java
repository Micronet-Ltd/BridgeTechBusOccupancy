package com.micronet.bridgetechbusoccupancy;

import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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

import com.micronet.bridgetechbusoccupancy.fragment.OdometerReadingFragment;
import com.micronet.bridgetechbusoccupancy.fragment.PcResetFragment;
import com.micronet.bridgetechbusoccupancy.repository.Bus;
import com.micronet.bridgetechbusoccupancy.repository.BusDriver;
import com.micronet.bridgetechbusoccupancy.utils.Log;
import com.micronet.bridgetechbusoccupancy.utils.OutgoingMessage;
import com.micronet.bridgetechbusoccupancy.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity implements PcResetFragment.PcResetListener {

    Spinner breakSpinner;
    MainViewModel mainViewModel;
    TextView occupancy;
    TextView opsNumber;
    TextView busNumber;
    int currentBreakItemSelected;
    LinearLayout logoutLayout;
    Button clockInButton;

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
        clockInButton = findViewById(R.id.clock_in_status_button);
        clockInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean onBreak = "resume route".equals(clockInButton.getText().toString().toLowerCase());
                updateClockInStatus(!onBreak);
            }
        });
        updateClockInStatus(BusDriver.getInstance().breakType.getValue() != 99 );
        occupancy = findViewById(R.id.occupancy_view);
        opsNumber = findViewById(R.id.ops_number_view);
        busNumber = findViewById(R.id.bus_number_view);

        mainViewModel.observeOccupancy(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                occupancy.setText(integer.toString());
            }
        });
        mainViewModel.observeOpsNumber(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                opsNumber.setText(integer.toString());
            }
        });
        mainViewModel.observeBusNumber(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                busNumber.setText(integer.toString());
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
        clockInButton.setText(onBreak ? "Resume route" : "Go on break" );
        OutgoingMessage.sendData();
    }

    @Override
    public void onBackPressed() {
        if(mainViewModel.canLogOut()) {
            promptToLogout();
        }
        else {
            Toast.makeText(this, "Cannot log out", Toast.LENGTH_LONG).show();
        }
    }



    public void onClickLogout(View view) {
        promptToLogout();
    }

    private void promptToLogout() {
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
    public void onPcReset(int busNumber, int opsNumber, int occupancy) {
        if(!"".equals(busNumber)) {
            Bus.getInstance().busNumber.setValue(busNumber);
        }
        if(!"".equals(opsNumber)) {
            BusDriver.getInstance().opsNumber.setValue(opsNumber);
        }
        if(!"".equals(occupancy)) {
            Bus.getInstance().currentOccupancy.setValue(occupancy);
        }
        OutgoingMessage.sendData();
    }
}
