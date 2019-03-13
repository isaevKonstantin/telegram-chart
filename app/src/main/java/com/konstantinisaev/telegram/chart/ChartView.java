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
	private float textHeaderInPixels = getResources().getDimensionPixelSize(R.dimen.text18);
	private float textTwelveInPixels = getResources().getDimensionPixelSize(R.dimen.text12);

	private Rect unselectRect;
	private boolean moveSelected = false;
	private boolean initView = false;

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
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {}


	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		log("dispatchDraw");
		drawGridBackground(canvas);
	}

	protected void drawGridBackground(Canvas canvas) {

		Paint textPaint = new Paint();
		textPaint.setStyle(Paint.Style.FILL);

		textPaint.setColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
		textPaint.setTextSize(textHeaderInPixels);
		canvas.drawText(getResources().getString(R.string.main_chart_title), 0, textHeaderInPixels, textPaint);

		Rect contentRect = new Rect(0,(int)(textHeaderInPixels + verticalMargin), canvas.getWidth(), Math.round(contentHeight));
		canvas.drawRect(contentRect, whitePaint);

		int topScrollerY = Math.round(contentHeight + verticalMargin);
		int bottomScrollerY = Math.round(contentHeight + verticalMargin + scrollerHeight);
		if(unselectRect == null){
			unselectRect = new Rect(0,topScrollerY, canvas.getWidth(), bottomScrollerY);
		}
		canvas.drawRect(unselectRect, grayPaint);

		if(!initView){
			int initialStartPosition = 80;
			selectRect = new Rect((canvas.getWidth() / 100) * initialStartPosition,topScrollerY,canvas.getWidth(), bottomScrollerY);
		}else {
			selectRect = new Rect(Math.round(beginX),topScrollerY,Math.round(endX),bottomScrollerY);
		}

		canvas.drawRect(selectRect, whitePaint);

		blueStrokePaint.setStrokeWidth(dpToPx(getContext(),5f));
		canvas.drawLine(selectRect.left,selectRect.top,selectRect.left,selectRect.bottom, blueStrokePaint);
		canvas.drawLine(selectRect.right,selectRect.top,selectRect.right,selectRect.bottom, blueStrokePaint);
		blueStrokePaint.setStrokeWidth(dpToPx(getContext(),1f));
		canvas.drawLine(selectRect.left,selectRect.top,selectRect.right,selectRect.top, blueStrokePaint);
		canvas.drawLine(selectRect.left,selectRect.bottom,selectRect.right,selectRect.bottom, blueStrokePaint);

		float stepYLines = contentRect.height() / 5f;
		float initialYLines = contentRect.bottom;
		while (initialYLines > contentRect.top){
			initialYLines -= stepYLines;
			canvas.drawLine(contentRect.left,initialYLines,contentRect.right,initialYLines,blueStrokePaint);
		}
		canvas.drawLine(contentRect.left,contentRect.bottom,contentRect.right,contentRect.bottom,blueStrokePaint);
		textPaint.setTextSize(textTwelveInPixels);
		canvas.drawText(getContext().getString(R.string.zero), contentRect.left, contentRect.bottom - dpToPx(getContext(),3f), textPaint);

		if(!data.isEmpty()){
			for (ChartData chartData : data) {
				float scrollerKoeficientY;
				float contentKoeficientY;
				float scrollerKoeficientX;
				float maxY = chartData.getMaxY();
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
				initView = true;
			}
		}
		if(!initView){
			initView = true;
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			return true;
		}

		if (event.getAction() == MotionEvent.ACTION_MOVE){
			float x = event.getX();
			float y = event.getY();
			if(moveSelected || (x >= selectRect.left && x <= selectRect.right && y >= selectRect.top && y <= selectRect.bottom)){
				moveSelected = true;
				beginX = x - getX() - (selectRect.width() / 2f);
				endX = beginX + selectRect.width();

				if(beginX < unselectRect.left){
					beginX = unselectRect.left;
					endX = beginX + selectRect.width();
				}else if(endX > unselectRect.right){
					endX = unselectRect.right;
					beginX = unselectRect.right - selectRect.width();
				}
				invalidate();
			}
			return true;

		}
		if (event.getAction() == MotionEvent.ACTION_UP){
			moveSelected = false;
			return true;
		}

		return false;
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

