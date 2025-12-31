package com.example.winnerxo;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private String playerid;
    private List<String> teams; // ✅ עכשיו זה סטרינגים של teamid

    // Constructor ריק (חובה ל-Firebase)
    public Player() {
        teams = new ArrayList<>();
    }

    // Constructor עם שם
    public Player(String playerid) {
        this.playerid = playerid;
        this.teams = new ArrayList<>();
    }

    public String getPlayerid() {
        return playerid;
    }

    public void setPlayerid(String playerid) {
        this.playerid = playerid;
    }

    public List<String> getTeams() {
        return teams;
    }

    public void setTeams(List<String> teams) {
        this.teams = teams;
    }
}
