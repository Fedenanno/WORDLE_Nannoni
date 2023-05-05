/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author fedenanno
 * Questo file è di proprietà di: fedenanno, ogni suo utilizzo va 
 * concordato con l'autore.
 * Creato in data: 
 * 
 */

public class User {
    private String username;
    private String password;
    private Integer win;
    private Integer lastStreak;
    private Integer bestStreak;
    //distribuzione distribuzione di tentativi impiegati per arrivare alla soluzione del gioco,
    //in ogni partita vinta dal giocatore
    private Integer[] distribution;
    

    User(String username, String password, Integer win, Integer lastStreak, Integer bestStreak, Integer[] distribution){
        this.username = username;
        this.password = password;
        this.win = win;
        this.lastStreak = lastStreak;
        this.bestStreak = bestStreak;
        this.distribution = distribution;
    }

    User(String username, String password){
        this.username = username;
        this.password = password;
        this.win = 0;
        this.lastStreak = 0;
        this.bestStreak = 0;
    }

    User() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //crea tutti i get e set
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Integer getWin() {
        return win;
    }

    public Integer getLastStreak() {
        return lastStreak;
    }

    public Integer getBestStreak() {
        return bestStreak;
    }

    public Integer[] getDistribution() {
        return distribution;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setWin(Integer win) {
        this.win = win;
    }

    public void setLastStreak(Integer lastStreak) {
        this.lastStreak = lastStreak;
    }

    public void setBestStreak(Integer bestStreak) {
        this.bestStreak = bestStreak;
    }

    public void setDistribution(Integer[] distribution) {
        this.distribution = distribution;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    

}
