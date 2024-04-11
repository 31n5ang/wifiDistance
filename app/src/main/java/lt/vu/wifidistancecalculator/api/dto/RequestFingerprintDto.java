package lt.vu.wifidistancecalculator.api.dto;

import java.util.ArrayList;
import java.util.List;

public class RequestFingerprintDto {
    private Node node;
    private List<Signal> signals = new ArrayList<>();

    public RequestFingerprintDto(Node node, List<Signal> signals) {
        this.node = node;
        this.signals = signals;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public List<Signal> getSignals() {
        return signals;
    }

    public void setSignals(List<Signal> signals) {
        this.signals = signals;
    }
}


