package me.a01eg.canyon.mustage.model;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pchmn.materialchips.model.ChipInterface;

import me.a01eg.canyon.mustage.Const;

public class Tag implements ChipInterface {

    private int id;
    private String label;
    private Color color;

    public static DatabaseReference collection(String postId) {
        return FirebaseDatabase.getInstance().getReference(Const.kTagKey);
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public Object getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public Uri getAvatarUri() {
        return null;
    }

    @Override
    public Drawable getAvatarDrawable() {
        return null;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getInfo() {
        return String.format("Id: %s, Label :%s, Color: %s", id, label, color);
    }
}
