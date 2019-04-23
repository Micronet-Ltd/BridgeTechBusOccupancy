package com.micronet.bridgetechbusoccupancy.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.micronet.bridgetechbusoccupancy.R;
import com.micronet.bridgetechbusoccupancy.repository.Bus;
import com.micronet.bridgetechbusoccupancy.repository.BusDriver;
import com.micronet.bridgetechbusoccupancy.repository.Settings;
import com.micronet.bridgetechbusoccupancy.utils.Log;
import com.micronet.bridgetechbusoccupancy.viewmodel.LoginViewModel;

import java.net.SocketException;
import java.time.Duration;
import java.util.List;

public class LoginFragment extends Fragment {

    LogInListener logInListener;
    EditText opsNumberEditText;
    EditText odometerReadingEditText;
    EditText busNumberEditText;
    Spinner routesSpinner;
    LinearLayout routesLinearLayout;
    int route = -1;
    private static final String TAG = "LoginFragment";

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment, container, false);
        opsNumberEditText = view.findViewById(R.id.ops_input);

        odometerReadingEditText = view.findViewById(R.id.odometer_reading);
        busNumberEditText = view.findViewById(R.id.bus_input);

        view.findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if("".equals(opsNumberEditText.getText().toString().trim())) {
                    Toast.makeText(getContext(), "Please enter a value for OPS number", Toast.LENGTH_LONG).show();
                    return;
                }
                if("".equals(odometerReadingEditText.getText().toString().trim())) {
                    Toast.makeText(getContext(), "Please enter a value for odometer", Toast.LENGTH_LONG).show();
                    return;
                }
                if("".equals(busNumberEditText.getText().toString().trim())) {
                    Toast.makeText(getContext(), "Please enter a value for bus number", Toast.LENGTH_LONG).show();
                }
                try {
                    int odometerReading = Integer.parseInt(odometerReadingEditText.getText().toString());
                    int busNumber = Integer.parseInt(busNumberEditText.getText().toString());
                    logInListener.onLogIn(Integer.parseInt(opsNumberEditText.getText().toString()), route, odometerReading, busNumber);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        routesLinearLayout = view.findViewById(R.id.routes_layout);
        if(logInListener.getRoutes() == null || logInListener.getRoutes().isEmpty()) {
            Toast.makeText(getContext(), String.format("No or malformed routes, see %s. May have been corrupted during file transfer.", Settings.SETTINGS_FILE_PATH), Toast.LENGTH_LONG).show();
            routesLinearLayout.setVisibility(View.GONE);
        }
        else {
            routesSpinner = view.findViewById(R.id.routes_spinner);
            ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), R.layout.spinner_item, logInListener.getRoutes());
            arrayAdapter.setDropDownViewResource(R.layout.spinner_item);
            routesSpinner.setAdapter(arrayAdapter);
            routesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    route = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // idunnolol
                }
            });
        }
        odometerReadingEditText.setText(Bus.getInstance().odometerReading.getValue() == null ? "" : Bus.getInstance().odometerReading.getValue() + "");
        Integer currentOpsNumber = BusDriver.getInstance().opsNumber.getValue();
        opsNumberEditText.setText(currentOpsNumber == null || currentOpsNumber == -1 ? "" : currentOpsNumber + "");

        Integer currentBusNumber = Bus.getInstance().busNumber.getValue();
        busNumberEditText.setText(currentBusNumber == null ? "" : currentBusNumber + "");

        Integer currentOdometerReading = Bus.getInstance().odometerReading.getValue();
        odometerReadingEditText.setText(currentOdometerReading == null ? "" : currentOdometerReading + "");

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO: Use the ViewModel
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            logInListener = (LogInListener)context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement LogInListener");
        }
    }

    public interface LogInListener {
        public void onLogIn(int opsNumber, int route, int odometerReading, int busNumber);
        public List<String> getRoutes();
        public int getOpsNumber();
        public int getOdometerReading();
    }
}
