package com.fifthgen.mustage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fifthgen.mustage.model.Story;
import com.fifthgen.mustage.model.Tag;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.tylersuehr.chips.Chip;
import com.tylersuehr.chips.ChipsInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagActivity extends AppCompatActivity implements ValueEventListener, View.OnClickListener {

    public static final String EXTRA_STORY_KEY = "extra.story.key";
    private DatabaseReference mTagRef;
    private DatabaseReference mStoryTagRef;
    private List<DatabaseReference> mTagsRef;

    private ChipsInputLayout chipsInput;
    private String storyKey;
    private List<Tag> tags = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);
        chipsInput = findViewById(R.id.chipsInput);
        Button saveTagsButton = findViewById(R.id.saveTagsButton);
        saveTagsButton.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        storyKey = getIntent().getStringExtra(EXTRA_STORY_KEY);

        if (storyKey != null) {
            // Tags reference.
            mTagRef = Tag.collection();
            mTagRef.addValueEventListener(this);

            // Story tags reference.
            mStoryTagRef = Story.tags(storyKey);
            mStoryTagRef.addValueEventListener(this);
        }
    }

    @Override
    protected void onStop() {
        if (mTagRef != null) {
            mTagRef.removeEventListener(this);
            mTagRef = null;
        }

        if (mStoryTagRef != null) {
            mStoryTagRef.removeEventListener(this);
            mStoryTagRef = null;
        }

        if (mTagsRef != null) {
            for (DatabaseReference ref : mTagsRef) {
                if (ref != null) {
                    ref.removeEventListener(this);
                }
            }

            mTagsRef = null;
        }

        super.onStop();
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {

            String ref = dataSnapshot.getRef().toString();
            if (mTagRef != null && ref.equals(mTagRef.toString())) {
                GenericTypeIndicator<HashMap<String, Tag>> typeIndicator = new GenericTypeIndicator<HashMap<String, Tag>>() {
                };

                HashMap<String, Tag> tags = dataSnapshot.getValue(typeIndicator);
                if (tags != null && !tags.isEmpty()) {
                    // Set the id property to the key for each tag.
                    for (Map.Entry<String, Tag> entry : tags.entrySet()) {
                        entry.getValue().setId(entry.getKey());
                    }

                    chipsInput.setFilterableChipList(new ArrayList<>(tags.values()));
                }
            }

            if (mStoryTagRef != null && ref.equals(mStoryTagRef.toString())) {
                GenericTypeIndicator<ArrayList<String>> typeIndicator = new GenericTypeIndicator<ArrayList<String>>() {
                };

                List<String> tagIds = dataSnapshot.getValue(typeIndicator);

                if (tagIds != null && !tagIds.isEmpty()) {
                    mTagsRef = new ArrayList<>();
                    for (String tagId : tagIds) {
                        DatabaseReference tagRef = FirebaseDatabase.getInstance().getReference(Const.kTagKey).child(tagId);
                        tagRef.addValueEventListener(this);
                        mTagsRef.add(tagRef);
                    }
                }
            }

            if (mTagsRef != null && !mTagsRef.isEmpty()) {
                for (DatabaseReference tagRef : mTagsRef) {
                    if (tagRef != null && ref.equals(tagRef.toString())) {
                        Tag tag = dataSnapshot.getValue(Tag.class);
                        if (tag != null) {
                            tag.setId(tagRef.getKey());
                            tags.add(tag);
                        }
                    }
                }

                chipsInput.clearSelectedChips();

                for (Tag tag : tags) {
                    tag.setFilterable(true);
                    List<Chip> toRemove = new ArrayList<>();
                    for (Chip origTag : chipsInput.getOriginalFilterableChips()) {
                        if (origTag.getId() != null)
                            if (origTag.getId().equals(tag.getId())) {
                                toRemove.add(origTag);
                                break;
                            }
                    }

                    chipsInput.getOriginalFilterableChips().removeAll(toRemove);
                    chipsInput.addSelectedChip(tag);
                }
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.e(getClass().getName(), "Cannot process reference. " + databaseError.getMessage());
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.saveTagsButton) {
            if (storyKey != null) {
                tags = new ArrayList<>();
                Tag.send(storyKey, chipsInput.getSelectedChips());
                Toast.makeText(this, "Tags saved.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
            }
        }
    }
}
