package com.example.rene.nightparty0;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.rene.nightparty0.Objetos.Reservacion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

public class ReservacionDetalleUsuarioActivity extends AppCompatActivity {

    //Componentes
    private TextView tvLugar,tvFecha,tvPersonas,tvHora,tvEstatus,tvComentario,tvComentario2;
    private String idLugar,idReservacion,nombreLugar;
    private ProgressBar progressBar;
    private ScrollView scrollView;
    private TextView tvSubtitulo;
    private ImageView imagenSubtitulo;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservacion_detalle_usuario);

        idLugar = getIntent().getStringExtra("idLugar");
        idReservacion = getIntent().getStringExtra("idReservacion");
        nombreLugar = getIntent().getStringExtra("nombreNegocio");

        Log.i("RESERVACION", "id Lugar => " +  idLugar);
        Log.i("RESERVACION","id ReservacioN => " +  idReservacion);
        Log.i("RESERVACION","Nombre Luagar => " +  nombreLugar);

        tvSubtitulo = (TextView)findViewById(R.id.tvSubtitulo);
        imagenSubtitulo = (ImageView)findViewById(R.id.imagenSubtitulo);

        tvLugar = (TextView)findViewById(R.id.tvLugar2);
        tvFecha = (TextView)findViewById(R.id.tvFecha2);
        tvPersonas = (TextView)findViewById(R.id.tvPersonas2);
        tvHora = (TextView)findViewById(R.id.tvHora2);
        tvEstatus = (TextView)findViewById(R.id.tvEstatus2);
        tvComentario = (TextView)findViewById(R.id.tvComentario2);
        tvComentario2 = (TextView)findViewById(R.id.tvComentario);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.INVISIBLE);


        llenarInformacionReservacion();
    }

    private void llenarInformacionReservacion() {
        db.collection(Reservacion.COLLECTION_RESERVACIONES).document(idReservacion).
                get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()) {
                        DocumentSnapshot document = task.getResult();
                        Reservacion reservacion = document.toObject(Reservacion.class);
                        Log.i("Reservacion", document.getId() + " => " + document.getData());
                        tvLugar.setText(nombreLugar);
                        tvFecha.setText(reservacion.getDia());
                        tvHora.setText(reservacion.getHora());
                        tvPersonas.setText(String.valueOf(reservacion.getNumPersonas()));
                        tvEstatus.setText(reservacion.getEstatusReservacion());
                        if (reservacion.getComentarioU() != null) {
                            tvComentario.setText(reservacion.getComentarioU());
                        } else {
                            tvComentario.setVisibility(View.INVISIBLE);
                            tvComentario2.setVisibility(View.INVISIBLE);
                        }

                        llenarSubtitulo(reservacion.getEstatusReservacion());

                        progressBar.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);
                    }else {
                        finish();
                    }
                }
            }
        });
    }

    private void llenarSubtitulo(String estatusReservacion) {
        if(estatusReservacion.equals("Aceptada")){
            tvSubtitulo.setText(getString(R.string.reser_deta_usua_aceptada));
            imagenSubtitulo.setBackgroundResource(R.drawable.ic_accept);

        }else if(estatusReservacion.equals("Rechazada")){
            tvSubtitulo.setText(getString(R.string.reser_deta_usua_rechazada));
            imagenSubtitulo.setBackgroundResource(R.drawable.ic_decline);

        }else if(estatusReservacion.equals("Pendiente")){
            tvSubtitulo.setText(getString(R.string.reser_deta_usua_pendiente));
            imagenSubtitulo.setBackgroundResource(R.drawable.ic_wait);
        }
    }
}
