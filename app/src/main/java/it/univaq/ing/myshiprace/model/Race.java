package it.univaq.ing.myshiprace.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ktulu on 04/12/17.
 */

public class Race
{
    public static final String RACE_JSON_NAME_START = "race_start";
    public static final String RACE_JSON_NAME_ID = "race_id";
    public static final String RACE_JSON_NAME_TRACK_ID = "track_id";
    private Timestamp startTime;
    private List<ShipPosition> path;
    private int id;
    private int trackID;

    public Race()
    {
        startTime = new Timestamp(System.currentTimeMillis());
        trackID = -1;
        path = new ArrayList<>();
        id = -1;
    }

    public Race(int trackID)
    {
        this.trackID = trackID;
        startTime = new Timestamp(System.currentTimeMillis());
        path = new ArrayList<>();
        id = -1;
    }

    public Race(Timestamp timestamp)
    {
        startTime = timestamp;
        trackID = -1;
        path = new ArrayList<>();
        id = -1;
    }

    public static List<Race> fromJSONArray(String jsonString)
    {
        List<Race> races = new ArrayList<>();
        try
        {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); ++i)
            {
                JSONArray array = jsonArray.getJSONArray(i);
                races.add(Race.parseJSON(array.toString()));
            }
        }
        catch (JSONException e)
        {
            races = new ArrayList<>();
        }
        return races;
    }

    public static Race parseJSON(String jsonarray)
    {
        Race r = null;
        try
        {
            JSONArray array = new JSONArray(jsonarray);
            JSONObject jsonObject = array.getJSONObject(0);
            r = new Race(new Timestamp(jsonObject.getLong(RACE_JSON_NAME_START)));
            r.setId(jsonObject.getInt(RACE_JSON_NAME_ID));
            r.setTrackID(jsonObject.getInt(RACE_JSON_NAME_TRACK_ID));

            for (int i = 1; i < array.length(); i++)
            {
                jsonObject = array.getJSONObject(i);
                if (jsonObject != null)
                {
                    ShipPosition shipPosition = ShipPosition.parseJSON(jsonObject);
                    if (shipPosition != null)
                    {
                        r.addPosition(shipPosition);
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

    public void addPosition(ShipPosition p)
    {
        path.add(p);
        Collections.sort(path);
    }

    public static JSONArray toJSONArray(List<Race> races)
    {
        JSONArray jsonArray = new JSONArray();
        for (Race race : races)
        {
            JSONArray item = race.toJSONArray();
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
            jsonObject.put(RACE_JSON_NAME_START, this.getStartTime().getTime());
            jsonObject.put(RACE_JSON_NAME_ID, this.getId());
            jsonObject.put(RACE_JSON_NAME_TRACK_ID, this.getTrackID());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
        jsonArray.put(jsonObject);
        for (int i = 0; i < length(); ++i)
        {
            JSONObject positionJSON = getPosition(i).toJSONObject();
            if (positionJSON != null)
            {
                jsonArray.put(positionJSON);
            }
        }
        return jsonArray;
    }

    public Timestamp getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Timestamp startTime)
    {
        this.startTime = startTime;
    }

    public int getId()
    {
        return id;
    }

    public int getTrackID()
    {
        return trackID;
    }

    public void setTrackID(int trackID)
    {
        this.trackID = trackID;
    }

    public int length()
    {
        return path.size();
    }

    public ShipPosition getPosition(int pos)
    {
        return path.get(pos);
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public List<ShipPosition> getPath()
    {
        return path;
    }

    public void setPath(List<ShipPosition> path)
    {
        this.path = path;
    }

    public void removePosition(ShipPosition b)
    {
        if (path.contains(b))
        {
            path.remove(b);
        }
    }

    public void removePosition(int position)
    {
        if (position < this.path.size())
        {
            path.remove(position);
        }
    }

    public void clearPath()
    {
        path.clear();
    }

    public boolean contains(ShipPosition b)
    {
        return path.contains(b);
    }

    public String toString()
    {
        return toJSONArray().toString();
    }
}
