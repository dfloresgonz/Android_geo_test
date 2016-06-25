package Beans;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        query = "CREATE TABLE IF NOT EXISTS incidencia ( id_incidencia INTEGER PRIMARY KEY, titulo TEXT, descripcion TEXT, synched INTEGER)";
        database.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        /*String query;
        query = "DROP TABLE IF EXISTS incidencia";
        database.execSQL(query);
        onCreate(database);*/
    }

    public void insertarIncidencia(IncidenciaBean incidencia) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_incidencia", this.getNextId());
        values.put("titulo", incidencia.getTitulo());
        values.put("descripcion", incidencia.getDescripcion());
        values.put("synched", incidencia.getEstadoSync());
        database.insert("incidencia", null, values);
        database.close();
    }

    public ArrayList<IncidenciaBean> getAllIncidencias() {
        //ArrayList<HashMap<String, String>> wordList;
        ArrayList<IncidenciaBean> lstIncidencias = new ArrayList<IncidenciaBean>();
        //wordList = new ArrayList<HashMap<String, String>>();
        String sql = "SELECT * FROM incidencia";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                int synched = (cursor.getInt(3) == 1 ? R.drawable.synched : R.drawable.notsynched);
                lstIncidencias.add(new IncidenciaBean(cursor.getInt(0),
                                                      cursor.getString(1),
                                                      cursor.getString(2),
                                                      synched));
            } while (cursor.moveToNext());
        }
        database.close();
        return lstIncidencias;
    }

    public int getNextId() {
        int id = 1;
        SQLiteDatabase database = this.getWritableDatabase();
        String sql = "SELECT MAX(id_incidencia) FROM incidencia";
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
        String selectQuery = "SELECT * FROM incidencia where synched = '0' ";
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

    public void updateSyncStatus(String id){
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "Update incidencia set synched = '1' where id_incidencia="+"'"+ id +"'";
        //Log.d("query",updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }
}
