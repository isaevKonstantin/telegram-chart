package com.konstantinisaev.telegram.chart;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class ChartView extends View {

	private static final String TAG = ChartView.class.getSimpleName();
	private Paint grayPaint;
	private Paint whitePaint;
	private Paint circlePaint;
	private Rect selectRect;

	private float beginX;
	private float endX;
	private Paint blueStrokePaint;
	private List<ChartData> data;
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
	private float maxXScrollAll = 0f;
	private int fifteenthDp = dpToPx(getContext(), 25f);
	private Rect contentRect;
	private float contentTouchX = -1f;
	private Rect infoRect;

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
			int initialStartPosition = 90;
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

			List<ChartItem> contentYItems = new ArrayList<>();
			List<ChartItem> contentXItems = new ArrayList<>();
			for (ChartData chartData : data) {
				for (ChartItem item : chartData.getItems()) {
					scrollerKoeficientX = canvas.getWidth() / (float)item.getPositions().size();
					Set<Integer> selectedPositions = new HashSet<>();
					if(item.isChecked()){
						paint.setColor(Color.parseColor(item.getColor()));
						ChartItem copyY = ChartItem.copyWithoutPositions(item);
						for (int i = 0; i < item.getPositions().size(); i++) {
							float nextX = -1f;
							float nextY = -1f;
							if(i < item.getPositions().size() - 1){
								nextX = scrollerKoeficientX * (i + 1);
								nextY = unselectRect.bottom - (item.getPositions().get(i + 1) * scrollerKoeficientY);
							}
							if(nextX >= 0 && nextY >= 0){
								canvas.drawLine(i * scrollerKoeficientX, unselectRect.bottom - (item.getPositions().get(i) * scrollerKoeficientY),nextX,nextY,paint);
							}
							float contentStartX = i * scrollerKoeficientX;
							if(contentStartX >= selectRect.left && nextX <= selectRect.right){
								if(maxYContentAll < item.getPositions().get(i)){
									maxYContentAll = item.getPositions().get(i);
								}
								if(contentYItems.contains(copyY)){
									contentYItems.get(contentYItems.indexOf(copyY)).addPosition(item.getPositions().get(i));
								}else {
									copyY.addPosition(item.getPositions().get(i));
									contentYItems.add(copyY);
								}
								selectedPositions.add(i);
							}
						}

						ChartItem copyX = ChartItem.copyWithoutPositions(chartData.getxItem());
						if(!contentXItems.contains(chartData.getxItem())){
							for (Integer selectedPosition : selectedPositions) {
								copyX.addPosition(chartData.getxItem().getPositions().get(selectedPosition));
							}
							contentXItems.add(copyX);
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
			float centerOfInfoY = 0f;
			float centerOfInfoX = 0f;
			for (ChartItem contentItem : contentYItems) {
				contentKoeficientX = contentRect.width() / (float)contentItem.getPositions().size();
				boolean isDrawCircle = false;
				paint.setColor(Color.parseColor(contentItem.getColor()));
				paint.setStyle(Paint.Style.STROKE);
				circlePaint.setColor(Color.parseColor(contentItem.getColor()));
				Path path = new Path();
				for (int i = 0; i < contentItem.getPositions().size(); i++) {
					float contentStartY = contentRect.bottom - (contentItem.getPositions().get(i) * contentKoeficientY);
					float contentStartX = (i * contentKoeficientX) + contentKoeficientX;
					if(i == 0){
						path.moveTo(0f,contentStartY);
					}else {
						path.lineTo(contentStartX,contentStartY);
					}
					if(contentTouchX >= 0f && !isDrawCircle && (contentTouchX - contentStartX) < contentKoeficientX){
						isDrawCircle = true;
						canvas.drawLine(contentStartX, contentRect.bottom, contentStartX, contentRect.top,blueStrokePaint);
						canvas.drawCircle(contentStartX,contentStartY,dpToPx(getContext(),4f),circlePaint);
						circlePaint.setColor(ContextCompat.getColor(getContext(),R.color.white));
						canvas.drawCircle(contentStartX,contentStartY,dpToPx(getContext(),3f),circlePaint);
						centerOfInfoX = contentStartX;
						if(centerOfInfoY < contentStartY){
							centerOfInfoY = contentStartY - dpToPx(getContext(),5f);
						}
					}
				}
				if(!path.isEmpty()){
					canvas.drawPath(path,paint);
				}

				Set<String> dates = new LinkedHashSet<>();
				List<Integer> xpositions = new ArrayList<>();
				ChartItem xItem = contentXItems.get(0);
				for (int i = 0; i < xItem.getPositions().size(); i++) {
					boolean success = dates.add(formatDate(xItem.getPositions().get(i)));
					if(success){
						xpositions.add(i);
					}
				}

				int position = 0;
				for (String date : dates) {
					textPaint.setTextSize(textTwelveInPixels);
					canvas.drawText(date,xpositions.get(position) * contentKoeficientX,contentRect.bottom + dpToPx(getContext(),10f),textPaint);
					position += 1;
				}

			}
			if(centerOfInfoX > 0f && centerOfInfoY > 0f){
				float width = dpToPx(getContext(),100f);
				float height = dpToPx(getContext(),100f);
				infoRect = new Rect(Math.round(centerOfInfoX - width / 2),Math.round(centerOfInfoY - height),Math.round(centerOfInfoX + width / 2),Math.round(centerOfInfoY));
				canvas.drawRect(infoRect,whitePaint);
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
				handleTouchOnUpperChart(x, y);
			}
		}

		if (event.getAction() == MotionEvent.ACTION_MOVE){
			float x = event.getX();
			float y = event.getY();
			if(draggingLeft || movingScroll || draggingRight || (x >= selectRect.left && x <= selectRect.right && y >= selectRect.top && y <= selectRect.bottom)){
				if(draggingLeft || (!movingScroll && selectRect.left - fifteenthDp < x && x < selectRect.left + fifteenthDp && ( y >= selectRect.top && y <= selectRect.bottom))){
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
				handleTouchOnUpperChart(x, y);
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

	private void handleTouchOnUpperChart(float x, float y) {
		movingContent = true;
		if(infoRect != null && (x >= infoRect.left && x <= infoRect.right && y >= infoRect.top && y <= infoRect.bottom)){
			contentTouchX = -1f;
		}else {
			contentTouchX = x;
		}
		invalidate();
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

	public void bindData(List<ChartData> chartData) {
		this.data = chartData;
		maxYScrollAll = 0f;
		for (ChartData tmp : chartData) {
			for (ChartItem chartItem : tmp.getItems()) {
				if(chartItem.isLine() && maxYScrollAll < chartItem.getMax()){
					maxYScrollAll = chartItem.getMax();
				}
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

	private String formatDate(long date){
		String format = "d MMM";
		SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getDefault());
		calendar.setTimeInMillis(date);
		Date convertedDate = calendar.getTime();
		return dateFormat.format(convertedDate);
	}
}

