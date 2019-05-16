package com.micronet.bridgetechbusoccupancy.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.micronet.bridgetechbusoccupancy.R;
import com.micronet.bridgetechbusoccupancy.repository.Bus;
import com.micronet.bridgetechbusoccupancy.repository.BusDriver;
import com.micronet.bridgetechbusoccupancy.utils.OutgoingMessage;

public class PcResetFragment extends DialogFragment {

    private PcResetViewModel mViewModel;
    private PcResetListener pcResetListener;
    private EditText busNumberEditText;
    private EditText opsNumberEditText;
    private EditText occupancyEditText;

    public static PcResetFragment newInstance(PcResetListener listener) {
        PcResetFragment fragment = new PcResetFragment();
        fragment.pcResetListener = listener;
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pc_reset_fragment, container, false);
        view.findViewById(R.id.submit_pc_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Submit PC reset", Toast.LENGTH_LONG).show();
                PcResetFragment.this.dismiss();
            }
        });
        occupancyEditText = view.findViewById(R.id.pc_reset_occupancy);
        busNumberEditText = view.findViewById(R.id.pc_reset_bus_number);
        opsNumberEditText = view.findViewById(R.id.pc_reset_ops);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(PcResetViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawableResource(R.color.black);
    }

    public interface PcResetListener {
        void onPcReset(int busNumber, int opsNumber, int occupancy);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        int busNumber;
        if(!busNumberEditText.getText().toString().isEmpty()) {
            busNumber = Integer.parseInt(busNumberEditText.getText().toString());
        }
        else {
            busNumber = Bus.getInstance().busNumber.getValue();
        }
        int opsNumber;
        if(!opsNumberEditText.getText().toString().isEmpty()) {
            opsNumber = Integer.parseInt(opsNumberEditText.getText().toString());
        }
        else {
            opsNumber = BusDriver.getInstance().opsNumber.getValue();
        }
        int occupancy;
        if(!occupancyEditText.getText().toString().isEmpty()) {
            occupancy = Integer.parseInt(occupancyEditText.getText().toString());
        }
        else {
            occupancy = Bus.getInstance().currentOccupancy.getValue();
        }
        pcResetListener.onPcReset(busNumber,
                opsNumber,
                occupancy);
        OutgoingMessage.sendData();
        super.onDismiss(dialog);
    }
}
