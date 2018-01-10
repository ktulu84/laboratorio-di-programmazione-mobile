package it.univaq.ing.myshiprace.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.univaq.ing.myshiprace.R;
import it.univaq.ing.myshiprace.model.Boa;

/**
 * Created by ktulu on 09/01/18.
 */

public class BoaAdapter extends RecyclerView.Adapter<BoaAdapter.ViewHolder>
{
    private List<Boa> data;

    public BoaAdapter(List<Boa> data)
    {
        if (data == null)
        {
            this.data = new ArrayList<>();
        }
        else
        {
            this.data = data;
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
        Boa item = data.get(position);

        if (item == null) return;

        holder.latitude.setText(String.valueOf(item.getLatitude()));
        holder.longitude.setText(String.valueOf(item.getLongitude()));
        holder.order.setText(String.valueOf(item.getOrder()));
    }

    @Override
    public int getItemCount()
    {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        TextView latitude, longitude, order;

        ViewHolder(View itemView)
        {
            super(itemView);
            latitude = itemView.findViewById(R.id.adapter_boa_latitude);
            longitude = itemView.findViewById(R.id.adapter_boa_longitude);
            order = itemView.findViewById(R.id.adapter_boa_order);
        }

        @Override
        public void onClick(View v)
        {

            Toast.makeText(v.getContext(), "Hai cliccato la posizione " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
        }

    }
}
