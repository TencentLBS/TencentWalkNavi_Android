package com.example.tencentnavigation.walknavidemo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tencentnavigation.walknavidemo.util.StorageUtil;
import com.tencent.map.navi.NaviMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 导航设置界面
 * @daxiazhang 2018/1/11
 */
public class SettingsActivity extends Activity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = "navisdk";

    private Switch naviPanel;
    private RadioGroup naviModeRadioGroup;
    private Switch arrivedStopSwitch;
    private Switch recordLocationSwitch;
    private Switch gpsStatusSwitch;
    private RadioGroup gpsTrackRadioGroup;
    private RecyclerView recyclerView;

    private boolean isNavigating = false;
    private boolean avoidJamSelected = false;
    private boolean noTollsSelected = false;
    private boolean avoidHighwaySelected = false;
    private boolean showNaviPanel = true;
    private boolean showElectriEyesPicture = true;
    private boolean showCrossingEnlargePicture = true;
    private NaviMode naviMode;
    private boolean arrivedStop = true;
    private boolean recordLocation = false;
    private int gpsStatus = LocationProvider.AVAILABLE;
    private int gpsTrackType = 2;
    private String gpsTrackPath;
    private String gpsDirPath;
    private ArrayList<String> fileNameList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initDatas();
        initViews();
        initEvents();
        initDeviceInfo();
    }

    private void initDatas() {
        Intent intent = getIntent();
        if (intent != null) {
            isNavigating = intent.getBooleanExtra("isNavigating", false);
            avoidJamSelected = intent.getBooleanExtra("avoidJam", false);
            noTollsSelected = intent.getBooleanExtra("noTolls", false);
            avoidHighwaySelected = intent.getBooleanExtra("avoidHighway", false);
            showNaviPanel = intent.getBooleanExtra("showNaviPanel", true);
            showElectriEyesPicture = intent.getBooleanExtra("electriEyes", true);
            showCrossingEnlargePicture = intent.getBooleanExtra("crossingEnlarge", true);
            naviMode = (NaviMode) intent.getSerializableExtra("naviMode");
            arrivedStop = intent.getBooleanExtra("arrivedStop", true);
            recordLocation = intent.getBooleanExtra("recordLocation", false);
            gpsStatus = intent.getIntExtra("gpsStatus", LocationProvider.AVAILABLE);
            gpsTrackType = intent.getIntExtra("gpsTrackType", 2);
            gpsTrackPath = intent.getStringExtra("gpsTrackPath");
        }
        gpsDirPath = StorageUtil.getStoragePath(this) + "/GPS";
        copyAssets(gpsDirPath);
        File file = new File(gpsDirPath);
        String[] fileNames = file.list();
        if (fileNames != null) {
            Collections.addAll(fileNameList, fileNames);
        }
    }

    private void initViews(){
        naviPanel = findViewById(R.id.naviPanelSwitch);
        naviModeRadioGroup = findViewById(R.id.naviMode);
        arrivedStopSwitch = findViewById(R.id.arrived_stop_switch);
        recordLocationSwitch = findViewById(R.id.record_location_switch);
        gpsStatusSwitch = findViewById(R.id.gps_status_switch);
        gpsTrackRadioGroup = findViewById(R.id.naviTrack);
        recyclerView = findViewById(R.id.recyclerview);

        naviPanel.setChecked(showNaviPanel);
        arrivedStopSwitch.setChecked(arrivedStop);
        recordLocationSwitch.setChecked(recordLocation);
        gpsStatusSwitch.setChecked(gpsStatus == LocationProvider.AVAILABLE);
        switch (naviMode) {
            case MODE_3DCAR_TOWARDS_UP:
                naviModeRadioGroup.check(R.id.mode_3d_up);
                break;
            case MODE_2DMAP_TOWARDS_NORTH:
                naviModeRadioGroup.check(R.id.mode_2d_north);
                break;
            case MODE_OVERVIEW:
                naviModeRadioGroup.check(R.id.mode_overview);
                break;
            case MODE_REMAINING_OVERVIEW:
                naviModeRadioGroup.check(R.id.mode_remaining_overview);
                break;
        }
        if (isNavigating) {
            gpsStatusSwitch.setEnabled(true);
            for (int i = 0; i < gpsTrackRadioGroup.getChildCount(); i++) {
                gpsTrackRadioGroup.getChildAt(i).setEnabled(false);
            }
            recyclerView.setVisibility(View.GONE);
        } else {
            gpsStatusSwitch.setEnabled(false);
            for (int i = 0; i < gpsTrackRadioGroup.getChildCount(); i++) {
                gpsTrackRadioGroup.getChildAt(i).setEnabled(true);
            }
            switch (gpsTrackType) {
                case 0:
                    gpsTrackRadioGroup.check(R.id.track_record);
                    break;
                case 1:
                    gpsTrackRadioGroup.check(R.id.track_replay);
                    break;
                case 2:
                    gpsTrackRadioGroup.check(R.id.track_none);
                    break;
            }
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new FilePathAdapter());
            recyclerView.setVisibility(gpsTrackType == 1 ? View.VISIBLE : View.GONE);
        }
    }

    private void initEvents() {
        naviPanel.setOnClickListener(this);
        naviModeRadioGroup.setOnCheckedChangeListener(this);
        arrivedStopSwitch.setOnClickListener(this);
        recordLocationSwitch.setOnClickListener(this);
        gpsStatusSwitch.setOnClickListener(this);
        gpsTrackRadioGroup.setOnCheckedChangeListener(this);
    }

    private void initDeviceInfo() {
//        String imei = CommonUtil.getImei(this);
//        TextView textView = findViewById(R.id.deviceInfo);
//        textView.setText("IMEI: " + imei);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.naviPanelSwitch:
                showNaviPanel = !showNaviPanel;
                break;
            case R.id.arrived_stop_switch:
                arrivedStop = !arrivedStop;
                break;
            case R.id.record_location_switch:
                recordLocation = !recordLocation;
                break;
            case R.id.gps_status_switch:
                if (gpsStatus == LocationProvider.AVAILABLE) {
                    gpsStatus = LocationProvider.OUT_OF_SERVICE;
                } else {
                    gpsStatus = LocationProvider.AVAILABLE;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i) {
            case R.id.mode_3d_up:
                naviMode = NaviMode.MODE_3DCAR_TOWARDS_UP;
                break;
            case R.id.mode_2d_north:
                naviMode = NaviMode.MODE_2DMAP_TOWARDS_NORTH;
                break;
            case R.id.mode_overview:
                naviMode = NaviMode.MODE_OVERVIEW;
                break;
            case R.id.mode_remaining_overview:
                naviMode = NaviMode.MODE_REMAINING_OVERVIEW;
                break;
            case R.id.track_record:
                gpsTrackType = 0;
                recyclerView.setVisibility(View.GONE);
                gpsTrackPath = gpsDirPath;
                break;
            case R.id.track_replay:
                gpsTrackType = 1;
                recyclerView.setVisibility(View.VISIBLE);
                break;
            case R.id.track_none:
                gpsTrackType = 2;
                recyclerView.setVisibility(View.GONE);
                gpsTrackPath = "";
                break;
            default:
                break;
        }
    }

    private void copyAssets(String gpsDirPath) {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("GPS");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to get asset gps file list.", e);
        }
        if (files != null) {
            for (String filename : files) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    File outFile = new File(gpsDirPath, filename);
                    if (outFile.exists()) {
                        continue;
                    } else {
                        File parent = outFile.getParentFile();
                        if (parent != null && !parent.exists()) {
                            parent.mkdirs();
                        }
                    }
                    in = assetManager.open("GPS" + File.separator + filename);
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                } catch(IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Failed to copy asset file: " + filename, e);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Failed to close input stream", e);
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Failed to close output stream", e);
                        }
                    }
                }
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    @Override
    public void onBackPressed() {
        if (gpsTrackType == 1 && TextUtils.isEmpty(gpsTrackPath)) {
            Toast.makeText(this, "请选择一个轨迹文件！", Toast.LENGTH_SHORT).show();
            return;
        }
        giveBackConfigInfo();
        super.onBackPressed();
    }

    /**
     * 将用户设置内容返回给导航界面
     */
    private void giveBackConfigInfo() {
        Intent intent = new Intent();
        intent.putExtra("avoidJam", avoidJamSelected);
        intent.putExtra("noTolls", noTollsSelected);
        intent.putExtra("avoidHighway", avoidHighwaySelected);
        intent.putExtra("showNaviPanel", showNaviPanel);
        intent.putExtra("electriEyes", showElectriEyesPicture);
        intent.putExtra("crossingEnlarge", showCrossingEnlargePicture);
        intent.putExtra("naviMode", naviMode);
        intent.putExtra("arrivedStop", arrivedStop);
        intent.putExtra("recordLocation", recordLocation);
        intent.putExtra("gpsStatus", gpsStatus);
        intent.putExtra("gpsTrackType", gpsTrackType);
        intent.putExtra("gpsTrackPath", gpsTrackPath);
        setResult(RESULT_OK, intent);
    }

    class FilePathAdapter extends RecyclerView.Adapter<FilePathAdapter.MyViewHolder> {

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(
                    getBaseContext()).inflate(R.layout.layout_item_tracker, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            String fileName = fileNameList.get(position);
            holder.fileName.setText(fileName);
            if (fileName.equalsIgnoreCase(gpsTrackPath)) {
                holder.fileSwitch.setChecked(true);
            } else {
                holder.fileSwitch.setChecked(false);
            }
        }

        @Override
        public int getItemCount() {
            return fileNameList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView fileName;
            Switch fileSwitch;

            public MyViewHolder(View view) {
                super(view);
                fileName = view.findViewById(R.id.file_name);
                fileSwitch = view.findViewById(R.id.file_switch);
                fileSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            gpsTrackPath = gpsDirPath + File.separatorChar + String.valueOf(fileName.getText());
                        } else {
                            gpsTrackPath = "";
                        }
                    }
                });
            }
        }
    }
}
