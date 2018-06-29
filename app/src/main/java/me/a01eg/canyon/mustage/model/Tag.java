package me.a01eg.canyon.mustage.model;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tylersuehr.chips.Chip;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.a01eg.canyon.mustage.Const;

public class Tag extends Chip implements Serializable {

    private String id;
    private String label;
    private String info;
    private String color;

    public static DatabaseReference collection() {
        return FirebaseDatabase.getInstance().getReference(Const.kTagKey);
    }

    public static boolean send(final String storyKey, List<? extends Chip> tags) {
        DatabaseReference tagRef = Story.tags(storyKey);
        tagRef.removeValue();

        Map<String, Object> tagsArr = new HashMap<>();
        for (int i = 0; i < tags.size(); i++) {
            tagsArr.put(Integer.toString(i), tags.get(i).getId());
        }

        tagRef.updateChildren(tagsArr);

        return true;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public Object getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @NonNull
    @Override
    public String getTitle() {
        return label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return info;
    }

    @Override
    public Uri getAvatarUri() {
        return null;
    }

    @Override
    public Drawable getAvatarDrawable() {
        return null;
    }
}
