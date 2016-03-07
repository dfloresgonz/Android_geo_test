package facilito.codigo.app.dflores.com.myapplicationcf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import com.google.android.gms.location.LocationListener;//android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
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
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import Beans.BeanCombo;
import Beans.GetResponse;
import Beans.MapaVariables;
import Beans.Publicidad;
import Beans.Usuario;
import Beans.Utiles;
import Servicios.DistanciaComunidad;
import Servicios.Locales;
import Servicios.RutaService;
import de.hdodenhof.circleimageview.CircleImageView;

public class Mapa extends AppCompatActivity implements OnMapReadyCallback,
                                                      GoogleApiClient.ConnectionCallbacks,
                                                      GoogleApiClient.OnConnectionFailedListener,
                                                      LocationListener,
                                                      GetResponse {

    private static GoogleMap mMap;
    private static double lat = 0.0;
    private static double lon = 0.0;
    private double latMovi = 0.0;
    private double lonMovi = 0.0;
    PolygonOptions comunidad = new PolygonOptions();
    protected GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    static View vistaMapa = null;
    private static final String[] values = {"Drawer 1", "Drawer 2", "Drawer 3"};

    int ICONS[] = {R.drawable.user_icon,
                   R.drawable.camara,
                   R.drawable.money,
                   R.drawable.musica,
                   R.drawable.twitter};
    //Similarly we Create a String Resource for the name and email in the header view
    //And we also create a int resource for profile picture in the header view
    int PROFILE = R.drawable.usuario;

    RecyclerView mRecyclerView;                           // Declaring RecyclerView
    RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    DrawerLayout Drawer;                                  // Declaring DrawerLayout

    ActionBarDrawerToggle mDrawerToggle;
    SupportMapFragment mapFragment;

    AnimationDrawable animation = new AnimationDrawable();
    ImageView publicidadImagen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        SharedPreferences pref = getSharedPreferences("BUHOO_APP", MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);

        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View
        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size
        mAdapter = new MyAdapter(values, ICONS, pref.getString("NOMBRE_USUARIO", null), pref.getString("CORREO", null),PROFILE, this);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
        mRecyclerView.setAdapter(mAdapter);                              // Setting the adapter to RecyclerView

        mLayoutManager = new LinearLayoutManager(this);                 // Creating a layout Manager
        mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager
        Drawer = (DrawerLayout) findViewById(R.id.drawer_layout);        // Drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(this,Drawer, toolbar/*R.drawable.ic_drawer*/,R.string.drawer_open,R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                //super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Buscar");
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                //super.onDrawerClosed(drawerView);
                getSupportActionBar().setTitle("Mapa");
                invalidateOptionsMenu();
            }
        }; // Drawer Toggle Object Made
        Drawer.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerToggle.syncState();               // Finally we set the drawer toggle sync State

        vistaMapa = (View) this.findViewById(R.id.map);
        publicidadImagen = (ImageView) findViewById(R.id.imageView);

        Utiles.invocarPublicidadServicio(this);
        Utiles.invocarImagenServicio(this, pref.getString("FOTO", null));
        createLocationRequest();

        setBtnCerrarPublicidadOnClick();

        //Usuario usuario = new Usuario(pref.getInt("ID_USUARIO", 0), pref.getString("NOMBRE_USUARIO", null));
        Utiles.invocarComunidadServicio(this, pref.getInt("ID_USUARIO", 0), pref.getInt("ID_COMUNIDAD", 0));
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    protected void createLocationRequest() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        ////////////////////////////////////////////////////////////////////////////////////////////
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d("BUHOO", "case success :::::::::::>>>>>>>>>>>");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            Log.d("BUHOO", "case RESOLUTION_REQUIRED :::::::::::>>>>>>>>>>>");
                            status.startResolutionForResult(Mapa.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d("BUHOO", "case SETTINGS_CHANGE_UNAVAILABLE :::::::::::>>>>>>>>>>>");
                        break;
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("BUHOO", ":::: onConnected ::::");
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch(SecurityException e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.d("BUHOO", " ERROR onLocationChanged "+errors.toString());
        }
        //if (mRequestingLocationUpdates) {
        startLocationUpdates();
        //}
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("BUHOO", ":::: onConnectionSuspended ::::");
    }

    protected void onStart() {
        Log.d("BUHOO", ":::: onStart ::::");
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        Log.d("BUHOO", ":::: onStop ::::");
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d("BUHOO", ":::: onPause ::::");
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        getSupportFragmentManager().beginTransaction()
                .remove(mapFragment)
                .commit();
    }

    @Override
    public void onResume() {
        Log.d("BUHOO", ":::: onResume ::::");
        super.onResume();
        if (mGoogleApiClient.isConnected() /*&& !mRequestingLocationUpdates*/) {
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        try {
            Log.d("BUHOO", ":::: startLocationUpdates ::::");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch(SecurityException e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.d("BUHOO", " ERROR startLocationUpdates "+errors.toString());
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("BUHOO", ":::: onConnectionFailed ::::");
    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.d("BUHOO", "onLocationChanged:::::: "+location.getLatitude()+"   longitud: "+location.getLongitude());
        lat = location.getLatitude();
        lon = location.getLongitude();
        onMapReady(mMap);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item) || item.getItemId() == android.R.id.home) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Void getDataCombo(ArrayList<String> comusList, ArrayList<BeanCombo> comunidadesBeanCombo, String tipoCombo) {
        return null;//No implementado
    }

    @Override
    public Void getDataPublicidad(List<Publicidad> arryDraw) {
        for(Publicidad itm : arryDraw) {
            animation.addFrame(itm.getImagen(), 6000);
        }
        MapaVariables.arryDraw = arryDraw;//usado en el handleClickPublicidad
        animation.setOneShot(false);
        publicidadImagen.setBackgroundDrawable(animation);
        animation.start();
        publicidadImagen.setOnClickListener(handleClickPublicidad);
        return null;
    }

    @Override
    public Void getDataUsuarioFoto(Drawable imagen) {
        CircleImageView civ = (CircleImageView) findViewById(R.id.circleView);
        try {
            civ.setImageDrawable(imagen);
        } catch (Exception e) {
            Utiles.printearErrores(e, "CIRCLE IMAGE ERROR: ");
        }
        return null;
    }

    @Override
    public Void getDataComunidad(PolygonOptions comunidad, int idComunidad, String descComunidad) {
        this.comunidad = comunidad;
        MapaVariables.idComunidad   = idComunidad;
        MapaVariables.descComunidad = descComunidad;
        return null;
    }

    public static class GPSCheck extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                //Snackbar.make(vistaMapa, "¡Apagaste tu GPS!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Log.d("BUHOO", " :D HABILITADO !!!!!!!!!!!!!!!!!!!");
            } else {
                Log.d("BUHOO", " :( DESHABILITADO !!!!!!!!!!!!!!!!!!!");
                Snackbar.make(vistaMapa, "¡Apagaste tu GPS!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                //Toast.makeText(context, "Please switch on the GPS", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static class ReceiverDrawer extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String servicioLocales = "http://"+MapaVariables.ipServer+"/buhoo/intranet/mi_comunidad/getPuntosBusqueda_Service";
            Log.d("BUHOO", "Recibido!!! " + MapaVariables.enBusqueda);
            MapaVariables.localesJSArray = null;
            new Locales(context).execute(servicioLocales);
        }
    }

    public static class ReceiverLocales extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BUHOO", "Recibido ReceiverLocales!!! ");
            if (MapaVariables.localesJSArray != null) {
                    JSONArray locales = MapaVariables.localesJSArray;
                    Log.d("BUHOO", "locales arrayyyyyyyyyy!!! " + locales.length());
                    for (int i = 0; i < locales.length(); ++i) {
                        try {
                            JSONObject local = locales.getJSONObject(i);
                            int idLocal = local.getInt("id_local");
                            String nombreLocal = local.getString("nombre_local");
                            String direccion = local.getString("direccion");
                            String coord = local.getString("coord");
                            Log.d("BUHOO", "idLocal: " + idLocal + " | nombreLocal: " + nombreLocal +
                                    " | direccion : " + direccion + " | coord: " + coord);
                            List<String> puntosList = Arrays.asList(coord.split(" "));
                            double lat = Double.parseDouble(puntosList.get(0).replaceAll("\"", ""));
                            double lon = Double.parseDouble(puntosList.get(1).replaceAll("\"", ""));
                            LatLng localPostition = new LatLng(lat, lon);
                            mMap.addMarker(new MarkerOptions().position(localPostition).title(nombreLocal).snippet(direccion).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        } catch (Exception e) {
                            Utiles.printearErrores(e, "tratando el JSON localesss: ");
                        }
                    }
            } else {
                Log.d("BUHOO", "MapaVariables.localesJSArray ES NULL!!! ");
            }
        }
    }

    public static class DistanciaComunidadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BUHOO", "Recibido DistanciaComunidadReceiver!!! ");
            if (MapaVariables.distanciaPoligono != 0.0) {
                Utiles utiles = new Utiles();
                String distancia = utiles.distanciaFormat(MapaVariables.distanciaPoligono);
                Toast.makeText(context, MapaVariables.descComunidad+" - Distancia: "+distancia, Toast.LENGTH_LONG).show();
                pintarRuta(MapaVariables.latLonComunidad.latitude, MapaVariables.latLonComunidad.longitude, context);
            } else {
                Log.d("BUHOO", "MapaVariables.distanciaPoligono ES 0.0!!! ");
            }
        }
    }

    public static class RutaReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            Log.d("BUHOO", ",,,,,,,,,,,,,,,,,,Recibido RutaReceiver!!! ");
            if (MapaVariables.lineas != null) {
                Polyline polylineToAdd = MapaVariables.polylineToAdd;
                if(polylineToAdd == null) {
                    polylineToAdd = mMap.addPolyline(new PolylineOptions().addAll(MapaVariables.lineas).width(7).color(Color.RED));
                } else {
                    polylineToAdd.remove();
                    polylineToAdd = mMap.addPolyline(new PolylineOptions().addAll(MapaVariables.lineas).width(7).color(Color.RED));
                }
                polylineToAdd.setClickable(true);
                MapaVariables.polylineToAdd = polylineToAdd;
                mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                    @Override
                    public void onPolylineClick(Polyline polyline) {
                        if(polyline.equals(MapaVariables.polylineToAdd)) {
                            float longitudRuta = getLongitudRuta(polyline.getPoints());
                            Utiles utiles = new Utiles();
                            String distancia = utiles.distanciaFormat(longitudRuta);
                            Toast.makeText(context,"Distancia de la ruta: "+distancia, Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } else {
                Log.d("BUHOO", "MapaVariables.lineas ES NULL !!! ");
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
        googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                latMovi = cameraPosition.target.latitude;
                lonMovi = cameraPosition.target.longitude;
                //Log.d("BUHOO", "MOVISTE MAPA A:  lat: " + cameraPosition.target.latitude + "   long: " + cameraPosition.target.longitude);
            }
        });
        // Add a marker in Sydney and move the camera
        if(lat != 0.0 && lon != 0.0) {
            LatLng myPos = new LatLng(lat, lon);
            //mMap.clear();
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);
            Marker markerMiPosicion = MapaVariables.markerMiPosicion;
            if(markerMiPosicion == null) {
                //icon(BitmapDescriptorFactory.fromResource(R.drawable.mi_ubicacion)
                markerMiPosicion = mMap.addMarker(new MarkerOptions().position(myPos).title("MI POSICION ACTUAL"));
            } else {
                markerMiPosicion.remove();
                markerMiPosicion = mMap.addMarker(new MarkerOptions().position(myPos).title("MI POSICION ACTUAL"));
            }
            MapaVariables.markerMiPosicion = markerMiPosicion;
            //mMap.addMarker(new MarkerOptions().position(myPos).title("MI POSICION ACTUAL").snippet("1"));
            float zoomLevel = 17; //This goes up to 21
            //Log.d("BUHOO"," PINTANDO MAPA cantidad latlongs: "+comunidad.getPoints().size());
            if(comunidad.getPoints().size() > 0) {
                Polygon polygon = mMap.addPolygon(comunidad/*.fillColor(Color.BLUE)*/);
                polygon.setClickable(true);
                mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener()  {
                    @Override
                    public void onPolygonClick(Polygon polygon) {
                        String servicioDistanciaPoly = "http://"+MapaVariables.ipServer+"/buhoo/intranet/mi_comunidad/getDistanciaComunidad_Service?latitud="+lat+"&longitud="+lon+"&idComunidad="+MapaVariables.idComunidad;
                        new DistanciaComunidad(Mapa.this).execute(servicioDistanciaPoly);
                    }
                });
            }
            if(!MapaVariables.enBusqueda) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPos, zoomLevel));
            }
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker arg0) {
                    if (!arg0.getTitle().equals("MI POSICION ACTUAL")) {
                        Location markerLoc = new Location("Marker");
                        markerLoc.setLatitude(arg0.getPosition().latitude);
                        markerLoc.setLongitude(arg0.getPosition().longitude);

                        Location currentLoc = new Location("Current");
                        currentLoc.setLatitude(lat);
                        currentLoc.setLongitude(lon);

                        Float distance = currentLoc.distanceTo(markerLoc);
                        Utiles u = new Utiles();
                        String distancia = u.distanciaFormat(distance);
                        pintarRuta(arg0.getPosition().latitude, arg0.getPosition().longitude, Mapa.this);
                        Toast.makeText(Mapa.this, arg0.getTitle()+" - "+arg0.getSnippet()+" Distancia: "+distancia, Toast.LENGTH_LONG).show();
                    }
                    return true;
                }

            });
        } else {
            LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            Log.d("BUHOO", "PINTO SYDNEY");
        }
    }

    public static void pintarRuta(double latitudPuntoDestino, double longitudPuntoDestino, Context ctx) {
        String servicio = "http://maps.googleapis.com/maps/api/directions/json?origin="
                + lat + "," + lon +"&destination="
                + latitudPuntoDestino + "," + longitudPuntoDestino + "&sensor=false";
        new RutaService(ctx).execute(servicio);
    }

    protected static float getLongitudRuta(List<LatLng> points) {
        float totalDistance = 0;
        for(int i = 1; i < points.size(); i++) {
            Location currLocation = new Location("this");
            currLocation.setLatitude(points.get(i).latitude);
            currLocation.setLongitude(points.get(i).longitude);

            Location lastLocation = new Location("this");
            lastLocation.setLatitude(points.get(i-1).latitude);
            lastLocation.setLongitude(points.get(i - 1).longitude);

            totalDistance += lastLocation.distanceTo(currLocation);
            Log.d("BUHOO", "totalDistance: "+totalDistance);
        }
        return totalDistance;
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

    private void setBtnCerrarPublicidadOnClick() {
        final Button btnCerrar = (Button) findViewById(R.id.btnCerrarPublicidad);
        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publicidadImagen.setVisibility(View.GONE);
                btnCerrar.setVisibility(View.GONE);
            }
        });
    }

    private View.OnClickListener handleClickPublicidad = new View.OnClickListener(){
        public void onClick(View arg0) {
            try {
                Publicidad publi = Utiles.getPublicidad(animation.getCurrent());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(publi.getRutaURL()));
                startActivity(browserIntent);
            } catch(Exception e) {
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                Log.d("BUHOO", " ERROR IMAGENES " + errors.toString());
            }
        }
    };

    public void cerrarSession(View v) {
        SharedPreferences pref = getSharedPreferences("BUHOO_APP", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
        Intent goToLogin = new Intent(Mapa.this, MainActivity.class);
        startActivity(goToLogin);
    }
}