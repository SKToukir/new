package app.solution.barcode;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.solution.barcode.adapter.AdapterBarcodeData;
import app.solution.barcode.database.DBHandler;
import app.solution.barcode.database.DbModelClass;
import app.solution.barcode.utils.RecyclerTouchListener;

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


        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

                showAlertDialog(position);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    private void showAlertDialog(final int position) {
        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Search on browser");
        alertDialog.setMessage(dbModelClassList.get(position).getScanQuery());
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (isOnline(getContext())){
                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                    intent.putExtra(SearchManager.QUERY, dbModelClassList.get(position).getScanQuery());
                    startActivity(intent);
                }else {
                    Toast.makeText(getContext(), "Please check your internet connection!",Toast.LENGTH_LONG).show();
                }
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public static boolean isOnline(final Context ctx) {

        try {
            final ConnectivityManager cm = (ConnectivityManager) ctx
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null) {
                return ni.isConnectedOrConnecting();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
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
