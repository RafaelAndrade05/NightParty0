package com.example.rene.nightparty0;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class RestablecerContrasenaActivity extends AppCompatActivity {

    FirebaseAuth auth = FirebaseAuth.getInstance();
    EditText etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restablecer_contrasena);
        etEmail = findViewById(R.id.et_email);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.button_restablecer_contra:
                if(validarForm())
                    restablecerContraseña(etEmail.getText().toString());
                break;
        }
    }

    public void restablecerContraseña(String email)
    {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(),"Email enviado",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    public boolean validarForm(){
        String email = etEmail.getText().toString();
        boolean validado = true;

        if(TextUtils.isEmpty(email)){
            etEmail.setError("Requerido");
            validado = false;
        }else{
            etEmail.setError(null);
        }

        return validado;

    }

}
