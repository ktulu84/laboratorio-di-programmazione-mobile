package it.univaq.ing.myshiprace.model;


import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by ktulu on 04/12/17.
 */

public class ShipPosition extends Position implements Comparable<ShipPosition>
{

    private static DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
    private Timestamp timestamp;
    private int raceID;
    private String shipName;

    public ShipPosition(JSONObject jsonObject)
    {
        super();
        try
        {
            latitude = jsonObject.getDouble("latitude");
            longitude = jsonObject.getDouble("longitude");
            timestamp = new Timestamp(jsonObject.getLong("timestamp"));
            shipName = jsonObject.getString("ship_name");
            id = jsonObject.getInt("ID");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public ShipPosition()
    {
        super();
        timestamp = new Timestamp(0);
    }

    public ShipPosition(double lat, double lon)
    {
        super(lat, lon);
        timestamp = new Timestamp(0);
    }

    public ShipPosition(double lat, double lon, Timestamp time)
    {
        super(lat, lon);
        timestamp = time;
    }

    public static ShipPosition parseJSON(JSONObject object)
    {
        try
        {
            ShipPosition shipPosition = new ShipPosition();
            shipPosition.setLatitude(object.getDouble("latitude"));
            shipPosition.setLongitude(object.getDouble("longitude"));
            shipPosition.setTimestamp(new Timestamp(object.getLong("timestamp")));
            shipPosition.setId(object.getInt("ID"));
            shipPosition.setShipName(object.getString("ship_name"));
            return shipPosition;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj.getClass().equals(ShipPosition.class))
        {
            ShipPosition o2 = (ShipPosition) obj;
            return super.equals(o2) && timestamp.equals(o2.getTimestamp()) && raceID == o2.getRaceID();
        }
        else
        {
            return false;
        }
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("Timestamp: ").append(dateFormat.format(timestamp)).append(System.getProperty("line.separator"));

        return sb.toString();
    }

    public Timestamp getTimestamp()
    {
        return timestamp;
    }

    public int getRaceID()
    {
        return raceID;
    }

    public void setRaceID(int raceID)
    {
        this.raceID = raceID;
    }

    public void setTimestamp(Timestamp timestamp)
    {
        this.timestamp = timestamp;
    }

    public JSONObject toJSONObject()
    {
        try
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ID", this.getId());
            jsonObject.put("latitude", this.getLatitude());
            jsonObject.put("longitude", this.getLongitude());
            jsonObject.put("timestamp", this.getTimestamp().getTime());
            jsonObject.put("ship_name", this.getShipName());
            return jsonObject;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public String getShipName()
    {
        return shipName;
    }

    public void setShipName(String shipName)
    {
        this.shipName = shipName;
    }

    @Override
    public int compareTo(@NonNull ShipPosition o)
    {
        return this.getTimestamp().compareTo(o.getTimestamp());
    }
}
