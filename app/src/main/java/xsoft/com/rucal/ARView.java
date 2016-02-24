package xsoft.com.rucal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.beyondar.android.fragment.BeyondarFragmentSupport;
import com.beyondar.android.plugin.radar.RadarView;
import com.beyondar.android.plugin.radar.RadarWorldPlugin;
import com.beyondar.android.view.OnClickBeyondarObjectListener;
import com.beyondar.android.world.BeyondarObject;
import com.beyondar.android.world.GeoObject;
import com.beyondar.android.world.World;

import java.util.ArrayList;

/**
 * Created by INGENIERIA on 24/02/2016.
 */
public class ARView extends ActionBarActivity implements OnClickBeyondarObjectListener {

    private BeyondarFragmentSupport mBeyondarFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_ar);
        mBeyondarFragment = (BeyondarFragmentSupport) getSupportFragmentManager().findFragmentById(R.id.beyondarFragment);
        mBeyondarFragment.setOnClickBeyondarObjectListener(this);

        Intent intent=getIntent();

        World world = new World(getApplicationContext());
        world.setDefaultImage(R.drawable.ic_my_location_black_36dp);
        world.setGeoPosition(intent.getDoubleExtra("locLat", 0.0)-30, intent.getDoubleExtra("locLon", 0.0)-4);
        GeoObject go1 = new GeoObject();
        go1.setGeoPosition(intent.getDoubleExtra("lat", 0.0)-30, intent.getDoubleExtra("lat", 0.0)-4);
        go1.setImageResource(R.drawable.ic_directions_run_black_36dp);
        go1.setName("Destino");
        world.addBeyondarObject(go1);
        mBeyondarFragment.setWorld(world);

        RadarView radarView = (RadarView) findViewById(R.id.radarView);
        // Create the Radar plugin
        RadarWorldPlugin mRadarPlugin = new RadarWorldPlugin(getApplicationContext());
        // set the radar view in to our radar plugin
        mRadarPlugin.setRadarView(radarView);
        // Set how far (in meters) we want to display in the view
        mRadarPlugin.setMaxDistance(1000);
        // and finally let's add the plugin
        world.addPlugin(mRadarPlugin);
        Toast.makeText(getApplicationContext(), ""+world.getLatitude(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClickBeyondarObject(ArrayList<BeyondarObject> arrayList) {
        Toast.makeText(this, arrayList.get(0).getName(), Toast.LENGTH_LONG).show();
    }
}
