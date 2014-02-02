package gr.tsagi.jekyllforandroid.utils;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by tsagi on 1/31/14.
 */

public class BasicWebService{

    DefaultHttpClient httpClient;
    String returnedValue;

    HttpResponse response = null;
    HttpGet httpGet = null;
    String webServiceUrl;
    OkHttpClient client;

    public BasicWebService(String serviceName){
        httpClient = new DefaultHttpClient();
        webServiceUrl = serviceName;
        client = new OkHttpClient();

    }

    public String[] webGetPost() {
        String results[] = new String[0];
        try {
            httpGet = new HttpGet(webServiceUrl);
            Log.e("WebGetURL",webServiceUrl);
            response = httpClient.execute(httpGet);
            InputStream in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line;
            int yaml_dash =0;
            String yaml = null;
            while((line = reader.readLine()) != null)
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
        try {
            httpGet = new HttpGet(webServiceUrl);
            Log.e("WebGetURL",webServiceUrl);
            response = httpClient.execute(httpGet);
            InputStream in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line;

            while((line = reader.readLine()) != null)
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
