package com.google.maps.android.utils.demo;

import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.utils.demo.model.GradientUtils;
import com.google.maps.android.utils.demo.model.MyItem;

import org.json.JSONException;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusteringDemoActivity extends BaseDemoActivity {
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

    private Map<Integer, Boolean> enumToChecked = new HashMap<>();
    private Map<Integer, Integer> enumToIndex = new HashMap<>();

    private void changeCheckedState(Integer enumItem, boolean newVal) {
        enumToChecked.put(enumItem, newVal);
        ((CheckBox)findViewById(enumItem)).setChecked(enumToChecked.get(enumItem));
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

    public void onCheckboxClicked(View view) {
        Integer selectedThing = view.getId();
        boolean newVal = !enumToChecked.get(selectedThing);
        if (classes.keySet().contains(selectedThing)) {
            // Superclass: need to modify it and children
            for (Integer item: classes.get(selectedThing)) {
                changeCheckedState(item, newVal);
            }
        }
        changeCheckedState(selectedThing, newVal);

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

        mClusterManager = new ClusterManager<>(this, getMap());
        getMap().setOnCameraIdleListener(mClusterManager);


        simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            {
                setProvider();
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
                setProvider();
                if (getMap() != null) {
                    getMap().clear();
                }
                mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                changeClusters();


                Toast.makeText(ClusteringDemoActivity.this, "Seek bar progress is : 201" + progressChangedValue,
                        Toast.LENGTH_SHORT).show();
            }

            private void setProvider() {
                mProvider = GradientUtils.makeProvider(progressChangedValue,
                        getResources(), getPackageName());
            }


        });
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