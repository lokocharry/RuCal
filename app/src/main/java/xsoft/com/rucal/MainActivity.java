package xsoft.com.rucal;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.ArrayList;

import GCM.Device;
import GCM.GCMPreference;
import common.Commons;

public class MainActivity extends ActionBarActivity {

    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;
    private GoogleCloudMessaging gcm;

    protected String regId;

    private void addDrawerItems() {
        ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();
        mNavItems.add(new NavItem("Rutas", "Calcular tu ruta", R.drawable.ic_directions_black_24dp));
        mNavItems.add(new NavItem("Alertas", "Crear una alerta", R.drawable.ic_warning_black_24dp));
        mNavItems.add(new NavItem("Acerca de", "Conocenos", R.drawable.ic_more_black_24dp));
        DrawerListAdapter adapter = new DrawerListAdapter(this, mNavItems);

        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                switch (position){
                    case 0:
                        RutaFragment rutaFragment=new RutaFragment();
                        ft.replace(R.id.content_frame, rutaFragment);
                        ft.commit();
                        getSupportActionBar().setTitle("Rutas");
                        break;
                    case 1:
                        AlertaFragment alertaFragment=new AlertaFragment();
                        ft.replace(R.id.content_frame, alertaFragment);
                        ft.commit();
                        getSupportActionBar().setTitle("Alertas");
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), "nou nou nou tambien", Toast.LENGTH_SHORT).show();
                        break;
                }

                mDrawerList.setItemChecked(position, true);
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Menu");
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();
        mDrawerList = (ListView)findViewById(R.id.navList);
        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        RutaFragment rutaFragment=new RutaFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.content_frame, rutaFragment);
        ft.commit();

    }

    private void registerGCM() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    String regId = GCMPreference.getRegistrationId(getApplicationContext());
                    if(regId.equals("")){
                        regId = gcm.register(Commons.GCM_SENDER);
                        GCMPreference.setRegistrationId(MainActivity.this.getApplicationContext(), regId);
                        String deviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                        Device.register(deviceID, regId);
                    }
                    return null;
                } catch (Exception e) {
                    return null;
                }
            }
        }.execute();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);

    }
}
