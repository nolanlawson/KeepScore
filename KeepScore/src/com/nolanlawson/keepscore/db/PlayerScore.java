package com.nolanlawson.keepscore.db;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.helper.PlayerColor;
import com.nolanlawson.keepscore.helper.PreferenceHelper;
import com.nolanlawson.keepscore.util.Pair;

/**
 * Main representation for a player in a particular game and his/her score and
 * score history.
 * 
 * @author nolan
 * 
 */
public class PlayerScore implements Parcelable, Cloneable {

    private int id = -1;
    private String name;
    private long score;
    private int playerNumber;
    private List<Delta> history;
    private long lastUpdate;
    private PlayerColor color;

    public PlayerScore() {
    }

    public PlayerScore(Parcel in) {
        id = in.readInt();
        name = in.readString();
        score = in.readLong();
        playerNumber = in.readInt();
        history = Delta.fromJoinedStrings(in.readString(), in.readString());
        lastUpdate = in.readLong();
        color = PlayerColor.deserialize(in.readString());

    }

    public PlayerColor getPlayerColor() {
        return color;
    }
    
    public void setPlayerColor(PlayerColor color) {
        this.color = color;
    }
    
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

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    public List<Delta> getHistory() {
        return history;
    }

    public void setHistory(List<Delta> history) {
        this.history = history;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String toString() {
        return "PlayerScore [history=" + history + ", id=" + id + ", name=" + name + ", playerNumber=" + playerNumber
                + ", score=" + score + "]";
    }

    public static Comparator<PlayerScore> sortByPlayerNumber() {
        return new Comparator<PlayerScore>() {

            @Override
            public int compare(PlayerScore left, PlayerScore right) {
                return left.getPlayerNumber() - right.getPlayerNumber();
            }
        };
    }
    
    public static Comparator<PlayerScore> sortByScore() {
        return new Comparator<PlayerScore>() {

            @Override
            public int compare(PlayerScore lhs, PlayerScore rhs) {
                return Long.valueOf(lhs.getScore()).compareTo(rhs.getScore());
            }
        };
    }

    public boolean isAtDefault(Context context) {
        return (history == null || history.isEmpty())
                && score == PreferenceHelper.getIntPreference(R.string.CONSTANT_pref_initial_score,
                        R.string.CONSTANT_pref_initial_score_default, context);
    }

    public CharSequence toDisplayName(Context context) {
        return toDisplayName(getName(), getPlayerNumber(), context);
    }
    
    public static CharSequence toDisplayName(String playerName, int playerNumber, Context context) {
        if (!TextUtils.isEmpty(playerName)) {
            return playerName;
        }

        return new StringBuilder(context.getString(R.string.text_player))
            .append(' ')
            .append((playerNumber + 1))
            ;        
    }

    @Override
    public Object clone() {
        PlayerScore playerScore = new PlayerScore();
        playerScore.setHistory(new ArrayList<Delta>(history));
        playerScore.setId(id);
        playerScore.setName(name);
        playerScore.setPlayerNumber(playerNumber);
        playerScore.setScore(score);
        playerScore.setLastUpdate(lastUpdate);
        playerScore.setPlayerColor(color);
        return playerScore;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeLong(score);
        dest.writeInt(playerNumber);
        Pair<String, String> historyAsStrings = Delta.toJoinedStrings(history);
        dest.writeString(historyAsStrings.getFirst());
        dest.writeString(historyAsStrings.getSecond());
        dest.writeLong(lastUpdate);
        dest.writeString(PlayerColor.serialize(color));
    }

    public static final Parcelable.Creator<PlayerScore> CREATOR = new Parcelable.Creator<PlayerScore>() {
        public PlayerScore createFromParcel(Parcel in) {
            return new PlayerScore(in);
        }

        public PlayerScore[] newArray(int size) {
            return new PlayerScore[size];
        }
    };
}