package facilito.codigo.app.dflores.com.myapplicationcf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import Servicios.ComboService;

public class ByPass extends AppCompatActivity implements GetResponse {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_by_pass);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if( MapaVariables.ipServer == null) {
            MapaVariables.ipServer = getResources().getString(R.string.ip_server);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences pref = getApplicationContext().getSharedPreferences("BUHOO_APP", 0); // 0 – for private mode
                SharedPreferences.Editor editor = pref.edit();
                int idComu = pref.getInt("ID_COMUNIDAD", 0);
                if (idComu != 0) {
                    Intent goToMap = new Intent(ByPass.this, Mapa.class);
                    startActivity(goToMap);
                } else {
                    Snackbar.make(view, "Selecciona una comunidad", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });

        SharedPreferences pref = getSharedPreferences("BUHOO_APP", MODE_PRIVATE);
        //Usuario usuario = new Usuario(pref.getInt("ID_USUARIO", 0), pref.getString("NOMBRE_USUARIO", null));
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id_persona", pref.getInt("ID_USUARIO", 0));
            jsonObject.put("tipoCombo", "rolesPersona");
            Utiles.invocarComboServicio(jsonObject, this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
      Implementa clase GetResponse
     */
    @Override
    public Void getDataCombo(ArrayList<String> comusList, final ArrayList<BeanCombo> comunidadesBeanCombo,
                             String tipoCombo) {
        int combo = 0;
        if("rolesPersona".equals(tipoCombo)) {
            combo = R.id.cmbComus;
        }
        Spinner mySpinner = (Spinner) findViewById(combo);
        mySpinner.setAdapter(new ArrayAdapter<String>(ByPass.this,
                             android.R.layout.simple_spinner_dropdown_item,
                             comusList));
        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0,
                                       View arg1, int position, long arg3) {
                TextView txtResu = (TextView) findViewById(R.id.resu);
                SharedPreferences pref = getApplicationContext().getSharedPreferences("BUHOO_APP", 0); // 0 – for private mode
                SharedPreferences.Editor editor = pref.edit();
                txtResu.setText("Id : " + comunidadesBeanCombo.get(position).getIdCombo() + "   -  desc: " + comunidadesBeanCombo.get(position).getDescCombo());
                editor.putInt("ID_COMUNIDAD", comunidadesBeanCombo.get(position).getIdCombo()); // Storing integer
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
        return null;
    }

    /*
      Implementa clase GetResponse
     */
    @Override
    public Void getDataPublicidad(List<Publicidad> arryDraw) {
        return null;
    }

    @Override
    public Void getDataUsuarioFoto(Drawable imagen) {
        return null;
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
        Intent goToLogin = new Intent(ByPass.this, MainActivity.class);
        startActivity(goToLogin);
    }
}
