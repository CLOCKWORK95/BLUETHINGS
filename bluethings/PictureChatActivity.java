package com.example.gianmarco.bluethings;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gianmarco.bluethings.BTSERVICES.BluetoothConnectionService;
import com.example.gianmarco.bluethings.DAO.DatabaseHelper;
import com.example.gianmarco.bluethings.UTILS.PictureArrayAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class PictureChatActivity extends AppCompatActivity {

    DatabaseHelper myDB = new DatabaseHelper(this);
    Button btn_up,btn_save,btn_no;
    TextView textFolder;
    static final int CUSTOM_DIALOG_ID = 0;
    static final int SAVEPICTURE_DIALOG_ID = 1;
    Bitmap saving_picture = null;
    ListView dialog_list_view;
    File root;
    File curFolder;
    ProgressBar pb;
    boolean send_flag = false;
    String path = null;
    ImageButton send_picture;
    ListView picturesListView;
    ArrayList<Bitmap> pictures_array = new ArrayList<Bitmap>();
    private List<String> fileList = new ArrayList<String>();
    private static BluetoothConnectionService my_connection_service;
    private static BluetoothDevice my_friend_device;
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    final int REQUEST_PERMISSION_CODE = 1000;

    Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();


    //BROADCAST RECEIVER: RICEZIONE CLIP AUDIO DA DISPOSITIVO ASSOCIATO
    BroadcastReceiver myReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Toast.makeText(getApplicationContext(),
                    "Immagine in arrivo!",
                    Toast.LENGTH_SHORT).show();

            byte[] incoming_picture_bytes = intent.getByteArrayExtra("picture");

            Bitmap picture = BitmapFactory.decodeByteArray(incoming_picture_bytes, 0,
                    incoming_picture_bytes.length);

            pictures_array.add(picture);


            PictureArrayAdapter adapter = new PictureArrayAdapter(PictureChatActivity.this,
                   R.layout.picture_array_adapter_view , pictures_array );
            picturesListView.setAdapter(adapter);

            picturesListView.smoothScrollToPosition(pictures_array.size()-1);


        }
    };


    //______________METODI__________________________________________________________________________


    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_chat);
        View view = this.getWindow().getDecorView();

        this.getWindow().setBackgroundDrawableResource(R.drawable.sword_world);

        picturesListView = (ListView) findViewById(R.id.pictures_lv);
        TextView friend_name = (TextView) findViewById(R.id.friend_device_name);
        ImageButton pick_a_file = (ImageButton) findViewById(R.id.pick_a_file_btn);
        ImageButton back = (ImageButton) findViewById(R.id.back);
        send_picture = (ImageButton) findViewById(R.id.send_picture_btn);
        send_picture.setClickable(false);
        pb = findViewById(R.id.progress_picture_bar);

        Intent receivedIntent = getIntent();
        String deviceName = receivedIntent.getStringExtra("theDeviceName");
        String deviceAddress = receivedIntent.getStringExtra("theDeviceAddress");

        for (BluetoothDevice device : pairedDevices){
            if(device.getName().equals(deviceName) && device.getAddress().equals(deviceAddress)){
                my_friend_device = device;
                friend_name.setText(my_friend_device.getName().toUpperCase());
            }
        }

        BluetoothConnectionService.type = 3;
        my_connection_service = new BluetoothConnectionService(PictureChatActivity.this);

        try{
            startConnectionChat();

        } catch(Exception e){
            e.printStackTrace();
        }


        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter("incomingPicture"));


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });


        pick_a_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(CUSTOM_DIALOG_ID);

            }
        });

        send_picture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (send_flag = true && path != null){
                    pb.setVisibility(View.VISIBLE);
                    try{
                        Bitmap picture = BitmapFactory.decodeFile(path);
                        pictures_array.add(picture);
                        PictureArrayAdapter adapter = new PictureArrayAdapter(
                                PictureChatActivity.this,
                                R.layout.picture_array_adapter_view , pictures_array );
                        picturesListView.setAdapter(adapter);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        picture.compress(Bitmap.CompressFormat.PNG, 50 , baos);
                        byte[] imageBytes = baos.toByteArray();

                        int chunk_size = 400;

                        //INVIO LA DIMENSIONE DELLA CLIP
                        my_connection_service.write(String.valueOf(imageBytes.length).getBytes());


                        for(int i = 0; i < imageBytes.length ; i+= chunk_size){

                            byte[] tempArray;

                            tempArray = Arrays.copyOfRange(imageBytes, i ,
                                    Math.min( imageBytes.length, i + chunk_size));

                            //INVIO UNO ALLA VOLTA I CHUNKS, DOPO CHE IL FLAG DEL SERVICE è MESSO A FALSE
                            my_connection_service.write(tempArray);


                        }

                        path = null;
                        send_flag = false;
                        send_picture.setClickable(false);

                        Toast.makeText(PictureChatActivity.this,
                                "Immagine inviata con successo.",
                                Toast.LENGTH_LONG).show();
                        pb.setVisibility(View.GONE);

                    } catch (Exception e){
                        Toast.makeText(PictureChatActivity.this,
                                "Il file selezionato non è compatibile.",
                                Toast.LENGTH_LONG).show();
                        path = null;
                        send_flag = false;
                        send_picture.setClickable(false);
                        pb.setVisibility(View.GONE);

                        e.printStackTrace();

                    }
                }
            }
        });

        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        curFolder = root;

        picturesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                saving_picture = pictures_array.get(position);
                showDialog(SAVEPICTURE_DIALOG_ID);
            }
        });


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

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "Premi la freccia in alto a destra!", Toast.LENGTH_LONG).show();
    }


    //------------------DIALOGS---------------------------------------------------------------------

    @Override
    protected Dialog onCreateDialog(int id) {

        Dialog dialog = null;

        switch(id){

            case CUSTOM_DIALOG_ID:
                dialog = new Dialog(PictureChatActivity.this);
                dialog.setContentView(R.layout.dialog_layout);
                dialog.setTitle("Custom Dialog");
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);

                textFolder = (TextView) dialog.findViewById(R.id.folder);
                btn_up = (Button) dialog.findViewById(R.id.up_btn);

                btn_up.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ListDir(curFolder.getParentFile());
                    }
                });

                dialog_list_view = (ListView) dialog.findViewById(R.id.dialogList);
                dialog_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        File selected = new File(fileList.get(position));
                        path = fileList.get(position);
                        if(selected.isDirectory()){
                            ListDir(selected);

                        } else{
                            Toast.makeText(PictureChatActivity.this,
                                    selected.toString() + " selected",
                                    Toast.LENGTH_LONG).show();

                            send_flag = true;
                            send_picture.setClickable(true);

                            dismissDialog(CUSTOM_DIALOG_ID);

                        }
                    }
                });
                break;

            case SAVEPICTURE_DIALOG_ID:
                dialog = new Dialog(PictureChatActivity.this);
                dialog.setContentView(R.layout.save_picture_dialog_layout);
                dialog.setTitle("Save Picture Dialog");
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);

        }

        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch(id){
            case CUSTOM_DIALOG_ID:
                ListDir(curFolder);
                break;
            case SAVEPICTURE_DIALOG_ID:
                break;
        }
    }


    public void ListDir(File file){
        if(file.equals(root)){
            btn_up.setEnabled(false);

        } else{
            btn_up.setEnabled(true);
        }
        curFolder = file;
        textFolder.setText(file.getPath());

        File[] files = file.listFiles();
        fileList.clear();

        for(File f : files){
            fileList.add(f.getAbsolutePath());
        }

        ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, fileList);
        dialog_list_view.setAdapter(directoryList);

    }


    public void savePicture(View v) {
        byte[] blob = null;
        if(saving_picture != null){
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            saving_picture.compress(Bitmap.CompressFormat.PNG, 100, stream);
            blob = stream.toByteArray();
            myDB.insertPicture(blob);
        }
        saving_picture = null;
        dismissDialog(SAVEPICTURE_DIALOG_ID);
    }


    public void dismiss(View v){
        dismissDialog(SAVEPICTURE_DIALOG_ID);
    }


    //-----------------------PERMISSIONS------------------------------------------------------------


    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                },
                REQUEST_PERMISSION_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        switch(requestCode)
        {
            case REQUEST_PERMISSION_CODE:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Permesso accordato", Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(this,"Permesso negato", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }

    private boolean checkPermissionFromDevice(){

        int write_external_storage_result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int read_external_storage_result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        return (write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                read_external_storage_result == PackageManager.PERMISSION_GRANTED);

    }



//--------------------------------------------------------------------------------------------------




//Metodo per iniziare la chat bluetooth (l'app va in crash se non ho fatto il match tra i device!)

    public void startConnectionChat(){
        startBluetoothConnection(my_friend_device, MY_UUID);
    }


    public void startBluetoothConnection(BluetoothDevice device, UUID uuid){

        my_connection_service.startClient(device,uuid);

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
