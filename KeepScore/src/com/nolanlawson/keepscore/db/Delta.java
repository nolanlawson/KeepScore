package com.nolanlawson.keepscore.db;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;
import com.nolanlawson.keepscore.util.Pair;
import com.nolanlawson.keepscore.util.StringUtil;

/**
 * Represents an amount by which a score changed, and at one time the change was effected.
 * 
 * @author nolan
 *
 */
public class Delta {

    private long timestamp;
    private int value;
    
    public Delta(long timestamp, int value) {
        this.timestamp = timestamp;
        this.value = value;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }

    public static List<Delta> fromJoinedStrings(String scoresStr, String timestampsStr) {
        List<String> scores = StringUtil.split(scoresStr, ',');
        List<String> timestamps = StringUtil.split(timestampsStr, ',');
        
        List<Delta> result = new ArrayList<Delta>(scores.size());
        
        for (int i = 0; i < scores.size(); i++) {
            int score = Integer.parseInt(scores.get(i));
            // the string could be empty, i.e. the scores were recorded before we started recording timestamps
            long timestamp = i < timestamps.size() ? Long.parseLong(timestamps.get(i)) : 0L;
            result.add(new Delta(timestamp, score));
        }
        return result;
    }
    
    public static List<Delta> fromJoinedScores(String scoresStr) {
        List<Integer> scores = CollectionUtil.stringsToInts(StringUtil.split(
                scoresStr, ','));
        return CollectionUtil.transform(scores, FROM_VALUE);
    }
    
    public static Pair<String, String> toJoinedStrings(List<Delta> deltas) {
        
        deltas = CollectionUtil.nullToEmpty(deltas);
        
        String values = TextUtils.join(",", CollectionUtil.transform(deltas, GET_VALUE));
        String timestamps = TextUtils.join(",", CollectionUtil.transform(deltas, GET_TIMESTAMP));
        
        return Pair.create(values, timestamps);
        
    }
    
    public static final Function<Delta, Integer> GET_VALUE = new Function<Delta, Integer>() {

        @Override
        public Integer apply(Delta obj) {
            return obj.getValue();
        }
    };
    
    public static final Function<Delta, Long> GET_TIMESTAMP = new Function<Delta, Long>() {

        @Override
        public Long apply(Delta obj) {
            return obj.getTimestamp();
        }
    };
    
    public static final Function<Integer, Delta> FROM_VALUE = new Function<Integer, Delta>(){
        @Override
        public Delta apply(Integer value) {
            return new Delta(0L, value);
        }
        
    };
    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
        result = prime * result + value;
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
        Delta other = (Delta) obj;
        if (timestamp != other.timestamp)
            return false;
        if (value != other.value)
            return false;
        return true;
    }
    
}
