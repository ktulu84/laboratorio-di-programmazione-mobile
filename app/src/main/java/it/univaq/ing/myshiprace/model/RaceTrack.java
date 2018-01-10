package it.univaq.ing.myshiprace.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ktulu on 04/12/17.
 */

public class RaceTrack
{
    public static RaceTrack parseJSON(String jsonarray)
    {
        RaceTrack r = null;
        try
        {
            JSONArray array = new JSONArray(jsonarray);
            JSONObject jsonObject = array.getJSONObject(0);
            r = new RaceTrack(jsonObject.getString("track_name"));

            for (int i = 1; i < array.length(); i++)
            {
                jsonObject = array.getJSONObject(i);
                if (jsonObject != null)
                {
                    Boa b = Boa.parseJSON(jsonObject);
                    if (b != null)
                    {
                        r.addBoa(b);
                    }
                }

            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return r;
    }

    private String trackName;
    private List<Boa> trackPath;

    public RaceTrack()
    {
        trackName = "";
        trackPath = new ArrayList<>();
    }

    public RaceTrack(String name)
    {
        trackName = name;
        trackPath = new ArrayList<>();
    }

    public void addBoa(Boa b)
    {
        trackPath.add(b);
        Collections.sort(trackPath);
    }

    public void removeBoa(Boa b)
    {
        if (trackPath.contains(b))
        {
            trackPath.remove(b);
        }
    }

    public void removeBoa(int position)
    {
        if (position < this.trackPath.size())
        {
            trackPath.remove(position);
        }
    }

    public void clearTrack()
    {
        trackPath.clear();
    }

    public boolean contains(Boa b)
    {
        return trackPath.contains(b);
    }

    public String getTrackName()
    {
        return trackName;
    }

    public int length()
    {
        return trackPath.size();
    }

    public Boa getBoa(int pos)
    {
        return trackPath.get(pos);
    }

    public JSONArray toJSONArray()
    {
        JSONArray jsonArray = new JSONArray();
        JSONObject trackName = new JSONObject();
        try
        {
            trackName.put("track_name", this.getTrackName());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
        jsonArray.put(trackName);
        for (int i = 0; i < length(); ++i)
        {
            JSONObject boaJSON = getBoa(i).toJSONObject();
            if (boaJSON != null)
            {
                jsonArray.put(boaJSON);
            }
        }
        return jsonArray;
    }

    public String toString()
    {
        return toJSONArray().toString();
    }
}
