package facilito.codigo.app.dflores.com.myapplicationcf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import Beans.BeanCombo;
import Beans.Usuario;
import Beans.Utiles;

public class ByPass extends AppCompatActivity {

    ArrayList<String> comusList;
    ArrayList<BeanCombo> comunidades;
    String server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_by_pass);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = getApplicationContext().getSharedPreferences("BUHOO_APP", 0); // 0 – for private mode
                SharedPreferences.Editor editor = pref.edit();
                int idComu = pref.getInt("ID_COMUNIDAD", 0);
                if(idComu != 0) {
                    Intent goToMap = new Intent(ByPass.this, Mapa.class);
                    startActivity(goToMap);
                } else {
                    Snackbar.make(view, "Selecciona una comunidad", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });

        SharedPreferences pref = getSharedPreferences("BUHOO_APP", MODE_PRIVATE);
        Usuario usuario = new Usuario(pref.getInt("ID_USUARIO", 0), pref.getString("NOMBRE_USUARIO", null));

        server = getResources().getString(R.string.ip_server);
        String servicio = "http://"+server+"/buhoo/login/getRolesPersona_Service?id_persona="+usuario.getIdUsuario();
        new llamarServicio().execute(servicio);
    }

    public void cerrarSession(View v) {
        SharedPreferences pref = getSharedPreferences("BUHOO_APP", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
        Intent goToLogin = new Intent(ByPass.this, MainActivity.class);
        startActivity(goToLogin);
    }

    private class llamarServicio extends AsyncTask<String, Void, String> {
        Utiles utiles = new Utiles();
        protected String doInBackground(String... urls) {
            return utiles.readJSONFeed(urls[0]);
        }

        protected void onPostExecute(String result) {
            try {
                Log.d("BUHOO", "result:::::: " + result);
                JSONObject mainResponseObject = new JSONObject(result);
                try {
                    String error = mainResponseObject.getString("error");
                    Log.d("CREATION", " ---- error: " + error);
                    if("0".equals(error)) {
                        JSONArray comus = mainResponseObject.getJSONArray("comunidades"); //new JSONArray(mainResponseObject.getString("comunidades"));
                        Log.d("CREATION", " ---- error = 0   polyObj: "+comus.length());
                        comunidades = new ArrayList<BeanCombo>();
                        comusList = new ArrayList<String>();
                        for (int i = 0; i < comus.length(); ++i) {
                            JSONObject comunidad = comus.getJSONObject(i);
                            String desc_comu = comunidad.getString("desc_comunidad");
                            String id_comu = comunidad.getString("_id_comunidad");
                            BeanCombo combo = new BeanCombo(Integer.parseInt(id_comu),desc_comu);
                            comunidades.add(combo);
                            comusList.add(desc_comu);
                        }
                    } else {
                        Log.d("CREATION", " ---- error inesperdo: " );
                    }
                    Spinner mySpinner = (Spinner) findViewById(R.id.cmbComus);
                    mySpinner.setAdapter(new ArrayAdapter<String>(ByPass.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            comusList));
                    mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> arg0,
                                                   View arg1, int position, long arg3) {
                            // Set the text followed by the position
                            TextView txtResu = (TextView) findViewById(R.id.resu);
                            SharedPreferences pref = getApplicationContext().getSharedPreferences("BUHOO_APP", 0); // 0 – for private mode
                            SharedPreferences.Editor editor = pref.edit();
                            txtResu.setText("Id : "+comunidades.get(position).getIdCombo()+"   -  desc: "+comunidades.get(position).getDescCombo());
                            editor.putInt("ID_COMUNIDAD", comunidades.get(position).getIdCombo()); // Storing integer
                            editor.commit();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {
                            SharedPreferences pref = getApplicationContext().getSharedPreferences("BUHOO_APP", 0); // 0 – for private mode
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putInt("ID_COMUNIDAD", 0);
                            editor.commit();
                        }
                    });
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

    @Override
    public void onBackPressed() {
        //Include the code here
        return;
    }
}
