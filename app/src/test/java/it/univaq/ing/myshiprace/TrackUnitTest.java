package it.univaq.ing.myshiprace;

import org.junit.Before;
import org.junit.Test;

import it.univaq.ing.myshiprace.model.Boa;
import it.univaq.ing.myshiprace.model.Track;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by ktulu on 04/12/17.
 */


public class TrackUnitTest
{
    Track r;
    Boa b;

    @Before
    public void init()
    {
        r = new Track();
        b = new Boa(12.1, 12.1);
    }

    @Test
    public void testAddBoa() throws Exception
    {
        r.addBoa(b);
        Boa a = new Boa(12.1, 12.1);
        assertTrue(r.contains(a));
    }

    @Test
    public void testTrackSize() throws Exception
    {
        r.addBoa(b);
        assertEquals(1, r.length(), 0);
    }

    @Test
    public void testRemoveBoa() throws Exception
    {
        r.addBoa(b);
        r.removeBoa(b);
        assertFalse(r.contains(b));
    }

    @Test
    public void testGetBoaLatitude() throws Exception
    {
        assertEquals(12.1, b.getLatitude(), 0);
    }

    @Test
    public void testGetBoaLongitude() throws Exception
    {
        assertEquals(12.1, b.getLongitude(), 0);
    }
}
