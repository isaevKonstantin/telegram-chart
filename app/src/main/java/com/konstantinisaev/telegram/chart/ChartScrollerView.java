package com.konstantinisaev.telegram.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class ChartScrollerView extends ViewGroup {

	private static final String TAG = ChartScrollerView.class.getSimpleName();
	private Paint grayPaint;
	private Paint whitePaint;

	private Rect unselectRect;
	private Rect selectRect;
	private final int initialStartPosition = 80;

	private float beginX;
	private float endX;
	private Paint blueStrokePaint;

	public ChartScrollerView(Context context) {
		super(context);
		init();
	}

	public ChartScrollerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ChartScrollerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public ChartScrollerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init(){
		grayPaint = new Paint();
		grayPaint.setStyle(Paint.Style.FILL);
		grayPaint.setColor(ContextCompat.getColor(getContext(), R.color.gray_scroller));

		whitePaint = new Paint();
		whitePaint.setStyle(Paint.Style.FILL);
		whitePaint.setColor(ContextCompat.getColor(getContext(), R.color.white));

		blueStrokePaint = new Paint();
		blueStrokePaint.setColor(ContextCompat.getColor(getContext(), R.color.blue_scroller));
		blueStrokePaint.setStrokeWidth(5f);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {}


	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		drawGridBackground(canvas);
	}

	protected void drawGridBackground(Canvas canvas) {
		unselectRect = new Rect(0,0,canvas.getWidth(),canvas.getHeight());


		canvas.drawRect(unselectRect, grayPaint);
		if(beginX == 0 ){
			selectRect = new Rect((canvas.getWidth() / 100) * initialStartPosition,5,canvas.getWidth(),canvas.getHeight() - 5);
		}else {
			selectRect = new Rect(Math.round(beginX),5,Math.round(endX),canvas.getHeight() - 5);
		}
		canvas.drawRect(selectRect, whitePaint);

		canvas.drawLine(selectRect.left,0,selectRect.left,canvas.getHeight(), blueStrokePaint);
		canvas.drawLine(selectRect.right,0,selectRect.right,canvas.getHeight(), blueStrokePaint);
		canvas.drawLine(selectRect.left,0,selectRect.right,0, blueStrokePaint);
		canvas.drawLine(selectRect.left,canvas.getHeight(),selectRect.right,canvas.getHeight(), blueStrokePaint);


	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if((x >= selectRect.left && x <= selectRect.right)){
				beginX = x - getX();
				endX = beginX + selectRect.width();
				invalidate();
			}
			return true;
		}
		return true;
	}

	private void log(String message){
		Log.i(TAG,message);
	}
}

