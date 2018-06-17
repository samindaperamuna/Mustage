package me.a01eg.canyon.mustage;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import me.a01eg.canyon.mustage.adapters.PaginationAdapter;
import me.a01eg.canyon.mustage.model.Chat;
import me.a01eg.canyon.mustage.model.Message;
import me.a01eg.canyon.mustage.model.User;

/**
 * The list of messages of selected room will be displayed here. As soon as new message
 * added it will be present on bottom of the list.
 *
 * Created on 21/07/2017.
 * Copyright by oleg
 */

public class MessagesFragment extends Fragment implements View.OnClickListener, TextWatcher {

    private String mUserId;
    private String mTitle;
    private RecyclerView mList;
    private PaginationAdapter mAdapter;
    private String mChatKey = "";
    private EditText mMessageBox;
    private View mSendButton;
    private DatabaseReference refMessages;
    private LinearLayoutManager mLayoutManager;

    public MessagesFragment() {
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    @SuppressLint("ValidFragment")
    public MessagesFragment(String chatId, String userId, String title) {
        mUserId = userId;
        mChatKey = chatId;
        mTitle = title;
        refMessages = User.messages(mChatKey);

        if (userId != null) {
            findChat(userId, new ChatCompleteListener() {
                @Override
                public void onChat(String chatId) {
                    mChatKey = chatId;
                    loadMessages();
                }

                @Override
                public void onNotFound() {
                }
            });
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_messages_list, null);

        mMessageBox = view.findViewById(R.id.et_message_box);
        mMessageBox.addTextChangedListener(this);
        mSendButton = view.findViewById(R.id.btn_send);
        mSendButton.setOnClickListener(this);

        mList = view.findViewById(R.id.list);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setStackFromEnd(true);
        mLayoutManager.setReverseLayout(true); // scroll to bottom
        mList.setLayoutManager(mLayoutManager);
        mList.setItemAnimator(new DefaultItemAnimator());

        loadMessages();
        getActivity().setTitle(mTitle);

        mMessageBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus && mAdapter != null){
                    scrollToBottom();
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    private void scrollToBottom() {
        mList.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLayoutManager.smoothScrollToPosition(mList, null, 0);
            }
        }, 100);
    }

    private void loadMessages() {
        if (mAdapter == null) {

            if (User.current() != null && mChatKey != null) {
                DatabaseReference ref = refMessages;

                mAdapter = new PaginationAdapter<MessageViewHolder>(MessageViewHolder.class, R.layout.item_message, ref) {
                    @Override
                    protected void populateViewHolder(MessageViewHolder viewHolder, DataSnapshot snapshot, int position) {
                        Message message = snapshot.getValue(Message.class);
                        message.setId(snapshot.getKey());
                        viewHolder.showMessage(message);
                    }
                };

                mList.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            } else {
                // show empty list
            }

            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    scrollToBottom();
                }
            });
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void findChat(String userId, final ChatCompleteListener listener) {
        // create chat if not created yet
        if (userId != null) {
            User.chats().orderByChild("contact").equalTo(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snap : dataSnapshot.getChildren()) {
                                loadMessages();
                                listener.onChat(snap.getKey());
                                return;
                            }

                            listener.onNotFound();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            listener.onNotFound();
                        }
                    });

        }
    }

    private void sendMessage(final String message) {

        // create chat if not created yet
        if (mChatKey == null && mUserId != null) {
            findChat(mUserId, new ChatCompleteListener() {
                @Override
                public void onChat(String chatId) {
                    mChatKey = chatId;
                    sendMessage(message);
                }

                @Override
                public void onNotFound() {
                    final DatabaseReference ref = User.chats().push();
                    mChatKey = ref.getKey();
                    loadMessages();

                    User.collection(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot userSnapshot) {
                            final User user = userSnapshot.getValue(User.class);

                            User.current().addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot curSnapshot) {
                                    User curUser = curSnapshot.getValue(User.class);

                                    // update chat data
                                    Chat chat = new Chat();
                                    chat.setName(user.getName());
                                    chat.setContact(userSnapshot.getKey());
                                    chat.setProfile(user.getPhoto());
                                    ref.setValue(chat);

                                    DatabaseReference refContact = FirebaseDatabase.getInstance()
                                            .getReference(Const.kChatsKey).child(userSnapshot.getKey());
                                    // update chat data
                                    Chat chat2 = new Chat();
                                    chat2.setName(curUser.getName());
                                    chat2.setContact(curSnapshot.getKey());
                                    chat2.setProfile(curUser.getPhoto());
                                    refContact.setValue(chat2);

                                    sendMessage(message);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });

        } else if (mChatKey != null) {
            // reload messages
            loadMessages();

            DatabaseReference reference = refMessages;
            // create the record
            DatabaseReference messageRef = reference.push();
            // update values
            messageRef.setValue(new Message(message, User.currentKey()));
            // load new element if needed
            mAdapter.notifyDataSetChanged();
            // scroll to bottom
            scrollToBottom();
        } else {
            // error!
        }
    }

    interface ChatCompleteListener {
        void onChat(String chatId);

        void onNotFound();
    }

    // OnClickListener

    @Override
    public void onClick(View v) {

        // send message and empty it
        String message = mMessageBox.getText().toString();

        if (!message.trim().isEmpty()) {
            sendMessage(message);
            mMessageBox.setText(null);
        }
    }

    // TextWatcher

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        // enable/disable the button if text is empty
        mSendButton.setEnabled(!s.toString().trim().isEmpty());
    }
}
