package com.konstantinisaev.telegram.chart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

import static com.konstantinisaev.telegram.chart.MainActivity.getContext;
import static com.konstantinisaev.telegram.chart.ResolutionUtils.dpToPx;

public class UpperChartDividers implements DrawComponent<UpperChartDividers.UpperChartDividerParam> {

	private Path contentLinesPath = new Path();
	private Paint blueStrokePaint;
	private Paint headerPaint;
	private Map<Float,String> textPositions = new HashMap<>();
	private UpperChartDividerParam param;

	UpperChartDividers() {
		blueStrokePaint = new Paint();
		blueStrokePaint.setColor(ContextCompat.getColor(getContext(), R.color.blue_scroller));
		blueStrokePaint.setStyle(Paint.Style.STROKE);

		headerPaint = new Paint();
		headerPaint.setStyle(Paint.Style.FILL);
		headerPaint.setColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
		headerPaint.setTextSize(getContext().getResources().getDimensionPixelSize(R.dimen.text12));
	}

	@Override
	public void build(UpperChartDividerParam params) {
		param = params;
		float stepYLines = params.contentRect.height() / 5f;
		float initialYLines = params.contentRect.bottom;
		float initalYPosition = 0;
		float step = param.maxYContent / 5f;
		while (initialYLines > params.contentRect.top){
			initialYLines -= stepYLines;
			initalYPosition += step;
			contentLinesPath.moveTo(params.contentRect.left,initialYLines);
			contentLinesPath.lineTo(params.contentRect.right,initialYLines);
			textPositions.put(initialYLines - dpToPx(getContext(),3f),String.valueOf(Math.round(initalYPosition)));
//					contentLinesPath.(String.valueOf(Math.round(initalYPosition)), contentRect.left, , headerPaint);
		}
		contentLinesPath.moveTo(params.contentRect.left, params.contentRect.bottom);
		contentLinesPath.lineTo(params.contentRect.right, params.contentRect.bottom);
	}

	@Override
	public void onDraw(Canvas canvas) {
		if(!contentLinesPath.isEmpty()){
			blueStrokePaint.setStrokeWidth(dpToPx(getContext(),1f));
			canvas.drawPath(contentLinesPath,blueStrokePaint);
			canvas.drawText(getContext().getString(R.string.zero), param.contentRect.left, param.contentRect.bottom - dpToPx(getContext(),3f), headerPaint);
			for (Map.Entry<Float, String> entry : textPositions.entrySet()) {
				canvas.drawText(entry.getValue(), param.contentRect.left, entry.getKey(), headerPaint);
			}

		}
	}

	@Override
	public void clear() {
		contentLinesPath.reset();
		textPositions.clear();
	}

	static class UpperChartDividerParam{

		final Rect contentRect;
		final float maxYContent;

		UpperChartDividerParam(Rect contentRect, float maxYContent) {
			this.contentRect = contentRect;
			this.maxYContent = maxYContent;
		}
	}
}
