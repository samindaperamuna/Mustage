package com.fifthgen.mustage;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.fifthgen.mustage.model.Tag;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.skydoves.colorpickerpreference.ColorPickerDialog;
import com.tylersuehr.chips.Chip;
import com.tylersuehr.chips.ChipDataSource;
import com.tylersuehr.chips.ChipsInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class SettingsActivity extends AppCompatActivity implements ValueEventListener, View.OnClickListener {

    // Tag in memory data store. First param is true if the entry needs to be added.
    private final Queue<Pair<Boolean, Tag>> addedTags = new LinkedList<>();

    private ChipsInputLayout chipsInput;
    private EditText editColorTextView;
    private EditText editInfoTextView;
    private EditText editLabelTextView;
    private FrameLayout colorView;

    private DatabaseReference mTagRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button addTagButton = findViewById(R.id.addTagButton);
        addTagButton.setOnClickListener(this);
        Button saveTagsButton = findViewById(R.id.saveTagsButton);
        saveTagsButton.setOnClickListener(this);

        editInfoTextView = findViewById(R.id.editInfoTextView);
        editLabelTextView = findViewById(R.id.editLabelTextView);

        // Chips input view related initialisations.
        chipsInput = findViewById(R.id.chipsInput);
        chipsInput.addSelectionObserver(new ChipDataSource.SelectionObserver() {
            @Override
            public void onChipSelected(Chip chip) {
                addedTags.add(new Pair<>(true, (Tag) chip));
                Log.i("Chip", "Chip Added");
            }

            @Override
            public void onChipDeselected(Chip chip) {
                addedTags.add(new Pair<>(false, (Tag) chip));
                Log.i("Chip", "Chip removed");

            }
        });

        colorView = findViewById(R.id.colorView);
        colorView.setOnClickListener(this);

        editColorTextView = findViewById(R.id.editColorTextView);
        editColorTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String color = charSequence.toString();

                if (color.length() > 1)
                    if (Utility.isValidColor(color)) {
                        colorView.setBackgroundColor(Color.parseColor(color));
                    }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Sample adapter.
        String[] items = {"Default"};
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        Spinner settingsSpinner = findViewById(R.id.categorySpinner);
        settingsSpinner.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mTagRef = Tag.collection();
        mTagRef.addValueEventListener(this);
    }

    @Override
    protected void onStop() {
        mTagRef.removeEventListener(this);
        mTagRef = null;

        super.onStop();
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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

                    chipsInput.setSelectedChipList(new ArrayList<>(tags.values()));
                }
            }
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Log.e(getClass().getName(), "Cannot process reference. " + databaseError.getMessage());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.colorView:
                ColorPickerDialog.Builder colorPickerBuilder = new ColorPickerDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                colorPickerBuilder.setTitle("ColorPicker");
                colorPickerBuilder.setPreferenceName("ColorPickerDialog");
                // colorPickerBuilder.setFlagView(new CustomFlag(this, R.layout.layout_flag));
                colorPickerBuilder.setPositiveButton(getString(R.string.color_pos), colorEnvelope -> {
                    String color = "#" + colorEnvelope.getColorHtml();
                    editColorTextView.setText(color);
                    colorView.setBackgroundColor(Color.parseColor(color));
                });
                colorPickerBuilder.setNegativeButton(getString(R.string.color_neg), (dialogInterface, i) -> dialogInterface.dismiss());
                colorPickerBuilder.show();
                break;
            case R.id.addTagButton:
                if (validateNewTag()) {
                    Tag tag = new Tag();
                    tag.setInfo(editInfoTextView.getText().toString());
                    tag.setLabel(editLabelTextView.getText().toString());
                    tag.setColor(editColorTextView.getText().toString());

                    chipsInput.addSelectedChip(tag);
                }
                break;
            case R.id.saveTagsButton:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Confirm Data Change")
                        .setCancelable(true)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            if (addedTags.isEmpty()) {
                                Toast.makeText(SettingsActivity.this, "There's nothing to save", Toast.LENGTH_SHORT).show();
                            } else {
                                saveTags();
                            }
                            dialogInterface.cancel();
                        }).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel())
                        .show();
                break;
        }
    }

    private void saveTags() {
        DatabaseReference tagsRef = Tag.collection();


        // Run Firebase transaction.
        tagsRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                GenericTypeIndicator<HashMap<String, Tag>> typeIndicator = new GenericTypeIndicator<HashMap<String, Tag>>() {
                };
                HashMap<String, Tag> tags = mutableData.getValue(typeIndicator);

                if (tags == null) {
                    tags = new HashMap<>();
                }

                while (!addedTags.isEmpty()) {
                    Pair<Boolean, Tag> entry = addedTags.poll();

                    if (entry.first) {
                        // If adding a new entry.
                        tags.put(tagsRef.push().getKey(), entry.second);
                    } else {
                        // If removing an entry.
                        if (entry.second.getId() == null) {
                            // If there is no ID find by label.
                            for (Map.Entry<String, Tag> tag : tags.entrySet()) {
                                if (tag.getValue().getLabel().equals(entry.second.getLabel())) {
                                    tags.remove(tag.getKey());
                                    break;
                                }
                            }
                        } else {
                            tags.remove(entry.second.getId().toString());
                        }
                    }
                }


                // Set value and report transaction success.
                mutableData.setValue(tags);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.d("Firebase Write", "postTransaction:onComplete:" + databaseError);

                if (databaseError == null) {
                    Toast.makeText(SettingsActivity.this, "All data saved successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Failed to save tags.", Toast.LENGTH_SHORT).show();
                }

                onBackPressed();
            }
        });

    }

    private boolean validateNewTag() {
        String message = "";

        if (editInfoTextView.getText().toString().equals("")) {
            message += "Empty tag info\n";
        }

        if (editLabelTextView.getText().toString().equals("")) {
            message += "Empty tag label\n";
        } else {
            for (Chip chip : chipsInput.getSelectedChips()) {
                if (chip.getTitle().equals(editLabelTextView.getText().toString())) {
                    message += "Label already exists\n";
                    break;
                }
            }
        }

        if (editColorTextView.getText().toString().equals("")) {
            message += "Empty tag color\n";
        } else if (!Utility.isValidColor(editColorTextView.getText().toString())) {
            message += "Invalid tag color\n";
        }

        if (!message.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error in data.")
                    .setCancelable(false)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage("Fix the following issues:\n\n" + message)
                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.cancel())
                    .show();

            return false;
        } else {
            return true;
        }
    }
}