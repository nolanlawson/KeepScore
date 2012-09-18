package com.nolanlawson.keepscore.data;

/**
 * Simple POJO representing a change made to a PlayerScore's history
 * @author nolan
 *
 */
public class RecordedChange {
	
	private Type type;
	private int value;
	private int playerNumber;
	
	public RecordedChange(int playerNumber, Type type, int value) {
	    this.playerNumber = playerNumber;
		this.type = type;
		this.value = value;
	}
	
	public int getPlayerNumber() {
	    return playerNumber;
	}
	
	public Type getType() {
		return type;
	}

	public int getValue() {
		return value;
	}

	public static enum Type {
		AddNew, ModifyLast, DeleteLast
	}

}
