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
		/** New value is added to the score history */
	    	AddNew, 
	    	/** The last value in the score history is modified */
		ModifyLast, 
		/** The last value in the score history is deleted manually using the "delete last" menu item*/
		DeleteLast, 
		/** The last value in the score history is deleted, but only because otherwise 
		 * a +0 would have been added, due to a + or - button being pressed */
		DeleteLastZero
	}

}
