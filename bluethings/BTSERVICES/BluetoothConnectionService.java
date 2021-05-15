package com.example.gianmarco.bluethings.BTSERVICES;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;



//BLUETOOTH CHAT SERVICE ("CHAT HANDLER"-CLASS)

public class BluetoothConnectionService {


    //_____________________________ATTRIBUTI________________________________________________________

    public static int type;

    private static final String appName = "BLUETOOTHCHAT";

    /*
    Definizione dell'attributo Universal Unique IDentifier (insecure)
    Questo identificatore viene utilizzato come indirizzo per lo scambio di pacchetti
    tramite bluetooth, tra due dispositivi
    */
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");


    private final BluetoothAdapter my_bluetooth_adapter = BluetoothAdapter.getDefaultAdapter();


    Context myContext;


    private AcceptThread my_insecure_accept_thread;

    private ConnectThread my_connect_thread;

    private ConnectedThread my_connected_thread;


    private BluetoothDevice my_device;

    private UUID deviceUUID;

    ProgressDialog my_progress_dialog;







    //_____________________________COSTRUTTORE______________________________________________________


    public BluetoothConnectionService(){}

    public BluetoothConnectionService(Context context){
        myContext = context;
        startChat();
    }



    //__________________________INNER CLASSES_______________________________________________________


    private class AcceptThread extends Thread{
        /*Questo thread corre mentre attende per l'avvenimento di una connessione
        Corre finchè una connessione non viene stabilita o cancellata.
        */


        private final BluetoothServerSocket my_server_socket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            //Creazione di un "Listening Server Socket"
            try {
                tmp = my_bluetooth_adapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID);

            } catch (IOException e){
            }
            my_server_socket = tmp;
        }


        public void run(){

            BluetoothSocket socket = null;

            try{
                //Questo metodo non ritorna nulla finchè la connessione non è stabilita!
                socket = my_server_socket.accept();


            } catch(Exception e){

            }

            if (socket != null){
                connected(socket,my_device);
            }
        }

        public void cancel(){
            try{
                my_server_socket.close();

            } catch(Exception e){

            }
        }

    }






    private class ConnectThread extends Thread{

        /*
        Questo Thread Corre mentre tenta di far partire la connessione con un altro dispositivo.
        La connessione può essere stabilita oppure fallire.
         */

        private BluetoothSocket my_socket;



        public ConnectThread(BluetoothDevice device, UUID uuid){

            my_device = device;
            deviceUUID = uuid;

        }



        public void run(){

            BluetoothSocket tmp = null;

            try{

                tmp = my_device.createRfcommSocketToServiceRecord(deviceUUID);

            } catch(IOException e){

            }

            my_socket = tmp;

            //cancelDiscovery(): è chiamato per non rallentare la connessione.
            my_bluetooth_adapter.cancelDiscovery();

            try {
                 /*
                BLOCKING CALL: ritorna solo a connessione avvenuta con successo,
                oppure se sollevata un'Eccezione
                */

                my_socket.connect();


            } catch (IOException e) {

                try{

                    my_socket.close();

                } catch(IOException e2){


                }

            }

            connected(my_socket,my_device);

        }



        public void cancel(){

            try{
                my_socket.close();

            } catch(Exception e){

            }
        }



    }







    private class ConnectedThread extends Thread{

        /*
        ConnectedThread è responsabile di mantenere la connessione stabile, una volta creata,
        e si occupa dello scambio di dati, in ingresso e uscita, con il device associato.
         */

        private final BluetoothSocket my_socket;

        private final InputStream my_in_stream;

        private final OutputStream my_out_stream;


        public ConnectedThread(BluetoothSocket socket){

            my_socket = socket;

            InputStream in_tmp = null;

            OutputStream out_tmp = null;

            try{
                //elimina il progress dialog quando la connessione è stabilita
                my_progress_dialog.dismiss();
            } catch(NullPointerException e){

            }


            try{
                in_tmp = my_socket.getInputStream();
                out_tmp = my_socket.getOutputStream();
            } catch(IOException e){

            }

            my_in_stream = in_tmp;

            my_out_stream = out_tmp;
        }


        public void run(){

            byte[] buffer = null;

            int bytes;

            int index = 0;

            int number_of_bytes = 0;

            boolean flag = true;

            while(true){


                if (flag){

                    try{
                        byte[] temp = new byte [my_in_stream.available()] ;

                        if (my_in_stream.read(temp) > 0){

                            number_of_bytes = Integer.parseInt(new String(temp,"UTF-8"));

                            buffer = new byte[number_of_bytes];

                            flag = false;

                        }



                    } catch(IOException e){
                        //Chiuso il canale di comunicazione in ingresso, interrompe il ciclo.
                        break;
                    }

                } else{

                    try {

                        byte[] data = new byte[my_in_stream.available()];

                        int numbers = my_in_stream.read(data);

                        System.arraycopy(data,0,buffer,index,numbers);

                        index = index + numbers;

                        if (index == number_of_bytes){

                            switch(type){

                                case 1:
                                    //CHAT

                                    Intent incomingMessageIntent = new Intent("incomingMessage");
                                    incomingMessageIntent.putExtra("message",buffer);
                                    LocalBroadcastManager.getInstance(myContext).sendBroadcast(incomingMessageIntent);

                                    index = 0;
                                    number_of_bytes = 0;
                                    buffer = null;
                                    flag = true;

                                    break;

                                case 2:
                                   //WALKIETALKIE
                                    Intent incomingClipIntent = new Intent("incomingClip");
                                    incomingClipIntent.putExtra("clip",buffer);
                                    LocalBroadcastManager.getInstance(myContext).sendBroadcast(incomingClipIntent);

                                    index = 0;
                                    number_of_bytes = 0;
                                    buffer = null;
                                    flag = true;

                                    break;

                                case 3:
                                    //PICTURECHAT
                                    Intent incomingPictureIntent = new Intent("incomingPicture");
                                    incomingPictureIntent.putExtra("picture",buffer);
                                    LocalBroadcastManager.getInstance(myContext).sendBroadcast(incomingPictureIntent);

                                    index = 0;
                                    number_of_bytes = 0;
                                    buffer = null;
                                    flag = true;

                                    break;

                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        }


        //Chiamare questo metodo dalla Main Activity per chiudere la connessione.
        public void cancel(){

            try{
                my_socket.close();

            } catch(Exception e){

            }
        }


        //Chiamare questo metodo dalla Main Activity per inviare un messaggio al device associato.
        public void write(byte[] bytes){

            try {
                my_out_stream.write(bytes);
                my_out_stream.flush();

            } catch (IOException e) {

            }
        }




    }



    //______________________________METODI__________________________________________________________




    /*
    START DEL CHAT SERVICE: in particolare, questo metodo inizializza l'AcceptThread
    del BluetoothConnectionService
     */
    public synchronized void startChat(){

        //Cancellazione di qualsiasi thread che sta tentando una connessione
        if(my_connect_thread != null){

            my_connect_thread.cancel();
            my_connect_thread = null;

        }

        if(my_insecure_accept_thread == null){

            my_insecure_accept_thread = new AcceptThread();
            my_insecure_accept_thread.start();

        }
    }


    /*
    Una volta che AcceptThread ha avuto inizio, aspetta una connessione:
    ConnectThread tenta di stabilire una connessione con un altro device.
     */
    public void startClient(BluetoothDevice device, UUID uuid){

        my_progress_dialog = ProgressDialog.show(myContext,"Connessione Bluetooth",
                "Attendere...", true);

        my_connect_thread = new ConnectThread(device,uuid);

        my_connect_thread.start();



    }


    /*
    Chiamata del Metodo che gestisce la connessione e la trasmissione dei dati tra i dispositivi.
     */
    private void connected(BluetoothSocket my_socket, BluetoothDevice my_device){

        my_connected_thread = new ConnectedThread(my_socket);
        my_connected_thread.start();

    }


    //scrivere al ConnectedThread in modo non sincronizzato.
    public void write(byte[] out){
        try{
            my_connected_thread.write(out);

        }catch (Exception e){

        }
    }

    public synchronized void stop() {

        if (my_connect_thread != null) {
            my_connect_thread.cancel();
            my_connect_thread = null;
        }

        if (my_connected_thread != null) {
            my_connected_thread.cancel();
            my_connected_thread = null;
        }

        if (my_insecure_accept_thread != null) {
            my_insecure_accept_thread.cancel();
            my_insecure_accept_thread = null;
        }

    }
}


