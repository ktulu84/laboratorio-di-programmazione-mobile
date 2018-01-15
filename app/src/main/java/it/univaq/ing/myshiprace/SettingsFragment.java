package it.univaq.ing.myshiprace;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by ktulu on 15/01/18.
 */

public class SettingsFragment extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference_settings);
    }
}
