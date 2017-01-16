package fi.centria.ruuvitag.networking;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.entity.StringEntity;
import fi.centria.ruuvitag.data.RuuvitagObject;

public class DweetIoConnector
{
    static RequestQueue requestQueue = null;
    public void postData(RuuvitagObject ruuvitagObject, Context context, AsyncHttpResponseHandler handler)
    {

        String URL = "https://dweet.io/dweet/for/"+ruuvitagObject.getId();
         AsyncHttpClient client = new AsyncHttpClient();

        try {
            StringEntity entity = new StringEntity(ruuvitagObject.getLastDataJSON());

            ByteArrayEntity be = new ByteArrayEntity(ruuvitagObject.getLastDataJSON().getBytes());
            client.post(context, URL, be, "application/json", handler);
        }catch (Exception e){}
    }
}
