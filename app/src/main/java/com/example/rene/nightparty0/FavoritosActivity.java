package com.example.rene.nightparty0;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.rene.nightparty0.Adaptadores.AdapterLugares;
import com.example.rene.nightparty0.Objetos.Lugar;
import com.example.rene.nightparty0.Objetos.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FavoritosActivity extends AppCompatActivity {

    private RecyclerView recyclerViewFavoritos;
    private ArrayList<Lugar> listFavoritos;
    private ProgressBar progressBarInicio;
    private TextView tvNoFavoritos;
    private ImageView imagenNoReservacion;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    int cont = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritos);

        progressBarInicio = findViewById(R.id.progress_barInicio);
        tvNoFavoritos = findViewById(R.id.tvSinFavoritos);
        imagenNoReservacion = findViewById(R.id.imagenSinFavoritos);
        recyclerViewFavoritos = findViewById(R.id.recyclerViewFavoritos);
        recyclerViewFavoritos.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        listFavoritos = new ArrayList<Lugar>();

        if(user == null){
            finish();
        }

        llenarRecyclerView();

    }

    private void llenarRecyclerView() {
        if (user == null)
            return;

        progressBarInicio.setVisibility(View.VISIBLE);
        recyclerViewFavoritos.setVisibility(View.VISIBLE);
        final AdapterLugares adapter = new AdapterLugares(getApplicationContext(), listFavoritos);
        recyclerViewFavoritos.setAdapter(adapter);

        //Consulta a los lugares favoritos del usuario
        final CollectionReference usuarioReference = db.collection(Usuario.COLLECTION_USER).document(user.getUid())
                .collection(Usuario.COLLECTION_FAVORITOS);
        usuarioReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(final QuerySnapshot queryDocumentSnapshots) {
                listFavoritos.remove(listFavoritos);
                if(queryDocumentSnapshots.isEmpty()){
                    //Usuario no ha agregado ningun favorito
                    progressBarInicio.setVisibility(View.INVISIBLE);
                    imagenNoReservacion.setVisibility(View.VISIBLE);
                    tvNoFavoritos.setVisibility(View.VISIBLE);
                    Log.i("FAVORITOSACTIVITY ","VACIO");
                }else{
                    Log.i("FAVORITOSACTIVITY ","NO VACIO");
                    for(final QueryDocumentSnapshot document: queryDocumentSnapshots){
                        //Recupera los id de los lugares favoritos
                        final DocumentReference lugarDocument = db.collection(Lugar.COLLECTION_LUGARES).
                                document(document.getId());
                        lugarDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Log.i("FAVORITOSACTIVITY ",""+documentSnapshot.getData());
                                if(documentSnapshot.exists()){
                                    //recupera la informacion del lugar
                                    Lugar lugar = new Lugar();
                                    lugar = documentSnapshot.toObject(Lugar.class);
                                    lugar.setDistancia(0.0);
                                    lugar.setId(documentSnapshot.getId());
                                    listFavoritos.add(lugar);
                                    cont++;

                                    //entra en la ultima iteracion del foreach
                                    if(cont == queryDocumentSnapshots.size()){
                                        adapter.notifyDataSetChanged();
                                        recyclerViewFavoritos.setVisibility(View.VISIBLE);
                                        progressBarInicio.setVisibility(View.INVISIBLE);
                                        cont = 0;
                                    }

                                }else {
                                    //validacion por si no se borro correctamente un favorito
                                    //validacion cuando se borra un lugar y el usuario lo sigue teniendo de favorito
                                    usuarioReference.document(documentSnapshot.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            finish();
                                            startActivity(getIntent());
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
        });

        //evento click en el adapter

        adapter.setOnClickListener(new View.OnClickListener() { //evento cuando presionan un Lugar
            @Override
            public void onClick(View view) {
                cargarDetalles(view);
            }
        });

    }

    //Componentes de la vista Detalle
    private TextView tvId, tvNombre,tvDistancia;

    private void cargarDetalles(View view) {
        tvId = (TextView) view.findViewById(R.id.txt_id);
        tvNombre = (TextView) view.findViewById(R.id.txt_nombre);
        tvDistancia = (TextView) view.findViewById(R.id.txt_distancia);
        Intent i = new Intent(getApplicationContext(), DetallesActivity.class);
        i.putExtra("id", tvId.getText().toString());
        i.putExtra("nombre", tvNombre.getText().toString());
        i.putExtra("distancia",tvDistancia.getText().toString());
        startActivity(i);
    }

    //reinicia el activity cuando el usuario presiona el back button
    @Override
    public void onRestart() {
        super.onRestart();
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
        Log.i("ACTIVITYRELOAD","RENICIANDO");
        finish();
        startActivity(getIntent());
    }
}
