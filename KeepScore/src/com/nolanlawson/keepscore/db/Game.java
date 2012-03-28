package com.nolanlawson.keepscore.db;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;

public class Game implements Parcelable, Cloneable {

	private int id = -1;
	private long dateStarted;
	private long dateSaved;
	private String name;
	private List<PlayerScore> playerScores;
	
	public Game() {
	}

	public Game(Parcel in) {
		id = in.readInt();
		dateStarted = in.readLong();
		dateSaved = in.readLong();
		name = in.readString();
		playerScores = new ArrayList<PlayerScore>();
		while (true) {
 			PlayerScore playerScore = in.readParcelable(PlayerScore.class.getClassLoader());
 			if (playerScore == null) {
 				break;
 			}
			playerScores.add(playerScore);
		}

	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getDateStarted() {
		return dateStarted;
	}
	public void setDateStarted(long dateStarted) {
		this.dateStarted = dateStarted;
	}
	public long getDateSaved() {
		return dateSaved;
	}
	public void setDateSaved(long dateSaved) {
		this.dateSaved = dateSaved;
	}
	public List<PlayerScore> getPlayerScores() {
		return playerScores;
	}
	public void setPlayerScores(List<PlayerScore> playerScores) {
		this.playerScores = playerScores;
	}
	@Override
	public String toString() {
		return "Game [dateSaved=" + dateSaved
				+ ", dateStarted=" + dateStarted + ", id=" + id + ", name="
				+ name + ", playerScores=" + (playerScores != null ? playerScores.size() : 0) + "]";
	}
	
	public static Comparator<Game> byRecentlySaved() {
		return new Comparator<Game>() {

			@Override
			public int compare(Game object1, Game object2) {
				return new Long(object2.getDateSaved()).compareTo(object1.getDateSaved());
			}
		};
	}
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeLong(dateStarted);
		dest.writeLong(dateSaved);
		dest.writeString(name);
		
		for (PlayerScore playerScore : playerScores) {
			dest.writeParcelable(playerScore, 0);
		}
	}
	
	public static final Parcelable.Creator<Game> CREATOR = new Parcelable.Creator<Game>() {
		public Game createFromParcel(Parcel in) {
			return new Game(in);
		}

		public Game[] newArray(int size) {
			return new Game[size];
		}
	};
	
	@Override
	public Object clone() {
		Game game = new Game();
		game.setDateSaved(dateSaved);
		game.setDateStarted(dateStarted);
		game.setId(id);
		game.setName(name);
		game.setPlayerScores(CollectionUtil.transform(playerScores, new Function<PlayerScore,PlayerScore>(){

			@Override
			public PlayerScore apply(PlayerScore obj) {
				return (PlayerScore)obj.clone();
			}
		}));
		return game;
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
		Game other = (Game) obj;
		if (id == -1 && other.id == -1) // not saved yet
			return this == obj; // use simple equality comparison
		if (id != other.id)
			return false;
		return true;
	}

	/**
	 * Make a clone of this Game that can be saved cleanly to the database as a new game.
	 * @return
	 */
	public Game makeCleanCopy() {
		
		Game newGame = (Game) clone();
		
		// reset everything except the player names
		newGame.setId(-1);
		newGame.setDateStarted(System.currentTimeMillis());
		newGame.setDateSaved(0);
		newGame.setName(null);
		
		for (PlayerScore playerScore : newGame.getPlayerScores()) {
			playerScore.setId(-1);
		}	
		
		return newGame;
	}
}
