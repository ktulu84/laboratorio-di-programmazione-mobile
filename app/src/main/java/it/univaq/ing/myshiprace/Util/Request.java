package it.univaq.ing.myshiprace.Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * MyService
 * Created by leonardo on 17/11/17.
 * <p>
 * BiTE s.r.l.
 * contact info@bitesrl.it
 *
 * Whit some modifications by ktulu (me) to use parameters
 */

public class Request
{

    // method to handle requests. if parameters and values are null or contains nothing
    // just connect and get a result, otherwise send a POST request to the page and get the result
    public static String doRequest(String address, String[] parameters, String[] values)
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
            if (parameters != null && parameters.length > 0)
            {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                StringBuilder params = new StringBuilder();

                params.append(parameters[0]).append("=").append(values[0]);

                if (parameters.length > 1)
                {
                    for (int i = 1; i < parameters.length - 1; ++i)
                    {
                        params.append("&");
                        params.append(parameters[i]).append("=").append(values[i]);
                    }
                    params.append(parameters[parameters.length - 1]).append("=").append(values[parameters.length - 1]);
                }


                OutputStream out = connection.getOutputStream();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
                bw.write(params.toString());
                bw.flush();
                bw.close();
            }

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
            return "ERROR";
        }
        finally
        {
            if (connection != null) connection.disconnect();
        }
    }
}
