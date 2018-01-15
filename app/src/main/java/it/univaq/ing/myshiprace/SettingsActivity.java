package it.univaq.ing.myshiprace;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by ktulu on 15/01/18.
 */

public class SettingsActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
