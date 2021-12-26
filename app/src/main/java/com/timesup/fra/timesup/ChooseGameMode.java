package com.timesup.fra.timesup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChooseGameMode extends AppCompatActivity
{
    TimesUpParameters app;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_game_mode_layout);
    }

    public void goToClassic(View view)
    {
        intent = new Intent(this,PhaseSetup.class);
        intent.putExtra("phaseNumber",1);
        startActivity(intent);
    }

    public void goToPersonnalize(View view)
    {
        Intent intent = new Intent(this,CreateParty.class);
        startActivity(intent);
    }
}
