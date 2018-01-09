package it.univaq.ing.myshiprace.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.univaq.ing.myshiprace.R;
import it.univaq.ing.myshiprace.model.RaceTrack;

/**
 * Created by ktulu on 03/11/17.
 */

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.ViewHolder>
{
    private List<RaceTrack> data;

    public TrackAdapter(List<RaceTrack> tracks)
    {

        if (tracks != null)
        {
            data = tracks;
        }
        else
        {
            data = new ArrayList<>();
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_tracciato, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {

        RaceTrack item = data.get(position);
        if (item == null) return;
        holder.title.setText(item.getTrackName());
        holder.subtitle.setText(String.valueOf(item.length()));

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
            title = itemView.findViewById(R.id.adapter_tracciato_text_name);
            subtitle = itemView.findViewById(R.id.adapter_tracciato_num_boe);
        }

    }
}