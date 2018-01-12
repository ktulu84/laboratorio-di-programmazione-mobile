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

public class Track
{
    public static final String TRACK_JSON_NAME = "track_name";
    public static final String TRACK_JSON_ID = "track_id";
    public static List<Track> fromJSONArray(String jsonString)
    {
        List<Track> tracks = new ArrayList<>();
        try
        {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); ++i)
            {
                JSONArray array = jsonArray.getJSONArray(i);
                tracks.add(Track.parseJSON(array.toString()));
            }
        }
        catch (JSONException e)
        {
            tracks = new ArrayList<>();
        }
        return tracks;
    }

    public static Track parseJSON(String jsonarray)
    {
        Track r = null;
        try
        {
            JSONArray array = new JSONArray(jsonarray);
            JSONObject jsonObject = array.getJSONObject(0);
            r = new Track(jsonObject.getString(TRACK_JSON_NAME));
            r.setId(jsonObject.getInt(TRACK_JSON_ID));

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
    private int id;

    public int getId()
    {
        return id;
    }

    public void setTrackName(String trackName)
    {
        this.trackName = trackName;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public Track()
    {
        trackName = "";
        trackPath = new ArrayList<>();
        id = -1;
    }

    public Track(String name)
    {
        trackName = name;
        trackPath = new ArrayList<>();
        id = -1;
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

    public List<Boa> getBoas()
    {
        return trackPath;
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

    public static JSONArray toJSONArray(List<Track> tracks)
    {
        JSONArray jsonArray = new JSONArray();
        for (Track track : tracks)
        {
            JSONArray item = track.toJSONArray();
            if (item != null) jsonArray.put(item);
        }
        return jsonArray;
    }

    public JSONArray toJSONArray()
    {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put(TRACK_JSON_NAME, this.getTrackName());
            jsonObject.put(TRACK_JSON_ID, this.getId());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
        jsonArray.put(jsonObject);
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
