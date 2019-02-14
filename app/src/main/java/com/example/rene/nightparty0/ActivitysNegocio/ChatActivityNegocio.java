package com.example.rene.nightparty0.ActivitysNegocio;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.rene.nightparty0.ChatActivityUsuario;
import com.example.rene.nightparty0.Objetos.Chat;
import com.example.rene.nightparty0.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;

import javax.annotation.Nullable;

public class ChatActivityNegocio extends AppCompatActivity {

    public static String TAG = "chatActivity";

    LinearLayout layout;
    RelativeLayout layout_2;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;

    FirebaseUser user;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference collectionReference;
    String idChat;
    String idLugar;
    String otherUser;
    String nombre;

    int aux = 0;
    int numMensajes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_negocio);

        layout = (LinearLayout) findViewById(R.id.layout1);
        layout_2 = (RelativeLayout)findViewById(R.id.layout2);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        idChat = getIntent().getStringExtra("idChat");
        idLugar = getIntent().getStringExtra("idLugar");
        nombre = getIntent().getStringExtra("nombre");
        otherUser = getIntent().getStringExtra("otherUser");


        collectionReference = db.collection(Chat.COLLECTION_CHAT).document(idChat)
                .collection(Chat.COLLECTION_MENSAJES);

        collectionReference.orderBy(Chat.CAMPO_DATE)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if(!documentSnapshots.isEmpty()){
                            Log.i(TAG,"Escuchador tiene informacion");
                            numMensajes=documentSnapshots.size();
                            for (QueryDocumentSnapshot doc : documentSnapshots) {

                                Chat chat;
                                chat = doc.toObject(Chat.class);

                                aux++;
                                //no agrega el ultimo mensaje ya que el listener lo hara
                                if(aux == numMensajes){
                                    break;
                                }

                                if (idLugar.equals(chat.getLugarId())) {
                                    Log.i(TAG, "Añadiendo mensaje Negocio");
                                    addMessageBox("You:\n" + chat.getMessage(), 1);
                                } else {
                                    Log.i(TAG, "Añadiendo mensaje Usuario");
                                    addMessageBox("Other:\n" + chat.getMessage(), 2);
                                }

                            }
                            //anañade escuchador que va a mostrar el ultimo mensaje
                            addListeners();


                        }else{
                            Log.i(TAG,"Escuchador esta vacio");
                            addListeners();
                        }
                    }
                });
    }


    public void addListeners(){
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = messageArea.getText().toString();

                if(!messageText.equals("")) {
                    Date actual = Calendar.getInstance().getTime();
                    Chat objChat = new Chat();
                    objChat.setMessage(messageText);
                    objChat.setLugarId(idLugar);
                    objChat.setTimestamp(actual);
                    objChat.setNombreRemitente(nombre);
                    objChat.setRecibidoId(otherUser);

                    Log.i(TAG,"tiempo del mensaje"+actual);

                    collectionReference.add(objChat).
                            addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    messageArea.setText("");
                                }
                            });
                }
            }
        });

        //consulta donde obtiene el ultimo mensaje generado
        collectionReference.
                orderBy(Chat.CAMPO_DATE,Query.Direction.DESCENDING).limit(1).
                addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if(!documentSnapshots.isEmpty()){
                            Log.i(TAG,"Escuchador tiene informacion");
                                Log.i(TAG, "llena el ultimo mensaje");
                                numMensajes = documentSnapshots.size();

                                        //agrega el ultimo mensaje
                                        Log.i(TAG, "Agrega Ultimo Mensaje");

                                        Chat chat = new Chat();
                                        chat = documentSnapshots.getDocuments().get(numMensajes-1).toObject(Chat.class);

                                        if (idLugar.equals(chat.getLugarId())) {
                                            Log.i(TAG, "Añadiendo mensaje Negocio");
                                            addMessageBox("You:\n" + chat.getMessage(), 1);
                                        } else {
                                            Log.i(TAG, "Añadiendo mensaje Usuario");
                                            addMessageBox("Other:\n" + chat.getMessage(), 2);
                                        }

                        }else{
                            Log.i(TAG,"Escuchador esta vacio");
                        }
                    }
                });
    }

    public void addMessageBox(String message, int type){
        TextView textView = new TextView(ChatActivityNegocio.this);
        textView.setText(message);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;

        if(type == 1) {
            lp2.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.bubble_in_9);
        }
        else{
            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.bubble_out);
        }
        textView.setLayoutParams(lp2);
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }

}
