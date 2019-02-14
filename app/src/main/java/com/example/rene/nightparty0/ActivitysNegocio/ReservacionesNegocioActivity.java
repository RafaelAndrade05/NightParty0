package com.example.rene.nightparty0.ActivitysNegocio;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rene.nightparty0.Adaptadores.AdapterReservacionesNegocios;
import com.example.rene.nightparty0.Objetos.Lugar;
import com.example.rene.nightparty0.Objetos.Reservacion;
import com.example.rene.nightparty0.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class ReservacionesNegocioActivity extends AppCompatActivity {

    private static final int DETALLE_RESERVACION_CODE = 1323;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ArrayList<Reservacion> listReservaciones;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String idLugar;
    String nombreNegocio;

    //Componentes de recyler
    TextView tvidReservacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservaciones_negocio);

        if(user == null){
            finish();
        }

        listReservaciones = new ArrayList<>();
        recyclerView=(RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);

        idLugar = getIntent().getStringExtra("idLugar");
        nombreNegocio = getIntent().getStringExtra("nombre");

        setTitle(nombreNegocio);

        llenarRecyclerView();


    }

    private void llenarRecyclerView() {
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        final AdapterReservacionesNegocios adapter = new AdapterReservacionesNegocios(getApplicationContext(),listReservaciones);
        recyclerView.setAdapter(adapter);
        CollectionReference reservacionesRef = db.collection(Reservacion.COLLECTION_RESERVACIONES);
        reservacionesRef.whereEqualTo(Reservacion.CAMPO_LUGARID,idLugar).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                listReservaciones.removeAll(listReservaciones);
                if(task.isSuccessful()){
                    for (DocumentSnapshot document : task.getResult())
                    {
                        Reservacion reservacion = document.toObject(Reservacion.class);
                        reservacion.setId(document.getId());
                        listReservaciones.add(reservacion);
                        Log.i("RESERVACIONESNEGOCIO", document.getId() + "=>" + document.getData());
                    }
                    recyclerView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    Collections.sort(listReservaciones);
                    adapter.notifyDataSetChanged();
                }else{
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "No hay ninguna reservacion", Toast.LENGTH_SHORT).show();
                }
            }
        });

        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvidReservacion = (TextView)v.findViewById(R.id.tvIdReservacion);
                Intent i = new Intent(getApplicationContext(),DetalleReservacionNegocioActivity.class);
                i.putExtra("idReservacion",tvidReservacion.getText().toString());
                i.putExtra("idLugar",idLugar);
                i.putExtra("nombreNegocio",nombreNegocio);
                startActivityForResult(i,DETALLE_RESERVACION_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == DETALLE_RESERVACION_CODE){
            switch (resultCode){
                case RESULT_OK:
                    finish();
                    startActivity(getIntent());
                    break;
                case RESULT_CANCELED:
                    finish();
                    startActivity(getIntent());
                    break;
            }
        }
    }
}
