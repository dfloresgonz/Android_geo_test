package facilito.codigo.app.dflores.com.myapplicationcf;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import Adaptadores.IncidenciasAdapter;
import Beans.DBController;
import Beans.IncidenciaBean;
import Beans.Utiles;
import Interfaces.IncidenciasInterface;

/**
 * Created by diego on 25/06/2016.
 */
public class Incidencia extends AppCompatActivity implements IncidenciasInterface {

    private static RecyclerView recycler;
    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;
    List<IncidenciaBean> lstIncidenciasRemote = new ArrayList<IncidenciaBean>();
    DBController controller = new DBController(this);

    private String IMAGE_DIRECTORY_NAME = null;

    static Context ctx;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.incidencias);

        ctx = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarInc);
        setSupportActionBar(toolbar);

        IMAGE_DIRECTORY_NAME = getResources().getString(R.string.carpeta_archivos_subida);

        recycler = (RecyclerView) findViewById(R.id.lstIncidencias);
        recycler.setHasFixedSize(true);

        // Usar un administrador para LinearLayout
        lManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(lManager);

        adapter = new IncidenciasAdapter(lstIncidenciasRemote, ctx);
        recycler.setAdapter(adapter);
        boolean conectado = Utiles.checkInternet(this);
        if (conectado) {
            int unsynched = controller.dbSyncCount();Log.d("BUHOO", "unsynched: "+unsynched);
            if (unsynched > 0) {
                List<IncidenciaBean> pendientes = controller.getUnsynchedIncidencias();
                if (pendientes.size() > 0) {
                    JSONObject jsonGeneral = new JSONObject();
                    for (IncidenciaBean pend : pendientes) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.accumulate("id_incidencia_local", pend.getIdIncidenciaLocal());
                            jsonObject.accumulate("titulo", pend.getTitulo());
                            jsonObject.accumulate("descripcion", pend.getDescripcion());
                            jsonGeneral.accumulate("objInsert", jsonObject);
                        } catch (Exception e) {
                            //...
                        }
                    }
                    Utiles.insertarIncidenciasServicio(jsonGeneral, controller, this, null);
                }
            } else if (unsynched == 0) {
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
                goToPnatallaRegistro();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public static void actualizarUI(List<IncidenciaBean> incidencias) {
        adapter = new IncidenciasAdapter(incidencias, ctx);
        if (recycler != null) {
            recycler.setAdapter(adapter);
        }
    }

    public static void getIncidenciasRemoteAux(List<IncidenciaBean> lstIncidenciasRemote, DBController _controller) {
        for (IncidenciaBean pend : lstIncidenciasRemote) {
            _controller.insertarIncidencia(pend);
        }
        lstIncidenciasRemote = _controller.getAllIncidencias();
        actualizarUI(lstIncidenciasRemote);
        return;
    }

    public static void getIncidenciasInsertadasAux(List<IncidenciaBean> lstIncidenciasRemote, DBController _controller) {
        for (IncidenciaBean pend : lstIncidenciasRemote) {
            _controller.updateSyncStatus(pend.getIdIncidenciaLocal(), pend.getIdIncidenciaRemota());
        }
        List<IncidenciaBean> newListUI = _controller.getAllIncidencias();
        actualizarUI(newListUI);
        return;
    }

    @Override
    public Void getIncidenciasRemote(List<IncidenciaBean> lstIncidenciasRemote, DBController _controller) {
        getIncidenciasRemoteAux(lstIncidenciasRemote, controller);
        return null;
    }

    @Override
    public Void getIncidenciasInsertadas(List<IncidenciaBean> lstIncidenciasRemote, DBController _controller) {
        getIncidenciasInsertadasAux(lstIncidenciasRemote, controller);
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public static class ConeccionCheck extends BroadcastReceiver implements IncidenciasInterface {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BUHOO", " CONECTADO A LA RED :D");
            boolean conectado = Utiles.checkInternet(context);
            if (conectado) {
                DBController controller = new DBController(context);
                int unsynched = controller.dbSyncCount();
                if (unsynched > 0) {
                    List<IncidenciaBean> pendientes = controller.getUnsynchedIncidencias();
                    if (pendientes.size() > 0) {
                        JSONObject jsonGeneral = new JSONObject();
                        for (IncidenciaBean pend : pendientes) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.accumulate("id_incidencia_local", pend.getIdIncidenciaLocal());
                                jsonObject.accumulate("titulo", pend.getTitulo());
                                jsonObject.accumulate("descripcion", pend.getDescripcion());
                                jsonGeneral.accumulate("objInsert", jsonObject);
                            } catch (Exception e) {
                                //...
                            }
                        }
                        Utiles.insertarIncidenciasServicio(jsonGeneral, controller, this, null);
                    }
                } else if (unsynched == 0) {
                    JSONObject jsonIdsLocalesSynched = controller.getIdsRemotosIncidencias();
                    Utiles.verificarIncidenciasNewRemotoServicio(jsonIdsLocalesSynched, controller, this);
                }
            } else {
                Log.d("BUHOO", " DESCONECTADO DE LA RED :(");
            }
        }

        @Override
        public Void getIncidenciasRemote(List<IncidenciaBean> lstIncidenciasRemote, DBController _controller) {
            getIncidenciasRemoteAux(lstIncidenciasRemote, _controller);
            return null;
        }

        @Override
        public Void getIncidenciasInsertadas(List<IncidenciaBean> lstIncidenciasRemote, DBController _controller) {
            getIncidenciasInsertadasAux(lstIncidenciasRemote, _controller);
            return null;
        }
    }

    private void goToPnatallaRegistro() {
        Intent nextPage = new Intent(Incidencia.this, NewIncidencia.class);
        startActivity(nextPage);
    }
}