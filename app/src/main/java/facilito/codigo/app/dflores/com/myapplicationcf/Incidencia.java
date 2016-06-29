package facilito.codigo.app.dflores.com.myapplicationcf;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
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
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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

    EditText txtDescri;

    private final int REQ_CODE_SPEECH_INPUT = 100;

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
                Log.d("BUHOO", "jsonIdsLocalesSynched: "+jsonIdsLocalesSynched);
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
        if(adapter == null) {
            Log.d("BUHOO", "ADAPTER ES NULO");
        } else {
            try {
                if(recycler != null) {
                    recycler.setAdapter(adapter);
                } else {
                    Log.d("BUHOO", "recycler ES NULO");
                }
            } catch(Exception e) {
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                Log.d("BUHOO", " ERROR actualizarUI " + errors.toString());
            }
        }
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
            controller.updateSyncStatus(pend.getIdIncidenciaLocal(), pend.getIdIncidenciaRemota());
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
                int unsynched = controller.dbSyncCount();Log.d("BUHOO", "unsynched: "+unsynched);
                if(unsynched > 0) {
                    List<IncidenciaBean> pendientes = controller.getUnsynchedIncidencias();
                    Log.d("BUHOO", "pendientes.size(): "+pendientes.size());
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
            List<IncidenciaBean> lstIncidenciasRemoteLocal = controller.getAllIncidencias();
            actualizarUI(lstIncidenciasRemoteLocal);
            return null;
        }

        @Override
        public Void getIncidenciasInsertadas(List<IncidenciaBean> lstIncidenciasRemote, DBController controller) {
            for (IncidenciaBean pend : lstIncidenciasRemote) {
                controller.updateSyncStatus(pend.getIdIncidenciaLocal(), pend.getIdIncidenciaRemota());
            }
            List<IncidenciaBean> newListUI = controller.getAllIncidencias();

            for (IncidenciaBean pend : newListUI) {
                Log.d("BUHOO", "newListUI: "+pend.toString());
            }

            actualizarUI(newListUI);
            return null;
        }
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es_ES");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                        getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.d("BUHOO", "resultadooooooo: "+result.get(0));

                    String texto = result.get(0)+"";
                    txtDescri.setText(texto, TextView.BufferType.EDITABLE);
                }
                break;
            }
        }
    }

    private void initiatePopupWindow() {
        try {
            LayoutInflater layoutInflater = LayoutInflater.from(Incidencia.this);
            View promptView = layoutInflater.inflate(R.layout.popup_layout, null);

            ctx = this;
            final EditText txtTitulo = (EditText) promptView.findViewById(R.id.titulo_incidencia);
            txtDescri = (EditText) promptView.findViewById(R.id.descripcion_incidencia);
            ImageButton btnSpeak = (ImageButton) promptView.findViewById(R.id.btnSpeak);

            btnSpeak.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    promptSpeechInput();
                }
            });



            final AlertDialog dialog = new AlertDialog.Builder(ctx)
                    .setView(promptView)
                    .setTitle("Registro de Incidencias")
                    .setPositiveButton("REGISTRAR", null)
                    .setNegativeButton("CANCELAR", null)
                    .create();

            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {
                    Button b = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String titulo_incidencia      = txtTitulo.getText().toString().trim();
                            String descripcion_incidencia = txtDescri.getText().toString().trim();

                            if(titulo_incidencia.trim().length() == 0 || descripcion_incidencia.trim().length() == 0) {
                                Toast.makeText(ctx, "Escriba el título y/o la descripción", Toast.LENGTH_LONG).show();
                                return;
                            }
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
                            Toast.makeText(ctx, "Se registró la incidencia", Toast.LENGTH_LONG).show();
                            dialog.cancel();
                        }
                    });
                }
            });
            dialog.show();


            /*alertDialogBuilder.setCancelable(false)
                    .setPositiveButton("REGISTRAR", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            String titulo_incidencia      = txtTitulo.getText().toString();
                            String descripcion_incidencia = txtDescri.getText().toString();

                            if(titulo_incidencia.trim().length() == 0 || descripcion_incidencia.trim().length() == 0) {
                                Toast.makeText(ctx, "Escriba el título y/o descripción", Toast.LENGTH_LONG).show();
                                return;
                            }
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
                            Toast.makeText(ctx, "Se registró la incidencia", Toast.LENGTH_LONG).show();
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton("CANCELAR",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            // create an alert dialog
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();*/


            /*LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.popup_layout, (ViewGroup) findViewById(R.id.popup_element));
            pw = new PopupWindow(layout, 700, 700, true);
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
            };*/
            //registrarButton.setOnClickListener(oclBtnOk);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}