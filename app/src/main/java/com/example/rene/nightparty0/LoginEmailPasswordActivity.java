package com.example.rene.nightparty0;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rene.nightparty0.Ayuda.SharedPreferencesManager;
import com.example.rene.nightparty0.Objetos.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class LoginEmailPasswordActivity extends AppCompatActivity {

    private EditText etEmail,etPassword;
    private FirebaseAuth mAuth;
    int tipoPassword = 1;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email_password);

        /*Linea que nos ayuda a que el background ocupe toda la pantalla incluyendo statusbar  */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        mAuth = FirebaseAuth.getInstance();

        etEmail = (EditText) findViewById(R.id.txt_email);
        etPassword = (EditText) findViewById(R.id.txt_password);
        preferences = getSharedPreferences("email", Context.MODE_PRIVATE);
        etEmail.setText(preferences.getString("email",""));

    }
    
    public void onClick(View view){
        switch (view.getId()){
            case R.id.button_close:
                finish();
                break;
            case R.id.button_login_password:
                iniciarSesion();
                break;
            case R.id.button_forgot_password:
                startActivity(new Intent(getApplicationContext(),RestablecerContrasenaActivity.class));
                break;
        }
    }

    private void iniciarSesion() {
        if(!validarForm()){
            return;
        }

        //Guardamos el email en sharedPreferences
        preferences = getSharedPreferences("email",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email",etEmail.getText().toString());
        editor.commit();
        finish();

        mAuth.signInWithEmailAndPassword(etEmail.getText().toString(),etPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    irPantallaPrincipal();
                }else{
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    Log.w("TAGLOGIN", task.getException().getMessage());
                }
            }
        });


    }


    private void irPantallaPrincipal() {
        Intent i = new Intent(this,Principal.class);
        startActivity(i);

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

        if(TextUtils.isEmpty(password)){
            etPassword.setError("Requerido");
            validado = false;
        }else{
            etPassword.setError(null);
        }

        return validado;
    }


}
