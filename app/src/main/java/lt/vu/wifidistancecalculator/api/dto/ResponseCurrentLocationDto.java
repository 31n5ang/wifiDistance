package lt.vu.wifidistancecalculator.api.dto;

public class ResponseCurrentLocationDto {
    private int number;
    private int floor;

    public ResponseCurrentLocationDto(int number, int floor) {
        this.number = number;
        this.floor = floor;
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
