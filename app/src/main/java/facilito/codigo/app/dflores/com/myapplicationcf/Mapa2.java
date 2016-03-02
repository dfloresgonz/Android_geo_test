package facilito.codigo.app.dflores.com.myapplicationcf;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import org.json.JSONObject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import Beans.Usuario;
import Beans.Utiles;

public class Mapa2 extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, ResultCallback<LocationSettingsResult>, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private double lat = 0.0;
    private double lon = 0.0;
    PolygonOptions comunidad = new PolygonOptions();
    LocationManager mLocationManager;
    Criteria criteria;
    String provider;
    Location location;
    /* GPS Constant Permission */
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;
    private static final long CADA_CUANTOS_SEGUNDOS_ACTUALIZA_UBICACION = 2000;//milisegundos
    private static final float CADA_CUANTOS_METROS_ACTUALIZA_UBICACION = 1;//metros

    protected LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected GoogleApiClient mGoogleApiClient;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 4000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    View vistaMapa = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        vistaMapa = (View) this.findViewById(R.id.map);
        //getCoordenadas();

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        provider = mLocationManager.getBestProvider(criteria, true);
        /*if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }*/
        /*if (mLocationManager != null) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.removeUpdates(GPSListener.this);
            }
        }*/
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("BUHOO", "::::::: STEP 1 :::::: ");
            // No one provider activated: prompt GPS
            if (provider == null || provider.equals("")) {
                Log.d("BUHOO", "::::::: STEP 2 :::::: ");
                //startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                mGoogleApiClient = new GoogleApiClient
                        .Builder(this)
                        .enableAutoManage(this, 34992, this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                locationChecker(mGoogleApiClient, Mapa2.this);
            }


            boolean providerEnabled = mLocationManager.isProviderEnabled(provider);
            if(providerEnabled) {
                Log.d("BUHOO", ":::::::  providerEnabled :::::: ");
                mLocationManager.requestLocationUpdates(provider, CADA_CUANTOS_SEGUNDOS_ACTUALIZA_UBICACION, CADA_CUANTOS_METROS_ACTUALIZA_UBICACION, mLocationListener);
            } else {
                Log.d("BUHOO", "::::::: else providerEnabled :::::: ");
                mGoogleApiClient = new GoogleApiClient
                        .Builder(this)
                        .enableAutoManage(this, 34992, this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                locationChecker(mGoogleApiClient, Mapa2.this);
            }
            location = mLocationManager.getLastKnownLocation(provider);
            if(location == null) {
                Log.d("BUHOO", "::::::: CASE 3.4 location NULL :::::: ");
                mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();/*new GoogleApiClient
                        .Builder(this)
                        .enableAutoManage(this, 34992, this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();*/
                locationChecker(mGoogleApiClient, Mapa2.this);
                Log.d("BUHOO", "::::::: locationChecker seteado :::::: ");
            } else {
                Log.d("BUHOO", "::::::: CASE 3.5 :::::: "+location.toString());
                lat = location.getLatitude();
                lon = location.getLongitude();
            }
            Log.d("BUHOO", "::::::: STEP 3 :::::: ");
            // One or both permissions are denied.
        } else {
            Log.d("BUHOO", "::::::: STEP 4 :::::: ");
            // The ACCESS_COARSE_LOCATION is denied, then I request it and manage the result in
            // onRequestPermissionsResult() using the constant MY_PERMISSION_ACCESS_FINE_LOCATION
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSION_ACCESS_COARSE_LOCATION);
                Log.d("BUHOO", "::::::: STEP 5 :::::: ");
            }
            // The ACCESS_FINE_LOCATION is denied, then I request it and manage the result in
            // onRequestPermissionsResult() using the constant MY_PERMISSION_ACCESS_FINE_LOCATION
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                        MY_PERMISSION_ACCESS_FINE_LOCATION);
                Log.d("BUHOO", "::::::: STEP 6 :::::: ");
            }

        }
        SharedPreferences pref = getSharedPreferences("BUHOO_APP", MODE_PRIVATE);
        Usuario usuario = new Usuario(pref.getInt("ID_USUARIO", 0), pref.getString("NOMBRE_USUARIO", null));
        int idComu = pref.getInt("ID_COMUNIDAD", 0);
        String server = getResources().getString(R.string.ip_server);
        String servicio = "http://"+server+"/buhoo/intranet/mi_comunidad/getComunidadByPersona_Service?id_persona="+usuario.getIdUsuario()+"&id_comunidad="+idComu;
        Log.d("BUHOO", "servicioservicioservicio:::::: "+servicio);
        new llamarServicio().execute(servicio);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            try {
                Log.d("BUHOO", "::::::::onLocationChanged:::::: ");
                Log.d("BUHOO", "onLocationChanged:::::: "+location.getLatitude()+"   longitud: "+location.getLongitude());
                lat = location.getLatitude();
                lon = location.getLongitude();
                onMapReady(mMap);
            } catch(Exception e) {
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                Log.d("BUHOO", " ERROR onLocationChanged "+errors.toString());
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("BUHOO", " ............. onStatusChanged ......... ");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d("BUHOO", " ............. onProviderEnabled ......... ");
            onMapReady(mMap);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d("BUHOO", " ............. onProviderDisabled ......... ");
            Snackbar.make(vistaMapa, "¡Apagaste tu GPS!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    };

    public static void locationChecker(GoogleApiClient mGoogleApiClient, final Activity activity) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);//30 000
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);//5 000
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                Log.d("BUHOO", "............  status.getStatusCode()  ............. "+status.getStatusCode());
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        Log.d("BUHOO", "............  LocationSettingsStatusCodes.SUCCESS  .............");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            Log.d("BUHOO", "............  LocationSettingsStatusCodes.RESOLUTION_REQUIRED  .............");
                            status.startResolutionForResult(activity, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d("BUHOO", "............  LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE  .............");
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Log.d("BUHOO", "mGoogleApiClient.isConnected()::::::::::::::::::::::::::::");
        //new Thread(new GetContent()).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            mLocationManager.removeUpdates(mLocationListener);
        } catch (SecurityException se) {
            StringWriter errors = new StringWriter();
            se.printStackTrace(new PrintWriter(errors));
            Log.d("BUHOO", " ERROR SecurityException onLocationChanged "+errors.toString());
        }
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("BUHOO", "............  onConnected  .............");
        try {
            mLocationManager.requestLocationUpdates(provider, CADA_CUANTOS_SEGUNDOS_ACTUALIZA_UBICACION, CADA_CUANTOS_METROS_ACTUALIZA_UBICACION, mLocationListener);
        } catch (SecurityException se) {
            StringWriter errors = new StringWriter();
            se.printStackTrace(new PrintWriter(errors));
            Log.d("BUHOO", " ERROR SecurityException onConnected "+errors.toString());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("BUHOO", "............  onConnectionSuspended  .............");
    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        Log.d("BUHOO", "............  onResult  .............");
        final Status status = locationSettingsResult.getStatus();
        Intent resolutionIntent;
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                // Everything is OK, starting request location updates
                Log.d("BUHOO", "ACEPTO EL GPS!!!!!");
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.d("BUHOO", "RESOLUTION_REQUIRED!!!!!");
                // Seems the user need to change setting to enable locations updates, call startResolutionForResult(Activity, REQUEST_CODE)
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.d("BUHOO", "SETTINGS_CHANGE_UNAVAILABLE!!!!!");
                // Error, cannot retrieve location updates.
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("BUHOO", "onActivityResult(" + requestCode + ", " + resultCode + ")");
        // Decide what to do based on the original request code
        /*switch (requestCode) {
            case Mapa.PLAY_CONNECTION_FAILURE_RESOLUTION_REQUEST:*/
        switch (resultCode) {
            case Activity.RESULT_OK:
                // here we want to initiate location requests!
                // mLocationClient = new LocationClient(this, this, this);
                Log.d("BUHOO", "Activity.RESULT_OK...................");
                try {
                    mLocationManager.requestLocationUpdates(provider, CADA_CUANTOS_SEGUNDOS_ACTUALIZA_UBICACION, CADA_CUANTOS_METROS_ACTUALIZA_UBICACION, mLocationListener);
                } catch (SecurityException se) {
                    StringWriter errors = new StringWriter();
                    se.printStackTrace(new PrintWriter(errors));
                    Log.d("BUHOO", " ERROR SecurityException onLocationChanged "+errors.toString());
                }
                break;
        }
        //break;
        //}
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("BUHOO", "............  onConnectionFailed  .............");
    }

    private class llamarServicio extends AsyncTask<String, Void, String> {
        Utiles utiles = new Utiles();
        protected String doInBackground(String... urls) {
            return utiles.readJSONFeed(urls[0]);
        }

        protected void onPostExecute(String result) {
            try {
                Log.d("BUHOO", "result:::::: "+result);
                JSONObject mainResponseObject = new JSONObject(result);
                try {
                    String error = mainResponseObject.getString("error");
                    if("0".equals(error)) {
                        JSONObject polyObj = new JSONObject(mainResponseObject.getString("poligono"));
                        String puntos = polyObj.getString("puntos");
                        List<String> puntosList = Arrays.asList(puntos.split(","));
                        for(Iterator it = puntosList.iterator(); it.hasNext(); ) {
                            String str = (String) it.next();
                            String[] latlon = str.split(" ");
                            double lati  = Double.parseDouble(latlon[0].replaceAll("\"", ""));
                            double longi = Double.parseDouble(latlon[1].replaceAll("\"", ""));

                            comunidad.add(new LatLng(lati, longi));
                            //Log.d("BUHOO", "lati:::::: "+lati+"  ... "+longi);
                        }
                        Log.d("BUHOO"," onPostExecute cantidad latlongs: "+comunidad.getPoints().size());
                        onMapReady(mMap);
                    } else {
                        Log.d("CREATION", " ---- error inesperdo: " );
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    StringWriter errors = new StringWriter();
                    e.printStackTrace(new PrintWriter(errors));
                    Log.d("CREATION", "tratando el JSON: "+errors.toString());
                }
            } catch (Exception e) {
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                Log.d("CREATION", "errorrrr onPostExecute: "+errors.toString());
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        if(lat != 0.0 && lon != 0.0) {
            LatLng myPos = new LatLng(lat, lon);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);
            mMap.addMarker(new MarkerOptions().position(myPos).title("MI POSICION ACTUAL"));
            float zoomLevel = 17; //This goes up to 21
            //Polygon polygon = mMap.addPolygon(comunidad);
            Log.d("BUHOO"," PINTANDO MAPA cantidad latlongs: "+comunidad.getPoints().size());
            if(comunidad.getPoints().size() > 0) {
                mMap.addPolygon(comunidad/*.fillColor(Color.BLUE)*/);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPos, zoomLevel));
        } else {
            LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            Log.d("BUHOO", "PINTO SYDNEY");
        }
    }

    public void getCoordenadas() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        Location l = null;
        try {
            Log.d("BUHOO", "PROVIDERS: "+providers.size());
            for (int i = 0; i < providers.size(); i++) {
                l = lm.getLastKnownLocation(providers.get(i));Log.d("BUHOO", "L to strng: "+l.toString());
                if (l != null) {
                    Log.d("BUHOO", "LATITUDE....: "+l.getLatitude()+"  -  LONGITUD: "+l.getLongitude());
                    lat = l.getLatitude();
                    lon = l.getLongitude();
                    break;
                }
            }
        } catch(SecurityException e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.d("BUHOO", "obteniendo data....: "+errors.toString());
        }
    }

    @Override
    public void onBackPressed() {
        //Include the code here
        return;
    }

    public void cerrarSession(View v) {
        SharedPreferences pref = getSharedPreferences("BUHOO_APP", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
        Intent goToLogin = new Intent(Mapa2.this, MainActivity.class);
        startActivity(goToLogin);
    }
}
