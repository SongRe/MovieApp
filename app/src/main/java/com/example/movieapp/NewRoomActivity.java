package com.example.movieapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class NewRoomActivity extends AppCompatActivity {
    private Button createButton;
    private EditText numParticipants;
    private int participantCount;
    private String roomName;
    FirebaseFirestore db;
    CollectionReference rooms;
    private int movieCount;
    public final static String ROOM_TAG = "com.example.movieapp.ROOM_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_room);
        createButton = findViewById(R.id.createRoomButton);
        numParticipants = findViewById(R.id.editTextParticipants);
        createButton.setActivated(false); //false by default
        db = FirebaseFirestore.getInstance();
        rooms = db.collection("rooms"); //we're creating documents in the "rooms" collection
        db.collection("movies")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            movieCount = 0;
                            for (DocumentSnapshot document : task.getResult()) {
                                movieCount++;
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });

        numParticipants.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {



            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = numParticipants.getText().toString();
                try {
                    int num =  Integer.parseInt(text);
                    createButton.setActivated(true); //activate the button, since the text is a button
                    participantCount = num;

                } catch (Exception e) {
                    Log.e("ERROR", e.toString());
                    Toast.makeText(NewRoomActivity.this, R.string.num_input_error, Toast.LENGTH_LONG).show();
                }
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewRoomOnDatabase(); //begin next activity and create a room on the database
            }
        });



    }

    //this should start the waiting activity for the host
    private void startWaitActivity(String roomName) {
        Intent intent = new Intent(this, HostWaitActivity.class);
        intent.putExtra(ROOM_TAG, roomName);
        startActivity(intent); //go to waiting room
        this.finish();
    }

    //this method creates all the data we need for a new room on FireStore. it also begins the next activity
    private void createNewRoomOnDatabase() {
        int code = new Random().nextInt(900000) + 100000;; //generate random 6-digit user room key
        roomName = String.valueOf(code); //transform into a string for use
        List<Long> voteCountArr = new ArrayList<>();
        for(int i = 0; i < movieCount; i++) {
            voteCountArr.add(((long)0));
        }
        Map<String, Object> data = new HashMap<>(); //creating the data map
        data.put("roomSize", participantCount);
        data.put("activeMembers", 1);
        data.put("isSwiping", false); // the room will not be in swiping phase by default
        data.put("voteCountArray", voteCountArr);
        data.put("isEnded", false);


        //TODO: Implement the movies index into the room
        Log.d("TAG", "room name: " + roomName);
        rooms.document(roomName).set(data); //this puts the data onto the database
        startWaitActivity(roomName);

    }
}