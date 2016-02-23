package xsoft.com.rucal;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.overlays.MarkerInfoWindow;
import org.osmdroid.views.MapView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import common.Commons;

/**
 * Created by Usuario on 22/02/2016.
 */
public class CustomInfoWindow extends MarkerInfoWindow {

    private int idEvento;

    public CustomInfoWindow(MapView mapView, int id) {
        super(R.layout.bubble_vote, mapView);
        this.idEvento=id;
        Button btn1 = (Button)(mView.findViewById(R.id.bubble_vote_up));
        btn1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                votar("/votarEventoUp/");
            }
        });

        Button btn2 = (Button)(mView.findViewById(R.id.bubble_vote_down));
        btn2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                votar("/votarEventoDown/");
            }
        });
    }

    @Override public void onOpen(Object item){
        super.onOpen(item);
        mView.findViewById(R.id.bubble_vote_up).setVisibility(View.VISIBLE);
        mView.findViewById(R.id.bubble_vote_down).setVisibility(View.VISIBLE);
    }

    private void votar(String URL){
        final HttpClient client=new DefaultHttpClient();
        final HttpPost post=new HttpPost(Commons.SERVER_IP+URL);

        List<NameValuePair> pairs=new ArrayList<>();
        pairs.add(new BasicNameValuePair("id", ""+idEvento));
        try {
            post.setEntity(new UrlEncodedFormEntity(pairs));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        new AsyncTask<Void, Void, String>(){

            @Override
            protected String doInBackground(Void... params) {
                InputStream is=null;
                String json="";
                try {
                    HttpResponse response=client.execute(post);
                    HttpEntity httpEntity = response.getEntity();
                    is = httpEntity.getContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    json = sb.toString();
                } catch (Exception e) {
                    Log.e("Buffer Error", "Error converting result " + e.toString());
                }

                return json;
            }

            @Override
            protected void onPostExecute(String s) {
                try {
                    JSONObject jObj = new JSONObject(s);
                    Toast.makeText(getView().getContext(), (String)jObj.get("mensage"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Log.e("JSON Parser", "Error parsing data " + e.toString());
                }
            }
        }.execute();
    }

}