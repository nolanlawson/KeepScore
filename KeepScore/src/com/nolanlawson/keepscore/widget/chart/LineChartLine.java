package com.nolanlawson.keepscore.widget.chart;

import java.util.List;

/**
 * Representation of a single line in the line chart.
 * @author nolan
 *
 */
public class LineChartLine {

    private String label;
    private List<Integer> dataPoints;
    
    public LineChartLine() {
    }
    
    public LineChartLine(String label, List<Integer> dataPoints) {
        this.label = label;
        this.dataPoints = dataPoints;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public List<Integer> getDataPoints() {
        return dataPoints;
    }
    public void setDataPoints(List<Integer> dataPoints) {
        this.dataPoints = dataPoints;
    }
    
}
