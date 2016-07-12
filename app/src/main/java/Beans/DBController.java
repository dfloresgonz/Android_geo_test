package Beans;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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

    public int insertarIncidencia(IncidenciaBean incidencia, int esRemoto) {
        String IMAGE_DIRECTORY_NAME = ctx.getString(R.string.carpeta_archivos_subida);
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        int newId = this.getNextId();
        String idIncidencia = UUID.randomUUID().toString();
        Utiles.log("NEWid::: "+newId+"   idIncidencia: "+idIncidencia);

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

                valuesImg.put("idImagen", imgBean.getIdImagen());
                //Grabar imagen localmente
                if(esRemoto == 1) {
                    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), IMAGE_DIRECTORY_NAME);
                    // Create the storage directory if it does not exist
                    if (!mediaStorageDir.exists()) {
                        if (!mediaStorageDir.mkdirs()) {
                            Log.d("BUHOO", " insertarIncidenciaOops! Failed create " + IMAGE_DIRECTORY_NAME + " directory");
                            return 0;
                        }
                    }
                    String timeStamp = new SimpleDateFormat("dd_MM_yyyy_HHmmss", Locale.getDefault()).format(new Date());
                    File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp+"_"+imgBean.getIdImagen()+"_"+ ".jpg");
                    String rutaImg = mediaFile.getPath();

                    byte[] imageAsBytes = Base64.decode(imgBean.getRutaImagen().getBytes(), Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
                    FileOutputStream out = null;
                    try {
                        out = new FileOutputStream(rutaImg);
                        valuesImg.put("rutaImagen", rutaImg);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                        // PNG is a lossless format, the compression factor (100) is ignored
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    valuesImg.put("rutaImagen", imgBean.getRutaImagen());
                }
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
                id = (id == 0) ? 1 : id;
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
        Log.d("BUHOO","SQL UPDATE: "+updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }

    public void updateSyncStatus_PendienteSync(int idIncidenciaLocal){
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "UPDATE incidencia  " +
                             "   SET synched = 2 "+
                             " WHERE id_incidencia_local  = "+idIncidenciaLocal;
        Log.d("BUHOO","SQL UPDATE PEND:: "+updateQuery);
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
