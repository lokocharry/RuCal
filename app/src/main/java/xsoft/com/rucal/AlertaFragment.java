package xsoft.com.rucal;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import HereMaps.HereTileSource;
import common.Commons;

/**
 * Created by Usuario on 02/02/2016.
 */
public class AlertaFragment extends Fragment implements MapEventsReceiver, Marker.OnMarkerClickListener, Marker.OnMarkerDragListener, View.OnClickListener {

    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay miUbicacion;
    private EditText txtLugar;
    private Context context;
    private ImageButton btnLugar;
    private Button btnEnviar;
    private EditText txtComentario;
    private Spinner spinerTipo;

    private Marker lugar;

    private View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_alerta, container, false);
        context=getActivity().getApplicationContext();

        txtLugar=(EditText)v.findViewById(R.id.txtLugar);
        txtComentario=(EditText)v.findViewById(R.id.txtComentario);
        spinerTipo=(Spinner)v.findViewById(R.id.spinnerTipo);

        btnLugar=(ImageButton)v.findViewById(R.id.btnlugar);
        btnLugar.setOnClickListener(this);
        btnEnviar=(Button)v.findViewById(R.id.btnEnviar);
        btnEnviar.setOnClickListener(this);
        btnEnviar=(Button)v.findViewById(R.id.btnEnviar);

        map = (MapView) v.findViewById(R.id.map2);
        map.setClickable(true);
        map.setBuiltInZoomControls(true);

        map.setScrollableAreaLimit(new BoundingBoxE6(5.4983000, -73.3851000, 5.5864000, -73.3176000));
        mapController = map.getController();
        mapController.setZoom(13);
        map.setMinZoomLevel(8);
        mapController.animateTo(new GeoPoint(5.552223, -73.356618));

        MapEventsOverlay evOverlay = new MapEventsOverlay(getActivity(), this);
        map.getOverlays().add(0, evOverlay);

        miUbicacion=new MyLocationNewOverlay(getActivity(), map);
        miUbicacion.enableMyLocation();
        miUbicacion.disableFollowLocation();
        miUbicacion.setDrawAccuracyEnabled(true);
        map.getOverlays().add(miUbicacion);

        String[] myStringArray = {"https://1.aerial.maps.cit.api.here.com/maptile/2.1/maptile/newest/hybrid.day/"};
        final MapTileProviderBasic tileProvider = new MapTileProviderBasic(getActivity().getApplicationContext());
        final ITileSource tileSource = new HereTileSource("HereMaps", 1, 20, 256, ".png", myStringArray);
        tileProvider.setTileSource(tileSource);
        final TilesOverlay tilesOverlay = new TilesOverlay(tileProvider, this.getActivity().getBaseContext());
        tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        map.setTileSource(tileSource);

        //BingMapTileSource bing=null;
        //try{
           // bing = new BingMapTileSource("en");
          //  bing.setStyle(BingMapTileSource.IMAGERYSET_ROAD);
         //   map.setTileSource(bing);
       // }
       // catch(Exception e){
         //   e.printStackTrace();
       // }

        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(context, map);
        mRotationGestureOverlay.setEnabled(true);
        map.setMultiTouchControls(true);
        map.getOverlays().add(mRotationGestureOverlay);

        MinimapOverlay miniMapOverlay = new MinimapOverlay(getActivity(), map.getTileRequestCompleteHandler());
        miniMapOverlay.setZoomDifference(5);
        miniMapOverlay.setHeight(200);
        miniMapOverlay.setWidth(200);
        miniMapOverlay.setTileSource(tileSource);
        map.getOverlays().add(miniMapOverlay);

        lugar=new Marker(map);
        lugar.setOnMarkerClickListener(this);
        lugar.setOnMarkerDragListener(this);
        crearMarker(lugar, new GeoPoint(5.508006, -73.37202), "Lugar", getResources().getDrawable(R.drawable.ic_add_location_black_36dp));

        return v;
    }

    private void crearMarker(Marker marker, GeoPoint geoPoint, String titulo, Drawable icono){
        marker.setPosition(geoPoint);
        marker.setDraggable(true);
        marker.setOnMarkerClickListener(this);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(marker);
        marker.setIcon(icono);
        marker.setTitle(titulo);
    }

    public String parametrosAUrl(List<NameValuePair> params){
        String paramString = URLEncodedUtils.format(params, "utf-8");
        return paramString;
    }

    public void obtenerNombrePunto(final Marker marker, final EditText editText){
        final String [] nombre = {""};
        new AsyncTask<Void, Void, String>() {
            InputStream is = null;
            String json = "";
            JSONArray jObj = null;
            @Override
            protected String doInBackground(Void... params) {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                String[] latlon=marker.getPosition().toInvertedDoubleString().split(",");

                List<NameValuePair> parametros = new LinkedList<NameValuePair>();
                parametros.add(new BasicNameValuePair("lat", latlon[0]));
                parametros.add(new BasicNameValuePair("lon", latlon[1]));

                HttpGet get=new HttpGet(Commons.SERVER_IP+"/nombreRuta?"+parametrosAUrl(parametros));
                //Log.e("URL", "http://"+Commons.SERVER_IP+"/nombreRuta?"+parametrosAUrl(parametros));
                HttpResponse httpResponse = null;
                try {
                    httpResponse = httpClient.execute(get);
                    HttpEntity httpEntity = httpResponse.getEntity();
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
                    //Log.e("Mensage", json);
                } catch (Exception e) {
                    Log.e("Buffer Error", "Error converting result " + e.toString());
                }
                try {
                    jObj = new JSONArray(json);
                    nombre[0] =(String)jObj.toString().split(",")[1];
                    Log.i("NombreRuta", nombre[0]);
                } catch (JSONException e) {
                    Log.e("JSON Parser", "Error parsing data " + e.toString());
                }
                return nombre[0];
            }

            @Override
            protected void onPostExecute(String s) {
                editText.setText(s);
            }
        }.execute();
    }

    private void enviarAlerta(){
        final HttpClient client=new DefaultHttpClient();
        final HttpPost post=new HttpPost(Commons.SERVER_IP+"/crearAlerta/");

        List<NameValuePair> pairs=new ArrayList<>();
        pairs.add(new BasicNameValuePair("lat", ""+lugar.getPosition().getLatitude()));
        pairs.add(new BasicNameValuePair("lon", ""+lugar.getPosition().getLongitude()));
        pairs.add(new BasicNameValuePair("tipo", spinerTipo.getSelectedItem().toString()));
        pairs.add(new BasicNameValuePair("comentario", txtComentario.getText().toString()));
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
                    Toast.makeText(context, (String)jObj.get("mensage"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Log.e("JSON Parser", "Error parsing data " + e.toString());
                }
            }
        }.execute();
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        return true;
    }

    @Override
    public boolean longPressHelper(final GeoPoint geoPoint) {
        PopupMenu menu = new PopupMenu(getActivity(), v.findViewById(R.id.map2));
        menu.inflate(R.menu.popup_menu2);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
               lugar.setPosition(geoPoint);
               obtenerNombrePunto(lugar, txtLugar);
               map.invalidate();
               return true;
            }
        });
        menu.show();
        return true;
    }

    private GeoPoint obtenerMiUbicacion(){
        return miUbicacion.getMyLocation();
    }

    public void setLugar(){
        GeoPoint ubicacion=obtenerMiUbicacion();
        if(ubicacion!=null) {
            String [] latlon=ubicacion.toInvertedDoubleString().split(",");
            lugar.setPosition(ubicacion);
            map.invalidate();
            obtenerNombrePunto(lugar, txtLugar);
            mapController.animateTo(ubicacion);
        }
        else{
            Toast.makeText(context, "No se pudo obtener la ubicación, vuelva a intentarlo", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnlugar:
                setLugar();
                break;
            case R.id.btnEnviar:
                enviarAlerta();
                break;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        Toast.makeText(context, "Marker click: " + marker.getTitle(), Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        String [] latlon=marker.getPosition().toInvertedDoubleString().split(",");
        txtLugar.setText(latlon[0]+","+latlon[1]);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        obtenerNombrePunto(lugar, txtLugar);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }
}
