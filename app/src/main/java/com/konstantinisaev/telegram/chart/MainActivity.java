package com.konstantinisaev.telegram.chart;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

	private ChartView chartView;
	private LinearLayout container;
	private ScrollView scrollView;
	private List<ChartItem> chartData = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		chartView = findViewById(R.id.chartView);
		container = findViewById(R.id.container);
		scrollView = findViewById(R.id.scroll);

		parseJson();
	}

	private void parseJson() {
		String json = loadJSONFromAsset();
		if(TextUtils.isEmpty(json)){
			return;
		}
		try {
			JSONArray jsonArray = new JSONArray(json);
			chartData.clear();
			for (int i = 0;i < jsonArray.length();i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				ChartData data = ChartData.parseFromJson(jsonObject);
				for (ChartItem item : data.getItems()) {
					if(item.isLine()){
						View view = getLayoutInflater().inflate(R.layout.item_chart,null,false);
						CheckBox itemCheckbox = view.findViewById(R.id.chItem);
						itemCheckbox.setText(item.getTitle());
						itemCheckbox.setChecked(false);
						CompoundButtonCompat.setButtonTintList(itemCheckbox, ColorStateList.valueOf(Color.parseColor(item.getColor())));
						view.setTag(item.getUuid());
						itemCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
								reloadData();
							}
						});
						container.addView(view);
					}
					chartData.add(item);
				}
			}
			reloadData();
			Log.d(MainActivity.class.getSimpleName(),jsonArray.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void reloadData(){
		List<ChartItem> reloadList = new ArrayList<>();
		for (ChartItem item : chartData) {
			if(item.isLine()){
				View view = container.findViewWithTag(item.getUuid());
				CheckBox checkBox = view.findViewById(R.id.chItem);
				if(checkBox.isChecked()){
					reloadList.add(item);
				}
			}
		}
		chartView.bindData(reloadList);

	}

	private String loadJSONFromAsset() {
		String json;
		try {
			InputStream is = this.getAssets().open("chart_data.json");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			json = new String(buffer);
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return json;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main,menu);
		return super.onCreateOptionsMenu(menu);
	}
}

