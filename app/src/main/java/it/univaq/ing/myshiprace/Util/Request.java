package it.univaq.ing.myshiprace.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * MyService
 * Created by leonardo on 17/11/17.
 * <p>
 * BiTE s.r.l.
 * contact info@bitesrl.it
 */

public class Request
{

    public static String doRequest(String address)
    {

        URL url;
        try
        {
            url = new URL(address);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            return null;
        }

        HttpURLConnection connection = null;
        try
        {
            connection = (HttpURLConnection) url.openConnection();
            // connection.setRequestMethod("POST");
            // connection.setDoOutput(true);

            // String params = "key=value&key=value";

            // OutputStream out = connection.getOutputStream();
            // BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
            // bw.write(params);
            // bw.flush();
            // bw.close();

            InputStream is;
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK)
            {
                is = connection.getInputStream();
            }
            else
            {
                is = connection.getErrorStream();
            }

            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }
            br.close();
            return sb.toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            if (connection != null) connection.disconnect();
        }
    }
}
