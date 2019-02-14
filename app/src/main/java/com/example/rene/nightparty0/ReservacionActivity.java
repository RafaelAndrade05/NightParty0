package com.example.rene.nightparty0;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rene.nightparty0.Objetos.Reservacion;
import com.example.rene.nightparty0.Objetos.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class ReservacionActivity extends AppCompatActivity {

    private EditText etComentario,etNombre,etTelefono;
    private Button selectDate;
    private TextView date;
    private DatePickerDialog datePickerDialog;
    private Spinner spinnerHora,spinnerPersona;
    private ProgressBar progressBar;
    private int year;
    private int month;
    private int dayOfMonth;
    private Calendar calendar;
    private String[] hora = {"06:00 PM","06:30 PM","07:00 PM","07:30 PM","08:00 PM","08:30 PM","09:00 PM",
            "09:30 PM","10:00 PM","10:30 PM","11:00 PM","11:30 PM","12:00 AM","12:30 PM","01:00 AM","01:30 AM"};
    private String[] personas = {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20"};
    private String fechaEscogida;
    private String horaEscogida;
    private String nombreLugar;
    private int personasEscogida;
    private String comentario;
    private boolean actualizarDatosUsuario = false;
    private boolean finalizadoReservacion = false;
    private boolean finalizadoUsuario = false;

    private String idLugar;
    //Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user;

    Dialog myDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservacion);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            finish();
        }

        idLugar = getIntent().getStringExtra("idLugar");
        nombreLugar = getIntent().getStringExtra("nombreLugar");

        selectDate = findViewById(R.id.btnDate);
        spinnerHora = findViewById(R.id.spinnerHora);
        spinnerPersona = findViewById(R.id.spinnerPersonas);
        etComentario = findViewById(R.id.et_Comentario);
        etNombre = findViewById(R.id.et_Nombre);
        etTelefono= findViewById(R.id.et_Telefono);
        progressBar = findViewById(R.id.progress_bar);
        myDialog = new Dialog(this);

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        fechaEscogida = dayOfMonth + "/" + (month + 1) + "/" + year;

        selectDate.setText(fechaEscogida);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,hora);
        spinnerHora.setAdapter(adapter);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,personas);
        spinnerPersona.setAdapter(adapter1);

        llenarEditText(); //se llenan el nombre y telefono si el usuario ya tiene esa informacion

    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.btnDate:
                datePickerDialog = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                fechaEscogida = day + "/" + (month + 1) + "/" + year;
                                selectDate.setText(fechaEscogida);
                            }
                        }, year, month, dayOfMonth);
                datePickerDialog.show();
                break;
            case R.id.btnReservar:
                if(!validarForm()){
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                iniciarMensajePopup();
                solicitarReservacion(fechaEscogida,horaEscogida,personasEscogida);
                break;
        }
    }

    public boolean validarForm(){
        boolean validado = true;
        String nombre = etNombre.getText().toString();
        String telefono = etTelefono.getText().toString();

        if(TextUtils.isEmpty(nombre)){
            etNombre.setError(getString(R.string.alta_fromulario_requerido));
            validado = false;
        }else{
            etNombre.setError(null);
        }

        if(TextUtils.isEmpty(telefono)){
            etTelefono.setError(getString(R.string.alta_fromulario_requerido));
            validado = false;
        }else{
            etTelefono.setError(null);
        }

        return validado;

    }

    private void solicitarReservacion(String fechaEscogida, String horaEscogida, int personasEscogida) {
        horaEscogida = spinnerHora.getSelectedItem().toString();
        personasEscogida = Integer.parseInt(spinnerPersona.getSelectedItem().toString());
        comentario = etComentario.getText().toString();

        final Reservacion reservacion = new Reservacion();
        reservacion.setDia(fechaEscogida);
        reservacion.setHora(horaEscogida);
        reservacion.setNombreLugar(nombreLugar);
        reservacion.setNumPersonas(personasEscogida);
        reservacion.setConfirmacionReservacion(false);
        reservacion.setEstatusReservacion(getString(R.string.reservacion_pendiente));
        reservacion.setUid(user.getUid());
        reservacion.setLugarId(idLugar);
        reservacion.setTimestamp(Calendar.getInstance().getTime());

        if(!comentario.isEmpty()) {
            reservacion.setComentarioU(comentario);
        }
        Log.i("RESERVACIÓN DIA" , reservacion.getDia());
        Log.i("RESERVACIÓN HORA" , reservacion.getHora());
        Log.i("RESERVACIÓN HORA" , reservacion.getHora());
        Log.i("RESERVACIÓN PERSONAS" , String.valueOf(reservacion.getNumPersonas()));
        Log.i("RESERVACIÓN USERID" , reservacion.getUid());
        Log.i("RESERVACION LUGARID",reservacion.getLugarId());

        //Actualizar datos de usuario si modifico los editText
        if(actualizarDatosUsuario){
            actualizarDatos();
        }

        //Guardarlo en firestore
        db.collection(Reservacion.COLLECTION_RESERVACIONES).
                add(reservacion).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                finalizadoReservacion = true;
                verificarSubidaDatos();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(),getString(R.string.reservacion_fallida),Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void verificarSubidaDatos() {
        if(actualizarDatosUsuario){
            if(finalizadoReservacion && finalizadoUsuario){
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(),getString(R.string.reservacion_confimacion),Toast.LENGTH_SHORT).show();
                finish();
            }
        }else{
            if(finalizadoReservacion){
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(),getString(R.string.reservacion_confimacion),Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void actualizarDatos() {
        //Creo un objeto usuario como ayuda para actualizar datos
        Usuario usuario = new Usuario();
        usuario.setName(etNombre.getText().toString());
        usuario.setPhoneNumber(etTelefono.getText().toString());


        UserProfileChangeRequest profilesUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(usuario.getName())
                .build();

        user.updateProfile(profilesUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("USERUPDATE", "Datos actualizados en auth");
            }
        });

        //Actualizamos los datos de usuario en FireStore
        DocumentReference documentUsuario = db.collection(Usuario.COLLECTION_USER).document(user.getUid());
        documentUsuario.update(
                Usuario.CAMPO_NAME, usuario.getName(),
                Usuario.CAMPO_PHONE_NUMBER, usuario.getPhoneNumber()
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("USER UPDATE", "datos en firestore actualizados");
                finalizadoUsuario = true;
            }
        });
    }

    public void llenarEditText(){
        db.collection(Usuario.COLLECTION_USER).document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot != null){
                    Log.i("MODIFICARUSUARIO",documentSnapshot.getData().toString());
                    Usuario u = new Usuario();
                    u = documentSnapshot.toObject(Usuario.class);
                    etNombre.setText(u.getName());
                    etTelefono.setText(u.getPhoneNumber());

                    etNombre.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            actualizarDatosUsuario = true; //si se modifico el editText se debe cambiar los datos del usuario
                            Log.i("MODIFICARUSUARIO","SE MODIFICARA USUARIO");
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });

                    etTelefono.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            actualizarDatosUsuario = true;
                            Log.i("MODIFICARUSUARIO","SE MODIFICARA USUARIO");
                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                        }
                    });
                }
            }
        });
    }

    public void iniciarMensajePopup(){
        TextView tvTitulo,tvSubtitulo;
        myDialog.setContentView(R.layout.popupreservacion);
        tvTitulo = (TextView)myDialog.findViewById(R.id.tvTitulo);
        tvSubtitulo = (TextView)myDialog.findViewById(R.id.tvSubtitulo);
        tvTitulo.setText(R.string.popup_cargando_creando_reservacion);
        tvSubtitulo.setText(R.string.popup_cargando_espere);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }


}
