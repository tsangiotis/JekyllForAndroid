package gr.tsagi.jekyllforandroid.utils;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by tsagi on 1/31/14.
 */

public class BasicWebService {

    OkHttpClient client;
    String returnedValue;

    HttpResponse response = null;
    HttpGet httpGet = null;
    String webServiceUrl;

    public BasicWebService(String serviceName){
        webServiceUrl = serviceName;
        client = new OkHttpClient();

    }

    public String[] webGetPost() {
        String results[] = new String[0];
        HttpURLConnection connection;
        try {
            Log.d("okhttp", "With ok");
            connection = client.open(new URL(webServiceUrl));
            InputStream in = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(in);

            // from StackOverflow: http://stackoverflow.com/a/2549222
            BufferedReader r = new BufferedReader(isr);
            StringBuilder str = new StringBuilder();
            String line;
            int yaml_dash =0;
            String yaml = null;
            while((line = r.readLine()) != null)
            {
                if(line.equals("---")){
                    yaml_dash++;
                }
                if (yaml_dash!=2)
                    if(!line.equals("---"))
                        yaml = yaml + line + "\n";

                if (yaml_dash==2){
                    if(!line.equals("---"))
                        if(line.isEmpty())
                            str.append("\n");
                        else
                            str.append(line);
                }
            }
            in.close();
            String content = str.toString().replaceAll("\n","\n\n");
            results = new String[]{yaml, content};


        } catch (ClientProtocolException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return results;
    }

    public String webGetJson() {
        HttpURLConnection connection;
        try {
            connection = client.open(new URL(webServiceUrl));
            int response = connection.getResponseCode();
            if(response == 404)
                return "404";

            InputStream in = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(in);

            // from StackOverflow: http://stackoverflow.com/a/2549222
            BufferedReader r = new BufferedReader(isr);
            StringBuilder str = new StringBuilder();
            String line;

            while((line = r.readLine()) != null)
            {
                str.append(line);
            }
            in.close();
            returnedValue = str.toString();

        } catch (IOException e) {
            Log.e("WebService", " Message " +  e.getMessage());
            return "IOerror";
        } catch (Exception e) {
            Log.e("WebService", " Message " + e.getMessage());
            return "error";
        }
        return returnedValue;
    }

}
