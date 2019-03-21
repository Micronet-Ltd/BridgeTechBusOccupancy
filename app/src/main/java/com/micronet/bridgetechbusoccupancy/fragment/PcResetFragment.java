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
import com.micronet.bridgetechbusoccupancy.utils.OutgoingMessage;

public class PcResetFragment extends DialogFragment {

    private PcResetViewModel mViewModel;
    private PcResetListener pcResetListener;
    private EditText busNumber;
    private EditText opsNumber;
    private EditText occupancy;

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
        occupancy = view.findViewById(R.id.pc_reset_occupancy);
        busNumber = view.findViewById(R.id.pc_reset_bus_number);
        opsNumber = view.findViewById(R.id.pc_reset_ops);
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
        void onPcReset(String busNumber, String opsNumber, String occupancy);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        pcResetListener.onPcReset(busNumber.getText().toString(),
                opsNumber.getText().toString(),
                occupancy.getText().toString());
        OutgoingMessage.sendData();
        super.onDismiss(dialog);
    }
}
