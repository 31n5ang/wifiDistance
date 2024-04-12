package lt.vu.wifidistancecalculator;

import static android.app.ProgressDialog.show;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.ScanResultsCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lt.vu.wifidistancecalculator.api.NullOnEmptyConverterFactory;
import lt.vu.wifidistancecalculator.api.dto.Node;
import lt.vu.wifidistancecalculator.api.dto.RequestCurrentLocationDto;
import lt.vu.wifidistancecalculator.api.dto.RequestFingerprintDto;
import lt.vu.wifidistancecalculator.api.dto.ResponseCurrentLocationDto;
import lt.vu.wifidistancecalculator.api.dto.ResponseFingerprintDto;
import lt.vu.wifidistancecalculator.api.dto.Signal;
import lt.vu.wifidistancecalculator.api.service.FingerPrintService;
import lt.vu.wifidistancecalculator.databinding.FragmentFirstBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private WifiManager wifiManager;
    //안드로이드로 와이파이를 관리하는 라이브러리로 현재 와이파이를 읽을때 startscan이라는 라이브러리를 사용하는데 안드로이드 10이상에서는
    //해당 메소드를 사용할 수 없다. 대신하여 registerScanResultsCallback, ScanResultsCallback를 사용한다.
    private List<String> scanResults = new ArrayList<>();
    private BroadcastReceiver wifiScanReceiver;

    private List<String> desiredBSSIDs = new ArrayList<>();

    private ScanResultsCallback scanResultsCallback;
    //안드로이드 11버전 이상에서 와이파이와 통신을 하기 위해 해당 객체가 필요

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        wifiManager = (WifiManager) requireActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        setupWifiScanMethod();

        return binding.getRoot();
    }

    private void setupWifiScanMethod() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 이상에서는 ScanResultsCallback를 사용
            scanResultsCallback = new ScanResultsCallback() {
                @Override
                public void onScanResultsAvailable() {
                    scanSuccess();
                }
            };
            wifiManager.registerScanResultsCallback(requireContext().getMainExecutor(), scanResultsCallback);
        } else {
            // Android 10 이하에서는 BroadcastReceiver를 사용
            IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            wifiScanReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                    if (success) {
                        scanSuccess();
                    }
                }
            };
            requireActivity().getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
        }
    }

    private void scanWifi() {
        binding.responseTV.setText("wifi 스캔중..");
        binding.responseTV.setTextColor(Color.BLACK);
        if (!wifiManager.isWifiEnabled()) { //와이파이 켜져있는지 확인
            showSnackbar("WiFi is disabled ...", BaseTransientBottomBar.LENGTH_LONG);
            return;
        }
        boolean success = wifiManager.startScan();
        if (!success) { //와이파이 스캔이 되었는지 확인
            // Scan failure handling
            showSnackbar("Scan initiation failed", BaseTransientBottomBar.LENGTH_SHORT);
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            });

    private void checkForLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }


    private void scanSuccess() {
        hideKeyboard(getContext(), getView());
        binding.responseTV.setText("wifi 스캔 성공!");
        binding.responseTV.setTextColor(Color.BLUE);
        checkForLocationPermission();
        List<ScanResult> results = wifiManager.getScanResults();
//        showSnackbar("Result size:" + results.size(), BaseTransientBottomBar.LENGTH_LONG);

        List<String> stringResults = results.stream()
                .filter(scanResult -> StringUtils.isNotEmpty(scanResult.SSID))
                .sorted(Comparator.comparing(scanResult -> scanResult.level, Comparator.reverseOrder()))
                // 쉼표 추가로 가독성 확보
                .map(scanResult -> scanResult.SSID + ", " + scanResult.BSSID + ", " + scanResult.level)
                .collect(Collectors.toList());
        scanResults = stringResults;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, stringResults);
        binding.wifiList.setAdapter(adapter);
//        saveToFile(); //이 부분 주석 시 스캔하자마자 저장 안함, 저장 버튼 누를때만 저장 되는지 확인 필요
//        scanWifi(); //현재 이 부분때문에 스캔이 계속 되니깐 이후에 주석처리 해서 버튼을 눌렀을 때에만 되게 가능
    }

    private void requestPutFingerprint() {
        hideKeyboard(getContext(), getView());
        binding.responseTV.setTextColor(Color.GRAY);
        binding.responseTV.setText("Fingerprint 저장 요청중..");
        //
        /**
         * REST API 통신
         * /api/v1/admin/node/fingerprint/building-name
         */
        List<Signal> signalList = new ArrayList<>();

        // 파싱
        for (String result : scanResults) {
            StringTokenizer st = new StringTokenizer(result, ",");
            String ssid = st.nextToken().trim();
            String mac = st.nextToken().trim();
            int rssi = Integer.parseInt(st.nextToken().trim());
            signalList.add(new Signal(ssid, mac, rssi));
        }

        String buildingName = binding.buildingNameEdit.getText().toString();
        String floorNumber = binding.floorNumberEdit.getText().toString();
        String nodeNumber = binding.nodeNumberEdit.getText().toString();

        Node node = new Node();
        node.setBuildingName(buildingName);
        node.setNumber(Integer.parseInt(nodeNumber));
        node.setFloor(Integer.parseInt(floorNumber));

        RequestFingerprintDto requestFingerprintDto = new RequestFingerprintDto(node, signalList);

        // API 요청
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(FingerPrintService.URL)
                .addConverterFactory(new NullOnEmptyConverterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        FingerPrintService fingerPrintService = retrofit.create(FingerPrintService.class);

        fingerPrintService.putFingerPrintService(requestFingerprintDto)
                .enqueue(new Callback<ResponseFingerprintDto>() {
                    @Override
                    public void onResponse(Call<ResponseFingerprintDto> call, Response<ResponseFingerprintDto> response) {
                        binding.responseTV.setText("FingerPrint 저장 성공!" + "[" + response.body() + "]");
                        binding.responseTV.setTextColor(Color.BLUE);
                    }

                    @Override
                    public void onFailure(Call<ResponseFingerprintDto> call, Throwable throwable) {
                        binding.responseTV.setText("FingerPrint 실패!" + "[" + throwable + "]");
                        binding.responseTV.setTextColor(Color.RED);

                    }
                });
    }

    private void requestCurrentLocation() {
        hideKeyboard(getContext(), getView());
        binding.responseTV.setTextColor(Color.GRAY);
        binding.responseTV.setText("현재 위치 요청중..");
        //
        checkForLocationPermission();
        List<ScanResult> results = wifiManager.getScanResults();

        List<String> stringResults = results.stream()
                .filter(scanResult -> StringUtils.isNotEmpty(scanResult.SSID))
                .sorted(Comparator.comparing(scanResult -> scanResult.level, Comparator.reverseOrder()))
                // 쉼표 추가로 가독성 확보
                .map(scanResult -> scanResult.SSID + ", " + scanResult.BSSID + ", " + scanResult.level)
                .collect(Collectors.toList());
        List<String> scanResults = stringResults;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, stringResults);
        binding.wifiList.setAdapter(adapter);

        /**
         * REST API 통신
         * /api/v1/admin/node/position
         */
        List<Signal> signalList = new ArrayList<>();

        int buildingNo = Integer.parseInt(binding.buildingNameEdit.getText().toString());

        RequestCurrentLocationDto requestCurrentLocationDto = new RequestCurrentLocationDto(
                buildingNo, signalList
        );

        // 파싱
        for (String result : scanResults) {
            StringTokenizer st = new StringTokenizer(result, ",");
            String ssid = st.nextToken().trim();
            String mac = st.nextToken().trim();
            int rssi = Integer.parseInt(st.nextToken().trim());
            signalList.add(new Signal(ssid, mac, rssi));
        }

        // API 요청
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(FingerPrintService.URL)
                .addConverterFactory(new NullOnEmptyConverterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        // Test data
//        Signal signal = new Signal("KOREATECH", "34:5D:AD:14:DB:20", -46);
//        List<Signal> list = new ArrayList<>();
//        list.add(signal);
//        RequestCurrentLocationDto requestCurrentLocationDto1 = new RequestCurrentLocationDto(
//                1, list
//
//        );

        FingerPrintService fingerPrintService = retrofit.create(FingerPrintService.class);

        fingerPrintService.getCurrentLocationService(requestCurrentLocationDto)
                .enqueue(new Callback<ResponseCurrentLocationDto>() {
                    @Override
                    public void onResponse(Call<ResponseCurrentLocationDto> call, Response<ResponseCurrentLocationDto> response) {
                        binding.responseTV.setTextColor(Color.BLUE);
//                        assert response.body() != null;
                        ResponseCurrentLocationDto responseCurrentLocationDto =
                                (ResponseCurrentLocationDto) response.body();
                        String text = responseCurrentLocationDto.getFloor() + "층, " +
                                responseCurrentLocationDto.getNumber() + "번 노드";
                        binding.responseTV.setText(text);
                        Log.d("test", "CurrentLocation 성공");
                    }

                    @Override
                    public void onFailure(Call<ResponseCurrentLocationDto> call, Throwable throwable) {
                        binding.responseTV.setText("요청 실패!" + "[" + throwable + "]");
                        binding.responseTV.setTextColor(Color.RED);
                    }
                });
    }
    private int[] scanCurrentWifiRssi() {
        checkForLocationPermission();
        scanWifi();
        int[] rssiValues = new int[desiredBSSIDs.size()];
        Arrays.fill(rssiValues, 0); // 모든 값을 0으로 초기화

        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (int i = 0; i < desiredBSSIDs.size(); i++) {
            for (ScanResult result : scanResults) {
                if (result.BSSID.equals(desiredBSSIDs.get(i))) {
                    rssiValues[i] = result.level;
                    break;
                }
            }
        }
        Log.d("AppLog", "rssiValues4 : " + rssiValues[4]);
        Log.d("AppLog", "rssiValues5 : " + rssiValues[5]);
        return rssiValues;
    }


    private void setDesiredBSSIDs(){
        try {
            Log.d("AppLog", "00");
            File file = new File(getContext().getExternalFilesDir(null), "mydir/bssiddata.txt");
            Log.d("AppLog", "01");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            Log.d("AppLog", "02");
            while ((line = br.readLine()) != null) {
                if (line.isEmpty() || !line.contains(":")) continue;
                Log.d("AppLog", "03");
                desiredBSSIDs.add(line);
                Log.d("AppLog", "04");
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void determineCurrentLocation() {
        Log.d("AppLog", "0");
        setDesiredBSSIDs();
        Log.d("AppLog", "1");
        int[] currentRssiValues = scanCurrentWifiRssi();
        Log.d("AppLog", "2");
        String bestMatch = "";
        int minDifference = Integer.MAX_VALUE;
        Log.d("AppLog", "3");
        try {
            File file = new File(getContext().getExternalFilesDir(null), "mydir/ssiddata.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                Log.d("AppLog", "line : " + line);
                if (line.isEmpty() || !line.contains("건물명: ")) continue;

                String buildingInfo = line;
                Log.d("AppLog", "buildingInfo : " + buildingInfo);

                line = br.readLine();
                String[] parts = line.split("와이파이 정보: ");
                if (parts.length < 2) continue;
                String[] wifiInfos = parts[1].split(";");

                // BSSID 별 RSSI 값을 임시로 저장할 배열
                int[] tempRssiValues = new int[desiredBSSIDs.size()];
                Arrays.fill(tempRssiValues, 0); // 기본값으로 초기화

                for (String wifiInfo : wifiInfos) {
                    String[] wifiParts = wifiInfo.split(", ");
                    String bssid = wifiParts[1].split(" ")[1];
                    Log.d("AppLog", "bssid : " + bssid);
                    Log.d("AppLog", "wifiParts 0: " + wifiParts[0]);
                    Log.d("AppLog", "wifiParts 1: " + wifiParts[1]);
                    int rssi = Integer.parseInt(wifiParts[1].split("RSSI:")[1]);
                    Log.d("AppLog", "rssi 오류: " + rssi);
                    int index = desiredBSSIDs.indexOf(bssid);
                    if (index != -1) {
                        tempRssiValues[index] = rssi;
                    }
                }

                // 차이를 계산
                int difference = 0;
                for (int i = 0; i < currentRssiValues.length; i++) {
                    difference += Math.pow(currentRssiValues[i] - tempRssiValues[i], 2);
                }
                Log.d("AppLog", "difference 오류: " + difference);
                if (difference < minDifference) {
                    minDifference = difference;
                    bestMatch = buildingInfo;
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!bestMatch.isEmpty()) {
            showSnackbar(bestMatch, BaseTransientBottomBar.LENGTH_LONG);
        } else {
            showSnackbar("위치를 파악할 수 없습니다.", BaseTransientBottomBar.LENGTH_LONG);
        }
    }

    public void showSnackbar(String text, int length) {
        Snackbar.make(requireActivity().findViewById(android.R.id.content), text, length)
                .setAction("Action", null).show();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.scanBtn.setOnClickListener(v -> scanWifi());
        binding.saveBtn.setOnClickListener(v -> requestPutFingerprint());
        binding.clBtn.setOnClickListener(v -> requestCurrentLocation());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && scanResultsCallback != null) {
            wifiManager.unregisterScanResultsCallback(scanResultsCallback);
        } else if (wifiScanReceiver != null) {
            requireActivity().getApplicationContext().unregisterReceiver(wifiScanReceiver);
        }
        binding = null;
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}