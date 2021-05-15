package com.example.gianmarco.bluethings.UTILS;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gianmarco.bluethings.DAO.DatabaseHelper;
import com.example.gianmarco.bluethings.ENTITY.Contact;
import com.example.gianmarco.bluethings.MainActivity;
import com.example.gianmarco.bluethings.R;

import java.util.ArrayList;

public class DeviceListAdapter extends ArrayAdapter {

    private Context context;
    int resource;
    private ArrayList<BluetoothDevice> devices;
    public static Cursor cursor = null;

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    public DeviceListAdapter(Context context, int resource, ArrayList<BluetoothDevice> object){
        super(context, resource, object);

        this.context = context;
        this.resource = resource;
        this.devices = object;

    }


    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent){


        BluetoothDevice device = (BluetoothDevice) getItem(position);

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);

        final TextView dispositivo_associato_nome = (TextView) convertView.findViewById(R.id.dispositivo_associato_nome);
        final TextView dispositivo_associato_indirizzo = (TextView) convertView.findViewById(R.id.dispositivo_associato_indirizzo);
        final ImageView img = (ImageView) convertView.findViewById(R.id.bluetooth_image);

        String deviceName = device.getName();
        String deviceAddress = device.getAddress();

        if(cursor!= null){
            while(cursor.moveToNext()){
                if(deviceName.equals(cursor.getString(1)) &&
                        deviceAddress.equals(cursor.getString(2))){
                    deviceName = cursor.getString(3);
                    deviceAddress = "";
                    if(cursor.getBlob(4) != null){
                        Bitmap avatar = BitmapFactory.decodeByteArray(cursor.getBlob(4),
                                0,cursor.getBlob(4).length);
                        img.setImageBitmap(avatar);
                    }
                }
            }
        }


        dispositivo_associato_nome.setText(deviceName);
        dispositivo_associato_indirizzo.setText(deviceAddress);


        return convertView;

    }
}
