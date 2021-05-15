package com.example.gianmarco.bluethings.UTILS;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.gianmarco.bluethings.ENTITY.Contact;
import com.example.gianmarco.bluethings.ENTITY.Picture;
import com.example.gianmarco.bluethings.MainActivity;
import com.example.gianmarco.bluethings.R;

import java.util.ArrayList;

public class ChatsRecyclerviewAdapter extends RecyclerView.Adapter<ChatsRecyclerviewAdapter.ViewHolder>{

    public Cursor cursor;
    public ArrayList<Contact> contacts = new ArrayList<>();
    public ArrayList<Picture> pictures = new ArrayList<>();
    int type;
    private Context context;


    //BUILDER
    public ChatsRecyclerviewAdapter(Context context,int type, Cursor cursor) {

        this.cursor = cursor;
        this.context = context;
        this.type = type;

        switch(type){

            case 1:

                while(cursor.moveToNext()){
                    Bitmap avatar = null;
                    int id = cursor.getInt(0);
                    String nick = cursor.getString(3);
                    if(cursor.getBlob(4) != null){
                        avatar = BitmapFactory.decodeByteArray(cursor.getBlob(4),
                                0,cursor.getBlob(4).length);
                    }
                    Contact contact = new Contact(avatar,nick);
                    contact.setId(id);
                    contacts.add(contact);
                }

                break;

            case 2:

                while(cursor.moveToNext()){
                    int id = cursor.getInt(0);
                    Bitmap picture = BitmapFactory.decodeByteArray(cursor.getBlob(1),
                            0,cursor.getBlob(1).length);

                    pictures.add(new Picture(id,picture));

                }

                break;

            default:
                break;
        }

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate
                        (R.layout.my_chats_recycler_element_layout,parent,false);

        ViewHolder holder = new ViewHolder(view);

        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        switch(type){

            case 1:
                final Contact contact = contacts.get(position);
                holder.nickname.setText(contact.getNick_name());
                if(contact.getAvatar() != null){
                    holder.avatar.setImageBitmap(contact.getAvatar());
                }

                holder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent incomingIntent = new Intent("chatID");
                        incomingIntent.putExtra("chatID", contact.getId());
                        incomingIntent.putExtra("nick", contact.getNick_name());
                        LocalBroadcastManager.getInstance(context).sendBroadcast(incomingIntent);
                    }
                });

                break;

            case 2:
                final Picture picture = pictures.get(position);
                holder.nickname.setText("");
                holder.avatar.setImageBitmap(picture.getPicture());
                holder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent incomingIntent = new Intent("open_picture_dialog");
                        incomingIntent.putExtra("pictID", picture.getId());
                        LocalBroadcastManager.getInstance(context).sendBroadcast(incomingIntent);
                    }
                });

                break;

            default:
                break;
        }
    }


    @Override
    public int getItemCount() {
        switch(type){
            case 1:
                return contacts.size();
            case 2:
                return pictures.size();
            default:
                return 0;
        }
    }



    public class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout layout;
        ImageView avatar;
        TextView nickname;

        public ViewHolder(View itemView){
            super(itemView);
            avatar = itemView.findViewById(R.id.my_friend_avatar_recycler);
            nickname = itemView.findViewById(R.id.my_friend_nickname_recycler);
            layout = itemView.findViewById(R.id.recycle_layout);
        }

    }


}
