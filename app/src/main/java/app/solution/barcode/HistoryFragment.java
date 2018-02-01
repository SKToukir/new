package app.solution.barcode;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.solution.barcode.adapter.AdapterBarcodeData;
import app.solution.barcode.database.DBHandler;
import app.solution.barcode.database.DbModelClass;

/**
 * Created by toukirul on 30/1/2018.
 */

public class HistoryFragment extends Fragment {

    private DBHandler db;
    private DbModelClass dbModelClass;
    private TextView txtHistoryFound;
    private List<DbModelClass> dbModelClassList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_history, container, false);

        txtHistoryFound = view.findViewById(R.id.txtHistoryFound);
        recyclerView = view.findViewById(R.id.recyclerItems);
        adapter = new AdapterBarcodeData(getActivity(), dbModelClassList);
        layoutManager = new LinearLayoutManager(view.getContext(),LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        db = new DBHandler(view.getContext());
        dbModelClass = new DbModelClass();

        getAllItems();
    }

    private void getAllItems() {

        int i = db.getContactsCount();
        Log.d("Database", String.valueOf(i));

        if (i==0){
            txtHistoryFound.setVisibility(View.VISIBLE);
            return;
        }else {
            txtHistoryFound.setVisibility(View.GONE);
            dbModelClassList.addAll(db.getAllContacts());
            adapter.notifyDataSetChanged();
        }

    }
}
