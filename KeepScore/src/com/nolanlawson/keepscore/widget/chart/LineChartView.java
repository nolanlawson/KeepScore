package com.nolanlawson.keepscore.widget.chart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.CollectionUtil.Function;

/**
 * Basic View for drawing line graphs. Uses some basic color defaults.
 * 
 * @author nolan
 * 
 */
public class LineChartView extends View {

	private static final int MAIN_COLOR = Color.BLACK;
	private static final int SECONDARY_COLOR = Color.LTGRAY;
	private static final int TERTIARY_COLOR = 0xFF999999; // gray
	
	private static final List<Integer> LINE_COLORS = Arrays.asList(
			Color.BLUE,
			0xFFCC0000, // dark red 
			Color.GREEN, 
			Color.CYAN, 
			0xFFCC00CC, // dark magenta
			0xFF993300, // brown
			Color.YELLOW, 
			Color.DKGRAY);
	private static final int MIN_INTERVAL = 5;  // round to nearest five
	private static final List<Integer> INTERVAL_ROUNDING_POINTS = Arrays.asList(5, 10, 50, 100, 1000); // possible roundings

	private Paint mainPaint;
	private Paint secondaryPaint;
	private Paint tertiaryPaint;
	private List<Paint> linePaints;
	private List<Paint> lineLabelPaints;

	private Rect bounds = new Rect();
	
	// values determined by the data
	private List<LineChartLine> data;
	private int minDataPoint;
	private int maxDataPoint;
	private int labelTextHeight;
	private int yAxisLabelWidth;
	private int legendWidth;
	private int legendTextHeight;
	private int mainChartAreaWidth;
	
	// values taken from dimensions.xml
	private int chartPadding;
	private int itemWidth;
	private int dotRadius;
	private int fontSize;
	private int lineWidth;
	
	

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
		chartPadding = getContext().getResources().getDimensionPixelSize(
				R.dimen.chart_padding);
		itemWidth = getContext().getResources().getDimensionPixelSize(
				R.dimen.chart_item_width);
		dotRadius = getContext().getResources().getDimensionPixelSize(
				R.dimen.chart_dot_radius);
		fontSize = getContext().getResources().getDimensionPixelSize(
				R.dimen.chart_font_size);
		lineWidth = getContext().getResources().getDimensionPixelSize(
				R.dimen.chart_line_width);

		mainPaint = new Paint();
		mainPaint.setColor(MAIN_COLOR);
		mainPaint.setTextSize(fontSize);
		mainPaint.setTypeface(Typeface.MONOSPACE);
		
		secondaryPaint = new Paint();
		secondaryPaint.setColor(SECONDARY_COLOR);
		tertiaryPaint = new Paint();
		tertiaryPaint.setColor(TERTIARY_COLOR);
		
		linePaints = CollectionUtil.transform(LINE_COLORS,
				new Function<Integer, Paint>() {

					@Override
					public Paint apply(Integer colorId) {
						Paint paint = new Paint();
						paint.setColor(colorId);
						paint.setStyle(Paint.Style.STROKE);
						paint.setStrokeWidth(lineWidth);
						paint.setStyle(Style.FILL_AND_STROKE);
						return paint;
					}
				});
		
		lineLabelPaints = CollectionUtil.transform(LINE_COLORS,
				new Function<Integer, Paint>() {

					@Override
					public Paint apply(Integer colorId) {
						Paint paint = new Paint();
						paint.setColor(colorId);
						paint.setTextSize(fontSize);
						return paint;
					}
				});		
	}

	/**
	 * Provide a list of data lines to draw in the chart
	 * 
	 * @param dataPoints
	 */
	public void loadData(List<LineChartLine> data) {
		this.data = data;
		determineMinAndMaxDataPoints();
		determineYAxisLabelInfo();
		determineLegendInfo();
		determineMainChartAreaWidth();
		invalidate();
	}

	private void determineMinAndMaxDataPoints() {

		minDataPoint = 0;
		maxDataPoint = 0;

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
		
		// round up/down to a multiple of 5
		while (minDataPoint % MIN_INTERVAL != 0) {
			minDataPoint--;
		}
		while (maxDataPoint % MIN_INTERVAL != 0) {
			maxDataPoint++;
		}
	}

	private void determineYAxisLabelInfo() {
		String minText = Integer.toString(minDataPoint);
		String maxText = Integer.toString(maxDataPoint);
		
		Rect maxRect = new Rect();
		mainPaint.getTextBounds(maxText, 0, maxText.length(), maxRect);
		
		Rect minRect = new Rect();
		mainPaint.getTextBounds(minText, 0, minText.length(), minRect);
		
		yAxisLabelWidth = Math.max(maxRect.width(), minRect.width());
		labelTextHeight = maxRect.height();
	}

	private void determineLegendInfo()  {
		// figure out the expected text height
		Rect rect = new Rect();
		lineLabelPaints.get(0).getTextBounds("X",0,1, rect);
		legendTextHeight = rect.height();
		
		// figure out the length of the longest text
		int maxTextWidth = 0;
		for (int i = 0; i < data.size(); i++) {
			LineChartLine line = data.get(i);
			Paint paint = lineLabelPaints.get(i % lineLabelPaints.size());
			paint.getTextBounds(line.getLabel(), 0, line.getLabel().length(), rect);
			if (rect.width() > maxTextWidth) {
				maxTextWidth = rect.width();
			}
		}
		
		legendWidth = maxTextWidth;
	}
	
	private void determineMainChartAreaWidth() {
		int maxNumDataPoints = 0;
		for (LineChartLine line : data) {
			int numDataPoints = line.getDataPoints().size();
			if (numDataPoints > maxNumDataPoints) {
				maxNumDataPoints = numDataPoints;
			}
		}
		
		mainChartAreaWidth =  ((maxNumDataPoints - 1) * itemWidth);
	}
	

	@Override
	public void onDraw(Canvas canvas) {
		if (data == null) {
			return;
		}

		canvas.getClipBounds(bounds);

		int height = bounds.bottom - (chartPadding * 2);
		
		// initial padding
		int offsetY = chartPadding;
		int offsetX = chartPadding;
		
		drawLegendArea(canvas, height, offsetX, offsetY);
		
		offsetX += legendWidth + chartPadding; //  pad on the right
		
		List<Integer> intervalPoints = determineIntervalPoints(canvas, height);
		
		drawYAxisLabel(canvas, height, offsetX, offsetY, intervalPoints);
		
		offsetX += yAxisLabelWidth + chartPadding; // pad on the right
		
		drawMainChartArea(canvas, height, offsetX, offsetY, intervalPoints);

	}

	private List<Integer> determineIntervalPoints(Canvas canvas, int height) {
		// interval points are the points on the Y axis between the min and max values.
		// It makes the chart easier to read
		
		
		// determine how many intervals we can fit in here, e.g. round to 5, round to 10, round to 50...
		
		int pixelHeight = height - (3 * labelTextHeight); // assume 0.5 padding for the min and max labels
		for (Integer interval : INTERVAL_ROUNDING_POINTS) {
			
			int firstInterval = minDataPoint + (interval - (Math.abs(minDataPoint % interval)));
			
			int numIntervals = 0;
			for (int i = firstInterval; i < maxDataPoint; i+= interval) {
				numIntervals++;
			}
			int pixelHeightRequired = (int)Math.round((0.5 * labelTextHeight) + (1.5 * labelTextHeight * numIntervals));
			if (pixelHeightRequired <= pixelHeight) {
				// use this interval length
				List<Integer> result = new ArrayList<Integer>();
				for (int i = firstInterval; i < maxDataPoint; i+= interval) {
					result.add(i);
				}
				return result;
			}
		}
		
		return Collections.emptyList();
	}

	private void drawYAxisLabel(Canvas canvas, int height, int offsetX, int offsetY, List<Integer> intervalPoints) {
		
		String maxText = Integer.toString(maxDataPoint);
		String minText = Integer.toString(minDataPoint);
		
		canvas.drawText(maxText, offsetX, offsetY + labelTextHeight, mainPaint);
		canvas.drawText(minText, offsetX, offsetY + height, mainPaint);	
		
		for (int i = 0; i < intervalPoints.size(); i++) {
			Integer intervalPoint = intervalPoints.get(i);
			
			int yLocation = getYLocationForIntervalPoint(intervalPoint, height);
			
			// make sure it doesn't overlap with the top one, which it can because of the font size itself
			if (i == intervalPoints.size() - 1) {
				int topOfText = offsetY + yLocation - labelTextHeight;
				if (topOfText < offsetY + labelTextHeight) {
					break;
				}
			}
			canvas.drawText(intervalPoint.toString(), offsetX, offsetY + yLocation, mainPaint);
		}
	}
	
	
	private int getYLocationForIntervalPoint(Integer intervalPoint, int height) {
		double scaleFactor = (1.0 * intervalPoint - minDataPoint) / (maxDataPoint - minDataPoint);
		int yLocation = (int)Math.round(scaleFactor * height);
		return (height - yLocation);
	}

	private void drawLegendArea(Canvas canvas, int height, int offsetX, int offsetY) {
		
		int maxTextWidth = 0;
		int x = offsetX;
		int y = offsetY + legendTextHeight;
		int ySpacing = (legendTextHeight / 2);
		
		Rect rect = new Rect();
		for (int i = 0; i < data.size(); i++) {
			LineChartLine line = data.get(i);
			Paint paint = lineLabelPaints.get(i % lineLabelPaints.size());
			canvas.drawText(line.getLabel(), x, y, paint);
			
			paint.getTextBounds(line.getLabel(), 0, line.getLabel().length(), rect);
			if (rect.width() > maxTextWidth) {
				maxTextWidth = rect.width();
			}
			
			y += rect.height() + ySpacing;
		}
		
	}

	private void drawMainChartArea(Canvas canvas, int height, int offsetX, int offsetY, 
			List<Integer> intervalPoints) {

		drawChartBordersAndGrid(canvas, height, offsetX, offsetY, intervalPoints);
		
		for (int i = 0; i < data.size(); i++) {
			LineChartLine line = data.get(i);
			Paint linePaint = linePaints.get(i % linePaints.size());
			Paint lineLabelPaint = lineLabelPaints.get(i % lineLabelPaints.size());

			int dataPointX = offsetX;
			int previousDataPointX = 0;
			int previousDataPointY = 0;
			boolean first = true;
			for (Integer dataPoint : line.getDataPoints()) {

				// draw a dot
				int dataPointY = offsetY
						+ (int) Math.round(height - (((1.0 * dataPoint - minDataPoint) / (maxDataPoint - minDataPoint)) * height));
				canvas.drawCircle(dataPointX, dataPointY, dotRadius, lineLabelPaint);

				if (!first) {
					// draw a line to the last data point
					canvas.drawLine(previousDataPointX, previousDataPointY,
							dataPointX, dataPointY, linePaint);
				}

				previousDataPointX = dataPointX;
				previousDataPointY = dataPointY;
				first = false;
				dataPointX += itemWidth;
			}
		}
	}

	private void drawChartBordersAndGrid(Canvas canvas, int height, int offsetX, int offsetY, 
			List<Integer> intervalPoints) {
		
		int maxLineDataPoints = CollectionUtil.max(data, new Function<LineChartLine,Integer>(){

			@Override
			public Integer apply(LineChartLine obj) {
				return obj.getDataPoints().size();
			}
		});
		
		int edgeRight = offsetX + (itemWidth * (maxLineDataPoints - 1));
		int edgeBottom = height + offsetY;
		
		// draw border lines at the top, right, bottom, and left
		int[] topleft     = {offsetX, offsetY};
		int[] topright    = {edgeRight, offsetY};
		int[] bottomright = {edgeRight, edgeBottom};
		int[] bottomleft  = {offsetX, edgeBottom};
		
		
		// top
		canvas.drawLine(topleft[0], topleft[1], topright[0], topright[1], mainPaint);
		
		// right
		canvas.drawLine(topright[0], topright[1], bottomright[0], bottomright[1], mainPaint);
		
		// bottom
		canvas.drawLine(bottomright[0], bottomright[1], bottomleft[0], bottomleft[1], mainPaint);
				
		// left
		canvas.drawLine(bottomleft[0], bottomleft[1], topleft[0], topleft[1], mainPaint);
		
		// draw vertical grid lines
		
		for (int i = 1; i < maxLineDataPoints - 1; i++) {
			int x = offsetX + (i * itemWidth);
			canvas.drawLine(x, offsetY, x, height + offsetY, secondaryPaint);
		}
		
		// draw horizontal grid lines
		for (Integer intervalDataPoint : intervalPoints) {
			int yLocation = getYLocationForIntervalPoint(intervalDataPoint, height);
			canvas.drawLine(offsetX, yLocation, edgeRight, yLocation, tertiaryPaint);
		}
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (data == null) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
		
		int expectedWidth = (4 * chartPadding) 
				+ legendWidth
				+ yAxisLabelWidth
				+ mainChartAreaWidth;
				
		
		setMeasuredDimension(expectedWidth,
				heightMeasureSpec);
	}
	
}
