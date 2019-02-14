package com.example.rene.nightparty0.Objetos;

import java.util.ArrayList;

public class Usuario {
    public static final String COLLECTION_USER = "usuarios";
    public static final String COLLECTION_FAVORITOS = "favoritos";
    public static final String COLLECTION_CHATS = "chats";

    public static final String TYPE_USER_ADMIN = "administrador";
    public static final String TYPE_USER_CLIENT = "cliente";
    public static final String TYPE_USER_BOUSSINES = "negocio";

    public static final String CAMPO_UID = "uid";
    public static final String CAMPO_EMAIL = "email";
    public static final String CAMPO_NAME = "name";
    public static final String CAMPO_PHOTO_URL = "photoUrl";
    public static final String CAMPO_PHONE_NUMBER = "phoneNumber";
    public static final String CAMPO_TYPE_USER = "typeUser";
    public static final String CAMPO_REGISTRATION_TOKENS = "registrationTokens";

    private String Uid;
    private String email;
    private String name;
    private String photoUrl;
    private String phoneNumber;
    private String typeUser;
    private ArrayList<String> registrationTokens;


    public Usuario() {

    }

    public Usuario(String uid, String email, String name, String photoUrl, String phoneNumber, String typeUser, ArrayList<String> registrationTokens) {
        Uid = uid;
        this.email = email;
        this.name = name;
        this.photoUrl = photoUrl;
        this.phoneNumber = phoneNumber;
        this.typeUser = typeUser;
        this.registrationTokens = registrationTokens;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTypeUser() {
        return typeUser;
    }

    public void setTypeUser(String typeUser) {
        this.typeUser = typeUser;
    }

    public ArrayList<String> getRegistrationTokens() {
        return registrationTokens;
    }

    public void setRegistrationTokens(ArrayList<String> registrationTokens) {
        this.registrationTokens = registrationTokens;
    }
}