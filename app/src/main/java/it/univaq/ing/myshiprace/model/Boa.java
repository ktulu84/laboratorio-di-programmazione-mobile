package it.univaq.ing.myshiprace.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ktulu on 08/01/18.
 */

public class Boa extends Position implements Comparable<Boa>
{
    private int order;
    private int trackID;

    public int getTrackID()
    {
        return trackID;
    }

    public void setTrackID(int trackID)
    {
        this.trackID = trackID;
    }

    public Boa()
    {
        super();
        order = 0;
    }

    public Boa(double latitude, double longitude)
    {
        super(latitude, longitude);
    }

    public Boa(double latitude, double longitude, int order)
    {
        super(latitude, longitude);
        this.order = order;
    }

    public int getOrder()
    {
        return order;
    }

    public void setOrder(int order)
    {
        this.order = order;
    }

    @Override
    public int compareTo(@NonNull Boa boa)
    {
        return this.getOrder() - boa.getOrder();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj.getClass().equals(Boa.class))
        {
            Boa o2 = (Boa) obj;
            return latitude == o2.getLatitude() && longitude == o2.getLongitude() && order == o2.getOrder();
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
        sb.append("Ordine: ").append(order).append(System.getProperty("line.separator"));

        return sb.toString();
    }

    public JSONObject toJSONObject()
    {
        try
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ID", this.getId());
            jsonObject.put("latitude", this.getLatitude());
            jsonObject.put("longitude", this.getLongitude());
            jsonObject.put("order", this.getOrder());
            return jsonObject;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static Boa parseJSON(JSONObject object)
    {
        try
        {
            Boa b = new Boa();
            b.setLatitude(object.getDouble("latitude"));
            b.setLongitude(object.getDouble("longitude"));
            b.setOrder(object.getInt("order"));
            b.setId(object.getInt("ID"));
            return b;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
