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

import facilito.codigo.app.dflores.com.myapplicationcf.R;

/**
 * Created by diego on 25/06/2016.
 */
public class DBController extends SQLiteOpenHelper {

    public DBController(Context applicationcontext) {
        super(applicationcontext, "androidsqlite.db", null, 1);
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
                lstIncidencias.add(new IncidenciaBean(cursor.getInt(0),
                                                      cursor.getInt(1),
                                                      cursor.getString(2),
                                                      cursor.getString(3),
                                                      cursor.getInt(4)));
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
                             "       id_incidencia_remoto = " +idIncidenciaRemota+
                             " WHERE id_incidencia_local  = "+idIncidenciaLocal;
        //Log.d("query",updateQuery);
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
}
