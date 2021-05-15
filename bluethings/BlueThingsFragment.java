package com.example.gianmarco.bluethings;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class BlueThingsFragment extends Fragment {

    TextView my_friend_name;
    ImageView chat_btn;
    ImageView picture_chat_btn;
    ImageView walkie_talkie_btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_blue_things, container, false);



        my_friend_name = view.findViewById(R.id.connected_friend);
        my_friend_name.setText(((MainActivity)getActivity()).my_bluetooth_device.getName());
        chat_btn = view.findViewById(R.id.cloud);
        picture_chat_btn = view.findViewById(R.id.photosi);
        walkie_talkie_btn = view.findViewById(R.id.microphone);

        chat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((MainActivity)getActivity()).startChat();

            }
        });

        picture_chat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((MainActivity)getActivity()).startPictureChat();
            }
        });

        walkie_talkie_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((MainActivity)getActivity()).startWalkieTalkie();
            }
        });




        // Inflate the layout for this fragment
        return view;
    }

}
