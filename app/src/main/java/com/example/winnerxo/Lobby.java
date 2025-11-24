package com.example.winnerxo;

import java.util.List;

public class Lobby {

    private List<Game> games;
    private User user;

    public Lobby() {
    }

    public Lobby(User user) {
        this.user = user;
    }

    public List<Game> getGames() {
        return games;
    }

    public void setGames(List<Game> games) {
        this.games = games;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
