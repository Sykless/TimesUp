package com.timesup.fra.timesup;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class PhaseTwoThree extends AppCompatActivity
{
    int currentCard = 0;
    int timer;
    int finalTimer;
    int bubble;
    int teamNumber;
    int phaseNumber;
    boolean hasClicked = false;
    String timerString;
    String lastCardName = "";

    TextView cardName;
    ArrayList<String> cardList;
    ArrayList<String> validateCards = new ArrayList<>();
    ArrayList<Team> teamList;
    LinearLayout bubbleList;
    Button validateButton;
    Button passButton;

    TimesUpParameters app;
    Handler timerHandler;
    Handler hideValidate = new Handler();
    Handler launchBeginPhase = new Handler();

    MediaPlayer validateSound;
    MediaPlayer passSound;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phase_two_three_layout);

        app = (TimesUpParameters) getApplicationContext();
        cardList = app.getCurrentCardList();
        teamList = app.getTeamList();
        bubbleList = findViewById(R.id.bubbleList);
        validateButton = findViewById(R.id.validateButton);
        passButton = findViewById(R.id.passButton);
        cardName = findViewById(R.id.cardName);
        cardName.setText(cardList.remove(0));

        validateSound = MediaPlayer.create(this, R.raw.coin);
        passSound = MediaPlayer.create(this, R.raw.whoosh);

        Intent intent = getIntent();
        timerString = intent.getStringExtra("timer");
        teamNumber = intent.getIntExtra("teamNumber",0);
        phaseNumber = intent.getIntExtra("phaseNumber",2);
        String[] timerParts = timerString.split(":");

        finalTimer = 60*Integer.valueOf(timerParts[0]) + Integer.valueOf(timerParts[1]);
        timer = finalTimer;
        bubble = timer / 12;

        timerHandler = new Handler();
        timerHandler.postDelayed(new Runnable()
        {
            public void run()
            {
                timer--;

                int id = (timer + bubble - 1) / bubble;

                if (id < 12)
                {
                    bubbleList.getChildAt(id).setVisibility(View.INVISIBLE);
                }

                if (timer == 0)
                {
                    lastCardName = cardName.getText().toString();
                    cardName.setText("Terminé !");

                    passButton.getBackground().setAlpha(128);
                    passButton.setClickable(false);

                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    {
                        v.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE));
                    }
                    else
                    {
                        v.vibrate(2000); // Deprecated in API 26
                    }

                    hideValidate = new Handler();
                    launchBeginPhase = new Handler();

                    hideValidate.postDelayed(new Runnable()
                    {
                        public void run()
                        {
                            validateButton.getBackground().setAlpha(128);
                            validateButton.setClickable(false);

                            if (!hasClicked)
                            {
                                cardList.add(lastCardName);
                                app.setCurrentCardList(cardList);
                            }

                            if (phaseNumber == 2)
                            {
                                teamList.get(teamNumber).setScorePhaseTwo(validateCards.size() + teamList.get(teamNumber).getScorePhaseTwo());
                            }
                            else
                            {
                                teamList.get(teamNumber).setScorePhaseThree(validateCards.size() + teamList.get(teamNumber).getScorePhaseThree());
                            }

                            teamNumber = (teamNumber + 1) % teamList.size();
                        }
                    },2000);

                    launchBeginPhase.postDelayed(new Runnable()
                    {
                        public void run()
                        {
                            Intent intent = new Intent(getApplicationContext(),PhaseBegin.class);
                            intent.putExtra("phaseNumber",phaseNumber);
                            intent.putExtra("teamNumber",teamNumber);
                            intent.putExtra("timer",timerString);
                            startActivity(intent);
                            finish();
                        }
                    },5000);
                }
                else
                {
                    timerHandler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    @Override
    public void onBackPressed()
    {
        return;
    }

    public void clickValidate(View view)
    {
        nextCard(true);
    }

    public void clickPass(View view)
    {
        nextCard(false);
    }

    public void nextCard(boolean validate)
    {
        if (validate)
        {
            validateSound.start();

            if (lastCardName.length() > 0)
            {
                hasClicked = true;

                validateCards.add(lastCardName);
                validateButton.getBackground().setAlpha(128);
                validateButton.setClickable(false);
            }
            else
            {
                validateCards.add(cardName.getText().toString());
            }
        }
        else
        {
            passSound.start();
            cardList.add(cardName.getText().toString());
        }

        if (cardList.size() > 0)
        {
            if (lastCardName.length() == 0)
            {
                cardName.setText(cardList.remove(0));
            }
        }
        else
        {
            cardName.setText("Fin de la manche !");
            validateButton.setVisibility(View.INVISIBLE);
            passButton.setVisibility(View.INVISIBLE);
            bubbleList.setVisibility(View.INVISIBLE);

            if (phaseNumber == 2)
            {
                teamList.get(teamNumber).setScorePhaseTwo(validateCards.size() + teamList.get(teamNumber).getScorePhaseTwo());
            }
            else
            {
                teamList.get(teamNumber).setScorePhaseThree(validateCards.size() + teamList.get(teamNumber).getScorePhaseThree());
            }

            timerHandler.removeCallbacksAndMessages(null);
            hideValidate.removeCallbacksAndMessages(null);
            launchBeginPhase.removeCallbacksAndMessages(null);

            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            else
            {
                v.vibrate(1000); // Deprecated in API 26
            }

            new Handler().postDelayed(new Runnable()
            {
                public void run()
                {
                    if ( timer / (double) finalTimer < 0.75)
                    {
                        teamNumber = (teamNumber + 1) % teamList.size();
                    }

                    Intent intent;

                    if (phaseNumber == 2)
                    {
                        intent = new Intent(getApplicationContext(),PhaseSetup.class);
                        intent.putExtra("phaseNumber",3);
                        intent.putExtra("teamNumber",teamNumber);
                    }
                    else
                    {
                        intent = new Intent(getApplicationContext(),Scoreboard.class);
                    }

                    startActivity(intent);
                    finish();
                }
            },5000);
        }

        System.out.println("Cards : (" + cardList.size() + ") : " +  cardList);
    }
}
