package com.example.winnerxo;

import java.util.List;

public class Game {

    private String gameid;
    private User user1;
    private User user2;
    private List<Team> teams;

    public Game() {
    }

    public Game(String gameid, User user1, User user2) {
        this.gameid = gameid;
        this.user1 = user1;
        this.user2 = user2;
    }

    public String getGameid() {
        return gameid;
    }

    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }
}
