package lt.vu.wifidistancecalculator.api.service;

import java.util.List;

import lt.vu.wifidistancecalculator.api.dto.RequestCurrentLocationDto;
import lt.vu.wifidistancecalculator.api.dto.RequestFingerprintDto;
import lt.vu.wifidistancecalculator.api.dto.ResponseCurrentLocationDto;
import lt.vu.wifidistancecalculator.api.dto.ResponseFingerprintDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface FingerPrintService {
    String URL = "http://-:8081";

    @POST("/api/v1/admin/node/fingerprint/building-name")
    Call<ResponseFingerprintDto> putFingerPrintService(
            @Body RequestFingerprintDto requestFingerprintDto
            );

    @POST("/api/v1/admin/node/position")
    Call<ResponseCurrentLocationDto> getCurrentLocationService(
            @Body RequestCurrentLocationDto requestCurrentLocationDto
            );
}