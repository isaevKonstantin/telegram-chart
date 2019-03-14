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
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class ChartView extends View {

	private static final String TAG = ChartView.class.getSimpleName();
	private Paint grayPaint;
	private Paint whitePaint;
	private Paint circlePaint;
	private Rect selectRect;

	private float beginX;
	private float endX;
	private Paint blueStrokePaint;
	private List<ChartItem> data;
	private float scrollerHeight = dpToPx(getContext(),80f);
	private float contentHeight = dpToPx(getContext(),300f);
	private float verticalMargin = dpToPx(getContext(),16f);
	private float oneDp = dpToPx(getContext(),1f);
	private float textHeaderInPixels = getResources().getDimensionPixelSize(R.dimen.text18);
	private float textTwelveInPixels = getResources().getDimensionPixelSize(R.dimen.text12);

	private Rect unselectRect;
	private boolean movingScroll = false;
	private boolean draggingLeft = false;
	private boolean draggingRight = false;
	private boolean movingContent = false;
	private boolean initView = false;
	private int fiveDp = dpToPx(getContext(), 5f);
	private float maxYScrollAll = 0f;
	private int fifteenthDp = dpToPx(getContext(), 25f);
	private Rect contentRect;
	private float contentTouchX = -1f;

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

		circlePaint = new Paint();
		circlePaint.setStyle(Paint.Style.FILL);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawGridBackground(canvas);
	}

	protected void drawGridBackground(Canvas canvas) {

		Paint textPaint = new Paint();
		textPaint.setStyle(Paint.Style.FILL);

		textPaint.setColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
		textPaint.setTextSize(textHeaderInPixels);
		canvas.drawText(getResources().getString(R.string.main_chart_title), 0, textHeaderInPixels, textPaint);

		contentRect = new Rect(0,(int)(textHeaderInPixels + verticalMargin), canvas.getWidth(), Math.round(contentHeight));
		canvas.drawRect(contentRect, whitePaint);

		int topScrollerY = Math.round(contentHeight + verticalMargin);
		int bottomScrollerY = Math.round(contentHeight + verticalMargin + scrollerHeight);
		if(unselectRect == null){
			unselectRect = new Rect(0,topScrollerY, canvas.getWidth(), bottomScrollerY);
		}
		canvas.drawRect(unselectRect, grayPaint);

		if(!initView){
			int initialStartPosition = 80;
			beginX = Math.round((canvas.getWidth() / 100f) * initialStartPosition);
			endX = canvas.getWidth();
			selectRect = new Rect((int) beginX,topScrollerY,(int) endX, bottomScrollerY);
		}else {
			selectRect = new Rect(Math.round(beginX),topScrollerY,Math.round(endX),bottomScrollerY);
		}

		canvas.drawRect(selectRect, whitePaint);

		blueStrokePaint.setStrokeWidth(fiveDp);
		canvas.drawLine(selectRect.left,selectRect.top,selectRect.left,selectRect.bottom, blueStrokePaint);
		canvas.drawLine(selectRect.right,selectRect.top,selectRect.right,selectRect.bottom, blueStrokePaint);
		blueStrokePaint.setStrokeWidth(oneDp);
		canvas.drawLine(selectRect.left,selectRect.top,selectRect.right,selectRect.top, blueStrokePaint);
		canvas.drawLine(selectRect.left,selectRect.bottom,selectRect.right,selectRect.bottom, blueStrokePaint);

		if(!data.isEmpty()){
			float maxYContentAll = 0f;
			float scrollerKoeficientY = unselectRect.height() / maxYScrollAll;
			float scrollerKoeficientX;
			float contentKoeficientX;
			Paint paint = new Paint();
			paint.setStyle(Paint.Style.FILL);
			paint.setStrokeWidth(oneDp);

			List<ChartItem> contentItems = new ArrayList<>();
			for (ChartItem item : data) {
				scrollerKoeficientX = canvas.getWidth() / (float)item.getPositions().size();
				if(item.isLine()){
					paint.setColor(Color.parseColor(item.getColor()));
					ChartItem copy = ChartItem.copyWithoutPositions(item);
					for (int i = 0; i < item.getPositions().size() - 1; i++) {
						float nextX = scrollerKoeficientX * (i + 1);
						float scrollerY = unselectRect.bottom - (item.getPositions().get(i + 1) * scrollerKoeficientY);
						canvas.drawLine(i * scrollerKoeficientX, unselectRect.bottom - (item.getPositions().get(i) * scrollerKoeficientY),nextX,scrollerY,paint);
						float contentStartX = i * scrollerKoeficientX;
						if(contentStartX >= selectRect.left && nextX <= selectRect.right){
							if(maxYContentAll < item.getPositions().get(i)){
								maxYContentAll = item.getPositions().get(i);
							}
							if(contentItems.contains(copy)){
								contentItems.get(contentItems.indexOf(copy)).addPosition(item.getPositions().get(i));
							}else {
								copy.addPosition(item.getPositions().get(i));
								contentItems.add(copy);
							}
						}
					}
				}
			}

			float stepYLines = contentRect.height() / 5f;
			float stepYPositions = maxYContentAll / 5f;
			float initialYLines = contentRect.bottom;
			float initalYPosition = 0f;
			textPaint.setTextSize(textTwelveInPixels);
			while (initialYLines > contentRect.top){
				initialYLines -= stepYLines;
				initalYPosition += stepYPositions;
				canvas.drawLine(contentRect.left,initialYLines, contentRect.right,initialYLines,blueStrokePaint);
				canvas.drawText(String.valueOf(Math.round(initalYPosition)), contentRect.left, initialYLines - dpToPx(getContext(),3f), textPaint);
			}
			canvas.drawLine(contentRect.left, contentRect.bottom, contentRect.right, contentRect.bottom,blueStrokePaint);
			canvas.drawText(getContext().getString(R.string.zero), contentRect.left, contentRect.bottom - dpToPx(getContext(),3f), textPaint);

			float contentKoeficientY = contentRect.height() / maxYContentAll;
			for (ChartItem contentItem : contentItems) {
				contentKoeficientX = canvas.getWidth() / (float)contentItem.getPositions().size();
				boolean isDrawCircle = false;
				paint.setColor(Color.parseColor(contentItem.getColor()));
				circlePaint.setColor(Color.parseColor(contentItem.getColor()));
				for (int i = 0; i < contentItem.getPositions().size() - 1; i++) {
					float nextX = contentKoeficientX * (i + 1);
					float nextY = contentRect.bottom - (contentItem.getPositions().get(i + 1) * contentKoeficientY);
					float contentStartY = contentRect.bottom - (contentItem.getPositions().get(i) * contentKoeficientY);
					float contentStartX = i * contentKoeficientX;
					canvas.drawLine(contentStartX, contentStartY ,nextX,nextY,paint);
					if(contentTouchX >= 0f && !isDrawCircle && (contentTouchX - contentStartX) < contentKoeficientX){
						isDrawCircle = true;
						canvas.drawLine(contentStartX, contentRect.bottom, contentStartX, contentRect.top,blueStrokePaint);
						canvas.drawCircle(contentStartX,contentStartY,dpToPx(getContext(),4f),circlePaint);
						circlePaint.setColor(ContextCompat.getColor(getContext(),R.color.white));
						canvas.drawCircle(contentStartX,contentStartY,dpToPx(getContext(),3f),circlePaint);
					}
				}
			}
		}

		if(!initView){
			initView = true;
		}
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			float x = event.getX();
			float y = event.getY();
			if(movingContent || (x >= contentRect.left && x <= contentRect.right && y >= contentRect.top && y <= contentRect.bottom)) {
				log("MotionEvent.ACTION_DOWN_Content");
				movingContent = true;
				contentTouchX = x;
				invalidate();
			}
		}

		if (event.getAction() == MotionEvent.ACTION_MOVE){
			log("MotionEvent.ACTION_MOVE");
			float x = event.getX();
			float y = event.getY();
			if(draggingLeft || movingScroll || draggingRight || (x >= selectRect.left && x <= selectRect.right && y >= selectRect.top && y <= selectRect.bottom)){
				if(draggingLeft || (!movingScroll && selectRect.left - fifteenthDp < x && x < selectRect.left + fifteenthDp && ( y >= selectRect.top && y <= selectRect.bottom))){
					log("Drag left");
					draggingLeft = true;
					beginX = x - getX();
					if(beginX < unselectRect.left){
						beginX = unselectRect.left;
					}
					if(beginX > selectRect.right - fifteenthDp){
						beginX = selectRect.right - fifteenthDp;
					}
					endX = selectRect.right;
					invalidate();
				}else if(draggingRight || (!movingScroll && selectRect.right - fifteenthDp < x && x < selectRect.right + fifteenthDp && ( y >= selectRect.top && y <= selectRect.bottom))){
					draggingRight = true;
					endX = x - getX();
					if(endX > unselectRect.right){
						endX = unselectRect.right;
					}
					if(endX < selectRect.left + fifteenthDp){
						endX = selectRect.left + fifteenthDp;
					}
					beginX = selectRect.left;
					invalidate();
				}else {
					moveX(x);
				}
			}else if(movingContent || (x >= contentRect.left && x <= contentRect.right && y >= contentRect.top && y <= contentRect.bottom)) {
				movingContent = true;
				contentTouchX = x;
				invalidate();
			}
		}

		if(event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_SCROLL){
			movingScroll = false;
			draggingLeft = false;
			draggingRight = false;
			movingContent = false;
		}
		log(event.toString());
		return true;
	}



	private void moveX(float x) {
		log("Move");
		movingScroll = true;
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

	private void log(String message){
		Log.i(TAG,message);
	}

	public void bindData(List<ChartItem> chartData) {
		this.data = chartData;
		maxYScrollAll = 0f;
		for (ChartItem chartItem : chartData) {
			if(chartItem.isLine() && maxYScrollAll < chartItem.getMax()){
				maxYScrollAll = chartItem.getMax();
			}
		}
		invalidate();
	}

	public boolean isScrollUnavailable(){
		return movingContent || movingScroll || draggingRight || draggingLeft;
	}

	public static int dpToPx(Context context, float dp) {
		Resources r = context.getResources();
		return (int) (TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()) + 0.5f);
	}
}

