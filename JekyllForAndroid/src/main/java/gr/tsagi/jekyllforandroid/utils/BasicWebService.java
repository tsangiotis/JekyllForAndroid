package gr.tsagi.jekyllforandroid.utils;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

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

    public BasicWebService(String serviceName){
        HttpParams myParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(myParams, 10000);
        httpClient = new DefaultHttpClient();
        webServiceUrl = serviceName;

    }

    public String webGet() {
        httpGet = new HttpGet(webServiceUrl);
        Log.e("WebGetURL: ",webServiceUrl);
        try {
            response = httpClient.execute(httpGet);
            InputStream in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null)
            {
                str.append(line);
            }
            in.close();
            returnedValue = str.toString();
        } catch (IOException e) {
            Log.e("WebService", " Message " +  e.getMessage());
        } catch (Exception e) {
            Log.e("WebService", " Message " + e.getMessage());
        }
        return returnedValue;
    }
}
