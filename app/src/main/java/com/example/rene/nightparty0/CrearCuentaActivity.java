package com.example.rene.nightparty0;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rene.nightparty0.Ayuda.SharedPreferencesManager;
import com.example.rene.nightparty0.Objetos.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CrearCuentaActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText etEmail,etPassword;

    SharedPreferences preferences;

    Dialog myDialog; //mensaje popup

    private ArrayList<String> listTokens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_cuenta);

        /*Linea que nos ayuda a que el background ocupe toda la pantalla incluyendo statusbar  */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        mAuth = FirebaseAuth.getInstance();

        etEmail =(EditText)findViewById(R.id.create_email) ;
        etPassword= (EditText)findViewById(R.id.create_password);
        preferences = getSharedPreferences("email", Context.MODE_PRIVATE);
        myDialog = new Dialog(this);

        listTokens = new ArrayList<>();

        if(SharedPreferencesManager.getInstance(getApplicationContext()).getToken() != null){
            //añade token a arrayList
            listTokens.add(SharedPreferencesManager.getInstance(getApplicationContext()).getToken());
        }

    }

    private void irPantallaPrincipal(FirebaseUser user) {
        if(user != null){
            Intent i = new Intent(this,Principal.class);
            startActivity(i);
        }
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_cerrar:
                finish();
                break;
            case R.id.button_create_account:
                crearCuenta(etEmail.getText().toString(),etPassword.getText().toString());
                break;
        }
    }


    private void crearCuenta(String email, String password) {
        if(!validarForm()){
            return;
        }

        TextView tvTitulo,tvSubtitulo;
        ProgressBar progressBarPopup;
        myDialog.setContentView(R.layout.popupcargar);
        tvTitulo = (TextView)myDialog.findViewById(R.id.tvTitulo);
        tvSubtitulo = (TextView)myDialog.findViewById(R.id.tvSubtitulo);
        progressBarPopup = (ProgressBar)myDialog.findViewById(R.id.progress_bar_popup);
        tvTitulo.setText(R.string.popup_cargando_creando_usuario);
        tvSubtitulo.setText(R.string.popup_cargando_espere);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();


        //crea el usuario en FiresAuth
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){ //crea el usuario en Firestore
                    Toast.makeText(getApplicationContext(),"Usuario creado correctamente",Toast.LENGTH_SHORT).show();
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    Usuario usuario = new Usuario();
                    usuario.setUid(currentUser.getUid());
                    usuario.setName(currentUser.getDisplayName());
                    usuario.setEmail(currentUser.getEmail());
                    usuario.setTypeUser(usuario.TYPE_USER_CLIENT);
                    usuario.setRegistrationTokens(listTokens);
                    db.collection(Usuario.COLLECTION_USER).document(currentUser.getUid()).set(usuario);

                    //Guardamos el email en sharedPreferences
                    preferences = getSharedPreferences("email",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("email",etEmail.getText().toString());
                    editor.commit();
                    finish();

                    myDialog.dismiss();

                    irPantallaPrincipal(currentUser);
                }else{
                        // If sign in fails, display a message to the user.
                        Log.i("INICIODESESION", "createUserWithEmail:failure", task.getException());
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();

                        myDialog.dismiss();
                }
            }
        });


    }

    public boolean validarForm(){
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        boolean validado = true;

        if(TextUtils.isEmpty(email)){
            etEmail.setError("Requerido");
            validado = false;
        }else{
            etEmail.setError(null);
        }

        if(TextUtils.isEmpty(password) || password.length() <= 6){
            etPassword.setError("Debe contener minimo 6 caracteres");
            validado = false;
        }else{
            etPassword.setError(null);
        }

        return validado;

    }
}
