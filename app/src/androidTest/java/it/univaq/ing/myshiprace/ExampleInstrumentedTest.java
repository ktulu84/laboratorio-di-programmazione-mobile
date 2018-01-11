package it.univaq.ing.myshiprace;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import it.univaq.ing.myshiprace.model.Boa;
import it.univaq.ing.myshiprace.model.Track;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest
{
    @Test
    public void raceTrackToString() throws Exception
    {
        Boa b1 = new Boa();
        b1.setOrder(1);
        b1.setLatitude(42.1);
        b1.setLongitude(42.2);
        b1.setId(222);
        Track rt = new Track("Prova");
        rt.addBoa(b1);
        Boa b2 = new Boa(12.1, 14.2, 2);
        b2.setId(111);

//        rt.addBoa(b2);
//        assertEquals("[{\"track_name\":\"Prova\"},{\"ID\":222,\"latitude\":42.1,\"longitude\":42.2,\"order\":1},{\"ID\":111,\"latitude\":12.1,\"longitude\":14.2,\"order\":2}]", rt.toString());
//
//        rt.removeBoa(b1);
//        assertEquals("[{\"track_name\":\"Prova\"},{\"ID\":111,\"latitude\":12.1,\"longitude\":14.2,\"order\":2}]", rt.toString());
//
//        rt.addBoa(b1);
//        assertEquals("[{\"track_name\":\"Prova\"},{\"ID\":222,\"latitude\":42.1,\"longitude\":42.2,\"order\":1},{\"ID\":111,\"latitude\":12.1,\"longitude\":14.2,\"order\":2}]", rt.toString());

    }
}
