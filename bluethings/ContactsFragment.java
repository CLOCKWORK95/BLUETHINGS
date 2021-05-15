package com.example.gianmarco.bluethings;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;


public class ContactsFragment extends Fragment {

    ListView contacts_listview;
    SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        contacts_listview = view.findViewById(R.id.contacts__listView);
        ((MainActivity)getActivity()).contacts_listview = contacts_listview;
        searchView = view.findViewById(R.id.search_view);
        ((MainActivity)getActivity()).searchView = searchView;
        ((MainActivity)getActivity()).getContactsOnListView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return onQueryTextSubmit(query);
            }

            @Override
            public boolean onQueryTextChange(String query) {
                return ((MainActivity)getActivity()).query_text_change(query);

            }
        });

        contacts_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((MainActivity)getActivity()).getContactInfos(position);
            }
        });

        return view;
    }



}
