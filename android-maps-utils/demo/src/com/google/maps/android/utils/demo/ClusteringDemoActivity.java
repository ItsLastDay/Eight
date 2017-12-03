/*
 * Copyright 2013 Google Inc.
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

import android.content.res.Resources;
import android.graphics.Color;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.google.maps.android.utils.demo.model.MyItem;

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

/**
 * Simple activity demonstrating ClusterManager.
 */
public class ClusteringDemoActivity extends BaseDemoActivity {
    private static final int ALT_HEATMAP_RADIUS = 40;

    private static final double ALT_HEATMAP_OPACITY = 100;

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
    private static final Map<Integer, Integer> enumToJsonName = Collections.unmodifiableMap(
            new HashMap<Integer, Integer>() {
                {
                    put(R.id.arts_centre, R.raw.child0);
                    put(R.id.school, R.raw.child1);
                    put(R.id.university, R.raw.child2);
                    put(R.id.college, R.raw.child3);
                    put(R.id.kindergarten, R.raw.child4);
                    put(R.id.language_school, R.raw.child5);
                    put(R.id.bank, R.raw.money0);
                    put(R.id.bureau_de_change, R.raw.money1);
                    put(R.id.payment_terminal, R.raw.money2);
                    put(R.id.cinema, R.raw.wo0);
                    put(R.id.spa, R.raw.wo1);
                    put(R.id.pharmacy, R.raw.wo2);
                    put(R.id.bicycle_parking, R.raw.sport0);
                    put(R.id.bicycle_rental, R.raw.sport1);
                    put(R.id.training, R.raw.sport2);
                    put(R.id.boat_rental, R.raw.sport3);
                    put(R.id.pub_small, R.raw.pub0);
                    put(R.id.cafe, R.raw.pub1);
                    put(R.id.club, R.raw.pub2);
                    put(R.id.nightclub, R.raw.pub3);
                }
            });
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

        //selection.setText(getCheckedState());

        changeClusters();

    }

    private ClusterManager<MyItem> mClusterManager;

    @Override
    protected int getLayoutId() {
        return R.layout.clustering_demo;
    }

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(59.9343, 30.3351), 12));
        SeekBar simpleSeekBar=(SeekBar)findViewById(R.id.seekBar);
        // perform seek bar change listener event used for getting the progress value

        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            {
                try {
                    mProvider = new HeatmapTileProvider.Builder().weightedData(
                            getListOnPos(progressChangedValue)).build();
                    mProvider.setRadius(ALT_HEATMAP_RADIUS);
                    mProvider.setOpacity(ALT_HEATMAP_OPACITY);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                changeClusters();
            }


            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    mProvider = new HeatmapTileProvider.Builder().weightedData(
                            getListOnPos(progressChangedValue)).build();
                    mProvider.setRadius(ALT_HEATMAP_RADIUS);
                    mProvider.setOpacity(ALT_HEATMAP_OPACITY);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (getMap() != null) {
                    getMap().clear();
                }
                changeClusters();
                mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));



                Toast.makeText(ClusteringDemoActivity.this, "Seek bar progress is :" + progressChangedValue,
                        Toast.LENGTH_SHORT).show();
            }

            List<WeightedLatLng> getListOnPos(int pos) throws JSONException {
                ArrayList<WeightedLatLng> list = new ArrayList<>();
                String file = "d201" + Integer.toString(pos);
                Resources res = getResources();
                try {
                    int sourceId = res.getIdentifier(file, "raw", getPackageName());
                    InputStream inputStream = getResources().openRawResource(sourceId);
                    String json = new Scanner(inputStream).useDelimiter("\\A").next();
                    JSONArray array = new JSONArray(json);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        double lat = object.getDouble("lat");
                        double lng = object.getDouble("lng");
                        double weight = object.getDouble("weight");
                        list.add(new WeightedLatLng(new LatLng(lat, lng), weight));
                    }
                    return list;
                } catch (Resources.NotFoundException err) {
                    System.out.println(file);
                    return null;
                }
            }
        });
        mClusterManager = new ClusterManager<>(this, getMap());
        getMap().setOnCameraIdleListener(mClusterManager);
    }

    private void changeClusters() {
        mClusterManager.clearItems();

        for (Integer item: enumToChecked.keySet()) {
            if (!enumToChecked.get(item) || !enumToJsonName.containsKey(item))
                continue;

            InputStream inputStream = getResources().openRawResource(enumToJsonName.get(item));
            List<MyItem> items = null;
            try {
                items = new MyItemReader().read(inputStream);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mClusterManager.addItems(items);
        }
    }
}