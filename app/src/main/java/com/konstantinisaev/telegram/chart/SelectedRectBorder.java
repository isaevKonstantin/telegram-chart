package com.konstantinisaev.telegram.chart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;

import static com.konstantinisaev.telegram.chart.MainActivity.getContext;
import static com.konstantinisaev.telegram.chart.ResolutionUtils.dpToPx;

public class SelectedRectBorder implements DrawComponent<Rect> {

	private Path selectLeftRightBorder = new Path();
	private Path selectTopBottomBorder = new Path();
	private Paint blueStrokePaint;
	private float leftRightSelectedStroke = dpToPx(getContext(),1f);
	private float bottomSelectedBorder = dpToPx(getContext(),3f);

	public SelectedRectBorder() {
		blueStrokePaint = new Paint();
		blueStrokePaint.setColor(ContextCompat.getColor(getContext(), R.color.blue_scroller));
		blueStrokePaint.setStyle(Paint.Style.STROKE);
	}

	@Override
	public void build(Rect params) {
		selectTopBottomBorder.reset();
		selectLeftRightBorder.reset();

		selectLeftRightBorder.moveTo(params.left, params.top);
		selectLeftRightBorder.lineTo(params.left, params.bottom);
		selectLeftRightBorder.moveTo(params.right, params.top);
		selectLeftRightBorder.lineTo(params.right, params.bottom);

		selectTopBottomBorder.moveTo(params.right, params.bottom);
		selectTopBottomBorder.lineTo(params.left, params.bottom);
		selectTopBottomBorder.moveTo(params.right, params.top);
		selectTopBottomBorder.lineTo(params.left, params.top);
	}

	@Override
	public void onDraw(Canvas canvas) {
		blueStrokePaint.setStrokeWidth(bottomSelectedBorder);
		canvas.drawPath(selectLeftRightBorder,blueStrokePaint);
		blueStrokePaint.setStrokeWidth(leftRightSelectedStroke);
		canvas.drawPath(selectTopBottomBorder,blueStrokePaint);
	}

	@Override
	public void clear() {
		selectTopBottomBorder.reset();
		selectLeftRightBorder.reset();
	}
}
