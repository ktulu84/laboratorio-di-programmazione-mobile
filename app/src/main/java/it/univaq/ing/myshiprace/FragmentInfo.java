package it.univaq.ing.myshiprace;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import it.univaq.ing.myshiprace.Util.Informazioni;
import it.univaq.ing.myshiprace.adapter.InfoAdapter;

/**
 * Created by ktulu on 15/12/17.
 */

public class FragmentInfo extends Fragment
{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_info);
        View view = inflater.inflate(R.layout.activity_info, container, false);
        String[] titles = getResources().getStringArray(R.array.info_title);
        String[] subtitles = getResources().getStringArray(R.array.info_subtitle);
        List<Informazioni> infos = new ArrayList<>();
        RecyclerView list = view.findViewById(R.id.info_list);
        list.setLayoutManager(new LinearLayoutManager(view.getContext()));
        list.addItemDecoration(new DividerItemDecoration(list.getContext(), DividerItemDecoration.VERTICAL));

        for (int i = 0; i < titles.length; ++i)
        {
            Informazioni informazioni = new Informazioni();
            informazioni.setTitolo(titles[i]);
            informazioni.setSottotitolo(subtitles[i]);
            infos.add(informazioni);
        }

        list.setAdapter(new InfoAdapter(infos));
        return view;
    }
}
