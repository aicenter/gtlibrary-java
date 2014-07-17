package cz.agents.gtlibrary.utils;

public class Interval {

    private double upperBound;
    private double lowerBound;
    private double size;

    public Interval(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        size = upperBound - lowerBound;
//        assert upperBound >= lowerBound - 1e-4;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    public double getSize() {
        return size;
    }

    public double getRelativePosition(double value) {
        if(size < 1e-8) {
//            assert Math.abs(value - upperBound) < 1e-5;
            return 1;
        }
        return (value - lowerBound)/size;
    }

    @Override
    public String toString() {
        return "[" + lowerBound + ", " + upperBound + "]";
    }
}
