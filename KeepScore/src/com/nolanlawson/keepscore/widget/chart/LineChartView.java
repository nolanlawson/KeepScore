package com.nolanlawson.keepscore.widget.chart;

import java.util.Arrays;
import java.util.List;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Basic View for drawing line graphs.  Uses some basic color defaults.
 * @author nolan
 *
 */
public class LineChartView extends View {

    private static final int MAIN_COLOR = Color.BLACK;
    private static final List<Integer> LINE_COLORS = Arrays.asList(
	    Color.BLUE,
	    Color.RED,
	    Color.YELLOW,
	    Color.CYAN,
	    Color.MAGENTA,
	    Color.LTGRAY,
	    Color.GREEN,
	    Color.DKGRAY
	    );
    
    private Paint mainPaint;
    private List<Paint> linePaints;
    
    private Rect bounds = new Rect();
    private List<LineChartLine> data;
    private int chartPadding;
    private int itemWidth;
    private int dotRadius;
    
    public LineChartView(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
	init();
    }

    public LineChartView(Context context, AttributeSet attrs) {
	super(context, attrs);
	init();
    }

    public LineChartView(Context context) {
	super(context);
	init();
    }
    
    private void init() {
	chartPadding = getContext().getResources().getDimensionPixelSize(R.dimen.chart_padding);
	itemWidth = getContext().getResources().getDimensionPixelSize(R.dimen.chart_item_width);
	dotRadius = getContext().getResources().getDimensionPixelSize(R.dimen.chart_dot_radius);
	
	mainPaint = new Paint();
	mainPaint.setColor(MAIN_COLOR);
	linePaints = CollectionUtil.transform(LINE_COLORS, new Function<Integer, Paint>(){

	    @Override
	    public Paint apply(Integer colorId) {
		Paint paint = new Paint();
		paint.setColor(colorId);
		return paint;
	    }
	});
    }
    
    /**
     * Provide a list of data lines to draw in the chart
     * @param dataPoints
     */
    public void setData(List<LineChartLine> data) {
	this.data = data;
	invalidate();
    }
    

    @Override
    public void onDraw(Canvas canvas) {
	if (data == null) {
	    return;
	}
	
	canvas.getClipBounds(bounds);
	
	int height = bounds.bottom - (chartPadding * 2);
	
	drawMainGraphArea(canvas, height, chartPadding, chartPadding);
	
    }

    private void drawMainGraphArea(Canvas canvas, int height, int offsetX, int offsetY) {
	
	int minDataPoint = 0;
	int maxDataPoint = 0;
	
	for (LineChartLine line : data) {
	    for (Integer dataPoint : line.getDataPoints()) {
		if (dataPoint < minDataPoint) {
		    minDataPoint = dataPoint;
		}
		if (dataPoint > maxDataPoint) {
		    maxDataPoint = dataPoint;
		}
	    }
	}
	
	// in case they're both zero somehow
	if (minDataPoint == maxDataPoint) {
	    maxDataPoint++;
	}
	
	for (int i = 0; i < data.size(); i++) {
	    LineChartLine line = data.get(i);
	    Paint linePaint = linePaints.get(i % linePaints.size());
	    
	    int dataPointX = offsetX;
	    int previousDataPointX = 0;
	    int previousDataPointY = 0;
	    boolean first = true;
	    for (Integer dataPoint : line.getDataPoints()) {
		
		// draw a dot
		int dataPointY = offsetY + (int)Math.round(height - (
			((1.0 * dataPoint - minDataPoint) / (maxDataPoint - minDataPoint)) * height));
		canvas.drawCircle(dataPointX, dataPointY, dotRadius, linePaint);
		
		if (!first) {
		    // draw a line to the last data point
		    canvas.drawLine(previousDataPointX, previousDataPointY, dataPointX, dataPointY, linePaint);
		}
		
		previousDataPointX = dataPointX;
		previousDataPointY = dataPointY;
		first = false;
		dataPointX += itemWidth;
	    }
	}
	
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	if (data == null) {
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	    return;
	}
	
	int maxNumDataPoints = 0;
	for (LineChartLine line : data) {
	    int numDataPoints = line.getDataPoints().size();
	    if (numDataPoints > maxNumDataPoints) {
		maxNumDataPoints = numDataPoints;
	    }
	}
	setMeasuredDimension(chartPadding + (itemWidth * maxNumDataPoints), heightMeasureSpec);
    }
    
    
}
