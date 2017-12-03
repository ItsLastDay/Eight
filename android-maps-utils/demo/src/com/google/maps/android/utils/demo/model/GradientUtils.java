package com.google.maps.android.utils.demo.model;

import android.content.res.Resources;
import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
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


public class GradientUtils {
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.argb(0, 0, 255, 255),// transparent
            Color.argb(255 / 3 * 2, 221, 162, 0),
            Color.rgb(213,124,0),
            Color.rgb(198, 49, 0),
            Color.rgb(191, 22, 9)
    };

    private static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = {
            0.0f, 0.001f, 0.10f, 0.30f, 1.0f
    };

    private static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(GradientUtils.ALT_HEATMAP_GRADIENT_COLORS,
            GradientUtils.ALT_HEATMAP_GRADIENT_START_POINTS);

    private static final int ALT_HEATMAP_RADIUS = 40;

    private static final double ALT_HEATMAP_OPACITY = 1;

    public static HeatmapTileProvider makeProvider(int yearId, Resources res, String packName) {
        try {
            return new HeatmapTileProvider.Builder()
                    .weightedData(getListOnPos(yearId, res, packName))
                    .radius(ALT_HEATMAP_RADIUS)
                    .opacity(ALT_HEATMAP_OPACITY)
                    .gradient(GradientUtils.ALT_HEATMAP_GRADIENT)
                    .build();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static List<WeightedLatLng> getListOnPos(int pos, Resources res, String packName) throws JSONException {
        ArrayList<WeightedLatLng> list = new ArrayList<>();
        String file = "d201" + Integer.toString(pos);
        try {
            int sourceId = res.getIdentifier(file, "raw", packName);
            InputStream inputStream = res.openRawResource(sourceId);
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
}
