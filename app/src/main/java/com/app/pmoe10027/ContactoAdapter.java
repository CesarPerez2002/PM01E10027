package com.app.pmoe10027;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.List;

public class ContactoAdapter extends ArrayAdapter<Contactos> {

    private final Context context;
    private final List<Contactos> listaContactos;
    private final OnContactoListener listener;

    public interface OnContactoListener {
        void onEditar(Contactos contacto);
        void onEliminar(Contactos contacto);
    }

    public ContactoAdapter(Context context, List<Contactos> listaContactos, OnContactoListener listener) {
        super(context, R.layout.item_contacto, listaContactos);
        this.context = context;
        this.listaContactos = listaContactos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_contacto, parent, false);
        }

        Contactos contacto = listaContactos.get(position);

        ImageView imgContactoItem = convertView.findViewById(R.id.imgContactoItem);
        TextView tvNombreItem = convertView.findViewById(R.id.tvNombreItem);
        TextView tvTelefonoPaisItem = convertView.findViewById(R.id.tvTelefonoPaisItem);
        TextView tvNotaItem = convertView.findViewById(R.id.tvNotaItem);
        Button btnEditarItem = convertView.findViewById(R.id.btnEditarItem);
        Button btnEliminarItem = convertView.findViewById(R.id.btnEliminarItem);

        tvNombreItem.setText(contacto.getId() + ". " + contacto.getNombre());
        tvTelefonoPaisItem.setText(contacto.getPais() + " | " + contacto.getTelefono());
        tvNotaItem.setText("Nota: " + contacto.getNota());

        // Cargar imagen guardada desde la ruta interna
        if (contacto.getImagen() != null && !contacto.getImagen().isEmpty()) {
            File imgFile = new File(contacto.getImagen());
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imgContactoItem.setImageBitmap(myBitmap);
            } else {
                imgContactoItem.setImageResource(android.R.drawable.ic_menu_camera);
            }
        } else {
            imgContactoItem.setImageResource(android.R.drawable.ic_menu_camera);
        }

        btnEditarItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditar(contacto);
            }
        });

        btnEliminarItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEliminar(contacto);
            }
        });

        return convertView;
    }
}
