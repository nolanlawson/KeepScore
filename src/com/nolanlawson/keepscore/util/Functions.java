package com.nolanlawson.keepscore.util;

import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;

public class Functions {

	public static final Function<PlayerScore, Integer> PLAYER_SCORE_TO_HISTORY_SIZE = 	
			new Function<PlayerScore, Integer>(){

				@Override
				public Integer apply(PlayerScore obj) {
					return obj.getHistory().size();
				}
	};
	
	public static final Function<Integer, Integer> INTEGER_TO_LENGTH_WITH_SIGN = 
			new Function<Integer, Integer>(){

		@Override
		public Integer apply(Integer obj) {
			return IntegerUtil.toStringWithSign(obj).length();
		}
	};
	
	
}
