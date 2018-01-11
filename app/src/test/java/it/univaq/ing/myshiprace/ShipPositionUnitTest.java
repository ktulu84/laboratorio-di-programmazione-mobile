package it.univaq.ing.myshiprace;

import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;

import it.univaq.ing.myshiprace.model.ShipPosition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by ktulu on 06/12/17.
 */

public class ShipPositionUnitTest
{
    ShipPosition p;

    @Before
    public void initialize()
    {
        p = new ShipPosition(12.1, 12.1);
    }

    @Test
    public void testTimestampEmpty()
    {
        assertEquals(0, p.getTimestamp().getTime(), 0);
    }

    @Test
    public void testTimestamp()
    {
        p.setTimestamp(new Timestamp(1000));
        Timestamp t = new Timestamp(1000);
        assertTrue(p.getTimestamp().equals(p.getTimestamp()));
    }

    @Test
    public void testLatitude()
    {
        assertEquals(12.1, p.getLatitude(), 0);
    }

    @Test
    public void testLongitude()
    {
        assertEquals(12.1, p.getLongitude(), 0);
    }

    @Test
    public void testInitWithTimestamp()
    {
        p = new ShipPosition(12.1, 12.1, new Timestamp(1000));
        ShipPosition p2 = new ShipPosition(12.1, 12.1, new Timestamp(1000));
        p2.setId(1);
        p.setId(1);
        p.setTrackID(2);
        p2.setTrackID(2);
        assertTrue(p.equals(p2));
    }
}
