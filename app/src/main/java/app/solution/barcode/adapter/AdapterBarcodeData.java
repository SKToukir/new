package app.solution.barcode.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import app.solution.barcode.R;
import app.solution.barcode.database.DBHandler;
import app.solution.barcode.database.DbModelClass;

/**
 * Created by toukirul on 30/1/2018.
 */

public class AdapterBarcodeData extends RecyclerView.Adapter<AdapterBarcodeData.MyAdapter> {

    private List<DbModelClass> dbModelClassList;
    private Context mContext;

    public AdapterBarcodeData(Context context, List<DbModelClass> list){
        this.mContext = context;
        this.dbModelClassList = list;
    }

    @Override
    public MyAdapter onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_item, parent, false);
        return new MyAdapter(view);
    }

    @Override
    public void onBindViewHolder(MyAdapter holder, final int position) {

        final DbModelClass dbModelClass = dbModelClassList.get(position);

        holder.txtTitle.setText(dbModelClass.getTitle());
        holder.txtDateTime.setText(dbModelClass.getDateTime());
        holder.txtQuery.setText(dbModelClass.getScanQuery());

        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               showDialog(dbModelClass, position);
            }
        });
        holder.shareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if (isOnline(mContext)){
                   Intent sendIntent = new Intent();
                   sendIntent.setAction(Intent.ACTION_SEND);
                   sendIntent.putExtra(Intent.EXTRA_TEXT,
                           dbModelClass.getScanQuery());
                   sendIntent.setType("text/plain");
                   mContext.startActivity(Intent.createChooser(sendIntent,"QR Scanner Result"));
               }else {
                   Toast.makeText(mContext, "Please check your internet connection!",Toast.LENGTH_LONG).show();
               }
            }
        });
    }

    private void showDialog(final DbModelClass dbModelClass,final int position) {
        final AlertDialog dialog = new AlertDialog.Builder(mContext).create();
                dialog.setMessage("Are you sure to delete this information?");
                dialog.setIcon(R.mipmap.ic_launcher);
                dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DBHandler db = new DBHandler(mContext);
                        db.deleteContact(dbModelClass.getId());
                        dbModelClassList.remove(position);
                        notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
    }

    @Override
    public int getItemCount() {
        return dbModelClassList.size();
    }

    public class MyAdapter extends RecyclerView.ViewHolder{
        private TextView txtTitle, txtDateTime, txtQuery;
        private ImageView imgDelete;
        private LinearLayout shareLayout;
        public MyAdapter(View itemView) {
            super(itemView);
            shareLayout = itemView.findViewById(R.id.shareLayout);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtQuery = itemView.findViewById(R.id.txtQuery);
            txtDateTime = itemView.findViewById(R.id.txtDateTime);
        }
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
}
