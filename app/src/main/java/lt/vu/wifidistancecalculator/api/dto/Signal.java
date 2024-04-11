package lt.vu.wifidistancecalculator.api.dto;
public class Signal {
    private String ssid;
    private String mac;
    private int rssi;

    public Signal(String ssid, String mac, int rssi) {
        this.ssid = ssid;
        this.mac = mac;
        this.rssi = rssi;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
