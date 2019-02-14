package com.example.rene.nightparty0.ActivitysNegocio;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rene.nightparty0.Objetos.Reservacion;
import com.example.rene.nightparty0.Objetos.Usuario;
import com.example.rene.nightparty0.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DetalleReservacionNegocioActivity extends AppCompatActivity {

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String idLugar,idReservacion,numeroTelefono,nombreLugar;
    public final static int MSMPERMISSIONCODE = 225;
    private boolean mMSMPermissionsGranted = false;
    private boolean comprobarInformacionReservacion = false;
    private boolean comprobarInformacionUsuario = false;

    //Componentes
    private TextView tvFecha,tvPersonas,tvHora,tvEstatus,tvComentario,tvComentario2,tvNombre,tvCorreo,tvTelefono;
    private EditText etComentarioUsuario;
    private ProgressBar progressBar,progressBarConfirmacion;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_reservacion_negocio);

        if(user == null){
            finish();
        }


        idLugar = getIntent().getStringExtra("idLugar");
        idReservacion = getIntent().getStringExtra("idReservacion");
        nombreLugar = getIntent().getStringExtra("nombreNegocio");

        Log.i("RESERVACION", "id Lugar => " +  idLugar);
        Log.i("RESERVACION","id Reservacioc => " +  idReservacion);

        tvFecha = (TextView)findViewById(R.id.tvFecha2);
        tvPersonas = (TextView)findViewById(R.id.tvPersonas2);
        tvHora = (TextView)findViewById(R.id.tvHora2);
        tvEstatus = (TextView)findViewById(R.id.tvEstatus2);
        tvComentario = (TextView)findViewById(R.id.tvComentario2);
        tvComentario2 = (TextView)findViewById(R.id.tvComentario);
        tvNombre = (TextView)findViewById(R.id.tvNombre2);
        tvCorreo = (TextView)findViewById(R.id.tvCorreo2);
        tvTelefono = (TextView)findViewById(R.id.tvTelefono2);
        etComentarioUsuario = (EditText)findViewById(R.id.etComentarioNegocio);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        progressBarConfirmacion = (ProgressBar)findViewById(R.id.progress_barConfirmacion);
        scrollView = (ScrollView)findViewById(R.id.scrollView);


        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.INVISIBLE);

        checkSMSStatePermission();
        llenarInformacionReservacion();

    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.btnAceptar:
                enviarMensaje();
                break;
            case R.id.btnRechazar:
                progressBarConfirmacion.setVisibility(View.VISIBLE);
                db.collection(Reservacion.COLLECTION_RESERVACIONES).document(idReservacion).
                        update(Reservacion.CAMPO_ESTATUS,getString(R.string.reservacion_rechazada),
                                Reservacion.CAMPO_CONFIRMACION,false)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressBarConfirmacion.setVisibility(View.INVISIBLE);
                                Intent returnIntent = new Intent();
                                setResult(Activity.RESULT_CANCELED, returnIntent);
                                finish();
                            }
                        });

                break;
        }

    }

    private void llenarInformacionReservacion() {
        db.collection(Reservacion.COLLECTION_RESERVACIONES).document(idReservacion).
                get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    Reservacion reservacion = document.toObject(Reservacion.class);
                    Log.i("Reservacion",document.getId()+ " => " + document.getData());
                    tvFecha.setText(reservacion.getDia());
                    tvHora.setText(reservacion.getHora());
                    tvPersonas.setText(String.valueOf(reservacion.getNumPersonas()));
                    tvEstatus.setText(reservacion.getEstatusReservacion());
                    if(reservacion.getComentarioU() != null) {
                        tvComentario.setText(reservacion.getComentarioU());
                    }else{
                        tvComentario.setVisibility(View.INVISIBLE);
                        tvComentario2.setVisibility(View.INVISIBLE);
                    }
                    comprobarInformacionReservacion = true;
                    llenarInformacionUsuario(reservacion.getUid());
                }else{

                }
            }
        });
    }
    Usuario usuario;
    private void llenarInformacionUsuario(String idUser) {
        db.collection(Usuario.COLLECTION_USER).document(idUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    Log.i("Reservacion",document.getId()+ " => " + document.getData());
                    usuario = document.toObject(Usuario.class);
                    tvNombre.setText(usuario.getName());
                    tvCorreo.setText(usuario.getEmail());
                    tvTelefono.setText(usuario.getPhoneNumber());
                    numeroTelefono = usuario.getPhoneNumber();
                    comprobarInformacionUsuario = true;
                    comprobarInformacion();
                }else{

                }
            }
        });
    }

    private void comprobarInformacion() {
        if(comprobarInformacionReservacion && comprobarInformacionUsuario){
            progressBar.setVisibility(View.INVISIBLE);
            scrollView.setVisibility(View.VISIBLE);
        }
    }

    public boolean validarform(){
        String comentarioUsuario = etComentarioUsuario.getText().toString();

        if(TextUtils.isEmpty(comentarioUsuario)){
            etComentarioUsuario.setError("Requerido");
            return false;
        }else {
            etComentarioUsuario.setError(null);
        }
        return true;
    }

    private void enviarMensaje() {
        //verificamos los permisos de telefono
        try{
            if(mMSMPermissionsGranted) {
                progressBarConfirmacion.setVisibility(View.VISIBLE);
                String mensajeCompleto = getString(R.string.reservacion_confirmacion_titulo) + " " +
                         nombreLugar;
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(numeroTelefono, null, mensajeCompleto, null, null);
                Toast.makeText(getApplicationContext(), "Mensaje Enviado.", Toast.LENGTH_LONG).show();
                Log.i("MENSAJE","Numero enviado a => " +  numeroTelefono + " Mensaje: => " + mensajeCompleto);
                actualizarDatosReservacion();
            }else {
                checkSMSStatePermission();
            }
        }

        catch (Exception e){
            Toast.makeText(getApplicationContext(), "Mensaje no enviado, datos incorrectos." + e.getMessage().toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void actualizarDatosReservacion(){
        db.collection(Reservacion.COLLECTION_RESERVACIONES).document(idReservacion).
                update(Reservacion.CAMPO_ESTATUS,getString(R.string.reservacion_aceptada),
                        Reservacion.CAMPO_CONFIRMACION,true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressBarConfirmacion.setVisibility(View.INVISIBLE);
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });
    }


    private void checkSMSStatePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.SEND_SMS);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.i("Mensaje", "No se tiene permiso para enviar SMS.");
            mMSMPermissionsGranted = false;
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, MSMPERMISSIONCODE);
        } else {
            mMSMPermissionsGranted = true;
            Log.i("Mensaje", "Se tiene permiso para enviar SMS!");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mMSMPermissionsGranted = false;

        switch (requestCode){
            case MSMPERMISSIONCODE :
                for(int i = 0 ; i <grantResults.length; i++){
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        mMSMPermissionsGranted = false;
                        return;
                    }
                }
                Log.i("Mensajes","permisos aceptados se cambia a true");
                mMSMPermissionsGranted = true;
                break;
        }
    }
}
