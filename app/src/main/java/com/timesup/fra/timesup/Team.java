package com.timesup.fra.timesup;

public class Team
{
    Team(String name)
    {
        teamName = name;
    }

    String teamName;
    int scorePhaseOne = 0;
    int scorePhaseTwo = 0;
    int scorePhaseThree = 0;

    public String getTeamName() {
        return teamName;
    }
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public int getScorePhaseOne() {
        return scorePhaseOne;
    }
    public void setScorePhaseOne(int scorePhaseOne) {
        this.scorePhaseOne = scorePhaseOne;
    }

    public int getScorePhaseTwo() {
        return scorePhaseTwo;
    }
    public void setScorePhaseTwo(int scorePhaseTwo) {
        this.scorePhaseTwo = scorePhaseTwo;
    }

    public int getScorePhaseThree() {
        return scorePhaseThree;
    }
    public void setScorePhaseThree(int scorePhaseThree) {
        this.scorePhaseThree = scorePhaseThree;
    }

    public String teamInfo()
    {
        return "Team " + teamName + " with score " + scorePhaseOne + "/" + scorePhaseTwo + "/" + scorePhaseThree;
    }
}
