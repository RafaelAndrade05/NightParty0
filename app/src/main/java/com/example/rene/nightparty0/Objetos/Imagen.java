package com.example.rene.nightparty0.Objetos;

import android.net.Uri;

public class Imagen {
    private String urlimagen;
    private String id;
    private Uri uri;

    public Imagen(){}

    public Imagen(String urlimagen, String id, Uri uri) {
        this.urlimagen = urlimagen;
        this.id = id;
        this.uri = uri;
    }

    public String getUrlimagen() {
        return urlimagen;
    }

    public void setUrlimagen(String urlimagen) {
        this.urlimagen = urlimagen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
