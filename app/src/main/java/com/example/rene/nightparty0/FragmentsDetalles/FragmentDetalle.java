package com.example.rene.nightparty0.FragmentsDetalles;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rene.nightparty0.ChatActivityUsuario;
import com.example.rene.nightparty0.LoginActivity;
import com.example.rene.nightparty0.Objetos.Lugar;
import com.example.rene.nightparty0.R;
import com.example.rene.nightparty0.ReservacionActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FragmentDetalle extends android.support.v4.app.Fragment {

    View view;
    Lugar objLugar;
    Bundle argumentos;

    public FragmentDetalle() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {

            view = inflater.inflate(R.layout.detalles_fragment,container,false);
            Button btnReservar = (Button)view.findViewById(R.id.button_reservar);
            Button btnChat = (Button)view.findViewById(R.id.button_chat);
            TextView tvDescripcion = (TextView)view.findViewById(R.id.detalle_descripcion);
            TextView tvDisponibilidad = (TextView)view.findViewById(R.id.disponibilidad_texto);
            ImageView imageViewDisponibilidad = (ImageView)view.findViewById(R.id.disponibilidad_imagen);

            //obtenemos la informacion
            argumentos = getArguments();
            objLugar = (Lugar) argumentos.getSerializable("objLugar");

            btnReservar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user != null){
                        Intent i = new Intent(getContext(),ReservacionActivity.class);
                        i.putExtra("idLugar",objLugar.getId());
                        i.putExtra("nombreLugar",objLugar.getNombre());
                        startActivity(i);
                    }else{
                        Toast.makeText(getContext(),getString(R.string.detalle_login_obligatorio),Toast.LENGTH_SHORT).show();
                        Intent p = new Intent(getContext(),LoginActivity.class);
                        startActivity(p);
                    }
                }
            });

            btnChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user != null){
                        Intent i = new Intent(getContext(),ChatActivityUsuario.class);
                        i.putExtra("idLugar",objLugar.getId());
                        i.putExtra("nombreLugar",objLugar.getNombre());
                        startActivity(i);
                    }else{
                        Toast.makeText(getContext(),getString(R.string.detalle_login_obligatorio),Toast.LENGTH_SHORT).show();
                        Intent p = new Intent(getContext(),LoginActivity.class);
                        startActivity(p);
                    }
                }
            });

            llenarInformacion(tvDescripcion,tvDisponibilidad,imageViewDisponibilidad);

        } catch (InflateException e) {
            /* map is already there, just return view as it is */
        }

        return view;
    }

    private void llenarInformacion(final TextView tvDescripcion, final TextView tvDisponibilidad, final ImageView imageViewDisponibilidad) {

        Log.i("FRAGMENTS", "LLENANDO FRAGMENT DETALLE");
        tvDescripcion.setText(objLugar.getDescripcion());

        if (objLugar.getDisponibilidad() == 0) { //Lugar baja disponibilidad
            tvDisponibilidad.setText(getString(R.string.detalle_disponiblidad_baja));
            imageViewDisponibilidad.setImageResource(R.mipmap.alta);
            Log.i("ACTUALIZACION", "Entro 0");
        } else if (objLugar.getDisponibilidad() == 1) { //lugar media disponiblidad
            tvDisponibilidad.setText(getString(R.string.detalle_disponiblidad_media));
            imageViewDisponibilidad.setImageResource(R.mipmap.media);
            Log.i("ACTUALIZACION", "Entro 1");
        } else if (objLugar.getDisponibilidad() == 2) { //lugar alta disponiblidad
            tvDisponibilidad.setText(getString(R.string.detalle_disponiblidad_alta));
            imageViewDisponibilidad.setImageResource(R.mipmap.baja);
            Log.i("ACTUALIZACION", "Entro 2");
        }

    }

}
