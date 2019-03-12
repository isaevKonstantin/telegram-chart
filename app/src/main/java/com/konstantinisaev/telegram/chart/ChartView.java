package com.konstantinisaev.telegram.chart;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;

import java.util.List;

public class ChartView extends ViewGroup {

	private static final String TAG = ChartView.class.getSimpleName();
	private Paint grayPaint;
	private Paint whitePaint;

	private Rect selectRect;

	private float beginX;
	private float endX;
	private Paint blueStrokePaint;
	private List<ChartData> data;
	private float scrollerHeight = dpToPx(getContext(),80f);
	private float contentHeight = dpToPx(getContext(),300f);
	private float verticalMargin = dpToPx(getContext(),16f);
	private Rect unselectRect;

	public ChartView(Context context) {
		super(context);
		init();
	}

	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public ChartView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
		Rect contentRect = new Rect(0, 0, canvas.getWidth(), Math.round(contentHeight));
		canvas.drawRect(contentRect, whitePaint);

		int topScrollerY = Math.round(contentHeight + verticalMargin);
		int bottomScrollerY = Math.round(contentHeight + verticalMargin + scrollerHeight);
		unselectRect = new Rect(0,topScrollerY, canvas.getWidth(), bottomScrollerY);

		canvas.drawRect(unselectRect, grayPaint);
		if(beginX == 0 ){
			int initialStartPosition = 80;
			selectRect = new Rect((canvas.getWidth() / 100) * initialStartPosition,topScrollerY + 5,canvas.getWidth(), bottomScrollerY);
		}else {
			selectRect = new Rect(Math.round(beginX),topScrollerY + 5,Math.round(endX),bottomScrollerY - 5);
		}
		canvas.drawRect(selectRect, whitePaint);

		canvas.drawLine(selectRect.left,selectRect.top,selectRect.left,selectRect.bottom, blueStrokePaint);
		canvas.drawLine(selectRect.right,selectRect.top,selectRect.right,selectRect.bottom, blueStrokePaint);
		canvas.drawLine(selectRect.left,selectRect.top,selectRect.right,selectRect.top, blueStrokePaint);
		canvas.drawLine(selectRect.left,selectRect.bottom,selectRect.right,selectRect.bottom, blueStrokePaint);

		if(!data.isEmpty()){
			for (ChartData chartData : data) {
				float scrollerKoeficientY;
				float contentKoeficientY;
				float scrollerKoeficientX;
				float maxY = 1.0f;
				float maxX = 1.0f;
				for (ChartItem item : chartData.getItems()) {
					if(item.isLine()){
						if(maxY < item.getMax()){
							maxY = item.getMax();
						}
					}else {
						if(maxX < item.getMax()){
							maxX = item.getMax();
						}

					}
				}
				scrollerKoeficientY = unselectRect.height() / maxY;
				contentKoeficientY = contentRect.height() / maxY;
				Paint paint = new Paint();
				paint.setStyle(Paint.Style.FILL);
				paint.setStrokeWidth(dpToPx(getContext(),1f));

				for (ChartItem item : chartData.getItems()) {
					scrollerKoeficientX = canvas.getWidth() / (float)item.getPositions().size();
					if(item.isLine()){
						paint.setColor(Color.parseColor(item.getColor()));
						for (int i = 0; i < item.getPositions().size() - 1; i++) {
							float nextX = scrollerKoeficientX * (i + 1);
							float scrollerY = unselectRect.bottom - (item.getPositions().get(i + 1) * scrollerKoeficientY);
							float contentY = contentRect.bottom - (item.getPositions().get(i + 1) * contentKoeficientY);
							canvas.drawLine(i * scrollerKoeficientX, unselectRect.bottom - (item.getPositions().get(i) * scrollerKoeficientY),nextX,scrollerY,paint);
							canvas.drawLine(i * scrollerKoeficientX, contentRect.bottom - (item.getPositions().get(i) * contentKoeficientY),nextX,contentY,paint);
						}
					}
				}
			}
		}
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

	public void bindData(List<ChartData> chartData) {
		this.data = chartData;
		invalidate();
	}

	public static int dpToPx(Context context, float dp) {
		Resources r = context.getResources();
		return (int) (TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()) + 0.5f);
	}
}

