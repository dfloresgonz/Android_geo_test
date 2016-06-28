package facilito.codigo.app.dflores.com.myapplicationcf;

import android.content.Context;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import Beans.MapaVariables;
import Beans.Usuario;
import Beans.Utiles;

public class MainActivity extends AppCompatActivity {

    String server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        server = getResources().getString(R.string.ip_server);
        MapaVariables.ipServer = server;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        SharedPreferences pref = getSharedPreferences("BUHOO_APP", MODE_PRIVATE);
        int idUsuario = pref.getInt("ID_USUARIO", 0);
        Log.d("VER", "USUARIO: " + idUsuario);
        if(idUsuario != 0) {
            Intent nextPage = new Intent(MainActivity.this, Incidencia.class);
            startActivity(nextPage);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void clickbutton(View v) {
        String txtUsuario = ((EditText)findViewById(R.id.txtuser)).getText().toString();
        String txtPwd     = ((EditText)findViewById(R.id.txtpass)).getText().toString();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate("user", "Diego");
            jsonObject.accumulate("clave", "Peru");
        } catch(Exception e) {
            //...
        }
        String servicio = "http://"+server+"/buhoo/servicio/loginMovil?user="+txtUsuario+"&clave="+txtPwd;
        new LoginServicio().execute(servicio);
    }

    private class LoginServicio extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            Utiles utiles = new Utiles();
            return utiles.readJSONFeed(urls[0]);
        }

        protected void onPostExecute(String result) {
            try {
                JSONObject mainResponseObject = new JSONObject(result);
                TextView tv = (TextView) findViewById(R.id.rpta);
                SharedPreferences pref = getApplicationContext().getSharedPreferences("BUHOO_APP", 0); // 0 – for private mode
                SharedPreferences.Editor editor = pref.edit();
                try {
                    String msj   = mainResponseObject.getString("msj");
                    String error = mainResponseObject.getString("error");
                    Log.d("CREATION", "MSJ: " + msj + "   error: " + error);
                    if("0".equals(error)) {
                        Intent nextPage = new Intent(MainActivity.this, Incidencia.class);
                        Integer idUsuario = mainResponseObject.getInt("id_usuario");
                        String nombreUsuario = mainResponseObject.getString("nombrecompleto");
                        String correo = mainResponseObject.getString("correo");
                        String foto = mainResponseObject.getString("foto_persona");
                        Log.d("BUHOO", "FOTOOO:::::: "+foto);
                        /*Usuario usuario = new Usuario(idUsuario, nombreUsuario, correo, foto);
                        nextPage.putExtra("BeanUsuario", usuario);*/
                        editor.putString("NOMBRE_USUARIO", nombreUsuario);
                        editor.putInt("ID_USUARIO", idUsuario);
                        editor.putString("CORREO", correo);
                        editor.putString("FOTO", foto);
                        editor.commit();
                        startActivity(nextPage);
                    } else {
                        tv.setText(msj);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    StringWriter errors = new StringWriter();
                    e.printStackTrace(new PrintWriter(errors));
                    Log.d("CREATION", "obteniendo data: "+errors.toString());
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