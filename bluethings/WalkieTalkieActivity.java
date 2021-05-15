package com.example.gianmarco.bluethings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gianmarco.bluethings.BTSERVICES.BluetoothConnectionService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class WalkieTalkieActivity extends AppCompatActivity {


    private static BluetoothConnectionService my_connection_service;
    private static BluetoothDevice my_friend_device;
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaRecorder;
    private ImageButton btn_record;
    private ProgressBar progressBar;
    private String pathSave;

    final int REQUEST_PERMISSION_CODE = 1000;

    Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();


    //BROADCAST RECEIVER: RICEZIONE CLIP AUDIO DA DISPOSITIVO ASSOCIATO
    BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Toast.makeText(getApplicationContext(),
                    "Clip in arrivo!",
                    Toast.LENGTH_SHORT).show();

            byte[] buffer = intent.getByteArrayExtra("clip");

            String pathSave_incoming =
                    Environment.getExternalStorageDirectory().
                    getAbsolutePath() + "/" + UUID.randomUUID().toString()
                            + "_audio_record.3gp";

            //Save the clip into File in external device storage
            File incoming_clip = new File(pathSave_incoming);
            toFile(buffer,incoming_clip);

            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(pathSave_incoming);
                mediaPlayer.prepare();

            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaPlayer.start();



        }
    };


    //______________METODI__________________________________________________________________________



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkie_talkie);
        View view = this.getWindow().getDecorView();

        this.getWindow().setBackgroundDrawableResource(R.drawable.sword_world);

        Intent receivedIntent = getIntent();

        BluetoothConnectionService.type = 2;
        my_connection_service = new BluetoothConnectionService(WalkieTalkieActivity.this);
        String deviceName = receivedIntent.getStringExtra("theDeviceName");
        String deviceAddress = receivedIntent.getStringExtra("theDeviceAddress");

        TextView friend_name = (TextView) findViewById(R.id.friend_name);
        TextView wt = (TextView) findViewById(R.id.title);
        ImageButton back = (ImageButton) findViewById(R.id.back);

        for (BluetoothDevice device : pairedDevices){
            if(device.getName().equals(deviceName) && device.getAddress().equals(deviceAddress)){
                my_friend_device = device;

                friend_name.setText(my_friend_device.getName().toUpperCase());

            }
        }

        try{
            startConnectionChat();

        } catch(Exception e){
            e.printStackTrace();
        }


        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter("incomingClip"));


        btn_record = (ImageButton) findViewById(R.id.button_record);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });



        //              ----------  Concerning records  -----------



        if(checkPermissionFromDevice()){

            btn_record.setOnTouchListener(new View.OnTouchListener(){

                @Override
                public boolean onTouch(View v, MotionEvent event){

                    switch(event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            //while touching...
                            progressBar.setVisibility(View.VISIBLE);
                            Toast.makeText(getApplicationContext(),
                                    "Registrazione in corso... Rilascia per inviare la clip.",
                                    Toast.LENGTH_SHORT).show();

                            pathSave = Environment.getExternalStorageDirectory().
                                    getAbsolutePath() + "/" + UUID.randomUUID().toString()
                                    + "_audio_record.3gp";

                            setupMediaRecorder();

                            try{
                                mediaRecorder.prepare();
                                mediaRecorder.start();
                            } catch(IOException ioe){
                                ioe.printStackTrace();
                            }

                            return true;

                        case MotionEvent.ACTION_UP:
                            //when leaving...

                            progressBar.setVisibility(View.GONE);
                            mediaRecorder.stop();
                            mediaRecorder.release();
                            mediaRecorder = null;


                            File audioClip = new File(pathSave);

                            try{
                                byte[] clip_to_send = getBytes(audioClip);

                                int chunk_size = 400;

                                //INVIO LA DIMENSIONE DELLA CLIP
                                my_connection_service.write(String.valueOf(clip_to_send.length).getBytes());

                                for(int i = 0; i < clip_to_send.length ; i+= chunk_size){

                                    byte[] tempArray;

                                    tempArray = Arrays.copyOfRange(clip_to_send, i ,
                                            Math.min( clip_to_send.length, i + chunk_size));

                                    //INVIO UNO ALLA VOLTA I CHUNKS, DOPO CHE IL FLAG DEL SERVICE è MESSO A FALSE
                                    my_connection_service.write(tempArray);

                                }

                            } catch (FileNotFoundException fnfe){
                                fnfe.printStackTrace();
                            } catch(IOException ioe){
                                ioe.printStackTrace();
                            }

                            Toast.makeText(getApplicationContext(),
                                    "Clip inviata con successo.",
                                    Toast.LENGTH_SHORT).show();


                            return true;

                    }
                    return false;
                }
            });


        } else{

            requestPermission();

        }

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

    private void setupMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(pathSave);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    }


//-----------------------PERMISSIONS----------------------------------------------------------------


    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
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

        int record_audio_result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        return (write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED &&
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






    public static byte[] getBytes(File file) throws FileNotFoundException, IOException{

        byte[] buffer = new byte[20480];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(file);
        int read;
        while( (read = fis.read(buffer)) != -1 ) {
            baos.write(buffer, 0 , read);
        }
        fis.close();
        baos.close();
        return baos.toByteArray();
    }


    public static void toFile(byte[] data, File destination) {

        try (FileOutputStream fos = new FileOutputStream(destination)) {

            fos.write(data);
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

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


