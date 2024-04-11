package lt.vu.wifidistancecalculator.api.dto;

import java.util.ArrayList;
import java.util.List;

public class RequestCurrentLocationDto {
    private int buildingId;
    private List<Signal> signals = new ArrayList<>();

    public RequestCurrentLocationDto(int buildingId, List<Signal> signals) {
        this.buildingId = buildingId;
        this.signals = signals;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public List<Signal> getSignals() {
        return signals;
    }

    public void setSignals(List<Signal> signals) {
        this.signals = signals;
    }
}
