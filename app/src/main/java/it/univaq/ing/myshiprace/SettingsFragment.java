package it.univaq.ing.myshiprace;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ktulu on 15/01/18.
 */

public class SettingsFragment extends PreferenceFragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        addPreferencesFromResource(R.xml.preference_settings);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings);
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
