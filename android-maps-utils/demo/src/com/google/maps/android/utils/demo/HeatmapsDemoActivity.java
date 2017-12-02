/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.utils.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * A demo of the Heatmaps library. Demonstrates how the HeatmapTileProvider can be used to create
 * a colored map overlay that visualises many points of weighted importance/intensity, with
 * different colors representing areas of high and low concentration/combined intensity of points.
 */
public class HeatmapsDemoActivity extends BaseDemoActivity {

    private static final Map<Integer, List<Integer>> classes = Collections.unmodifiableMap(
        new HashMap<Integer, List<Integer>>() {
            {

                put(R.id.children, Arrays.asList(R.id.arts_centre, R.id.school, R.id.university, R.id.college, R.id.kindergarten, R.id.language_school));
                put(R.id.women, Arrays.asList(R.id.cinema, R.id.spa, R.id.pharmacy));
                put(R.id.money, Arrays.asList(R.id.bank, R.id.bureau_de_change, R.id.payment_terminal));
                put(R.id.sport, Arrays.asList(R.id.bicycle_parking, R.id.bicycle_rental, R.id.training, R.id.boat_rental));
                put(R.id.pub, Arrays.asList(R.id.pub_small, R.id.cafe, R.id.club, R.id.nightclub));            }
        }
    );
    private static final int totalClasses;

    private Map<Integer, Boolean> enumToChecked = new HashMap<>();
    private Map<Integer, Integer> enumToIndex = new HashMap<>();

    private void changeCheckedState(Integer enumItem, boolean newVal) {
        enumToChecked.put(enumItem, newVal);
        ((CheckBox)findViewById(enumItem)).setChecked(enumToChecked.get(enumItem));
    }

    private String getCheckedState() {
        char[] s = new char[totalClasses];
        for (Integer enumItem: enumToChecked.keySet()) {
            s[enumToIndex.get(enumItem)] = enumToChecked.get(enumItem) ? '1' : '0';
        }
        return new String(s);
    }

    {
        for (Integer item: classes.keySet()) {
            enumToIndex.put(item, enumToIndex.size());
        }
        for (List<Integer> item: classes.values()) {
            for (Integer cls: item) {
                enumToIndex.put(cls, enumToIndex.size());
            }
        }

        for (Integer enumItem: enumToIndex.keySet()) {
            enumToChecked.put(enumItem, false);
        }
    }

    static {
        int addClasses = 0;
        for (List<Integer> item: classes.values()) {
            addClasses += item.size();
        }

        totalClasses = classes.size() + addClasses;
    }

    public void onCheckboxClicked(View view) {
        TextView selection = (TextView) findViewById(R.id.selection);
        Integer selectedThing = view.getId();
        boolean newVal = !enumToChecked.get(selectedThing);
        if (classes.keySet().contains(selectedThing)) {
            // Superclass: need to modify it and children
            for (Integer item: classes.get(selectedThing)) {
                changeCheckedState(item, newVal);
            }
        }
        changeCheckedState(selectedThing, newVal);

        selection.setText(getCheckedState());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView selection = (TextView) findViewById(R.id.selection);
        selection.setText(getCheckedState());
    }

    /**
     * Alternative radius for convolution
     */
    private static final int ALT_HEATMAP_RADIUS = 10;

    /**
     * Alternative opacity of heatmap overlay
     */
    private static final double ALT_HEATMAP_OPACITY = 0.4;

    /**
     * Alternative heatmap gradient (blue -> red)
     * Copied from Javascript version
     */
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.argb(0, 0, 255, 255),// transparent
            Color.argb(255 / 3 * 2, 0, 255, 255),
            Color.rgb(0, 191, 255),
            Color.rgb(0, 0, 127),
            Color.rgb(255, 0, 0)
    };

    public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = {
            0.0f, 0.10f, 0.20f, 0.60f, 1.0f
    };

    public static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(ALT_HEATMAP_GRADIENT_COLORS,
            ALT_HEATMAP_GRADIENT_START_POINTS);

    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;

    private boolean mDefaultGradient = true;
    private boolean mDefaultRadius = true;
    private boolean mDefaultOpacity = true;

    /**
     * Maps name of data set to data (list of LatLngs)
     * Also maps to the URL of the data set for attribution
     */
    private HashMap<String, DataSet> mLists = new HashMap<String, DataSet>();

    @Override
    protected int getLayoutId() {
        return R.layout.heatmaps_demo;
    }

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(60.0119720000000, 29.72), 12));

        // Set up the spinner/dropdown list
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.heatmaps_datasets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        try {
            mLists.put(getString(R.string.police_stations), new DataSet(readItems(R.raw.police),
                    getString(R.string.police_stations_url)));
            mLists.put(getString(R.string.medicare), new DataSet(readItems(R.raw.medicare),
                    getString(R.string.medicare_url)));
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
        }

        // Make the handler deal with the map
        // Input: list of WeightedLatLngs, minimum and maximum zoom levels to calculate custom
        // intensity from, and the map to draw the heatmap on
        // radius, gradient and opacity not specified, so default are used
    }


        // Datasets from http://data.gov.au
    private ArrayList<LatLng> readItems(int resource) throws JSONException {
        ArrayList<LatLng> list = new ArrayList<LatLng>();
        InputStream inputStream = getResources().openRawResource(resource);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            list.add(new LatLng(lat, lng));
        }
        return list;
    }

    /**
     * Helper class - stores data sets and sources.
     */
    private class DataSet {
        private ArrayList<LatLng> mDataset;
        private String mUrl;

        public DataSet(ArrayList<LatLng> dataSet, String url) {
            this.mDataset = dataSet;
            this.mUrl = url;
        }

        public ArrayList<LatLng> getData() {
            return mDataset;
        }

        public String getUrl() {
            return mUrl;
        }
    }

}
