package it.univaq.ing.myshiprace.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    }

    @Override
    public int getItemCount()
    {
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {

        TextView title, subtitle;

        ViewHolder(View itemView)
        {
            super(itemView);

        }

    }
}
