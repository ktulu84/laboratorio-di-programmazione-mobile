package it.univaq.ing.myshiprace.Util;

import android.view.View;

/**
 * Created by ktulu on 10/01/18.
 */

public interface ClickListener
{
    void onClick(View view, int position);

    void onLongClick(View view, int position);
}
