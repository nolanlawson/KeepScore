package com.nolanlawson.keepscore.db;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.helper.PreferenceHelper;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.StringUtil;

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
    private List<Integer> history;
    private long lastUpdate;

    public PlayerScore() {
    }

    public PlayerScore(Parcel in) {
        id = in.readInt();
        name = in.readString();
        score = in.readLong();
        playerNumber = in.readInt();
        history = CollectionUtil.stringsToInts(StringUtil.split(in.readString(), ','));
        lastUpdate = in.readLong();

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

    public List<Integer> getHistory() {
        return history;
    }

    public void setHistory(List<Integer> history) {
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

    public String toDisplayName(Context context) {
        if (!TextUtils.isEmpty(getName())) {
            return getName();
        }

        return context.getString(R.string.text_player) + " " + (getPlayerNumber() + 1);
    }

    @Override
    public Object clone() {
        PlayerScore playerScore = new PlayerScore();
        playerScore.setHistory(new ArrayList<Integer>(history));
        playerScore.setId(id);
        playerScore.setName(name);
        playerScore.setPlayerNumber(playerNumber);
        playerScore.setScore(score);
        playerScore.setLastUpdate(lastUpdate);
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
        dest.writeString(TextUtils.join(",", history));
        dest.writeLong(lastUpdate);
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