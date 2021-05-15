package com.example.gianmarco.bluethings;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;


public class BTSettingsFragment extends Fragment{

    Switch switch_bluetooth;
    Button ricerca_nuovi_dispositivi, visibility;
    ListView devices_lv;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_btsettings, container, false);

        ricerca_nuovi_dispositivi = (Button) view.findViewById(R.id.btn_ricerca_nuovi);
        ((MainActivity)getActivity()).ricerca_nuovi_dispositivi = ricerca_nuovi_dispositivi;

        visibility = (Button) view.findViewById(R.id.visibility);
        Activity act = getActivity();
        if (act instanceof MainActivity) {
            ((MainActivity) act).visibility = visibility;
        }

        switch_bluetooth = (Switch) view.findViewById(R.id.switch_bluetooth);
        ((MainActivity)getActivity()).switch_bluetooth = switch_bluetooth;

        devices_lv = (ListView) view.findViewById(R.id.listview);
        ((MainActivity)getActivity()).devices_lv = devices_lv;

        BluetoothAdapter BA = ((MainActivity)getActivity()).BA;
        if(BA.isEnabled()){
            switch_bluetooth.setChecked(true);
        }



        ricerca_nuovi_dispositivi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).discoverDevices();
            }
        });


        devices_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ((MainActivity)getActivity()).enable_bluethings(position);
            }
        });


        visibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity act = getActivity();
                if (act instanceof MainActivity) {
                    ((MainActivity) act).visible();
                }
            }
        });




        switch_bluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()

            {
                @Override
                public void onCheckedChanged (CompoundButton buttonView,boolean isChecked){
                Activity act = getActivity();
                if (act instanceof MainActivity) {
                    ((MainActivity) act).switch_change(isChecked);
                }
            }


        });



        // Inflate the layout for this fragment
        return view;
    }


}
