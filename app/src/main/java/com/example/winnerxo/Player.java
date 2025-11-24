package com.example.winnerxo;

import java.util.List;

public class Player {

    private String playerid;
    private List<Team> teams;

    public Player() {
    }

    public Player(String playerid) {
        this.playerid = playerid;
    }

    public String getPlayerid() {
        return playerid;
    }

    public void setPlayerid(String playerid) {
        this.playerid = playerid;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }
}
