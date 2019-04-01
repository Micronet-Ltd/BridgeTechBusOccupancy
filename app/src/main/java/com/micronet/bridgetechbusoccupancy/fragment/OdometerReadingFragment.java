package com.micronet.bridgetechbusoccupancy.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.micronet.bridgetechbusoccupancy.LoginActivity;
import com.micronet.bridgetechbusoccupancy.R;
import com.micronet.bridgetechbusoccupancy.utils.OutgoingMessage;
import com.micronet.bridgetechbusoccupancy.viewmodel.OdometerReadingViewModel;

import java.security.acl.LastOwnerException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OdometerReadingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OdometerReadingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OdometerReadingFragment extends DialogFragment {

    private OdometerReadingViewModel mViewModel;
    private OnFragmentInteractionListener mListener;
    private EditText odometerReading;

    public OdometerReadingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided param
     * @return A new instance of fragment OdometerReadingFragment.
     */
    public static OdometerReadingFragment newInstance() {
        OdometerReadingFragment fragment = new OdometerReadingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(OdometerReadingViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_odometer_reading, container, false);
        odometerReading = view.findViewById(R.id.logout_odometer_reading);
        view.findViewById(R.id.logout_odometer_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.setOdometerReading(Integer.parseInt(odometerReading.getText().toString()));
                OutgoingMessage.sendData();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawableResource(R.color.black);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
