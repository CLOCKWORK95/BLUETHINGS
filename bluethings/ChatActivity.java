package com.example.gianmarco.bluethings;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.gianmarco.bluethings.BTSERVICES.BluetoothConnectionService;
import com.example.gianmarco.bluethings.DAO.DatabaseHelper;
import com.example.gianmarco.bluethings.ENTITY.ChatRaw;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;


public class ChatActivity extends AppCompatActivity {




    //_________________________ATTRIBUTI____________________________________________________________

    String offlineMsg ="";
    private static final int OFFLINE_MSG_DIALOG_ID = 0;
    DatabaseHelper myDb = new DatabaseHelper(ChatActivity.this);
    int chat_id;
    ArrayList<ChatRaw> storedChat = new ArrayList<ChatRaw>();
    boolean send_flag = false;
    ListView messages_lv;
    ImageButton btn_send;
    EditText my_message;
    ImageView friend_device_avatar;
    TextView friend_device_name;
    private static BluetoothConnectionService my_connection_service;
    private static BluetoothDevice my_friend_device;
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private ArrayList<String> cronology_messages_list = new ArrayList<>();
    Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();




    //BROADCAST RECEIVER: RICEZIONE MESSAGGI DA DISPOSITIVO ASSOCIATO
    BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            byte[] buffer= intent.getByteArrayExtra("message");
            String text = new String(buffer,0,buffer.length);
            String msg = "FRIEND :   " + text;
            cronology_messages_list.add(msg);
            myDb.insertChatRaw(chat_id, cronology_messages_list.indexOf(msg), msg);
            ArrayAdapter adapter = new ArrayAdapter(ChatActivity.this,
                    android.R.layout.simple_list_item_1, cronology_messages_list);
            messages_lv.setAdapter(adapter);
            scrollMyListViewToBottom();

        }
    };




    //______________METODI__________________________________________________________________________



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        View view = this.getWindow().getDecorView();

        this.getWindow().setBackgroundDrawableResource(R.drawable.sword_world);

        messages_lv = (ListView) findViewById(R.id.messages_lv);
        btn_send = (ImageButton) findViewById(R.id.btn_send_message_chat);
        my_message = (EditText) findViewById(R.id.et_send_message);
        friend_device_name = (TextView) findViewById(R.id.friend_device_name);
        friend_device_avatar = (ImageView) findViewById(R.id.chat_avatar);
        ImageButton back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
        BluetoothConnectionService.type = 1;
        my_connection_service = new BluetoothConnectionService(ChatActivity.this);
        Intent receivedIntent = getIntent();
        String deviceName = receivedIntent.getStringExtra("theDeviceName");
        String deviceAddress = receivedIntent.getStringExtra("theDeviceAddress");
        for (BluetoothDevice device : pairedDevices){
            if(device.getName().equals(deviceName) && device.getAddress().equals(deviceAddress)){
                my_friend_device = device;
            }
        }
        try{
            startConnectionChat();
        } catch(Exception e){
            e.printStackTrace();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter("incomingMessage"));
        setChat_id();
        //Scrivi lo storico della chat su listView
        viewChat();
        ArrayAdapter adapter = new ArrayAdapter(ChatActivity.this,
                android.R.layout.simple_list_item_1, cronology_messages_list);
        messages_lv.setAdapter(adapter);
        btn_send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Invia messaggi offline se presenti
                String text = my_message.getText().toString();
                byte[] bytes = my_message.getText().toString().getBytes(Charset.defaultCharset());
                int chunk_size = 100;
                //INVIO LA DIMENSIONE DELLA CLIP
                my_connection_service.write(String.valueOf(bytes.length).getBytes());
                for(int i = 0; i < bytes.length ; i+= chunk_size){
                    byte[] tempArray;
                    tempArray = Arrays.copyOfRange(bytes, i ,
                            Math.min( bytes.length, i + chunk_size));
                    //INVIO UNO ALLA VOLTA I CHUNKS, DOPO CHE IL FLAG DEL SERVICE è MESSO A FALSE
                    my_connection_service.write(tempArray);
                }
                send_flag = false;
                //Cronologia chat (memorizza anche i tuoi messaggi, cosi da visualizzare il dialogo
                String msg = "ME :   " + text;
                cronology_messages_list.add(msg);
                myDb.insertChatRaw(chat_id, cronology_messages_list.indexOf(msg), msg);
                my_message.setText("");
                ArrayAdapter adapter = new ArrayAdapter(ChatActivity.this,
                        android.R.layout.simple_list_item_1, cronology_messages_list);
                messages_lv.setAdapter(adapter);
                scrollMyListViewToBottom();
            }

        });

        scrollMyListViewToBottom();
       /* offlineMsg = getOfflineMsgIfExist();
        if (offlineMsg != ""){
            showDialog(OFFLINE_MSG_DIALOG_ID);
        }*/

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            this.getWindow().setBackgroundDrawableResource(R.drawable.untitled);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            this.getWindow().setBackgroundDrawableResource(R.drawable.sword_world);
        }
    }

    public void scrollMyListViewToBottom() {
        messages_lv.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                messages_lv.setSelection(messages_lv.getCount() - 1);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "Premi la freccia in alto a destra!", Toast.LENGTH_LONG).show();
    }



    //_____________________________INIT_CHAT________________________________________________________

    //Metodo per iniziare la chat bluetooth (l'app va in crash se non ho fatto il match tra i device!)
    public void startConnectionChat(){
        startBluetoothConnection(my_friend_device, MY_UUID);
    }

    public void startBluetoothConnection(BluetoothDevice device, UUID uuid){

        my_connection_service.startClient(device,uuid);

    }

    public void setChat_id(){

        Cursor cursor = myDb.getChatIdByDeviceName(my_friend_device.getName());

        if (cursor.getCount() == 0){

            Toast.makeText(this,
                    "E' la prima volta che chatti con " + my_friend_device.getName() + ". " +
                            "Il dispositivo verrà aggiunto nella lista dei contatti",
                    Toast.LENGTH_LONG).show();

            myDb.insertContact(my_friend_device.getName(), my_friend_device.getAddress(),
                    my_friend_device.getName(),null );

            setChat_id();


        }

        else {

            int id = -1;

            while (cursor.moveToNext()) {

                id = cursor.getInt(0);

                if (id != -1){

                    chat_id = id;
                    Cursor cursor_two = myDb.getContactInfosById(chat_id);
                    while(cursor_two.moveToNext()){

                        String nick = cursor_two.getString(3);
                        if (cursor_two.getBlob(4) != null){
                            Bitmap avatar = BitmapFactory.decodeByteArray(cursor_two.getBlob(4),
                                    0,cursor_two.getBlob(4).length);
                            friend_device_avatar.setImageBitmap(avatar);
                        }

                        friend_device_name.setText(nick);
                    }


                }
            }

        }

    }

    public void viewChat() {

        Cursor cursor = myDb.getChat(chat_id);

        if (cursor.getCount() == 0){
            Toast.makeText(this,"Chat vuota.",
                    Toast.LENGTH_LONG).show();
        }
        else{

            while(cursor.moveToNext()){

                int position = cursor.getInt(0);
                String text = cursor.getString(1);

                ChatRaw raw = new ChatRaw(position,text);

                storedChat.add(raw);

            }

            Collections.sort(storedChat);

            for (ChatRaw raw : storedChat){
                cronology_messages_list.add(raw.getText());
            }

        }
    }


    //________________________OFFLINE_MSGS__________________________________________________________

    public String getOfflineMsgIfExist(){
        Cursor cursor = myDb.getOfflineChat(chat_id);
        ArrayList<ChatRaw> messages = new ArrayList<ChatRaw>();
        while(cursor.moveToNext()){
            int position = cursor.getInt(0);
            String text = cursor.getString(1);
            ChatRaw raw = new ChatRaw(position, text);
            messages.add(raw);
        }
        Collections.sort(messages);
        String text = "";
        for (ChatRaw raw : messages){
            text = text + "\n" + raw.getText();
        }
        return text;
    }

    public void sendOfflineMsgIfExist(View view){

        if(offlineMsg != ""){
            byte[] bytes = offlineMsg.getBytes(Charset.defaultCharset());
            my_connection_service.write(bytes);
            int chunk_size = 100;
            //INVIO LA DIMENSIONE DELLA CLIP
            my_connection_service.write(String.valueOf(bytes.length).getBytes());
            for(int i = 0; i < bytes.length ; i+= chunk_size){
                byte[] tempArray;
                tempArray = Arrays.copyOfRange(bytes, i ,
                        Math.min( bytes.length, i + chunk_size));
                //INVIO UNO ALLA VOLTA I CHUNKS, DOPO CHE IL FLAG DEL SERVICE è MESSO A FALSE
                my_connection_service.write(tempArray);
            }
            send_flag = false;
            String msg = "ME :   " + offlineMsg;
            cronology_messages_list.add(msg);
            myDb.insertChatRaw(chat_id, cronology_messages_list.indexOf(msg), msg);
            my_message.setText("");
            ArrayAdapter adapter = new ArrayAdapter(ChatActivity.this,
                    android.R.layout.simple_list_item_1, cronology_messages_list);
            messages_lv.setAdapter(adapter);
            myDb.resetOfflineChat();
            dismissDialog(OFFLINE_MSG_DIALOG_ID);
        }
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch(id){
            case OFFLINE_MSG_DIALOG_ID:
                dialog = new Dialog(ChatActivity.this);
                dialog.setContentView(R.layout.send_offline_msg_dialog_layout);
                dialog.setTitle("offlineMsg Dialog");
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);
                break;

        }
        return dialog;
    }
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch(id){
            case OFFLINE_MSG_DIALOG_ID:
                break;
        }
    }

    public void dismiss(View view){
        dismissDialog(OFFLINE_MSG_DIALOG_ID);
    }




    public void back(){

        my_connection_service.stop();

        Intent friend_device_intent = new Intent(this, MainActivity.class);
        friend_device_intent.putExtra("theDeviceName",my_friend_device.getName());
        friend_device_intent.putExtra("theDeviceAddress", my_friend_device.getAddress());

        finish();

        startActivity(friend_device_intent);

    }






}



