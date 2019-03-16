package com.konstantinisaev.telegram.chart;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

	private ChartView chartView;
	private LinearLayout container;
	private ScrollView scrollView;
	private List<ChartData> chartData = new ArrayList<>();
	private static Context appContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		appContext = getApplicationContext();
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
					chartData.add(data);
				}
			}
			reloadData();
			Log.d(MainActivity.class.getSimpleName(),jsonArray.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void reloadData(){
		Set<ChartData> reloadSet = new HashSet<>();
		for (ChartData chartData : chartData) {
			for (ChartItem item : chartData.getItems()) {
				if(item.isLine()){
					View view = container.findViewWithTag(item.getUuid());
					CheckBox checkBox = view.findViewById(R.id.chItem);
					if(checkBox.isChecked()){
						reloadSet.add(chartData);
					}
					item.setChecked(checkBox.isChecked());
				}
			}

		}
		chartView.bindData(new ArrayList<>(reloadSet));

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

	public static Context getContext(){
		return appContext;
	}
}

