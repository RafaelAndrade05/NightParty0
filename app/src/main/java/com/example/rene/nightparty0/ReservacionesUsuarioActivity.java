package com.example.rene.nightparty0;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.rene.nightparty0.Adaptadores.AdapterReservacionesUsuarios;
import com.example.rene.nightparty0.Objetos.Reservacion;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;

public class ReservacionesUsuarioActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ArrayList<Reservacion> listReservaciones;
    private ProgressBar progressBarInicio;
    private TextView tvNoReservacion;
    private ImageView imagenNoReservacion;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    int cont = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservaciones_usuario);

        if(user == null){
            finish();
        }

        listReservaciones = new ArrayList<>();
        recyclerView=(RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        progressBarInicio = (ProgressBar)findViewById(R.id.progress_barInicio);
        tvNoReservacion = (TextView)findViewById(R.id.tvSinReservacion);
        imagenNoReservacion = (ImageView)findViewById(R.id.imagenSinReservacion);

        llenarRecyclerView();
    }

    private void llenarRecyclerView() {
        if(user == null){
            return;
        }
        progressBarInicio.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
        final AdapterReservacionesUsuarios adapter = new AdapterReservacionesUsuarios(getApplicationContext(),listReservaciones);
        recyclerView.setAdapter(adapter);
        //consulta a las reservaciones de el usuario logeado
        CollectionReference collection = db.collection(Reservacion.COLLECTION_RESERVACIONES);

        collection.whereEqualTo(Reservacion.CAMPO_USERID,user.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(final QuerySnapshot queryDocumentSnapshots) {
                listReservaciones.removeAll(listReservaciones);
                for(DocumentSnapshot document : queryDocumentSnapshots){
                    if(document.exists()){
                        Reservacion reservacion = new Reservacion();
                        reservacion = document.toObject(Reservacion.class);
                        reservacion.setId(document.getId());
                        Log.i("RESERVACIONUSUARIO",reservacion.getNombreLugar());
                        listReservaciones.add(reservacion);
                        cont++;
                    }
                }
                Collections.sort(listReservaciones);
                adapter.notifyDataSetChanged();
                progressBarInicio.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                if(cont==0){
                    tvNoReservacion.setVisibility(View.VISIBLE);
                    imagenNoReservacion.setVisibility(View.VISIBLE);
                }
            }
        });

        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),ReservacionDetalleUsuarioActivity.class);
                TextView tvIdLugar ,tvidReservacion,tvNombreNegocio;

                tvIdLugar = (TextView)view.findViewById(R.id.tvIdLugar);
                tvidReservacion = (TextView)view.findViewById(R.id.tvIdReservacion);
                tvNombreNegocio = (TextView)view.findViewById(R.id.tvNombre2);

                i.putExtra("idLugar",tvIdLugar.getText().toString());
                i.putExtra("idReservacion",tvidReservacion.getText().toString());
                i.putExtra("nombreNegocio",tvNombreNegocio.getText().toString());

                startActivity(i);
            }
        });

    }

}
