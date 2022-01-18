/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author Admin
 */
public class User {
    private int ID;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private int numberOfGame;
    private int numberOfwin;
    private int numberOfDraw;
    private boolean isOnline;
    private boolean isPlaying;
    private int rank;

    public int getRank() {
        return rank;
    }

    public User() {
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public User(int ID, String username, String password, String nickname, String avatar, int numberOfGame, int numberOfwin, int numberOfDraw, boolean isOnline, boolean isPlaying, int rank) {
        this.ID = ID;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.avatar = avatar;
        this.numberOfGame = numberOfGame;
        this.numberOfwin = numberOfwin;
        this.numberOfDraw = numberOfDraw;
        this.isOnline = isOnline;
        this.isPlaying = isPlaying;
        this.rank = rank;
    }
    
    
    
    
    public User(int ID, String username, String password, String nickname, String avatar, int numberOfGame, int numberOfwin, int numberOfDraw, boolean isOnline, boolean isPlaying) {
        this.ID = ID;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.avatar = avatar;
        this.numberOfGame = numberOfGame;
        this.numberOfwin = numberOfwin;
        this.numberOfDraw = numberOfDraw;
        this.isOnline = isOnline;
        this.isPlaying = isPlaying;
    }

    public User(int ID, String nickname) {
        this.ID = ID;
        this.nickname = nickname;
    }

    

    public User(int ID, String nickname, boolean isOnline, boolean isPlaying) {
        this.ID = ID;
        this.nickname = nickname;
        this.isOnline = isOnline;
        this.isPlaying = isPlaying;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String username, String password, String nickname, String avatar) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.avatar = avatar;
    }

    public int getID() {
        return ID;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public int getNumberOfGame() {
        return numberOfGame;
    }

    public int getNumberOfwin() {
        return numberOfwin;
    }

    public boolean getIsOnline() {
        return isOnline;
    }

    public boolean getIsPlaying() {
        return isPlaying;
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setNumberOfGame(int numberOfGame) {
        this.numberOfGame = numberOfGame;
    }

    public void setNumberOfwin(int numberOfwin) {
        this.numberOfwin = numberOfwin;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public User(int ID, String nickname, int numberOfGame, int numberOfDraw) {
        this.ID = ID;
        this.nickname = nickname;
        this.numberOfGame = numberOfGame;
        this.numberOfDraw = numberOfDraw;
    }

    public int getNumberOfDraw() {
        return numberOfDraw;
    }

    public void setNumberOfDraw(int numberOfDraw) {
        this.numberOfDraw = numberOfDraw;
    }

    
    
}
