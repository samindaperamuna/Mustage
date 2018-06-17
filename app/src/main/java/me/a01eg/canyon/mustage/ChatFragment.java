package me.a01eg.canyon.mustage;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import me.a01eg.canyon.mustage.adapters.PaginationAdapter;
import me.a01eg.canyon.mustage.model.Chat;
import me.a01eg.canyon.mustage.model.User;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class ChatFragment extends Fragment {

    public ChatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        DatabaseReference ref = User.chats();

        PaginationAdapter adapter = new PaginationAdapter<ChatViewHolder>(ChatViewHolder.class, R.layout.chat_item, ref) {
            @Override
            protected void populateViewHolder(ChatViewHolder viewHolder, final DataSnapshot snapshot, int position) {
                final Chat chat = snapshot.getValue(Chat.class);
                chat.setId(snapshot.getKey());

                viewHolder.showChat(chat);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openChat(snapshot.getKey(), chat.getName());
                    }
                });
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Messages");

        return view;
    }

    private void openChat(String snapshotKey, String name) {
        MessagesActivity.start(getActivity(), snapshotKey, name);
    }
}
