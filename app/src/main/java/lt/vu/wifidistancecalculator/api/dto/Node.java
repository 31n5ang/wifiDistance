package lt.vu.wifidistancecalculator.api.dto;

public class Node {
    private String buildingName;
    private int number;
    private int floor;

    public Node(String buildingName, int number, int floor) {
        this.buildingName = buildingName;
        this.number = number;
        this.floor = floor;
    }

    public Node() {

    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }
}
