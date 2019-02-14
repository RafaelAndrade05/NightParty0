package com.example.rene.nightparty0.ActivitysNegocio;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rene.nightparty0.Adaptadores.AdapterMisLugares;
import com.example.rene.nightparty0.Objetos.Lugar;
import com.example.rene.nightparty0.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MisNegociosActivity extends AppCompatActivity {

    private ArrayList<Lugar> listLugares;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Button btnNegocio;

    //componentes del recylcerView
    private TextView tvIdNegocio;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_negocio);

        if(user == null){
            finish();
        }

        listLugares = new ArrayList<>();
        recyclerView=(RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        btnNegocio = (Button)findViewById(R.id.buttonNegocio);

        llenarRecycler();
    }

    private void llenarRecycler() {
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        final AdapterMisLugares adapter = new AdapterMisLugares(getApplicationContext(), listLugares);
        CollectionReference lugaresRef = db.collection(Lugar.COLLECTION_LUGARES);
        recyclerView.setAdapter(adapter);

        lugaresRef.whereEqualTo(Lugar.CAMPO_UID, user.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                listLugares.removeAll(listLugares); //Limpia la lista
                if(task.isSuccessful()) {
                    if(!task.getResult().isEmpty()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Lugar lugar = document.toObject(Lugar.class);
                            lugar.setId(document.getId());
                            listLugares.add(lugar);
                            Log.i("MISNEGOCIOS", document.getId() + "=>" + document.getData());
                        }
                        recyclerView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        btnNegocio.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }else {
                            //Toast.makeText(getApplicationContext(), "No se ha registrado ningun negocio", Toast.LENGTH_SHORT).show();
                            btnNegocio.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);

                    }
                }
            }
        });

        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonNegocio:
                startActivity(new Intent(getApplicationContext(),AltaLugarActivity.class));
        }
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
