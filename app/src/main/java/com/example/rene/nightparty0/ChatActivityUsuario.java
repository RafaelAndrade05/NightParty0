package com.example.rene.nightparty0;

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
import android.widget.Toast;

import com.example.rene.nightparty0.Objetos.Chat;
import com.example.rene.nightparty0.Objetos.ChatIds;
import com.example.rene.nightparty0.Objetos.Lugar;
import com.example.rene.nightparty0.Objetos.Usuario;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class ChatActivityUsuario extends AppCompatActivity {

    public static String TAG = "chatActivity";

    LinearLayout layout;
    RelativeLayout layout_2;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;

    FirebaseUser user;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference collectionReference;
    DocumentReference documentReference;
    String idLugar;
    String userId;
    String idChat;
    int validacionCreacionChat = 0;
    int aux = 0;
    int numMensajes = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            Toast.makeText(getApplicationContext(),getString(R.string.chatActivity_login_requerido), Toast.LENGTH_LONG).show();
            finish();
        }

        layout = (LinearLayout) findViewById(R.id.layout1);
        layout_2 = (RelativeLayout)findViewById(R.id.layout2);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        idLugar = getIntent().getStringExtra("idLugar");
        userId = user.getUid();

        collectionReference = db.collection(Chat.COLLECTION_CHAT);

        Log.i(TAG,"idLugar =>"+ idLugar);
        Log.i(TAG,"UserId =>" +userId);

        collectionReference
                .whereEqualTo(ChatIds.CAMPO_IDUSUARIO,userId).whereEqualTo(ChatIds.CAMPO_IDLUGAR,idLugar)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.isEmpty()){
                    //Es la primera vez que habra chat
                    //crea chat
                    Log.i(TAG,"Creando Chat");
                    creandoChat();

                }else{
                    //ya hay mensajes anteriores recuperar chats
                    Log.i(TAG,"Recuperando Mensajes antiguos size => " +queryDocumentSnapshots.size());
                    //obtiene el id del chat para recuperar mensajes
                    DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                    llenarMensajesAntiguos(document.getId());
                }
            }
        });

    }

    public void addMessageBox(String message, int type){
        TextView textView = new TextView(ChatActivityUsuario.this);
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


    //Metodos para crear el chat nuevo
    public void creandoChat(){
        ChatIds objChatIds = new ChatIds();
        objChatIds.setIdLugar(idLugar);
        objChatIds.setIdUsuario(userId);

        collectionReference.add(objChatIds).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                idChat = documentReference.getId();
                Map<String,Object> data = new HashMap<>();
                data.put("idChat",idChat);
                db.collection(Usuario.COLLECTION_USER).document(userId).collection(Usuario.COLLECTION_CHATS).add(data)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                validacionCreacionChat++;
                                iniciarChat();
                            }
                        });
                db.collection(Lugar.COLLECTION_LUGARES).document(idLugar).collection(Lugar.COLLECTION_CHATS).add(data).
                        addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                validacionCreacionChat++;
                                iniciarChat();
                            }
                        });


            }
        });
    }

    private void iniciarChat() {
        if(validacionCreacionChat == 2) {//garantiza que se creo el chat
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String messageText = messageArea.getText().toString();

                    if (!messageText.equals("")) {
                        Chat objChat = new Chat();
                        objChat.setMessage(messageText);
                        objChat.setUserId(userId);
                        objChat.setTimestamp(Calendar.getInstance().getTime());

                        collectionReference.document(idChat).collection(Chat.COLLECTION_MENSAJES).add(objChat).
                                addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        messageArea.setText("");
                                    }
                                });
                    }
                }
            });

            //escuchador cuando crea el chat y no hay ningun mensaje
            collectionReference.document(idChat)
                    .collection(Chat.COLLECTION_MENSAJES)
                    .orderBy(Chat.CAMPO_DATE,Query.Direction.DESCENDING).limit(1)
                    .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if(!documentSnapshots.isEmpty()){
                                Log.i(TAG,"Escuchador tiene informacion");
                                numMensajes = documentSnapshots.size();

                                Log.i(TAG, "Docuemtns Size => " + numMensajes);

                                //Ultima iteracion
                                //agrega el ultimo mensaje
                                Log.i(TAG, "Agrega Ultimo Mensaje");

                                Chat chat = new Chat();
                                chat = documentSnapshots.getDocuments().get(numMensajes-1).toObject(Chat.class);

                                if (userId.equals(chat.getUserId())) {
                                    Log.i(TAG, "Añadiendo mensaje Usuario");
                                    addMessageBox("Tu:\n" + chat.getMessage(), 1);
                                } else {
                                    Log.i(TAG, "Añadiendo mensaje Negocio");
                                    addMessageBox("Negocio:\n" + chat.getMessage(), 2);
                                }


                            }else{
                                Log.i(TAG,"Escuchador esta vacio");

                            }
                        }
                    });
        }
    }



    //Aqui son los metodos para llenar el chat cuando ya se habia creado con anterioridad
    private void llenarMensajesAntiguos(String idChat) {
        this.idChat = idChat;
        collectionReference.document(idChat)
                .collection(Chat.COLLECTION_MENSAJES)
                .orderBy(Chat.CAMPO_DATE)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        if(!documentSnapshots.isEmpty()){
                            Log.i(TAG,"Escuchador tiene informacion");
                            numMensajes = documentSnapshots.size();
                            for (QueryDocumentSnapshot doc : documentSnapshots) {

                                Chat chat = new Chat();
                                chat = doc.toObject(Chat.class);

                                aux++;
                                //no llena el ultimo mensaje ya que el listener lo hara
                                if(numMensajes == aux) {
                                    break;
                                }


                                if (userId.equals(chat.getUserId())) {
                                    Log.i(TAG, "Añadiendo mensaje Usuario");
                                    addMessageBox("You:\n" + chat.getMessage(), 1);
                                } else {
                                    Log.i(TAG, "Añadiendo mensaje Negocio");
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
                    Chat objChat = new Chat();
                    objChat.setMessage(messageText);
                    objChat.setUserId(userId);
                    objChat.setTimestamp(Calendar.getInstance().getTime());

                    collectionReference.document(idChat).collection(Chat.COLLECTION_MENSAJES).add(objChat).
                            addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    messageArea.setText("");
                                }
                            });
                }
            }
        });

        collectionReference.document(idChat).
                collection(Chat.COLLECTION_MENSAJES).
                orderBy(Chat.CAMPO_DATE, Query.Direction.DESCENDING).
                limit(1).
                addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if(!documentSnapshots.isEmpty()){
                            Log.i(TAG,"Escuchador tiene informacion");
                            numMensajes = documentSnapshots.size();

                            Log.i(TAG, "Documents Size => " + numMensajes);

                            //agrega el ultimo mensaje
                            Log.i(TAG, "Agrega Ultimo Mensaje");

                            Chat chat = new Chat();
                            chat = documentSnapshots.getDocuments().get(numMensajes-1).toObject(Chat.class);

                            if (userId.equals(chat.getUserId())) {
                                Log.i(TAG, "Añadiendo mensaje Usuario");
                                addMessageBox("You:\n" + chat.getMessage(), 1);
                            } else {
                                Log.i(TAG, "Añadiendo mensaje Negocio");
                                addMessageBox("Other:\n" + chat.getMessage(), 2);
                            }


                        }else{
                            Log.i(TAG,"Escuchador esta vacio");
                        }
                    }
                });
    }


}
