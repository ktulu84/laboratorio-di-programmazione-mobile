package it.univaq.ing.myshiprace.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.univaq.ing.myshiprace.R;
import it.univaq.ing.myshiprace.Util.Informazioni;

/**
 * Created by ktulu on 03/11/17.
 */

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.ViewHolder>
{
    private List<Informazioni> data;

    public InfoAdapter(List<Informazioni> infos)
    {

        if (infos != null)
        {
            data = infos;
        }
        else
        {
            data = new ArrayList<>();
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {

        Informazioni item = data.get(position);
        if (item == null) return;

        holder.title.setText(item.getTitolo());

        holder.subtitle.setText(item.getSottotitolo());

    }

    @Override
    public int getItemCount()
    {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {

        TextView title, subtitle;

        ViewHolder(View itemView)
        {
            super(itemView);
            title = itemView.findViewById(R.id.adapter_info_title);
            subtitle = itemView.findViewById(R.id.adapter_info_subtitle);
        }

    }
}