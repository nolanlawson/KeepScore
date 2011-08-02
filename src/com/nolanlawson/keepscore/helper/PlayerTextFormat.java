package com.nolanlawson.keepscore.helper;

import com.nolanlawson.keepscore.R;

public enum PlayerTextFormat {

	TwoToFourPlayers (
			R.dimen.player_score_2_to_6, 
			R.dimen.player_plus_minus_button_2_to_6, 
			R.dimen.player_name_2_to_4,
			R.dimen.player_badge_2_to_4,
			R.dimen.player_badge_padding_left_right_2_to_4,
			R.dimen.player_badge_padding_top_bottom_2_to_4,
			R.dimen.player_badge_offset_2_to_4,
			0.15F), 
	FiveToSixPlayers (
			R.dimen.player_score_2_to_6, 
			R.dimen.player_plus_minus_button_2_to_6, 
			R.dimen.player_name_5_to_6,
			R.dimen.player_badge_5_to_6,
			R.dimen.player_badge_padding_left_right_5_to_6,
			R.dimen.player_badge_padding_top_bottom_5_to_6,	
			R.dimen.player_badge_offset_5_to_6,
			0.05F), 
	SevenToEightPlayers (
			R.dimen.player_score_7_to_8, 
			R.dimen.player_plus_minus_button_7_to_8, 
			R.dimen.player_name_7_to_8,
			R.dimen.player_badge_7_to_8,
			R.dimen.player_badge_padding_left_right_7_to_8,
			R.dimen.player_badge_padding_top_bottom_7_to_8,		
			R.dimen.player_badge_offset_7_to_8,
			0F), 
	;
	
	private int playerScoreTextSize;
	private int plusMinusTextSize;
	private int playerNameTextSize;
	private int badgeTextSize;
	private int badgePaddingLeftRight;
	private int badgePaddingTopBottom;
	private int badgeOffset;
	private float plusMinusButtonMargin;

	private PlayerTextFormat(int playerScoreTextSize, int plusMinusTextSize, int playerNameTextSize, int badgeTextSize,
			int badgePaddingLeftRight, int badgePaddingTopBottom, int badgeOffset, float plusMinusButtonMargin) {
		this.playerScoreTextSize = playerScoreTextSize;
		this.plusMinusTextSize = plusMinusTextSize;
		this.playerNameTextSize = playerNameTextSize;
		this.badgeTextSize = badgeTextSize;
		this.badgePaddingLeftRight = badgePaddingLeftRight;
		this.badgePaddingTopBottom = badgePaddingTopBottom;
		this.badgeOffset = badgeOffset;
		this.plusMinusButtonMargin = plusMinusButtonMargin;
	}
	public int getPlayerScoreTextSize() {
		return playerScoreTextSize;
	}
	public int getPlusMinusTextSize() {
		return plusMinusTextSize;
	}
	public int getPlayerNameTextSize() {
		return playerNameTextSize;
	}
	public int getBadgeTextSize() {
		return badgeTextSize;
	}
	public float getPlusMinusButtonMargin() {
		return plusMinusButtonMargin;
	}
	public int getBadgePaddingLeftRight() {
		return badgePaddingLeftRight;
	}
	public int getBadgePaddingTopBottom() {
		return badgePaddingTopBottom;
	}
	public int getBadgeOffset() {
		return badgeOffset;
	}
	public static PlayerTextFormat forNumPlayers(int numPlayers) {
		switch (numPlayers) {
			case 2:
			case 3:
			case 4:
				return TwoToFourPlayers;
			case 5:
			case 6:
				return FiveToSixPlayers;
			case 7:
			case 8:
			default:
				return SevenToEightPlayers;
		}
	}
}
