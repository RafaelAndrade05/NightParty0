package com.example.rene.nightparty0.FragmentsDetalles;

import android.app.Dialog;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rene.nightparty0.Adaptadores.AdapterImagenPromociones;
import com.example.rene.nightparty0.Adaptadores.AdapterImagenPromocionesDetalle2;
import com.example.rene.nightparty0.Objetos.Imagen;
import com.example.rene.nightparty0.Objetos.Promocion;
import com.example.rene.nightparty0.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;

public class FragmentPromociones extends android.support.v4.app.Fragment {

    View view;
    Bundle args;
    Promocion objPromocion;

    private RecyclerView recyclerViewPromociones;
    private TextView tvDescripcion,tvDescripcionLectura;
    private ArrayList<Uri> listUri;

    public FragmentPromociones() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.promociones_fragment,container,false);

        recyclerViewPromociones = (RecyclerView) view.findViewById(R.id.recyclerViewDetalle_fragment_promociones);
        tvDescripcion = (TextView) view.findViewById(R.id.fragment_promociones_descripcion);
        tvDescripcionLectura = (TextView) view.findViewById(R.id.txtDescripcionDetalle2);
        llenarInformacion();

        return view;
    }


    private void llenarInformacion() {
        Log.i("FRAGMENTPROMOCION","LLENANDO FRAGMENT PROMOCIONES");

        args = getArguments();
        objPromocion = (Promocion) args.getSerializable("objPromocion");
        listUri = new ArrayList<>();
        listUri = args.getParcelableArrayList("listUri");


        if(!objPromocion.getDescripcion().equals("")) {
            tvDescripcion.setText(objPromocion.getDescripcion());
        }else{
            tvDescripcionLectura.setVisibility(View.GONE);
        }

        for(Uri uri : listUri) {
            Log.i("FRAGMENTPROMOCIONES", "uri => " + uri);
        }

        recyclerViewPromociones.setHasFixedSize(true);

        recyclerViewPromociones.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        AdapterImagenPromocionesDetalle2 adapter = new AdapterImagenPromocionesDetalle2(getContext(),listUri);
        recyclerViewPromociones.setAdapter(adapter);


    }


}
