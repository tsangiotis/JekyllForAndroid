package gr.tsagi.jekyllforandroid.utils;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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

    HttpClient client;
    String returnedValue;

    String webServiceUrl;

    public BasicWebService(String serviceName){
        webServiceUrl = serviceName;
        client = new DefaultHttpClient();

    }

    public String[] webGetPost() {
        String results[] = new String[0];
        HttpURLConnection connection;
        try {
            HttpGet httpget = new HttpGet(webServiceUrl); // Set the action you want to do
            HttpResponse response = client.execute(httpget); // Executeit
            HttpEntity entity = response.getEntity();
            InputStream in = entity.getContent();
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
            HttpGet httpget = new HttpGet(webServiceUrl); // Set the action you want to do
            HttpResponse httpResp = client.execute(httpget);
            int response = httpResp.getStatusLine().getStatusCode();
            if(response == 404)
                return "404";

            HttpEntity entity = httpResp.getEntity();
            InputStream in = entity.getContent();
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
