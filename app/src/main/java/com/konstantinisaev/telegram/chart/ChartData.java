package com.konstantinisaev.telegram.chart;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChartData {

	private List<ChartItem> items;
	private ChartItem xItem;
	private long maxY;
	private long maxX;

	public List<ChartItem> getItems() {
		return items;
	}

	public void setItems(List<ChartItem> items) {
		this.items = new ArrayList<>();
		Collections.sort(items, new Comparator<ChartItem>() {
			@Override
			public int compare(ChartItem o1, ChartItem o2) {
				int index = 0;
				if(o1.isLine() && !o2.isLine()){
					index = 1;
				}else if(!o1.isLine() && !o2.isLine()){
					index = -1;
				}
				return index;
			}
		});
		for (ChartItem item : items) {
			if(item.isLine() ){
				this.items.add(item);
			}else {
				xItem = item;
			}
		}
	}

	public ChartItem getxItem() {
		return xItem;
	}

	public static ChartData parseFromJson(JSONObject jsonObject){
		List<ChartItem> chartItems = new ArrayList<>();
		try {
			JSONObject types = jsonObject.getJSONObject("types");
			JSONObject names = jsonObject.getJSONObject("names");
			JSONObject colors = jsonObject.getJSONObject("colors");
			JSONArray columns = jsonObject.getJSONArray("columns");
			Map<String,ChartItem> chartsItemMap = new HashMap<>();

			Iterator<String> keys = types.keys();
			while (keys.hasNext()){
				ChartItem chartItem = new ChartItem();
				chartItem.setPositions(new ArrayList<Long>());
				String key = keys.next();
				chartItem.setType(types.getString(key));
				chartsItemMap.put(key,chartItem);
			}

			Iterator<String> namesKeys = names.keys();
			while (namesKeys.hasNext()){
				String key = namesKeys.next();
				ChartItem chartItem = chartsItemMap.get(key);
				if(chartItem != null){
					chartItem.setTitle(names.getString(key));
				}
			}
			Iterator<String> colorsKeys = colors.keys();
			while (colorsKeys.hasNext()){
				String key = colorsKeys.next();
				ChartItem chartItem = chartsItemMap.get(key);
				if(chartItem != null){
					chartItem.setColor(colors.getString(key));
				}
			}
			for (int i = 0; i < columns.length(); i++){
				JSONArray values = columns.getJSONArray(i);
				String currentKey = "";
				for (int k = 0; k < values.length(); k++){
					if(k == 0){
						currentKey = values.getString(k);
					}else {
						ChartItem chartItem = chartsItemMap.get(currentKey);
						if(chartItem != null){
							chartItem.addPosition(values.getLong(k));
						}
					}
				}
			}

			chartItems.addAll(chartsItemMap.values());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ChartData chartData = new ChartData();
		chartData.setItems(chartItems);
		return chartData;
	}

}

class ChartItem {

	private String uuid;
	private String type;
	private String title;
	private String color;
	private List<Long> positions;
	private long max;
	private boolean checked;

	public ChartItem() {
		uuid = UUID.randomUUID().toString();
	}

	public static ChartItem copyWithoutPositions(ChartItem chartItem){
		ChartItem copy = new ChartItem();
		copy.uuid = chartItem.uuid;
		copy.type = chartItem.type;
		copy.title = chartItem.title;
		copy.color = chartItem.color;
		copy.positions = new ArrayList<>();
		return copy;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public List<Long> getPositions() {
		return positions;
	}

	public void setPositions(List<Long> positions) {
		this.positions = positions;
	}

	public long getMax() {
		return max;
	}

	public void setMax(long max) {
		this.max = max;
	}

	public String getUuid() {
		return uuid;
	}

	public void addPosition(Long position){
		positions.add(position);
		if(max < position){
			max = position;
		}
	}

	public boolean isLine(){
		return !TextUtils.isEmpty(type) && type.equals("line");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ChartItem)) return false;
		ChartItem chartItem = (ChartItem) o;
		return uuid.equals(chartItem.uuid);
	}

	@Override
	public int hashCode() {
		return uuid.hashCode();
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}
}
