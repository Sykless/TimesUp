package com.timesup.fra.timesup;

import android.app.Application;
import java.util.ArrayList;

public class TimesUpParameters extends Application
{
    ArrayList<String> cardList = new ArrayList<>();
    ArrayList<Team> teamList = new ArrayList<>();
    ArrayList<String> currentCardList = new ArrayList<>();
    long checkpoint = -1;

    public long getCardFirstID() {
        return cardFirstID;
    }

    public void setCardFirstID(long cardFirstID) {
        this.cardFirstID = cardFirstID;
    }

    long cardFirstID = 0;

    public ArrayList<String> getCurrentCardList() {
        return currentCardList;
    }

    public void setCurrentCardList(ArrayList<String> currentCardList) {
        this.currentCardList = currentCardList;
    }

    public ArrayList<Team> getTeamList()
    {
        return teamList;
    }

    public void setTeamList(ArrayList<Team> teamList)
    {
        this.teamList = teamList;
    }

    public ArrayList<String> getCardList()
    {
        return cardList;
    }

    public void setCardList(ArrayList<String> cardList)
    {
        this.cardList = cardList;
    }

    public long getCheckpoint()
    {
        return checkpoint;
    }

    public void setCheckpoint(long checkpoint)
    {
        this.checkpoint = checkpoint;
    }
}
