package it.univaq.ing.myshiprace.model;

/**
 * Created by ktulu on 15/12/17.
 */

public abstract class Position
{
    protected double latitude;
    protected double longitude;
    protected int id;

    public Position(double latitude, double longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        id = -1;
    }

    public Position()
    {
        id = -1;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (Position.class.isAssignableFrom(obj.getClass()))
        {
            Position o2 = (Position) obj;
            return id == o2.getId() && latitude == o2.getLatitude() && longitude == o2.getLongitude();
        }
        else
        {
            return false;
        }
    }

    public int getId()
    {
        return id;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(id).append(System.getProperty("line.separator"));
        sb.append("Latitudine: ").append(latitude).append(System.getProperty("line.separator"));
        sb.append("Longitudine: ").append(longitude).append(System.getProperty("line.separator"));

        return sb.toString();
    }
}
