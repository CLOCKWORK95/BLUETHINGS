package com.example.gianmarco.bluethings;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;


public class ContactOptionFragment extends Fragment {


    ImageView info_avatar, delete_contact_btn, send_offline_btn;
    TextView info_device_name;
    TextView info_device_address;
    EditText info_nick_name, send_offline_et;
    Button fatto_btn;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_option, container, false);
        info_avatar = view.findViewById(R.id.update_avatar_btn);
        ((MainActivity)getActivity()).info_avatar = info_avatar;
        info_nick_name = view.findViewById(R.id.infoNickNametxt);
        ((MainActivity)getActivity()).info_nick_name= info_nick_name;
        info_device_name = view.findViewById(R.id.device_name_txtView);
        ((MainActivity)getActivity()).info_device_name = info_device_name;
        info_device_address = view.findViewById(R.id.device_address_txtView);
        ((MainActivity)getActivity()).info_device_address = info_device_address;
        send_offline_et = view.findViewById(R.id.offline_msg_et);
        send_offline_btn = view.findViewById(R.id.send_offline_btn);
        fatto_btn = view.findViewById(R.id.fatto_btn);
        delete_contact_btn = view.findViewById(R.id.delete_contact_btn);

        ((MainActivity)getActivity()).setUpContactInfos();


        fatto_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newNick = info_nick_name.getText().toString();
                ((MainActivity)getActivity()).position = 0;
                ((MainActivity)getActivity()).updateContact(newNick);
            }
        });


        send_offline_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String device_name = info_device_name.getText().toString();
                String text = send_offline_et.getText().toString();
                ((MainActivity)getActivity()).storeOfflineMessage(device_name, text);
                send_offline_et.setText("");
                ((MainActivity)getActivity()).position++;
            }
        });

        delete_contact_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).areUSure();
            }
        });


        info_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).contact_or_user = 1;
                ((MainActivity)getActivity()).showAvailableAvatar();
            }
        });

        return view;
    }


}
