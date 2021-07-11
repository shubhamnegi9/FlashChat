package com.shubhamnegi.flashchatnewfirebase;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class ChatListAdapter extends BaseAdapter {

    // Base Adapter --> https://developer.android.com/reference/android/widget/BaseAdapter

    // Member Variables
    private Activity mActivity;
    private DatabaseReference mDatabaseReference;
    private String mDisplayName;
    private ArrayList<DataSnapshot> mSnapshotList;

    /* DataSnapshot is a type used by Firebase for passing the data from database back to our app.
        Every time we read from a cloud database, we receive the data in a form of DataSnapshot.
        DataSnapshot --> https://firebase.google.com/docs/reference/android/com/google/firebase/database/DataSnapshot
     */

    /*
        ChildEventListener is a listener that will get notified if there have been any changes to the database.
        For eg. when someone sends a message and new data gets added to our database, that qualifies as a change
        and our listener will report back.
        ChildEventListener --> https://firebase.google.com/docs/reference/android/com/google/firebase/database/ChildEventListener
     */

    private ChildEventListener mListener = new ChildEventListener() {
        // Fired when new chat message is added to the database
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            mSnapshotList.add(dataSnapshot);
            notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    // Constructor of ChatListAdapter
    public ChatListAdapter(Activity activity, DatabaseReference ref, String name) {

        mActivity = activity;
        mDisplayName = name;
        // common error: typo in the db location. Needs to match what's in MainChatActivity.
        mDatabaseReference = ref.child("messages");
        mDatabaseReference.addChildEventListener(mListener);

        mSnapshotList = new ArrayList<>();
    }

    // Inner class --> Class inside a class
    private static class ViewHolder{
        TextView authorName;
        TextView body;
        LinearLayout.LayoutParams params;
    }

    /**
     * Overriding Methods of Base Adapter
     */

    @Override
    public int getCount() {
        return mSnapshotList.size();
    }

    @Override
    // Returning item of type InstantMessage
    public InstantMessage getItem(int position) {

        DataSnapshot snapshot = mSnapshotList.get(position);
        // The DataSnapshot actually comes in the form of JSON and contains our chat message data.
        return snapshot.getValue(InstantMessage.class);     // converts the JSON from the snapshot into an InstantMessage object
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        /*
         Creating an individual row from scratch for each list item is computationally expensive, and it would be a crappy user experience if our phone starts lagging as we are scrolling through the list.
         One way to avoid a lag is to load up the entire list into memory. However, if we have a list of thousand items, we cannot possibly load all these rows at once.

         So the solution is, as soon as a row scroll out of the sight, we need to keep hold of the view that make up that row. And when a new row scrolls on the screen,
         we will supply that row with the view that we've used before, but we will populate the view wit new data.

         Reconfiguring an existing row has a big performance advantage because we don't constantly create and destroy the same type of object.
         */

        /*
          Checking if there's an existing row that can be reused :
          The convertView here represents a list item. If it exists (convertView != null) , we can reconfigure it.
          And if it does not exists (convertView == null), then we have to create a new row from the layout file.
        */

        if (convertView == null) {
            /*
                Creating new row from scratch
             */


            // To create a view from a layout XML file, we need a component called the layoutInflater
            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // With the inflate method, we will supply our chat_msg_row.xml file and stick that into the convertView.
            convertView = inflater.inflate(R.layout.chat_msg_row, parent, false);

            // This is the inner helper class that will hold onto all the things that make up an individual chat_msg_row.
            final ViewHolder holder = new ViewHolder();

            // Linking the fields of the ViewHolder to the views in the chat_msg_row
            holder.authorName = convertView.findViewById(R.id.author);
            holder.body = convertView.findViewById(R.id.message);
            holder.params = (LinearLayout.LayoutParams) holder.authorName.getLayoutParams();

            /* Finally we need to give the adapter a way of storing our ViewHolder for a short period of time so that we can reuse it later.
                Reusing the ViewHolder will allow us to avoid calling findViewById() method.
                Using the setTag() method to temporarily store our ViewHolder in the convertView
            */
            convertView.setTag(holder);

        }

        /**
         *  No need to create a new row from the scratch
         */

        // Getting the InstantMessage at the current position in the list
        final InstantMessage message = getItem(position);

        // Using the getTag() method in order to retrieve the ViewHolder that was temporarily saved in the convertView
        final ViewHolder holder = (ViewHolder) convertView.getTag();

        // For checking which user have sent the chat and style the chat messages accordingly
        boolean isMe = message.getAuthor().equals(mDisplayName);
        setChatRowAppearance(isMe, holder);

        /* The viewHolder that we just fetched from the convertView is still going to have the old data in it from the previous time that it was used.
            So we're going to change that by replacing the old data.
        */
        String author = message.getAuthor();
        holder.authorName.setText(author);

        String msg = message.getMessage();
        holder.body.setText(msg);

        // If convertView != null, return convertView
        return convertView;
    }

    // Method for styling the chat messages
    private void setChatRowAppearance(boolean isItMe, ViewHolder holder) {

        if (isItMe) {

            //  If message belongs to the sending user, change the layout of entire row to align to the right and change the color of the Author name to green

            holder.params.gravity = Gravity.END;
            holder.authorName.setTextColor(Color.GREEN);

            // If you want to use colours from colors.xml
            // int colourAsARGB = ContextCompat.getColor(mActivity.getApplicationContext(), R.color.yellow);
            // holder.authorName.setTextColor(colourAsARGB);

            // Wrapping chat message in speech bubble
            holder.body.setBackgroundResource(R.drawable.bubble2);  // bubble2.9.png --> Image file of type 9 patch
        } else {

            //  If message belongs to the other user, change the layout of entire row to align to the left and change the color of the Author name to blue
            holder.params.gravity = Gravity.START;
            holder.authorName.setTextColor(Color.BLUE);
            // Wrapping chat message in speech bubble
            holder.body.setBackgroundResource(R.drawable.bubble1);  // bubble1.9.png --> Image file of type 9 patch
        }

        holder.authorName.setLayoutParams(holder.params);
        holder.body.setLayoutParams(holder.params);

        /*
            9 patch image file defines a set of pixels which can be stretched in any direction.
            It helps in creating a speech bubble effect and cover the entire chat message regardless if the message is short or long

            9 Patch Graphics --> https://developer.android.com/guide/topics/graphics/drawables#nine-patch
            Creating 9 Patch Graphics --> https://developer.android.com/studio/write/draw9patch
         */
    }


    /*
        Calling method to stop checking for new events on the firebase's database so that we can free up resources when we don't need them anymore
     */
    void cleanup() {
        // Removes the firebase event listener when the app leaves the foreground
        mDatabaseReference.removeEventListener(mListener);
    }


}
