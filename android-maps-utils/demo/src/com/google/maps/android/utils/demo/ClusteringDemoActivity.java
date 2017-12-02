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

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.utils.demo.model.MyItem;

import org.json.JSONException;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple activity demonstrating ClusterManager.
 */
public class ClusteringDemoActivity extends BaseDemoActivity {

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
    private static final Map<Integer, String> enumToJsonName = Collections.unmodifiableMap(
            new HashMap<Integer, String>() {
                {
                    put(R.id.bicycle_parking, "sport0.json");
                    put(R.id.bicycle_rental, "sport1.json");
                    put(R.id.training, "sport2.json");
                    put(R.id.boat_rental, "sport3.json");
                    put(R.id.bank, "money0.json");
                    put(R.id.bureau_de_change, "money1.json");
                    put(R.id.payment_terminal, "money2.json");
                    put(R.id.cinema, "wo0.json");
                    put(R.id.spa, "wo1.json");
                    put(R.id.pharmacy, "wo2.json");
                    put(R.id.pub_small, "pub0.json");
                    put(R.id.cafe, "pub1.json");
                    put(R.id.club, "pub2.json");
                    put(R.id.nightclub, "pub3.json");
                    put(R.id.arts_centre, "child0.json");
                    put(R.id.school, "child1.json");
                    put(R.id.university, "child2.json");
                    put(R.id.college, "child3.json");
                    put(R.id.kindergarten, "child4.json");
                    put(R.id.language_school, "child5.json");
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

        try {
            changeClusters();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ClusterManager<MyItem> mClusterManager;

    @Override
    protected int getLayoutId() {
        return R.layout.clustering_demo;
    }

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));

        mClusterManager = new ClusterManager<>(this, getMap());
        getMap().setOnCameraIdleListener(mClusterManager);
    }

    private void changeClusters() throws JSONException {
        mClusterManager.clearItems();

        for (Integer item: enumToChecked.keySet()) {
            if (!enumToChecked.get(item))
                continue;

            InputStream inputStream = getResources().openRawResource(item);
            List<MyItem> items = new MyItemReader().read(inputStream);
            mClusterManager.addItems(items);
        }
    }
}