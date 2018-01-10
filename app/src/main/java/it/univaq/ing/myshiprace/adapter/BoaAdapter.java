package it.univaq.ing.myshiprace.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import it.univaq.ing.myshiprace.R;
import it.univaq.ing.myshiprace.model.Boa;
import it.univaq.ing.myshiprace.model.RaceTrack;

/**
 * Created by ktulu on 09/01/18.
 */

public class BoaAdapter extends RecyclerView.Adapter<BoaAdapter.ViewHolder>
{
    private RaceTrack data;

    public BoaAdapter(RaceTrack rt)
    {
        if (rt == null)
        {
            this.data = new RaceTrack();
        }
        else
        {
            this.data = rt;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_boa, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        Boa item = data.getBoa(position);

        if (item == null) return;

        holder.latitude.setText(String.valueOf(item.getLatitude()));
        holder.longitude.setText(String.valueOf(item.getLongitude()));
        holder.order.setText(String.valueOf(item.getOrder()));
    }

    @Override
    public int getItemCount()
    {
        return data.length();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
    {

        TextView latitude, longitude, order;

        ViewHolder(View itemView)
        {
            super(itemView);
            latitude = itemView.findViewById(R.id.adapter_boa_latitude);
            longitude = itemView.findViewById(R.id.adapter_boa_longitude);
            order = itemView.findViewById(R.id.adapter_boa_order);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v)
        {

            Toast.makeText(v.getContext(), "Hai cliccato la posizione " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onLongClick(View v)
        {
            AlertDialog.Builder adb = new AlertDialog.Builder(v.getContext());


            adb.setTitle(R.string.alert_boa_remove);


            adb.setIcon(R.drawable.ic_warning);

            final Context context = v.getContext();
            adb.setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {

                    data.removeBoa(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    notifyItemRangeChanged(0, data.length());
                    Toast.makeText(context, "Cliccato OK", Toast.LENGTH_SHORT).show();
                }
            });


            adb.setNegativeButton(R.string.alert_cancel, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    Toast.makeText(context, "Cliccato Cancel", Toast.LENGTH_SHORT).show();
                }
            });
            adb.show();

            return true;
        }
    }
}
