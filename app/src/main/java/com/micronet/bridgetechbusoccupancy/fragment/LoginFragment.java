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
import android.widget.Spinner;
import android.widget.Toast;

import com.micronet.bridgetechbusoccupancy.R;
import com.micronet.bridgetechbusoccupancy.utils.Log;
import com.micronet.bridgetechbusoccupancy.viewmodel.LoginViewModel;

import java.net.SocketException;
import java.util.List;

public class LoginFragment extends Fragment {

    LogInListener logInListener;
    EditText opsNumberEditText;
    EditText odometerReadingEditText;
    Spinner routesSpinner;
    int route = 0;
    private static final String TAG = "LoginFragment";

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment, container, false);
        opsNumberEditText = view.findViewById(R.id.ops_input);
        odometerReadingEditText = view.findViewById(R.id.odometer_reading);
        view.findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int odometerReading = Integer.parseInt(odometerReadingEditText.getText().toString());
                    logInListener.onLogIn(opsNumberEditText.getText().toString(), route, odometerReading);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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
        public void onLogIn(String opsNumber, int route, int odometerReading);
        public List<String> getRoutes();
    }
}
