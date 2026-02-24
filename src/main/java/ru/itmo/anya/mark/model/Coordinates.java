package ru.itmo.anya.mark.model;


public class Coordinates {
    private long x;
    private double y;

    public Coordinates(double y, long x) {
        this.y = y;
        this.x = x;
    }

    public long getX() {
        return x;
    }

    public void setX(long x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        if (y >= 663) {
            throw new IllegalArgumentException("Y не может быть больше 663 символов");
        }
        this.y = y;
    }
}