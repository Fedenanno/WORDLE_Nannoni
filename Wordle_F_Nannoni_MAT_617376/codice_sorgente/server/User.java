
import java.util.ArrayList;
import java.util.Collections;

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
    private Integer gamePlayed;
    private Integer wins;
    private Double winRate;
    private Integer lastStreak;
    private Integer bestStreak;
    //distribuzione distribuzione di tentativi impiegati per arrivare alla soluzione del gioco,
    //in ogni partita vinta dal giocatore
    private ArrayList<Integer> distribution;
    private boolean attivo;
    

    User(String username, String password, Integer gamePlayed, Double win, Integer lastStreak, Integer bestStreak, ArrayList<Integer> distribution){
        this.username = username;
        this.password = password;
        this.gamePlayed = gamePlayed;
        this.winRate = win;
        this.lastStreak = lastStreak;
        this.bestStreak = bestStreak;
        this.distribution = distribution;
    }

    User(String username, String password){
        this.username = username;
        this.password = password;
        this.gamePlayed = 0;
        this.wins = 0;
        this.winRate = 0.0;
        this.lastStreak = 0;
        this.bestStreak = 0;
        //inizializza l'array con 12 elementi a 0
        this.distribution = new ArrayList<>(Collections.nCopies(12, 0));
        this.attivo = false;
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

    public Double getWinRate() {
        return winRate;
    }

    public Integer getLastStreak() {
        return lastStreak;
    }

    public Integer getBestStreak() {
        return bestStreak;
    }

    public ArrayList<Integer> getDistribution() {
        return distribution;
    }
    
    public Integer getDistribution(Integer tryes){
        return this.distribution.get(tryes-1);
    }

    public Integer getGamePlayed() {
        return gamePlayed;
    }

    public Integer getWins() {
        return wins;
    }
    
    //_----------_

    public void setUsername(String username) {
        this.username = username;
    }

    public void setWinRate(Double winRate) {
        this.winRate = winRate;
    }

    public void setLastStreak(Integer lastStreak) {
        this.lastStreak = lastStreak;
    }

    public void setBestStreak(Integer bestStreak) {
        this.bestStreak = bestStreak;
    }

    public void setDistribution(Integer tryes) {
        this.distribution.set(tryes-1, this.distribution.get(tryes-1)+1);  
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAttivo() {
        return attivo;
    }

    public void setAttivo(boolean attivo) {
        this.attivo = attivo;
    }

    public void setGamePlayed(Integer gamePlayed) {
        this.gamePlayed = gamePlayed;
    }

    public void setWins(Integer wins) {
        this.wins = wins;
    }
    
    

    public void setWinsRate(){
        try{
            this.winRate = Double.valueOf(this.wins ) / Double.valueOf(this.gamePlayed);
        }
        catch(java.lang.ArithmeticException e){}
    }
    

}
