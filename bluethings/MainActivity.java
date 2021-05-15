package com.example.gianmarco.bluethings;

import android.Manifest;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.example.gianmarco.bluethings.DAO.DatabaseHelper;
import com.example.gianmarco.bluethings.ENTITY.ChatRaw;
import com.example.gianmarco.bluethings.ENTITY.Contact;
import com.example.gianmarco.bluethings.UTILS.ChatsRecyclerviewAdapter;
import com.example.gianmarco.bluethings.UTILS.ContactArrayAdapter;
import com.example.gianmarco.bluethings.UTILS.DeviceListAdapter;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;


public class MainActivity extends AppCompatActivity{

    Fragment fragment;
    Button BlueThings_btn;
    Button settings_btn;
    Button contacts_btn;
    Button profile_btn;
    HorizontalScrollView hScroll;

    //_____________________ATTRIBUTI : FRAGMENT BT SETTINGS_________________________________________

    Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
    Switch switch_bluetooth;
    Button ricerca_nuovi_dispositivi, visibility;
    ImageButton start_connection_chat;
    static final int ENABLE_REQUEST = 1;
    static boolean flag = false;
    BluetoothAdapter BA;
    ListView devices_lv;
    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private DeviceListAdapter device_adapter;
    BluetoothDevice my_bluetooth_device;

    //___________________ATTRIBUTI : FRAGMENT CONTACTS______________________________________________

    DatabaseHelper myDB = new DatabaseHelper(this);
    ListView contacts_listview;
    SearchView searchView;
    ArrayList<Contact> contacts = new ArrayList<Contact>();
    ContactArrayAdapter caa;

    //__________________ATTRIBUTI_FRAGMENT_CONTACT_INFOS____________________________________________

    Contact contact_info;
    ImageView info_avatar;
    TextView info_device_name;
    TextView info_device_address;
    EditText info_nick_name;
    int position = 0;
    static final int AVATAR_DIALOG_ID = 0;
    static final int ARE_U_SURE_DIALOG = 1;
    Bitmap newAvatar = null;

    //________________ATTRIBUTI_AVATAR_DIALOG_______________________________________________________

    int contact_or_user = 0;
    ImageView avatar_1,avatar_2,avatar_3,avatar_4,avatar_5,avatar_6,avatar_7,avatar_8,
            avatar_9,avatar_10,avatar_11,avatar_12,avatar_13,avatar_14,avatar_15,avatar_16;

    //________________ATTRIBUTI_FRAGMENT_ACCOUNT____________________________________________________

    LinearLayoutManager llm_chats,llm_pictures;
    ChatsRecyclerviewAdapter cra_1 = null;
    ChatsRecyclerviewAdapter cra_2 = null;
    Cursor cursor_chats, cursor_pictures;
    ImageView my_avatar_profile;
    EditText my_nick_profile;
    RecyclerView recycler_chats, recycler_pictures;
    static final int VIEW_CHAT_DIALOG_ID = 2;
    static final int OPEN_PICTURE_DIALOG_ID = 3;

    //_______________ATTRIBUTI VIEW_CHAT_DIALOG_____________________________________________________

    TextView viewChatNick;
    ListView viewChatLv;
    ArrayList<ChatRaw> storedChat = new ArrayList<ChatRaw>();
    private ArrayList<String> cronology_messages_list = new ArrayList<>();

    private ImageView opened_picture;


    //_________________MAINACTIVITY BROADCASTRECEIVERS______________________________________________


                //BROADCAST RECEIVER PER ACTION_FOUND  (BROADCAST RECEIVER 1)
    private BroadcastReceiver broadcast_receiver1 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bluetoothDevices.add(device);
                device_adapter = new DeviceListAdapter(context,
                        R.layout.device_adapter_view, bluetoothDevices);
                devices_lv.setAdapter(device_adapter);
            }
        }
    };


                //BROADCAST RECEIVER PER ACTION_BOND_STATE_CHANGE  (BROADCAST RECEIVER 2)
    private BroadcastReceiver broadcast_receiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice myDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(myDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Toast.makeText(MainActivity.this, "Bonded",
                            Toast.LENGTH_LONG).show();
                    my_bluetooth_device = myDevice;
                }
                if(myDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    Toast.makeText(MainActivity.this, "Trying to bond",
                            Toast.LENGTH_LONG).show();
                }
                if(myDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    Toast.makeText(MainActivity.this, "Not Bonded",
                            Toast.LENGTH_LONG).show();
                }
            }
        }

    };

           //BROADCAST RECEIVER PER ACTION_STATE_CHANGE : ABILITA/DISABILITA BLUETOOTH
    private BroadcastReceiver broadcast_receiver_enable_disable = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(BA.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BA.EXTRA_STATE, BA.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        if(flag == false){
                            if(switch_bluetooth.isChecked() == true){
                                switch_bluetooth.setChecked(false);
                                clearListView();
                                Toast.makeText(MainActivity.this,
                                        "Bluetooth disattivato con successo." ,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                        else{
                            clearListView();
                            start_connection_chat.setVisibility(View.GONE);
                            flag = false;
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        break;
                    case BluetoothAdapter.STATE_ON:
                        if(switch_bluetooth.isChecked()== false){
                            switch_bluetooth.setChecked(true);
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        break; } }}};

    //______________________________________________________________________________________________


    //                                   [ON CREATE]



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        BlueThings_btn = (Button) findViewById(R.id.blue_things_btn);
        settings_btn = (Button) findViewById(R.id.bt_settings_btn);
        contacts_btn = (Button) findViewById(R.id.contact_list_btn);
        profile_btn = (Button) findViewById(R.id.profile_btn);

        getWindow().setBackgroundDrawableResource(R.drawable.sword_world);


        hScroll = (HorizontalScrollView) findViewById(R.id.scrollV);


        BlueThings_btn.setClickable(false);
        BA = BluetoothAdapter.getDefaultAdapter();
        DeviceListAdapter.cursor = myDB.viewContactList();
        //NOTIFICA IL CAMBIAMENTO DELLO STATO DI CONNESSIONE CON UN ALTRO DISPOSITIVO AL BROADCAST RECEIVER 2
        IntentFilter filter_bond = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(broadcast_receiver2, filter_bond);
        //NOTIFICA IL CAMBIAMENTO DELLO STATO ENABLE/DISABLE DEL BLUETOOTH SUL DISPOSITIVO
        IntentFilter filter_enable_disable = new IntentFilter(BA.ACTION_STATE_CHANGED);
        registerReceiver(broadcast_receiver_enable_disable, filter_enable_disable);

        if (!BlueThings_btn.isClickable()) {
            BlueThings_btn.setBackgroundResource(R.drawable.button_disabled);
        }else if (BlueThings_btn.isClickable()){
            BlueThings_btn.setBackgroundResource(R.drawable.button_enabled);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(chat_id_receiver,
                new IntentFilter("chatID"));
        LocalBroadcastManager.getInstance(this).registerReceiver(open_picture_receiver,
                new IntentFilter("open_picture_dialog"));


        try{
            Intent receivedIntent = getIntent();
            String deviceName = receivedIntent.getStringExtra("theDeviceName");
            String deviceAddress = receivedIntent.getStringExtra("theDeviceAddress");
            for (BluetoothDevice device : pairedDevices){
                if(device.getName().equals(deviceName) && device.getAddress().equals(deviceAddress)){
                    my_bluetooth_device = device;
                    BlueThings_btn.setBackgroundResource(R.drawable.button_selected);

                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    fragment = new BlueThingsFragment();
                    ft.replace(R.id.fragment_container, fragment);
                    ft.commit();

                }
            }
            mantain_bluethings();
        } catch (Exception e){
            e.printStackTrace();
        }


        Cursor cursor = myDB.getUserInfos();
        if(cursor.getCount() == 0){
            myDB.insertUser("nick name");
        }



    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().setBackgroundDrawableResource(R.drawable.untitled);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            getWindow().setBackgroundDrawableResource(R.drawable.sword_world);
        }
    }
    //____________________BT_SETTING_FRAGMENT_METHODS_______________________________________________

    //METODO CHE SCRIVE SU LISTVIEW I DISPOSITIVI TROVATI DURANTE LA RICERCA
    public void discoverDevices(){


        if(BA.isDiscovering()){

            bluetoothDevices.clear();

            checkBTPermissions();

            BA.cancelDiscovery();

            BA.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcast_receiver1,discoverDevicesIntent);

        }

        if(!BA.isDiscovering()){

            bluetoothDevices.clear();

            checkBTPermissions();

            BA.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcast_receiver1,discoverDevicesIntent);


        }

    }

    public void visible(){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }

    //CONTROLLO PERMESSI BLUETOOTH PER L'UTILIZZO DEI METODI CHIAMATI IN onItemClick()
    private void checkBTPermissions(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if(permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        1001);
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(broadcast_receiver1);
        unregisterReceiver(broadcast_receiver2);
        unregisterReceiver(broadcast_receiver_enable_disable);
    }

    public ArrayList<BluetoothDevice> getBluetoothDevices(){
        return bluetoothDevices;
    }

    public void setMy_bluetooth_device(BluetoothDevice bd){
        my_bluetooth_device = bd;
    }

    public void clearListView(){
        bluetoothDevices.clear();
        device_adapter = new DeviceListAdapter(MainActivity.this,R.layout.device_adapter_view,
                bluetoothDevices);
        devices_lv.setAdapter(device_adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ENABLE_REQUEST:
                if (resultCode != RESULT_OK) {
                    flag = true;
                    Toast.makeText(MainActivity.this, "Permesso negato", Toast.LENGTH_SHORT).show();
                    switch_bluetooth.setChecked(false);
                }

                break;
        }
    }

    public void enable_bluethings(int position){

        BA.cancelDiscovery();

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {

            try {
                bluetoothDevices = getBluetoothDevices();
                if (bluetoothDevices.get(position).getBondState() == BluetoothDevice.BOND_BONDED) {

                    my_bluetooth_device = bluetoothDevices.get(position);
                    setMy_bluetooth_device(my_bluetooth_device);

                    //VAI A FRAGMENT BLUETHINGS
                    BlueThings_btn.setClickable(true);
                    Toast.makeText(MainActivity.this,
                            "Bluethings accessibile!",
                            Toast.LENGTH_LONG).show();
                    BlueThings_btn.setBackgroundResource(R.drawable.button_enabled);
                } else {
                    bluetoothDevices.get(position).createBond();
                    my_bluetooth_device = bluetoothDevices.get(position);

                    //VAI A FRAGMENT BLUETHINGS
                    BlueThings_btn.setClickable(true);
                    Toast.makeText(MainActivity.this,
                            "Bluethings accessibile!",
                            Toast.LENGTH_LONG).show();
                    BlueThings_btn.setBackgroundResource(R.drawable.button_enabled);
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }

        }

    }

    public void mantain_bluethings() {

        try {
            if (my_bluetooth_device.getBondState() == BluetoothDevice.BOND_BONDED) {

                //VAI A FRAGMENT BLUETHINGS
                BlueThings_btn.setClickable(true);
                Toast.makeText(MainActivity.this,
                        "Connessione stabile",
                        Toast.LENGTH_LONG).show();
            } else {
                my_bluetooth_device.createBond();

                //VAI A FRAGMENT BLUETHINGS
                BlueThings_btn.setClickable(true);
                Toast.makeText(MainActivity.this,
                        "Connessione stabile",
                        Toast.LENGTH_LONG).show();
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }


    }

    public void switch_change(boolean isChecked){

        if(isChecked == true){

            if(BA.isEnabled()==false){

                Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOn,ENABLE_REQUEST);

            }

            else{
                Toast.makeText(MainActivity.this,
                        "Bluetooth già attivo su questo dispositivo.",
                        Toast.LENGTH_LONG).show();
            }

        }

        else{
            BA.disable();
            clearListView();
        }
    }


    //______________________________BLUE_THINGS_FRAGMENT_METHODS____________________________________

    public void startChat(){

        Intent friend_device_intent = new Intent(MainActivity.this, ChatActivity.class);
        friend_device_intent.putExtra("theDeviceName",my_bluetooth_device.getName());
        friend_device_intent.putExtra("theDeviceAddress", my_bluetooth_device.getAddress());

        startActivity(friend_device_intent);
    }

    public void startWalkieTalkie(){
        Intent friend_device_intent = new Intent(MainActivity.this, WalkieTalkieActivity.class);
        friend_device_intent.putExtra("theDeviceName",my_bluetooth_device.getName());
        friend_device_intent.putExtra("theDeviceAddress", my_bluetooth_device.getAddress());

        startActivity(friend_device_intent);
    }

    public void startPictureChat(){
        Intent friend_device_intent = new Intent(MainActivity.this, PictureChatActivity.class);
        friend_device_intent.putExtra("theDeviceName",my_bluetooth_device.getName());
        friend_device_intent.putExtra("theDeviceAddress", my_bluetooth_device.getAddress());

        startActivity(friend_device_intent);
    }


    //_________________________CONTACTS_FRAGMENT_METHODS____________________________________________

    public void getContactsOnListView(){

        Cursor cursor = myDB.viewContactList();

        if (cursor.getCount() == 0){
            Toast.makeText(this,"Lista dei contatti vuota.",
                    Toast.LENGTH_LONG).show();
        }
        else{

            while(cursor.moveToNext()){

                Contact contact;
                String nick = cursor.getString(3);
                byte[] blob = cursor.getBlob(4);
                if(blob != null){
                    Bitmap avatar = BitmapFactory.decodeByteArray(blob,0,blob.length);
                    contact = new Contact(avatar,nick);
                } else{
                    contact = new Contact(nick);
                }

                contacts.add(contact);

            }
        }

        caa = new ContactArrayAdapter(MainActivity.this,
                R.layout.contact_adapter_view, contacts);
        contacts_listview.setAdapter(caa);



    }

    public boolean query_text_change(String query){
        caa.getFilter().filter(query);
        return false;
    }

    public boolean query_text_submit(String query){return false;}

    public void getContactInfos(int position){

        contact_info = contacts.get(position);
        Cursor cursor = myDB.getContactInfos(contact_info.getNick_name());
        while (cursor.moveToNext()){

            contact_info.setDevice_name(cursor.getString(1));
            contact_info.setDevice_address(cursor.getString(2));
            if(cursor.getBlob(4)!= null){
                contact_info.setAvatar(BitmapFactory.decodeByteArray(cursor.getBlob(4),
                        0, cursor.getBlob(4).length));
            }
            contact_info.setNick_name(cursor.getString(3));
        }

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        fragment = new ContactOptionFragment();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();



    }

    public void clearContactListView(){

        contacts.clear();
        caa = new ContactArrayAdapter(MainActivity.this,R.layout.contact_adapter_view,
                contacts);
        contacts_listview.setAdapter(caa);
    }


    //_________________________CONTACT_OPTIONS_FRAGMENT_METHODS_____________________________________

    public void setUpContactInfos(){

        if(contact_info.getAvatar() != null){
            info_avatar.setImageBitmap(contact_info.getAvatar());
        }

        info_nick_name.setText(contact_info.getNick_name());
        info_device_name.setText(contact_info.getDevice_name());
        info_device_address.setText(contact_info.getDevice_address());

    }

    public void deleteContact(View view){

        myDB.deleteContact(contact_info.getNick_name());
        clearContactListView();
        dismissDialog(ARE_U_SURE_DIALOG);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        fragment = new ContactsFragment();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();

    }

    public void updateContact(String newNick){
        byte[] image = null;
        if(newAvatar != null){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            newAvatar.compress(Bitmap.CompressFormat.PNG, 100, stream);
            image = stream.toByteArray();
        }
        myDB.updateContact(contact_info.getDevice_name(),newNick,image);

        clearContactListView();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        fragment = new ContactsFragment();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

    public void showAvailableAvatar(){
        showDialog(AVATAR_DIALOG_ID);
    }

    public void storeOfflineMessage(String device_name, String text){

        int id = -1;
        Cursor cursor = myDB.getChatIdByDeviceName(device_name);
        while(cursor.moveToNext()){
            id = cursor.getInt(0);
        }

        if(id != -1){
            myDB.insertOfflineChatRaw(id,position,text);
            Toast.makeText(MainActivity.this, "Il messaggio offline è stato aquisito. " +
                            "Sarà inviato non appena aperta la connessione in chat con" + device_name,
                    Toast.LENGTH_LONG).show();

        } else{
            Toast.makeText(MainActivity.this, "Chat non trovata.",
                    Toast.LENGTH_LONG).show();
        }


    }

    @Override
    protected Dialog onCreateDialog(int id) {

        Dialog dialog = null;

        switch(id){

            case AVATAR_DIALOG_ID:

                dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.avatar_dialog_layout);
                dialog.setTitle("Avatar Dialog");
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);

                avatar_1 = dialog.findViewById(R.id.avatar_1);
                avatar_2 = dialog.findViewById(R.id.avatar_2);
                avatar_3 = dialog.findViewById(R.id.avatar_3);
                avatar_4 = dialog.findViewById(R.id.avatar_4);
                avatar_5 = dialog.findViewById(R.id.avatar_5);
                avatar_6 = dialog.findViewById(R.id.avatar_6);
                avatar_7 = dialog.findViewById(R.id.avatar_7);
                avatar_8 = dialog.findViewById(R.id.avatar_8);
                avatar_9 = dialog.findViewById(R.id.avatar_9);
                avatar_10 = dialog.findViewById(R.id.avatar_10);
                avatar_11= dialog.findViewById(R.id.avatar_11);
                avatar_12 = dialog.findViewById(R.id.avatar_12);
                avatar_13 = dialog.findViewById(R.id.avatar_13);
                avatar_14 = dialog.findViewById(R.id.avatar_14);
                avatar_15 = dialog.findViewById(R.id.avatar_15);
                avatar_16= dialog.findViewById(R.id.avatar_16);

                break;

            case ARE_U_SURE_DIALOG:

                dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.sure_dialog_layout);
                dialog.setTitle("Sure Dialog");
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);

                break;

            case VIEW_CHAT_DIALOG_ID:

                dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.view_chat_dialog_layout);
                dialog.setTitle("ViewChat Dialog");
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);
                viewChatNick = dialog.findViewById(R.id.viewchat_dialog_friendNick);
                viewChatLv = dialog.findViewById(R.id.viewchat_dialog_listView);

                break;

            case OPEN_PICTURE_DIALOG_ID:

                dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.open_picture_dialog_layout);
                dialog.setTitle("OpenPicture Dialog");
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);
                opened_picture = dialog.findViewById(R.id.opened_picture);
                break;
        }

        return dialog;
    }
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch(id){
            case AVATAR_DIALOG_ID:
                break;
            case ARE_U_SURE_DIALOG:
                break;
            case VIEW_CHAT_DIALOG_ID:
                break;
            case OPEN_PICTURE_DIALOG_ID:
                break;
        }
    }

    public void areUSure(){
        showDialog(ARE_U_SURE_DIALOG);
    }

    public void dismiss(View view){dismissDialog(ARE_U_SURE_DIALOG);}

    public void choseAvatar(View view){

        switch(contact_or_user){
            case 1:
                if(view == avatar_1){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_1);
                    info_avatar.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_2){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_2);
                    info_avatar.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_3){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_3);
                    info_avatar.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_4){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_4);
                    info_avatar.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_5){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_5);
                    info_avatar.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_6){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_6);
                    info_avatar.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_7){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_7);
                    info_avatar.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_8){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_8);
                    info_avatar.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_9){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_rick);
                    info_avatar.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_10){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_morty);
                    info_avatar.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_11){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_jabba_the_hutt);
                    info_avatar.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_12){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_quai_gon_jin);
                    info_avatar.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_13){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_obi_one);
                    info_avatar.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_14){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_lord_vader);
                    info_avatar.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_15){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_luke_skywalker);
                    info_avatar.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_16){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_chubbe);
                    info_avatar.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                break;

            case 2:
                if(view == avatar_1){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_1);
                    my_avatar_profile.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_2){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_2);
                    my_avatar_profile.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_3){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_3);
                    my_avatar_profile.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_4){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_4);
                    my_avatar_profile.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_5){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_5);
                    my_avatar_profile.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_6){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_6);
                    my_avatar_profile.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_7){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_7);
                    my_avatar_profile.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_8){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_8);
                    my_avatar_profile.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_9){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_rick);
                    my_avatar_profile.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_10){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_morty);
                    my_avatar_profile.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_11){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_jabba_the_hutt);
                    my_avatar_profile.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_12){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_quai_gon_jin);
                    my_avatar_profile.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_13){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_obi_one);
                    my_avatar_profile.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_14){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_lord_vader);
                    my_avatar_profile.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_15){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_luke_skywalker);
                    my_avatar_profile.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                if(view == avatar_16){
                    newAvatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_chubbe);
                    my_avatar_profile.setImageBitmap(newAvatar);
                    dismissDialog(AVATAR_DIALOG_ID);
                }
                break;
        }

    }


    //_______________ACCOUNT_FRAGMENT_METHODS_______________________________________________________


    public void setCursors(){
        cursor_pictures = myDB.getPictures();
        cursor_chats = myDB.viewContactList();
    }

    public void viewRecyclers(){

        llm_chats = new LinearLayoutManager( this, LinearLayoutManager.HORIZONTAL, false);
        llm_pictures = new LinearLayoutManager( this, LinearLayoutManager.HORIZONTAL, false);
        recycler_chats.setLayoutManager(llm_chats);
        recycler_pictures.setLayoutManager(llm_pictures);
        cra_1 = new ChatsRecyclerviewAdapter(this, 1, cursor_chats);
        recycler_chats.setAdapter(cra_1);
        cra_2 = new ChatsRecyclerviewAdapter(this, 2, cursor_pictures);
        recycler_pictures.setAdapter(cra_2);

    }

    public void view_chat_on_dialog(int id, String nick){
        showDialog(VIEW_CHAT_DIALOG_ID);
        viewChatNick.setText(nick);
        Cursor cursor = myDB.getChat(id);
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
            ArrayAdapter adapter = new ArrayAdapter(MainActivity.this,
                    android.R.layout.simple_list_item_1, cronology_messages_list);
            viewChatLv.setAdapter(adapter);
        }
    }

    BroadcastReceiver chat_id_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int chat_id = intent.getIntExtra("chatID",-1);
            String nick = intent.getStringExtra("nick");
            if(chat_id != -1){
                view_chat_on_dialog(chat_id, nick);
            }
        }
    };

    public void open_picture(Bitmap picture_to_open){
        showDialog(OPEN_PICTURE_DIALOG_ID);
        opened_picture.setImageBitmap(picture_to_open);
    }

    BroadcastReceiver open_picture_receiver= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int id = intent.getIntExtra("pictID",-1);
            Cursor cursor = myDB.getPicture(id);
            byte[] blob = null;
            while(cursor.moveToNext()){
                blob = cursor.getBlob(0);
            }
            if(blob != null){
                Bitmap picture_to_open = BitmapFactory.decodeByteArray(blob, 0, blob.length);
                open_picture(picture_to_open);
            }
        }
    };

    public void setUpUserInfos(){
        Cursor cursor = myDB.getUserInfos();
        String nick = null;
        byte[] avatar = null;
        while(cursor.moveToNext()){
            nick = cursor.getString(0);
            avatar = cursor.getBlob(1);
        }
        if(nick != null){
            my_nick_profile.setText(nick);
        }
        if(avatar != null){
            my_avatar_profile.setImageBitmap(BitmapFactory.decodeByteArray(avatar,0,avatar.length));
        }
    }

    public void update_user(String newNick){
        byte[] image = null;
        if(newAvatar != null){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            newAvatar.compress(Bitmap.CompressFormat.PNG, 100, stream);
            image = stream.toByteArray();
        }
        myDB.updateUserNick(newNick);
        myDB.updateUserAvatar(image);


        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        fragment = new AccountFragment();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }



    //__________________FRAGMENT_____CHANGES________________________________________________________


    public void changeFragment(View view){

        if( view == findViewById(R.id.bt_settings_btn)){

            int y_pos = settings_btn.getTop();
            int x_pos = settings_btn.getLeft();
            hScroll.smoothScrollTo(x_pos,y_pos);

            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            fragment = new BTSettingsFragment();
            ft.replace(R.id.fragment_container, fragment);
            ft.commit();

            settings_btn.setBackgroundResource(R.drawable.button_selected);
            contacts_btn.setBackgroundResource(R.drawable.button_enabled);
            profile_btn.setBackgroundResource(R.drawable.button_enabled);
            if (BlueThings_btn.isClickable()){
                BlueThings_btn.setBackgroundResource(R.drawable.button_enabled);
            }

        }

        if( view == findViewById(R.id.blue_things_btn)){

            int y_pos = BlueThings_btn.getTop();
            int x_pos = BlueThings_btn.getLeft();
            hScroll.smoothScrollTo(x_pos,y_pos);

            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            fragment = new BlueThingsFragment();
            ft.replace(R.id.fragment_container, fragment);
            ft.commit();

            BlueThings_btn.setBackgroundResource(R.drawable.button_selected);
            contacts_btn.setBackgroundResource(R.drawable.button_enabled);
            settings_btn.setBackgroundResource(R.drawable.button_enabled);
            profile_btn.setBackgroundResource(R.drawable.button_enabled);
        }

        if( view == findViewById(R.id.contact_list_btn)){

            if(contacts.size()!=0){
                clearContactListView();
            }

            int y_pos = contacts_btn.getTop();
            int x_pos = contacts_btn.getLeft();
            hScroll.smoothScrollTo(x_pos,y_pos);

            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            fragment = new ContactsFragment();
            ft.replace(R.id.fragment_container, fragment);
            ft.commit();

            contacts_btn.setBackgroundResource(R.drawable.button_selected);
            settings_btn.setBackgroundResource(R.drawable.button_enabled);
            profile_btn.setBackgroundResource(R.drawable.button_enabled);
            if (BlueThings_btn.isClickable()){
                BlueThings_btn.setBackgroundResource(R.drawable.button_enabled);
            }
        }

        if( view == findViewById(R.id.profile_btn)){

            int y_pos = profile_btn.getTop();
            int x_pos = profile_btn.getRight();
            hScroll.smoothScrollTo(x_pos,y_pos);

            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            fragment = new AccountFragment();
            ft.replace(R.id.fragment_container, fragment);
            ft.commit();

            profile_btn.setBackgroundResource(R.drawable.button_selected);
            settings_btn.setBackgroundResource(R.drawable.button_enabled);
            contacts_btn.setBackgroundResource(R.drawable.button_enabled);
            if (BlueThings_btn.isClickable()){
                BlueThings_btn.setBackgroundResource(R.drawable.button_enabled);
            }
        }
    }






}
