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

import android.content.res.Resources;
import android.graphics.Color;
import android.text.method.LinkMovementMethod;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A demo of the Heatmaps library. Demonstrates how the HeatmapTileProvider can be used to create
 * a colored map overlay that visualises many points of weighted importance/intensity, with
 * different colors representing areas of high and low concentration/combined intensity of points.
 */
public class HeatmapsDemoActivity extends BaseDemoActivity {

    /**
     * Alternative radius for convolution
     */
    private static final int ALT_HEATMAP_RADIUS = 40;

    /**
     * Alternative opacity of heatmap overlay
     */
    private static final double ALT_HEATMAP_OPACITY = 100;

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

    /**
     * Maps name of data set to data (list of LatLngs)
     * Also maps to the URL of the data set for attribution
     */
    @Override
    protected int getLayoutId() {
        return R.layout.heatmaps_demo;
    }

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(59.9343, 30.3351), 12));

        SeekBar simpleSeekBar=(SeekBar)findViewById(R.id.seekBar);
        // perform seek bar change listener event used for getting the progress value
        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

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
                mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                TextView attribution = ((TextView) findViewById(R.id.attribution));
                attribution.setMovementMethod(LinkMovementMethod.getInstance());

                Toast.makeText(HeatmapsDemoActivity.this, "Seek bar progress is :" + progressChangedValue,
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
    }
}
