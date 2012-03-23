package com.nolanlawson.keepscore.helper;

import android.widget.LinearLayout;

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
			R.dimen.delta_button_height_2_to_4,
			R.dimen.delta_button_height_2_to_4_with_round_totals,
			R.dimen.player_onscreen_delta_button_text_size_2_to_4), 
	FiveToSixPlayers (
			R.dimen.player_score_2_to_6, 
			R.dimen.player_plus_minus_button_2_to_6, 
			R.dimen.player_name_5_to_6,
			R.dimen.player_badge_5_to_6,
			R.dimen.player_badge_padding_left_right_5_to_6,
			R.dimen.player_badge_padding_top_bottom_5_to_6,	
			R.dimen.player_badge_offset_5_to_6,
			R.dimen.delta_button_height_5_to_6,
			R.dimen.delta_button_height_5_to_6_with_round_totals,
			R.dimen.player_onscreen_delta_button_text_size_5_to_6), 
	SevenToEightPlayers (
			R.dimen.player_score_7_to_8, 
			R.dimen.player_plus_minus_button_7_to_8, 
			R.dimen.player_name_7_to_8,
			R.dimen.player_badge_7_to_8,
			R.dimen.player_badge_padding_left_right_7_to_8,
			R.dimen.player_badge_padding_top_bottom_7_to_8,		
			R.dimen.player_badge_offset_7_to_8,
			LinearLayout.LayoutParams.FILL_PARENT,
			LinearLayout.LayoutParams.FILL_PARENT,
			R.dimen.player_onscreen_delta_button_text_size_7_to_8), 
	;

	private int playerScoreTextSize;
	private int plusMinusTextSize;
	private int playerNameTextSize;
	private int badgeTextSize;
	private int badgePaddingLeftRight;
	private int badgePaddingTopBottom;
	private int badgeOffset;
	private int onscreenDeltaButtonTextSize;
	private int plusMinusButtonHeight;
	private int plusMinusButtonHeightWithRoundTotals;

	private PlayerTextFormat(int playerScoreTextSize, int plusMinusTextSize, int playerNameTextSize, int badgeTextSize,
			int badgePaddingLeftRight, int badgePaddingTopBottom, int badgeOffset, int plusMinusButtonHeight,
			int plusMinusButtonHeightWithRoundTotals,
			int onscreenDeltaButtonTextSize) {
		this.playerScoreTextSize = playerScoreTextSize;
		this.plusMinusTextSize = plusMinusTextSize;
		this.playerNameTextSize = playerNameTextSize;
		this.badgeTextSize = badgeTextSize;
		this.badgePaddingLeftRight = badgePaddingLeftRight;
		this.badgePaddingTopBottom = badgePaddingTopBottom;
		this.badgeOffset = badgeOffset;
		this.plusMinusButtonHeight = plusMinusButtonHeight;
		this.plusMinusButtonHeightWithRoundTotals = plusMinusButtonHeightWithRoundTotals;
		this.onscreenDeltaButtonTextSize = onscreenDeltaButtonTextSize;
	}
	
	public int getPlusMinusButtonHeightWithRoundTotals() {
		return plusMinusButtonHeightWithRoundTotals;
	}
	public int getPlusMinusButtonHeight() {
		return plusMinusButtonHeight;
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
	public int getBadgePaddingLeftRight() {
		return badgePaddingLeftRight;
	}
	public int getBadgePaddingTopBottom() {
		return badgePaddingTopBottom;
	}
	public int getBadgeOffset() {
		return badgeOffset;
	}
	public int getOnscreenDeltaButtonTextSize() {
		return onscreenDeltaButtonTextSize;
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
