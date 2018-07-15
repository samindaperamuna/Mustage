package com.fifthgen.mustage.model;

import com.google.firebase.database.DatabaseReference;

/**
 * Created on 06/09/2017.
 * Copyright by oleg
 */

public class Chat {
    private static final String TAG = "Chat";
    private String id;
    private String contact;
    private String name;
    private String profile;

    public Chat() {

    }

    public Chat(String key) {
        this.id = key;
    }

    public static Chat create(final User user) {

        // create chat for me
        DatabaseReference chatRef = User.chats().push();
        Chat chat = new Chat();
        chat.setName(user.getName());
        chat.setProfile(user.getPhoto());
        chat.setContact(user.getId());
        chatRef.setValue(chat);

//        // create chat for contact
//        current.loadData(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                DatabaseReference recentRef = user.chats().push();
//                Chat recent = new Chat();
////                recent.setName(current.getName());
////                recent.setProfile(current.getPhoto());
////                recent.setContact(current.getId());
//                recentRef.setLabel(recent);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.e(TAG, databaseError.toString());
//            }
//        });

        return chat;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
