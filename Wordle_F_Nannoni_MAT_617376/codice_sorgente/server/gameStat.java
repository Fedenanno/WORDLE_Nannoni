/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author fedenanno
 */
public class gameStat {
    
    private String username;
    private Integer trys;
    private Boolean wins;
    

    public gameStat(String username, Integer trys, Boolean wins) {
        this.username = username;
        this.trys = trys;
        this.wins = wins;
        
    }
    
    public gameStat(String username, Integer tryes){
        this.username = username;
        this.trys = tryes;
        this.wins = false;
        
    }

    public gameStat(String username) {
        this.username = username;
        this.trys = 0;
        this.wins = false;
        
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getTrys() {
        return trys;
    }

    public void setTrys(Integer trys) {
        this.trys = trys;
    }

    public Boolean getWins() {
        return wins;
    }

    public void setWins(Boolean wins) {
        this.wins = wins;
    } 
    
}
