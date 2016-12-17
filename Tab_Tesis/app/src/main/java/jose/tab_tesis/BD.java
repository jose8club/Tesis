package jose.tab_tesis;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Alicia on 16/12/2016.
 */
public class BD extends SQLiteOpenHelper {

    public BD(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, "tesis", factory, 1);
    }

    /**
     * Se crea la base de datos
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table obra(idobra text primary key not null," +
                "nombre text," +
                "autor text," +
                "fecha_creacion text," +
                "resumen text," +
                "tipo_obra text," +
                "estilo_obra text," +
                "tecnica_obra text," +
                "fecha_ingreso text," +
                "nacionalidad text," +
                "dimensiones text," +
                "peso text," +
                "imagen text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Funcion de guardar una obra de arte en la tabla obra
     * @param idobra
     * @param nombre
     * @param autor
     * @param fechauno
     * @param resumen
     * @param tipo
     * @param estilo
     * @param tecnica
     * @param fechados
     * @param nacionalidad
     * @param dimensiones
     * @param peso
     * @param imagen
     * @return
     */
    public String guardar(String idobra, String nombre, String autor, String fechauno, String resumen,
                          String tipo, String estilo, String tecnica, String fechados, String nacionalidad,
                          String dimensiones, String peso, String imagen){
        String mensaje="";
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contenedor = new ContentValues();
        contenedor.put("idobra",idobra);
        contenedor.put("nombre", nombre);
        contenedor.put("autor", autor);
        contenedor.put("fecha_creacion", fechauno);
        contenedor.put("resumen", resumen);
        contenedor.put("tipo_obra",tipo);
        contenedor.put("estilo_obra", estilo);
        contenedor.put("tecnica_obra", tecnica);
        contenedor.put("fecha_ingreso", fechados);
        contenedor.put("nacionalidad", nacionalidad);
        contenedor.put("dimensiones", dimensiones);
        contenedor.put("peso", peso);
        contenedor.put("imagen", imagen);
        try {
            database.insertOrThrow("obra",null,contenedor);
            mensaje="Obra Ingresada Correctamente";
        }catch (SQLException e){
            mensaje="Obra no pudo ser ingresada";
        }
        database.close();
        return mensaje;
    }

    /**
     * Busqueda de cada obra de arte deacuerdo a su idobra
     * @param idobra
     * @return
     */
    public String[] buscar(String idobra) {
        String[] datos= new String[14];
        SQLiteDatabase database = this.getWritableDatabase();
        String q = "SELECT * FROM obra WHERE idobra ='"+idobra+"'";
        Cursor registros = database.rawQuery(q, null);
        if(registros.moveToFirst()){
            for(int i = 0 ; i<13;i++){
                datos[i]= registros.getString(i);

            }
            datos[13]= "Encontrado";
        }else{

            datos[13]= "No se encontro a "+idobra;
        }
        database.close();
        return datos;
    }
}
