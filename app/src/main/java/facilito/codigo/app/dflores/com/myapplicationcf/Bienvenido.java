package facilito.codigo.app.dflores.com.myapplicationcf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import Beans.Usuario;

public class Bienvenido extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenido);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        SharedPreferences pref = getSharedPreferences("BUHOO_APP", MODE_PRIVATE);

        TextView tv = (TextView) findViewById(R.id.textView);
        /*Intent i = getIntent();
        Usuario usuario = (Usuario) i.getSerializableExtra("BeanUsuario");*/
        Usuario usuario = new Usuario(pref.getInt("ID_USUARIO", 0), pref.getString("NOMBRE_USUARIO", null));
        tv.setText("Hola, " + usuario.getNombreCompleto() + " bienvenido al sistema - " + usuario.getIdUsuario());
    }

    public void cerrarSession(View v) {
        SharedPreferences pref = getSharedPreferences("BUHOO_APP", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
        Intent goToLogin = new Intent(Bienvenido.this, MainActivity.class);
        startActivity(goToLogin);
    }

    @Override
    public void onBackPressed() {
        //Include the code here
        return;
    }
}