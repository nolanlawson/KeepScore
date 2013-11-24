package com.nolanlawson.keepscore.db;

import java.util.Comparator;
import java.util.List;

import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;
import com.nolanlawson.keepscore.util.Functions;
import com.nolanlawson.keepscore.util.LongUtil;

/**
 * Smaller POJO representing a "Game" object.  Designed so that the main page, with its list of all games,
 * can be very fast.
 * @author nolan
 *
 */
public class GameSummary {

    private int id = -1;
    private String name;
    private List<String> playerNames;
    private int numRounds;
    private long dateSaved;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<String> getPlayerNames() {
        return playerNames;
    }
    public void setPlayerNames(List<String> playerNames) {
        this.playerNames = playerNames;
    }
    
    public int getNumRounds() {
        return numRounds;
    }
    public void setNumRounds(int numRounds) {
        this.numRounds = numRounds;
    }
    public long getDateSaved() {
        return dateSaved;
    }
    public void setDateSaved(long dateSaved) {
        this.dateSaved = dateSaved;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GameSummary other = (GameSummary) obj;
        if (id != other.id)
            return false;
        return true;
    }
    
    public static Comparator<GameSummary> byRecentlySaved() {
        return new Comparator<GameSummary>() {

            @Override
            public int compare(GameSummary object1, GameSummary object2) {
                return LongUtil.compare(object2.getDateSaved(), object1.getDateSaved());
            }
        };
    }    
    
    public static GameSummary fromGame(Game game) {
        GameSummary result = new GameSummary();
        result.setId(game.getId());
        result.setName(game.getName());
        result.setDateSaved(game.getDateSaved());
        result.setPlayerNames(CollectionUtil.transform(game.getPlayerScores(), new Function<PlayerScore, String>(){
            @Override
            public String apply(PlayerScore obj) {
                return obj.getName();
            }
        }));
        result.setNumRounds(CollectionUtil.max(game.getPlayerScores(), Functions.PLAYER_SCORE_TO_HISTORY_SIZE));
        return result;
    }
    
    public static final Function<GameSummary, Integer> GET_ID = new Function<GameSummary, Integer>() {

        @Override
        public Integer apply(GameSummary obj) {
            return obj.getId();
        }
    };
}
