package facilito.codigo.app.dflores.com.myapplicationcf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Adaptadores.IncidenciasAdapter;
import Beans.DBController;
import Beans.IncidenciaBean;
import Beans.MapaVariables;
import Beans.Utiles;
import Interfaces.IncidenciasInterface;
import Servicios.Locales;

/**
 * Created by diego on 25/06/2016.
 */
public class Incidencia extends AppCompatActivity implements IncidenciasInterface{

    private static RecyclerView recycler;
    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;
    List<IncidenciaBean> lstIncidenciasRemote = new ArrayList<IncidenciaBean>();
    private PopupWindow pw;
    static Context ctx;
    DBController controller = new DBController(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.incidencias);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarInc);
        setSupportActionBar(toolbar);

        recycler = (RecyclerView) findViewById(R.id.lstIncidencias);
        recycler.setHasFixedSize(true);

        // Usar un administrador para LinearLayout
        lManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(lManager);

        adapter = new IncidenciasAdapter(lstIncidenciasRemote);
        recycler.setAdapter(adapter);
        //controller.clearDB();
        boolean conectado = checkInternet(this);
        if (conectado) {
            int unsynched = controller.dbSyncCount();
            if(unsynched > 0) {
                List<IncidenciaBean> pendientes = controller.getUnsynchedIncidencias();
                if(pendientes.size() > 0) {
                    JSONObject jsonGeneral = new JSONObject();
                    for (IncidenciaBean pend : pendientes) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.accumulate("id_incidencia_local", pend.getIdIncidenciaLocal());
                            jsonObject.accumulate("titulo", pend.getTitulo());
                            jsonObject.accumulate("descripcion", pend.getDescripcion());
                            jsonGeneral.accumulate("objInsert", jsonObject);
                        } catch(Exception e) {
                            //...
                        }
                    }
                    Utiles.insertarIncidenciasServicio(jsonGeneral, controller, this);
                }
            } else if(unsynched == 0) {
                JSONObject jsonIdsLocalesSynched = controller.getIdsRemotosIncidencias();
                Utiles.verificarIncidenciasNewRemotoServicio(jsonIdsLocalesSynched, controller, this);
            }
        } else {
            List<IncidenciaBean> newListUI = controller.getAllIncidencias();
            actualizarUI(newListUI);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabInc);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initiatePopupWindow();
            }
        });
    }

    public static void actualizarUI(List<IncidenciaBean> incidencias) {
        adapter = new IncidenciasAdapter(incidencias);
        recycler.setAdapter(adapter);
    }

    @Override
    public Void getIncidenciasRemote(List<IncidenciaBean> lstIncidenciasRemote, DBController _controller) {
        //this.lstIncidenciasRemote = lstIncidenciasRemote;
        for (IncidenciaBean pend : lstIncidenciasRemote) {
            Log.d("BUHOO", "a insertar: "+pend);
            controller.insertarIncidencia(pend);
        }
        lstIncidenciasRemote = controller.getAllIncidencias();
        this.actualizarUI(lstIncidenciasRemote);
        return null;
    }

    @Override
    public Void getIncidenciasInsertadas(List<IncidenciaBean> lstIncidenciasRemote, DBController _controller) {
        for (IncidenciaBean pend : lstIncidenciasRemote) {
            controller.updateSyncStatus(pend.getIdIncidenciaLocal());
        }
        List<IncidenciaBean> newListUI = controller.getAllIncidencias();
        this.actualizarUI(newListUI);
        return null;
    }

    public static boolean checkInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE );
        NetworkInfo activeNetInfoMobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo activeNetInfoWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isConnectedMobile = activeNetInfoMobile != null && activeNetInfoMobile.isConnectedOrConnecting();
        boolean isConnectedWifi   = activeNetInfoWifi   != null && activeNetInfoWifi.isConnectedOrConnecting();
        return isConnectedMobile || isConnectedWifi;
    }

    public static class ConeccionCheck extends BroadcastReceiver implements IncidenciasInterface {

        List<IncidenciaBean> lstIncidenciasRemote;

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BUHOO", " CONECTADO A LA RED");
            boolean conectado = checkInternet(context);
            if (conectado) {
                DBController controller = new DBController(context);
                int unsynched = controller.dbSyncCount();
                if(unsynched > 0) {
                    List<IncidenciaBean> pendientes = controller.getUnsynchedIncidencias();
                    if(pendientes.size() > 0) {
                        JSONObject jsonGeneral = new JSONObject();
                        for (IncidenciaBean pend : pendientes) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.accumulate("id_incidencia_local", pend.getIdIncidenciaLocal());
                                jsonObject.accumulate("titulo", pend.getTitulo());
                                jsonObject.accumulate("descripcion", pend.getDescripcion());
                                jsonGeneral.accumulate("objInsert", jsonObject);
                            } catch(Exception e) {
                                //...
                            }
                        }
                        Utiles.insertarIncidenciasServicio(jsonGeneral, controller, this);
                    }
                } else if(unsynched == 0) {
                    JSONObject jsonIdsLocalesSynched = controller.getIdsRemotosIncidencias();
                    Utiles.verificarIncidenciasNewRemotoServicio(jsonIdsLocalesSynched, controller, this);
                }
            } else {
                Log.d("BUHOO", " DESCONECTADO DE LA RED");
            }
        }

        @Override
        public Void getIncidenciasRemote(List<IncidenciaBean> lstIncidenciasRemote, DBController controller) {
            for (IncidenciaBean pend : lstIncidenciasRemote) {
                controller.insertarIncidencia(pend);
            }
            lstIncidenciasRemote = controller.getAllIncidencias();
            actualizarUI(lstIncidenciasRemote);
            return null;
        }

        @Override
        public Void getIncidenciasInsertadas(List<IncidenciaBean> lstIncidenciasRemote, DBController controller) {
            for (IncidenciaBean pend : lstIncidenciasRemote) {
                controller.updateSyncStatus(pend.getIdIncidenciaLocal());
            }
            List<IncidenciaBean> newListUI = controller.getAllIncidencias();
            actualizarUI(newListUI);
            return null;
        }
    }

    private void initiatePopupWindow() {
        try {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.popup_layout, (ViewGroup) findViewById(R.id.popup_element));
            pw = new PopupWindow(layout, 600, 600, true);
            pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
            Button registrarButton = (Button) layout.findViewById(R.id.btnRegistrar);
            ctx = this;

            View.OnClickListener oclBtnOk = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String titulo_incidencia = ((EditText) layout.findViewById(R.id.titulo_incidencia)).getText().toString();
                    String descripcion_incidencia = ((EditText) layout.findViewById(R.id.descripcion_incidencia)).getText().toString();
                    int newId = controller.insertarIncidencia(new IncidenciaBean(0, 0, titulo_incidencia, descripcion_incidencia,0));
                    boolean conectado = checkInternet(ctx);

                    if(conectado) {
                        JSONObject jsonGeneral = new JSONObject();
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.accumulate("id_incidencia_local", newId);
                            jsonObject.accumulate("titulo", titulo_incidencia);
                            jsonObject.accumulate("descripcion", descripcion_incidencia);
                            jsonGeneral.accumulate("objInsert", jsonObject);

                        } catch(Exception e) {
                            //...
                        }
                        Utiles.insertarIncidenciasServicio(jsonGeneral, controller, (IncidenciasInterface) ctx);
                    } else {
                        List<IncidenciaBean> newListUI = controller.getAllIncidencias();
                        actualizarUI(newListUI);
                    }
                    pw.dismiss();
                }
            };
            registrarButton.setOnClickListener(oclBtnOk);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}