package ru.itmo.anya.mark.model;

public class Location {
    private double x;
    private Double y; //Поле не может быть null
    private Integer z;//Поле не может быть null

    public Location(double x, Double y, Integer z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        if (y== null){
            throw new IllegalArgumentException("Эт не может быть null");
        }
        this.y = y;
    }

    public Integer getZ() {
        return z;
    }

    public void setZ(Integer z) {
        if (z == null) {
            throw new IllegalArgumentException("Y не может быть null");
        }
        this.z = z;
    }
}
