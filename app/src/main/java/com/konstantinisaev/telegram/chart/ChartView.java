package com.konstantinisaev.telegram.chart;

import android.content.Context;
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
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.konstantinisaev.telegram.chart.ResolutionUtils.dpToPx;

public class ChartView extends View {

	private static final String TAG = ChartView.class.getSimpleName();
	private Paint scrollerPaint;
	private Paint whitePaint;
	private Paint circlePaint;
	private Paint blueStrokePaint;
	private Paint scrollerChartPaint;
	private Paint upperChartPaint;

	private Rect selectRect;
	private Rect unselectRect;


	private UpperChartDividers upperChartDividers;
	private SelectedRectBorder selectedRectBorder;
	private UpperDateDrawer upperDateDrawer;

	private List<Pair<Path,String>> scrollerChartPaths = new ArrayList<>();
	private List<Pair<Path,String>> upperChartPaths = new ArrayList<>();

	private Rect infoRect;

	private List<ChartData> data;
	private float scrollerHeight = dpToPx(getContext(),80f);
	private float contentHeight = dpToPx(getContext(),300f);
	private float verticalMargin = dpToPx(getContext(),16f);

	private float textHeaderInPixels = getResources().getDimensionPixelSize(R.dimen.text18);
	private float textTwelveInPixels = getResources().getDimensionPixelSize(R.dimen.text12);

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
	private Paint headerPaint;

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
		scrollerPaint = new Paint();
		scrollerPaint.setStyle(Paint.Style.FILL);
		scrollerPaint.setColor(ContextCompat.getColor(getContext(), R.color.gray_scroller));

		whitePaint = new Paint();
		whitePaint.setStyle(Paint.Style.FILL);
		whitePaint.setColor(ContextCompat.getColor(getContext(), R.color.white));

		blueStrokePaint = new Paint();
		blueStrokePaint.setColor(ContextCompat.getColor(getContext(), R.color.blue_scroller));
		blueStrokePaint.setStyle(Paint.Style.STROKE);

		scrollerChartPaint = new Paint();
		scrollerChartPaint.setStyle(Paint.Style.STROKE);
		scrollerChartPaint.setStrokeWidth(dpToPx(getContext(),1f));

		upperChartPaint = new Paint();
		upperChartPaint.setStyle(Paint.Style.STROKE);
		upperChartPaint.setStrokeWidth(dpToPx(getContext(),1f));

		circlePaint = new Paint();
		circlePaint.setStyle(Paint.Style.FILL);

		headerPaint = new Paint();
		headerPaint.setStyle(Paint.Style.FILL);
		headerPaint.setColor(ContextCompat.getColor(getContext(),R.color.colorPrimary));
		headerPaint.setTextSize(textHeaderInPixels);

		upperChartDividers = new UpperChartDividers();
		selectedRectBorder = new SelectedRectBorder();
		upperDateDrawer = new UpperDateDrawer();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		long start = System.currentTimeMillis();
		log(String.format("onDraw starting time: %s",String.valueOf(start)));
		if(contentRect == null){
			contentRect = new Rect(0,(int)(textHeaderInPixels + verticalMargin), getWidth(), Math.round(contentHeight));
		}

		if(unselectRect == null){
			int topScrollerY = Math.round(contentHeight + verticalMargin);
			int bottomScrollerY = Math.round(contentHeight + verticalMargin + scrollerHeight);
			unselectRect = new Rect(0,topScrollerY, getWidth(), bottomScrollerY);
		}

		canvas.drawText(getResources().getString(R.string.main_chart_title), 0, textHeaderInPixels, headerPaint);
		canvas.drawRect(contentRect, whitePaint);
		canvas.drawRect(unselectRect, scrollerPaint);
		if(selectRect != null){
			canvas.drawRect(selectRect, whitePaint);
			selectedRectBorder.onDraw(canvas);
		}

		for (Pair<Path, String> scrollerChartPath : scrollerChartPaths) {
			scrollerChartPaint.setColor(Color.parseColor(scrollerChartPath.second));
			canvas.drawPath(scrollerChartPath.first,scrollerChartPaint);
		}

		for (Pair<Path, String> upperChartPath : upperChartPaths) {
			upperChartPaint.setColor(Color.parseColor(upperChartPath.second));
			canvas.drawPath(upperChartPath.first,upperChartPaint);
		}

		upperChartDividers.onDraw(canvas);
		upperDateDrawer.onDraw(canvas);

		if(!initView){
			initView = true;
		}



		log(String.format("onDraw executing time: %s",String.valueOf(System.currentTimeMillis() - start)));
		return;
	}

	private void drawGridBackground(Canvas canvas) {

//
//			float contentKoeficientY = contentRect.height() / maxYContentAll;
//			float centerOfInfoY = 0f;
//			float centerOfInfoX = 0f;
//			int infoDatePostion = 0;
//			Long longDate = 0L;
//			List<Counters> counters = new ArrayList<>();
//			for (Pair<ChartItem,ChartItem> pair : contentYItems) {
//				ChartItem yItem = pair.first;
//				ChartItem xItem = pair.second;
//				contentKoeficientX = contentRect.width() / (float)yItem.getPositions().size();
//				boolean isDrawCircle = false;
//				paint.setColor(Color.parseColor(yItem.getColor()));
//				paint.setStyle(Paint.Style.STROKE);
//				circlePaint.setColor(Color.parseColor(yItem.getColor()));
//				Path path = new Path();
//				for (int i = 0; i < yItem.getPositions().size(); i++) {
//					float contentStartY = contentRect.bottom - (yItem.getPositions().get(i) * contentKoeficientY);
//					float contentStartX = i * contentKoeficientX;
//					if(i == 0){
//						path.moveTo(0f,contentStartY);
//					}else {
//						path.lineTo(contentStartX,contentStartY);
//					}
//					if(contentTouchX >= 0f && !isDrawCircle && (contentTouchX - contentStartX) < contentKoeficientX){
//						infoDatePostion = i;
//						isDrawCircle = true;
//						canvas.drawLine(contentStartX, contentRect.bottom, contentStartX, contentRect.top,blueStrokePaint);
//						canvas.drawCircle(contentStartX,contentStartY,dpToPx(getContext(),4f),circlePaint);
//						circlePaint.setColor(ContextCompat.getColor(getContext(),R.color.white));
//						canvas.drawCircle(contentStartX,contentStartY,dpToPx(getContext(),3f),circlePaint);
//						centerOfInfoX = contentStartX;
//						counters.add(new Counters(yItem.getTitle(),yItem.getColor(),String.valueOf(yItem.getPositions().get(i))));
//						if(centerOfInfoY < contentStartY){
//							centerOfInfoY = contentStartY - dpToPx(getContext(),5f);
//						}
//					}
//				}
//				if(!path.isEmpty()){
//					canvas.drawPath(path,paint);
//				}
//
//				Set<String> dates = new LinkedHashSet<>();
//				List<Integer> xpositions = new ArrayList<>();
//				for (int i = 0; i < xItem.getPositions().size(); i++) {
//					boolean success = dates.add(formatDate(xItem.getPositions().get(i)));
//					if(success){
//						xpositions.add(i);
//					}
//				}
//
//				int position = 0;
//				for (String date : dates) {
//					headerPaint.setTextSize(textTwelveInPixels);
//					canvas.drawText(date,xpositions.get(position) * contentKoeficientX,contentRect.bottom + dpToPx(getContext(),10f), headerPaint);
//					position += 1;
//				}
//
//				longDate = xItem.getPositions().get(infoDatePostion);
//			}
//			if(centerOfInfoX > 0f && centerOfInfoY > 0f){
//				float width = dpToPx(getContext(),100f);
//				float height = dpToPx(getContext(),100f);
//				infoRect = new Rect(Math.round(centerOfInfoX - width / 3),Math.round(contentRect.top),Math.round(centerOfInfoX + width),Math.round(contentRect.top + height));
//				headerPaint.setTextSize(textTwelveInPixels);
//				canvas.drawRect(infoRect,whitePaint);
//				canvas.drawText(formatDate(longDate),infoRect.left,infoRect.top + dpToPx(getContext(),10f), headerPaint);
//				int k = 1;
//				for (Counters counter : counters) {
//					headerPaint.setColor(Color.parseColor(counter.color));
//					float textMargin = infoRect.top + dpToPx(getContext(),10f) + (textTwelveInPixels * k);
//					canvas.drawText(String.format("%s %s",counter.title,counter.counter),infoRect.left,textMargin, headerPaint);
//					k += 1;
//				}
//			}
//		}
//
//		if(!initView){
//			initView = true;
//		}
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		if (event.getAction() == MotionEvent.ACTION_MOVE){
			if(draggingLeft || movingScroll || draggingRight || (x >= selectRect.left && x <= selectRect.right && y >= selectRect.top && y <= selectRect.bottom)){
				if(draggingLeft || (!movingScroll && selectRect.left - fifteenthDp < x && x < selectRect.left + fifteenthDp && ( y >= selectRect.top && y <= selectRect.bottom))){
					dragLeft(x);
				}else if(draggingRight || (!movingScroll && selectRect.right - fifteenthDp < x && x < selectRect.right + fifteenthDp && ( y >= selectRect.top && y <= selectRect.bottom))){
					dragRight(x);
				}else {
					moveX(x);
				}
//			}else if(movingContent || (x >= contentRect.left && x <= contentRect.right && y >= contentRect.top && y <= contentRect.bottom)) {
//				handleTouchOnUpperChart(x, y);
			}
		}
//
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
		float beginX = x - getX() - (selectRect.width() / 2f);
		float endX = beginX + selectRect.width();

		if(beginX < unselectRect.left){
			beginX = unselectRect.left;
			endX = beginX + selectRect.width();
		}else if(endX > unselectRect.right){
			endX = unselectRect.right;
			beginX = unselectRect.right - selectRect.width();
		}
		createSelectRectIfNeed(Math.round(beginX),Math.round(endX));
		recreateSelectBorder();
		recreateUpperChart();
		invalidate();
	}

	private void dragLeft(float x){
		draggingLeft = true;
		float beginX = x - getX();
		if(beginX < unselectRect.left){
			beginX = unselectRect.left;
		}
		if(beginX > selectRect.right - fifteenthDp){
			beginX = selectRect.right - fifteenthDp;
		}
		float endX = selectRect.right;
		createSelectRectIfNeed(Math.round(beginX),Math.round(endX));
		recreateSelectBorder();
		recreateUpperChart();
		invalidate();
	}

	private void dragRight(float x){
		draggingRight = true;
		float endX = x - getX();
		if(endX > unselectRect.right){
			endX = unselectRect.right;
		}
		if(endX < selectRect.left + fifteenthDp){
			endX = selectRect.left + fifteenthDp;
		}
		float beginX = selectRect.left;
		createSelectRectIfNeed(Math.round(beginX),Math.round(endX));
		recreateSelectBorder();
		recreateUpperChart();
		invalidate();
	}

	private void log(String message){
		Log.w(TAG,message);
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
		if(selectRect == null && unselectRect != null){
			int initialStartPosition = 70;
			int beginX = Math.round((unselectRect.width() / 100f) * initialStartPosition);
			int endX = Math.round(unselectRect.width());
			createSelectRectIfNeed(beginX,endX);
		}
		recreateSelectBorder();
		recreateBottomChart();
		recreateUpperChart();
		invalidate();

	}

	private void createSelectRectIfNeed(int beginX,int endX){
		if(unselectRect == null){
			return;
		}

		selectRect = new Rect(beginX,unselectRect.top,endX, unselectRect.bottom);
	}

	private void recreateSelectBorder(){
		if(selectRect == null){
			return;
		}
		selectedRectBorder.build(selectRect);
	}

	private void recreateBottomChart(){
		if(unselectRect == null){
			return;
		}
		scrollerChartPaths.clear();
		float scrollerKoeficientY = unselectRect.height() / maxYScrollAll;
		for (ChartData chartData : data) {
			for (ChartItem item : chartData.getItems()) {
				float scrollerKoeficientX = unselectRect.width() / (float)item.getPositions().size();
				if(item.isChecked()){
					String color = item.getColor();
					Path path = new Path();
					for (int i = 0; i < item.getPositions().size(); i++) {
						if(i == 0){
							path.moveTo(unselectRect.left,unselectRect.bottom - (item.getPositions().get(i) * scrollerKoeficientY));
						}else if(i != item.getPositions().size() - 1){
							float nextX = scrollerKoeficientX * (i + 1) + scrollerKoeficientX;
							float nextY = unselectRect.bottom - (item.getPositions().get(i + 1) * scrollerKoeficientY);
							path.lineTo(nextX,nextY);
						}

					}
					scrollerChartPaths.add(new Pair<>(path, color));
				}
			}
		}
	}

	private void recreateUpperChart(){
		if(unselectRect == null){
			log("recreateUpperChart return");
			return;
		}
		float maxYContentAll = 0f;
		upperChartPaths.clear();
		int infoDatePostion = 0;
		Set<String> dates = new LinkedHashSet<>();
		List<Float> datePositions = new ArrayList<>();
		for (ChartData chartData : data) {
			for (ChartItem item : chartData.getItems()) {
				if(item.isChecked()){
					float contentRateX = contentRect.width() / (float)item.getPositions().size();
					String color = item.getColor();
					Path path = new Path();
					List<Long> selectedPositions = new ArrayList<>();
					for (int i = 0; i < item.getPositions().size(); i++) {
						float nextX = 0f;
						if(i == item.getPositions().size() - 1){
							nextX = unselectRect.right;
						}else if(i != item.getPositions().size() - 1){
							nextX = contentRateX * (i + 1) + contentRateX;
						}
						float contentStartX = i * contentRateX;
						if(contentStartX >= selectRect.left && nextX <= selectRect.right) {
							if (maxYContentAll < item.getPositions().get(i)) {
								maxYContentAll = item.getPositions().get(i);
							}
							selectedPositions.add(item.getPositions().get(i));
						}
					}

					contentRateX =  contentRect.width() / (float)selectedPositions.size();
					float contentRateY = contentRect.height() / maxYContentAll;
					for (int i = 0; i < selectedPositions.size(); i++) {
						float contentStartY = contentRect.bottom - (selectedPositions.get(i) * contentRateY);
						float contentStartX = i * contentRateX;
						if(path.isEmpty()){
							path.moveTo(contentRect.left,contentStartY);
						}else {
							path.lineTo(contentStartX,contentStartY);
						}
					}
					if(!path.isEmpty()){
						upperChartPaths.add(new Pair<>(path, color));
					}

					ChartItem xItem = chartData.getxItem();
					for (int i = 0; i < selectedPositions.size(); i++) {
						boolean success = dates.add(DateUtils.formatDate(xItem.getPositions().get(i)));
						if(success){
							datePositions.add(i * contentRateX);
						}
					}
				}
			}
		}



		upperChartDividers.clear();
		upperChartDividers.build(new UpperChartDividers.UpperChartDividerParam(contentRect,maxYContentAll));
		upperDateDrawer.build(new UpperDateDrawer.UpperDateDrawerParams(dates,datePositions, contentRect.bottom + dpToPx(getContext(),10f)));



//		Set<String> dates = new LinkedHashSet<>();
//				List<Integer> xpositions = new ArrayList<>();
//				for (int i = 0; i < xItem.getPositions().size(); i++) {
//					boolean success = dates.add(formatDate(xItem.getPositions().get(i)));
//					if(success){
//						xpositions.add(i);
//					}
//				}
//
//				int position = 0;
//				for (String date : dates) {
//					headerPaint.setTextSize(textTwelveInPixels);
//					canvas.drawText(date,xpositions.get(position) * contentKoeficientX,contentRect.bottom + dpToPx(getContext(),10f), headerPaint);
//					position += 1;
//				}
	}

	public boolean isScrollUnavailable(){
		return movingContent || movingScroll || draggingRight || draggingLeft;
	}


	private class Counters{

		public final String title;
		public final String color;
		public final String counter;

		public Counters(String title, String color, String counter) {
			this.title = title;
			this.color = color;
			this.counter = counter;
		}
	}
}


