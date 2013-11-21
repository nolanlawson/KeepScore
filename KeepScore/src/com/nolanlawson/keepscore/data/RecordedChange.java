package com.nolanlawson.keepscore.data;

import com.nolanlawson.keepscore.db.Delta;

/**
 * Simple POJO representing a change made to a PlayerScore's history
 * @author nolan
 *
 */
public class RecordedChange {
	
	private Type type;
	private Delta delta;
	private int playerNumber;
	
	public RecordedChange(int playerNumber, Type type, Delta delta) {
	    this.playerNumber = playerNumber;
		this.type = type;
		this.delta = delta;
	}
	
	public int getPlayerNumber() {
	    return playerNumber;
	}
	
	public Type getType() {
		return type;
	}

	public Delta getDelta() {
		return delta;
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
