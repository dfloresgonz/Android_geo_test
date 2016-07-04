package Beans;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import facilito.codigo.app.dflores.com.myapplicationcf.R;

/**
 * Created by diego on 25/06/2016.
 */
public class DBController extends SQLiteOpenHelper {

    private Context ctx;

    public DBController(Context applicationcontext) {
        super(applicationcontext, "androidsqlite.db", null, 1);
        this.ctx = applicationcontext;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String query;
        query = "CREATE TABLE IF NOT EXISTS incidencia ( id_incidencia_local  INTEGER PRIMARY KEY, " +
                                                        "id_incidencia_remota INTEGER, " +
                                                        "titulo               TEXT," +
                                                        "descripcion          TEXT, " +
                                                        "synched              INTEGER)";
        database.execSQL(query);
        //
        query = "CREATE TABLE IF NOT EXISTS incidencia_images ( id_incidencia_local  INTEGER NOT NULL, " +
                                                               "id_incidencia_remota INTEGER, "+
                                                               "correlativo          INTEGER NOT NULL," +
                                                               "rutaImagen           TEXT,"+
                                                               "idImagen             INTEGER," +
                                                               "PRIMARY KEY ( id_incidencia_local, correlativo) )";
        database.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        /*String query;
        query = "DROP TABLE IF EXISTS incidencia";
        database.execSQL(query);
        onCreate(database);*/
    }

    public int insertarIncidencia(IncidenciaBean incidencia) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        int newId = this.getNextId();
        values.put("id_incidencia_local", newId);
        values.put("id_incidencia_remota", incidencia.getIdIncidenciaRemota());
        values.put("titulo", incidencia.getTitulo());
        values.put("descripcion", incidencia.getDescripcion());
        values.put("synched", incidencia.getEstadoSync());
        database.insert("incidencia", null, values);

        if(incidencia.getLstImagenes() != null && incidencia.getLstImagenes().size() > 0) {
            for (IncidenciaImagenBean imgBean : incidencia.getLstImagenes()) {
                ContentValues valuesImg = new ContentValues();
                valuesImg.put("id_incidencia_local", newId);
                valuesImg.put("id_incidencia_remota", incidencia.getIdIncidenciaRemota());
                valuesImg.put("correlativo", imgBean.getCorrelativo());
                valuesImg.put("rutaImagen", imgBean.getRutaImagen());
                valuesImg.put("idImagen", imgBean.getIdImagen());
                database.insert("incidencia_images", null, valuesImg);
            }
        }

        database.close();
        return newId;
    }

    public ArrayList<IncidenciaBean> getAllIncidencias() {
        ArrayList<IncidenciaBean> lstIncidencias = new ArrayList<IncidenciaBean>();
        String sql = "SELECT * FROM incidencia";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                int synched = (cursor.getInt(4) == 1 ? R.drawable.synched : R.drawable.notsynched);
                lstIncidencias.add(new IncidenciaBean(cursor.getInt(0),
                                                      cursor.getInt(1),
                                                      cursor.getString(2),
                                                      cursor.getString(3),
                                                      synched));
            } while (cursor.moveToNext());
        }
        database.close();
        return lstIncidencias;
    }

    public JSONObject getIdsRemotosIncidencias() {
        JSONObject jsonGeneral = new JSONObject();
        ArrayList<IncidenciaBean> lstIncidencias = new ArrayList<IncidenciaBean>();
        String sql = "SELECT id_incidencia_remota FROM incidencia WHERE synched = 1";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.accumulate("id_incidencia_remoto", cursor.getInt(0));
                    jsonGeneral.accumulate("objLocal", jsonObject);
                } catch(Exception e) {
                    //...
                }
            } while (cursor.moveToNext());
        }
        database.close();
        return jsonGeneral;
    }

    public ArrayList<IncidenciaBean> getUnsynchedIncidencias() {
        ArrayList<IncidenciaBean> lstIncidencias = new ArrayList<IncidenciaBean>();
        String sql = "SELECT * FROM incidencia WHERE synched = 0";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                IncidenciaBean inciBean = new IncidenciaBean(cursor.getInt(0),
                                                             cursor.getInt(1),
                                                             cursor.getString(2),
                                                             cursor.getString(3),
                                                             cursor.getInt(4));
                List<IncidenciaImagenBean> lstImagenes = getImagenesByIncidencia(cursor.getInt(0));
                inciBean.setLstImagenes(lstImagenes);
                lstIncidencias.add(inciBean);
            } while (cursor.moveToNext());
        }
        database.close();
        return lstIncidencias;
    }

    public int getNextId() {
        int id = 1;
        SQLiteDatabase database = this.getWritableDatabase();
        String sql = "SELECT MAX(id_incidencia_local)+1 FROM incidencia";
        Cursor cursor = database.rawQuery(sql, null);
        try {
            if(cursor != null && cursor.moveToFirst()) {
                id = cursor.getInt(0);
            }
        } catch(Exception e) {
            Utiles.printearErrores(e, "ERROR getNextId: ");
        }
        return id;
    }

    public int dbSyncCount(){
        int count = 0;
        String selectQuery = "SELECT * FROM incidencia where synched = 0 ";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }

    public String getSyncStatus(){
        String msg = null;
        if(this.dbSyncCount() == 0) {
            msg = "Todo esta sincronizado";
        } else {
            msg = "Se necesita sincronizar";
        }
        return msg;
    }

    public void updateSyncStatus(int idIncidenciaLocal, int idIncidenciaRemota){
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "UPDATE incidencia" +
                             "   SET synched = 1," +
                             "       id_incidencia_remota = " +idIncidenciaRemota+
                             " WHERE id_incidencia_local  = "+idIncidenciaLocal;
        //Log.d("BUHOO","SQL UPDATE: "+updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }

    public void clearDB(){
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "DELETE FROM incidencia";
        //Log.d("query",updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }

    public IncidenciaBean getIncidenciaById(int idIncidenciaLocal) {
        IncidenciaBean incidencia = null;
        SQLiteDatabase database = this.getWritableDatabase();
        String sql = "SELECT * FROM incidencia WHERE id_incidencia_local = "+idIncidenciaLocal;
        Cursor cursor = database.rawQuery(sql, null);
        try {
            if(cursor != null && cursor.moveToFirst()) {
                incidencia = new IncidenciaBean(cursor.getInt(0),
                                                cursor.getInt(1),
                                                cursor.getString(2),
                                                cursor.getString(3),
                                                cursor.getInt(4));
                List<IncidenciaImagenBean> lstImagenes = getImagenesByIncidencia(idIncidenciaLocal);
                incidencia.setLstImagenes(lstImagenes);
            }
        } catch(Exception e) {
            Utiles.printearErrores(e, "ERROR getNextId: ");
        }
        return incidencia;
    }

    public List<IncidenciaImagenBean> getImagenesByIncidencia(int idIncidenciaLocal) {
        List<IncidenciaImagenBean> lstImagenes = new ArrayList<IncidenciaImagenBean>();
        String sql = "SELECT * FROM incidencia_images WHERE id_incidencia_local = "+idIncidenciaLocal;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                lstImagenes.add(new IncidenciaImagenBean(cursor.getInt(0),
                                                         cursor.getInt(1),
                                                         cursor.getInt(2),
                                                         cursor.getString(3),
                                                         cursor.getInt(4)));
            } while (cursor.moveToNext());
        }
        database.close();
        return lstImagenes;
    }

    public Context getCtx() {
        return ctx;
    }

    public void setCtx(Context ctx) {
        this.ctx = ctx;
    }
}
