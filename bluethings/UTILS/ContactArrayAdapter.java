package com.example.gianmarco.bluethings.UTILS;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.gianmarco.bluethings.ENTITY.Contact;
import com.example.gianmarco.bluethings.R;
import java.util.ArrayList;



public class ContactArrayAdapter extends ArrayAdapter {

    private Context context;
    int resource;
    private ArrayList<Contact> contacts;

    private CustomFilter filter;
    private ArrayList<Contact> filterList;


    public ContactArrayAdapter(Context context, int resource, ArrayList<Contact> contatti){
        super(context, resource, contatti);

        this.context = context;
        this.resource = resource;
        this.contacts = contatti;
        this.filterList = contacts;

    }


    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent){


        Contact contact = (Contact) getItem(position);


        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);

        final TextView nick_name = (TextView) convertView.findViewById(R.id.nick_name_contact);

        final ImageView image = (ImageView) convertView.findViewById(R.id.avatar_contact);


        nick_name.setText(contact.getNick_name());
        if(contact.getAvatar()!= null){
            image.setImageBitmap(contact.getAvatar());
        }


        return convertView;

    }








    //__________METODI FILTRO RICERCA_______________________________________________________________

    @Override
    public Filter getFilter(){

        if(filter == null){
            filter = new CustomFilter();
        }
        return filter;
    }

    @Override
    public int getCount(){
        return contacts.size();
    }

    @Override
    public Contact getItem(int pos){
        return contacts.get(pos);
    }

    @Override
    public long getItemId(int pos){
        return contacts.indexOf(getItem(pos));
    }


    private class CustomFilter extends Filter {


        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if( constraint != null && constraint.length() > 0){
                constraint = constraint.toString();
                ArrayList<Contact> filters = new ArrayList<Contact>();

                for(int i = 0; i < filterList.size(); i ++){
                    if (filterList.get(i).getNick_name().contains(constraint)){

                        Contact contact = new Contact(filterList.get(i).getAvatar(), filterList.get(i).getNick_name());

                        filters.add(contact);

                    }
                }

                results.count = filters.size();
                results.values = filters;

            }
            else{
                results.count = filterList.size();
                results.values = filterList;
            }

            return results;

        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            contacts = (ArrayList<Contact>) results.values;
            notifyDataSetChanged();
        }
    }
}
