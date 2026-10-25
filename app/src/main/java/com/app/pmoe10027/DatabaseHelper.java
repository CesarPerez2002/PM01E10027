package com.app.pmoe10027;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "contactos.db";
    private static final int DATABASE_VERSION = 1;

    // Nombre de la tabla y columnas
    public static final String TABLA_CONTACTOS = "contactos";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PAIS = "pais";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_TELEFONO = "telefono";
    public static final String COLUMN_NOTA = "nota";
    public static final String COLUMN_IMAGEN = "imagen";

    // Sentencia para crear la tabla
    private static final String CREATE_TABLE_CONTACTOS = "CREATE TABLE " + TABLA_CONTACTOS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PAIS + " TEXT NOT NULL, " +
            COLUMN_NOMBRE + " TEXT NOT NULL, " +
            COLUMN_TELEFONO + " TEXT NOT NULL, " +
            COLUMN_NOTA + " TEXT NOT NULL, " +
            COLUMN_IMAGEN + " TEXT" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CONTACTOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA_CONTACTOS);
        onCreate(db);
    }

    // guardadr contacto
    public long insertarContacto(Contactos contacto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PAIS, contacto.getPais());
        values.put(COLUMN_NOMBRE, contacto.getNombre());
        values.put(COLUMN_TELEFONO, contacto.getTelefono());
        values.put(COLUMN_NOTA, contacto.getNota());
        values.put(COLUMN_IMAGEN, contacto.getImagen());

        long id = db.insert(TABLA_CONTACTOS, null, values);
        db.close();
        return id;
    }

    // 2. obtener contactos de base de datos
    public List<Contactos> obtenerTodosContactos() {
        List<Contactos> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLA_CONTACTOS + " ORDER BY " + COLUMN_ID + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String pais = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAIS));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE));
                String telefono = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TELEFONO));
                String nota = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTA));
                String imagen = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGEN));

                Contactos contacto = new Contactos(id, pais, nombre, telefono, nota, imagen);
                lista.add(contacto);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return lista;
    }

    // 3. buscar id
    public Contactos obtenerContactoPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Contactos contacto = null;

        Cursor cursor = db.query(
                TABLA_CONTACTOS,
                null,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String pais = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PAIS));
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE));
            String telefono = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TELEFONO));
            String nota = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTA));
            String imagen = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGEN));

            contacto = new Contactos(id, pais, nombre, telefono, nota, imagen);
            cursor.close();
        }

        db.close();
        return contacto;
    }

    // actualiazar contactom
    public int actualizarContacto(Contactos contacto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PAIS, contacto.getPais());
        values.put(COLUMN_NOMBRE, contacto.getNombre());
        values.put(COLUMN_TELEFONO, contacto.getTelefono());
        values.put(COLUMN_NOTA, contacto.getNota());
        values.put(COLUMN_IMAGEN, contacto.getImagen());

        int filasAfectadas = db.update(
                TABLA_CONTACTOS,
                values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(contacto.getId())}
        );

        db.close();
        return filasAfectadas;
    }

    // elimianr contacto
    public int eliminarContacto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int filasEliminadas = db.delete(
                TABLA_CONTACTOS,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        db.close();
        return filasEliminadas;
    }
}
