package com.example.gianmarco.bluethings;

import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;


public class AccountFragment extends Fragment {

    ImageView my_avatar_profile, update_user;
    EditText my_nick_profile;
    RecyclerView recycler_chats, recycler_pictures;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_account, container, false);
        my_avatar_profile = view.findViewById(R.id.my_avatar_profile);
        ((MainActivity)getActivity()).my_avatar_profile = my_avatar_profile;
        update_user = view.findViewById(R.id.update_user);
        my_nick_profile = view.findViewById(R.id.my_nick_profile);
        ((MainActivity)getActivity()).my_nick_profile = my_nick_profile;
        recycler_chats = view.findViewById(R.id.recycler_chats);
        ((MainActivity)getActivity()).recycler_chats = recycler_chats;
        recycler_pictures = view.findViewById(R.id.recycler_pictures);
        ((MainActivity)getActivity()).recycler_pictures = recycler_pictures;

        ((MainActivity)getActivity()).setCursors();
        ((MainActivity)getActivity()).viewRecyclers();

        my_avatar_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).contact_or_user = 2;
                ((MainActivity)getActivity()).showAvailableAvatar();
            }
        });

        update_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update_user();
            }
        });

        ((MainActivity)getActivity()).setUpUserInfos();

        return view;
    }


    public void update_user(){
        String newNick = my_nick_profile.getText().toString();
        ((MainActivity)getActivity()).update_user(newNick);
    }
}
