package xsoft.com.rucal;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
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
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import HereMaps.HereTileSource;
import common.Commons;

/**
 * Created by Usuario on 02/02/2016.
 */
public class RutaFragment extends Fragment implements MapEventsReceiver, Marker.OnMarkerClickListener, Marker.OnMarkerDragListener, View.OnClickListener {

    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay miUbicacion;
    private EditText txtOrigen;
    private EditText txtDestino;
    private Context context;
    private ImageButton btnOrigen;
    private ImageButton btnDestino;
    private Button btnCalcular;

    private Marker origen;
    private Marker destino;

    private View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_ruta, container, false);

        context=getActivity().getApplicationContext();

        txtOrigen=(EditText)v.findViewById(R.id.txtOrigen);
        txtDestino=(EditText)v.findViewById(R.id.txtDestino);

        btnOrigen=(ImageButton)v.findViewById(R.id.btnOrigen);
        btnDestino=(ImageButton)v.findViewById(R.id.btnDestino);
        btnCalcular=(Button)v.findViewById(R.id.btnCalcular);

        btnOrigen.setOnClickListener(this);
        btnDestino.setOnClickListener(this);
        btnCalcular.setOnClickListener(this);

        map = (MapView) v.findViewById(R.id.map);
        map.setClickable(true);

        String[] myStringArray = {"https://1.aerial.maps.cit.api.here.com/maptile/2.1/maptile/newest/hybrid.day/"};
        final MapTileProviderBasic tileProvider = new MapTileProviderBasic(getActivity().getApplicationContext());
        final ITileSource tileSource = new HereTileSource("HereMaps", 1, 16, 256, ".png", myStringArray);
        tileProvider.setTileSource(tileSource);
        final TilesOverlay tilesOverlay = new TilesOverlay(tileProvider, this.getActivity().getBaseContext());
        tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        map.setTileSource(tileSource);

        //BingMapTileSource bing=null;
        //try{
            //bing = new BingMapTileSource("en");
            //bing.setStyle(BingMapTileSource.IMAGERYSET_ROAD);
            //map.setTileSource(bing);
        //}
        //catch(Exception e){
            //e.printStackTrace();
        //}

        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(context, map);
        mRotationGestureOverlay.setEnabled(true);
        map.setMultiTouchControls(true);
        map.getOverlays().add(mRotationGestureOverlay);

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

        MinimapOverlay miniMapOverlay = new MinimapOverlay(getActivity(), map.getTileRequestCompleteHandler());
        miniMapOverlay.setZoomDifference(5);
        //miniMapOverlay.setTileSource(bing);
        miniMapOverlay.setHeight(200);
        miniMapOverlay.setWidth(200);
        miniMapOverlay.setTileSource(tileSource);
        map.getOverlays().add(miniMapOverlay);

        origen=new Marker(map);
        destino=new Marker(map);
        origen.setOnMarkerClickListener(this);
        origen.setOnMarkerDragListener(this);
        destino.setOnMarkerClickListener(this);
        destino.setOnMarkerDragListener(this);
        crearMarker(origen, new GeoPoint(5.508006, -73.37202), "Origen", getResources().getDrawable(R.drawable.ic_person_pin_circle_black_36dp));
        crearMarker(destino, new GeoPoint(5.576521, -73.338031), "Destino", getResources().getDrawable(R.drawable.ic_directions_run_black_36dp));

        obtenerAlertas();
        obtenerEstacionesDeServicio();
        map.invalidate();

        return v;
    }

    private GeoPoint obtenerMiUbicacion(){
        return miUbicacion.getMyLocation();
    }

    public void setOrigen(){
        GeoPoint ubicacion=obtenerMiUbicacion();
        if(ubicacion!=null) {
            String [] latlon=ubicacion.toInvertedDoubleString().split(",");
            origen.setPosition(ubicacion);
            map.invalidate();
            obtenerNombrePunto(origen, txtOrigen);
            mapController.animateTo(ubicacion);
        }
        else{
            Toast.makeText(context, "No se pudo obtener la ubicación, vuelva a intentarlo", Toast.LENGTH_LONG).show();
        }
    }

    public void setDestino(){
        GeoPoint ubicacion=obtenerMiUbicacion();
        if(ubicacion!=null) {
            String [] latlon=ubicacion.toInvertedDoubleString().split(",");
            destino.setPosition(ubicacion);
            map.invalidate();
            obtenerNombrePunto(destino, txtDestino);
            mapController.animateTo(ubicacion);
        }
        else{
            Toast.makeText(context, "No se pudo obtener la ubicación, vuelva a intentarlo", Toast.LENGTH_LONG).show();
        }
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
                //Log.e("URL", Commons.SERVER_IP+"/nombreRuta?"+parametrosAUrl(parametros));
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

    public void obtenerAlertas(){
        new AsyncTask<Void, Void, Void>() {
            InputStream is = null;
            String json = "";
            JSONArray jObj = null;

            ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                dialog = ProgressDialog.show(getActivity(), "","Cargando alertas...", true);
            }

            @Override
            protected Void doInBackground(Void... params) {
                DefaultHttpClient httpClient = new DefaultHttpClient();

                HttpGet get=new HttpGet(Commons.SERVER_IP+"/alertas");
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
                } catch (Exception e) {
                    Log.e("Buffer Error", "Error converting result " + e.toString());
                }
                try {
                    Log.e("Resultado", json);
                    jObj = new JSONArray(json);
                    for (int i=0; i<jObj.length(); i++) {
                        JSONObject nodo = new JSONObject(jObj.getJSONArray(i).getString(0));
                        double lat=(double)nodo.getJSONArray("coordinates").get(1);
                        double lon=(double)nodo.getJSONArray("coordinates").get(0);
                        GeoPoint gPt=new GeoPoint(lat, lon);
                        Marker marker=new Marker(map);
                        marker.setPosition(gPt);
                        marker.setTitle(jObj.getJSONArray(i).getString(2));
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        switch (jObj.getJSONArray(i).getString(1)){
                            case "A":
                                marker.setIcon(new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.accidente), 90, 90, true)));
                                break;
                            case "C":
                                marker.setIcon(new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.cierre), 90, 90, true)));
                                break;
                            case "M":
                                marker.setIcon(new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.construccion), 90, 90, true)));
                                break;
                        }
                        map.getOverlays().add(marker);
                    }
                } catch (JSONException e) {
                    Log.e("JSON Parser", "Error parsing data " + e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                dialog.dismiss();
            }
        }.execute();
        map.invalidate();
    }

    public void obtenerEstacionesDeServicio(){
        new AsyncTask<Void, Void, Void>() {
            InputStream is = null;
            String json = "";
            JSONArray jObj = null;

            ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                dialog = ProgressDialog.show(getActivity(), "","Cargando estaciones de servicio...", true);
            }

            @Override
            protected Void doInBackground(Void... params) {
                DefaultHttpClient httpClient = new DefaultHttpClient();

                HttpGet get=new HttpGet(Commons.SERVER_IP+"/estaciones");
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
                } catch (Exception e) {
                    Log.e("Buffer Error", "Error converting result " + e.toString());
                }
                try {
                    Log.e("Resultado", json);
                    jObj = new JSONArray(json);
                    for (int i=0; i<jObj.length(); i++) {
                        JSONObject nodo = new JSONObject(jObj.getJSONArray(i).getString(0));
                        double lat=(double)nodo.getJSONArray("coordinates").get(1);
                        double lon=(double)nodo.getJSONArray("coordinates").get(0);
                        GeoPoint gPt=new GeoPoint(lat, lon);
                        Marker marker=new Marker(map);
                        marker.setPosition(gPt);
                        marker.setTitle(jObj.getJSONArray(i).getString(1));
                        String subDes="";
                        subDes+="Precio Gasolina: $"+jObj.getJSONArray(i).getString(2)+"\n";
                        subDes+="Precio Gas: $"+jObj.getJSONArray(i).getString(3)+"\n";
                        subDes+="Precio ACPM: $"+jObj.getJSONArray(i).getString(4);
                        marker.setSubDescription(subDes);
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        marker.setIcon(getResources().getDrawable(R.drawable.ic_local_gas_station_black_24dp));
                        map.getOverlays().add(marker);
                    }
                } catch (JSONException e) {
                    Log.e("JSON Parser", "Error parsing data " + e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                dialog.dismiss();
            }

        }.execute();
        map.invalidate();
    }

    private Polyline ruta;

    public void calcularRuta(){
        if(ruta!=null){
            map.getOverlays().remove(ruta);
        }
        ruta=new Polyline(getActivity());
        ruta.setWidth(7);
        ruta.setVisible(true);
        ruta.setColor(Color.BLUE);
        new AsyncTask<Void, Void, Void>() {
            InputStream is = null;
            String json = "";
            JSONArray jObj = null;
            @Override
            protected Void doInBackground(Void... params) {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                String[] orilatlon=origen.getPosition().toInvertedDoubleString().split(",");
                String[] deslatlon=destino.getPosition().toInvertedDoubleString().split(",");

                List<NameValuePair> parametros = new LinkedList<NameValuePair>();
                parametros.add(new BasicNameValuePair("orilat", orilatlon[0]));
                parametros.add(new BasicNameValuePair("orilon", orilatlon[1]));
                parametros.add(new BasicNameValuePair("deslat", deslatlon[0]));
                parametros.add(new BasicNameValuePair("deslon", deslatlon[1]));

                HttpGet get=new HttpGet(Commons.SERVER_IP+"/rutas?"+parametrosAUrl(parametros));
                Log.e("URL", Commons.SERVER_IP+"/rutas?"+parametrosAUrl(parametros));
                HttpResponse httpResponse = null;
                try {
                    httpResponse = httpClient.execute(get);
                    HttpEntity httpEntity = httpResponse.getEntity();
                    is = httpEntity.getContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
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
                    List<GeoPoint> puntosRuta=new ArrayList<GeoPoint>();
                    for (int i=0; i<jObj.length(); i++) {
                        JSONArray nodo = jObj.getJSONArray(i);

                        Log.i("Nodo", nodo.toString());
                        String aux=(String)nodo.get(0);

                        JSONArray auxPuntos=new JSONObject(aux.substring(aux.indexOf("{"), aux.lastIndexOf("}") + 1)).getJSONArray("coordinates");
                        Log.i("Puntos", auxPuntos.toString());
                        for (int j=0; j<auxPuntos.length(); j++){
                            double lat=(double)auxPuntos.getJSONArray(j).get(1);
                            double lon=(double)auxPuntos.getJSONArray(j).get(0);
                            GeoPoint puntoRuta=new GeoPoint(lat, lon);
                            puntosRuta.add(puntoRuta);
                        }
                    }
                    ruta.setPoints(puntosRuta);
                    map.getOverlays().add(ruta);
                    mapController.animateTo(ruta.getPoints().get(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    private Polyline rutaAlt;

    public void calcularRutaAlterna(){
        if(rutaAlt!=null){
            map.getOverlays().remove(rutaAlt);
        }
        rutaAlt=new Polyline(getActivity());
        rutaAlt.setWidth(7);
        rutaAlt.setVisible(true);
        rutaAlt.setColor(Color.RED);
        new AsyncTask<Void, Void, Void>() {
            InputStream is = null;
            String json = "";
            JSONArray jObj = null;
            @Override
            protected Void doInBackground(Void... params) {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                String[] orilatlon=origen.getPosition().toInvertedDoubleString().split(",");
                String[] deslatlon=destino.getPosition().toInvertedDoubleString().split(",");

                List<NameValuePair> parametros = new LinkedList<NameValuePair>();
                parametros.add(new BasicNameValuePair("orilat", orilatlon[0]));
                parametros.add(new BasicNameValuePair("orilon", orilatlon[1]));
                parametros.add(new BasicNameValuePair("deslat", deslatlon[0]));
                parametros.add(new BasicNameValuePair("deslon", deslatlon[1]));

                HttpGet get=new HttpGet(Commons.SERVER_IP+"/rutasAlt?"+parametrosAUrl(parametros));
                Log.e("URL", Commons.SERVER_IP+"/rutas?"+parametrosAUrl(parametros));
                HttpResponse httpResponse = null;
                try {
                    httpResponse = httpClient.execute(get);
                    HttpEntity httpEntity = httpResponse.getEntity();
                    is = httpEntity.getContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
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
                    List<GeoPoint> puntosRuta=new ArrayList<GeoPoint>();
                    for (int i=0; i<jObj.length(); i++) {
                        JSONArray nodo = jObj.getJSONArray(i);

                        Log.i("Nodo", nodo.toString());
                        String aux=(String)nodo.get(0);

                        JSONArray auxPuntos=new JSONObject(aux.substring(aux.indexOf("{"), aux.lastIndexOf("}") + 1)).getJSONArray("coordinates");
                        Log.i("Puntos", auxPuntos.toString());
                        for (int j=0; j<auxPuntos.length(); j++){
                            double lat=(double)auxPuntos.getJSONArray(j).get(1);
                            double lon=(double)auxPuntos.getJSONArray(j).get(0);
                            GeoPoint puntoRuta=new GeoPoint(lat, lon);
                            puntosRuta.add(puntoRuta);
                        }
                    }
                    rutaAlt.setPoints(puntosRuta);
                    map.getOverlays().add(rutaAlt);
                    mapController.animateTo(rutaAlt.getPoints().get(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();
    }

    public void obtenerRutas(){
        calcularRuta();
        calcularRutaAlterna();
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

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        return true;
    }

    @Override
    public boolean longPressHelper(final GeoPoint geoPoint) {
        PopupMenu menu = new PopupMenu(getActivity(), v.findViewById(R.id.btnCalcular));
        menu.inflate(R.menu.popup_menu);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.marcarOrigen:
                        origen.setPosition(geoPoint);
                        obtenerNombrePunto(origen, txtOrigen);
                        break;
                    case R.id.marcarDestino:
                        destino.setPosition(geoPoint);
                        obtenerNombrePunto(destino, txtDestino);
                        break;
                }
                map.invalidate();
                return true;
            }
        });
        menu.show();
        return true;
    }

    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        Toast.makeText(context, "Marker click: "+ marker.getTitle(), Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        if(marker==origen){
            String [] latlon=marker.getPosition().toInvertedDoubleString().split(",");
            txtOrigen.setText(latlon[0]+","+latlon[1]);
        }
        if(marker==destino){
            String [] latlon=marker.getPosition().toInvertedDoubleString().split(",");
            txtDestino.setText(latlon[0]+","+latlon[1]);
        }
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        if(marker==origen){
            obtenerNombrePunto(marker, txtOrigen);
        }
        if(marker==destino){
            obtenerNombrePunto(marker, txtDestino);
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnCalcular:
                obtenerRutas();
                break;
            case R.id.btnDestino:
                setDestino();
                break;
            case R.id.btnOrigen:
                setOrigen();
                break;
        }
    }
}