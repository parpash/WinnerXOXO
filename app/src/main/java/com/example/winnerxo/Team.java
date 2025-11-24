package com.example.winnerxo;

public class Team {

    private String teamid;
    private String url;

    // Constructor ריק
    public Team() {
    }

    // Constructor עם כל השדות (אין פה רשימות)
    public Team(String teamid, String url) {
        this.teamid = teamid;
        this.url = url;
    }

    public String getTeamid() {
        return teamid;
    }

    public void setTeamid(String teamid) {
        this.teamid = teamid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}


