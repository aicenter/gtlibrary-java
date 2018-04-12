package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.number;

import java.util.Arrays;
import java.util.stream.IntStream;

public class DigitArray {

    public static DigitArray ONE = new DigitArray(new int[]{1}, true);
    public static DigitArray ZERO = new DigitArray(new int[]{0}, true);

    private int[] digitArray;
    private boolean positive;

    public static DigitArray getAverage(DigitArray digit1, DigitArray digit2, int precision) {
        return digit1.add(digit2).divideByTwo().roundTo(precision);
    }

    public DigitArray(int[] digitArray, boolean positive) {
        this.digitArray = digitArray;
        this.positive = positive;
        assert validDecimalDigits();
    }

    public DigitArray(DigitArray other) {
        this.digitArray = new int[other.digitArray.length];
        System.arraycopy(other.digitArray, 0, this.digitArray, 0, this.digitArray.length);
        this.positive = other.positive;
    }

    public DigitArray(DigitArray other, boolean positive) {
        this.digitArray = other.digitArray;
        this.positive = positive;
    }

    private boolean validDecimalDigits() {
        for (int i = 0; i < digitArray.length; i++) {
            if (digitArray[i] < 0 || digitArray[i] > 9)
                return false;
        }
        return true;
    }

    private DigitArray roundTo(int precision) {
        int[] arrayCopy = new int[precision];

        if (precision >= size()) {
            System.arraycopy(digitArray, 0, arrayCopy, 0, size());
            return new DigitArray(arrayCopy, positive);
        }
        System.arraycopy(digitArray, 0, arrayCopy, 0, arrayCopy.length);
        DigitArray result = new DigitArray(arrayCopy, positive);

        if (digitArray[arrayCopy.length] > 4)
            result = result.incrementLSD();
        return result;
    }

    public DigitArray decrementLSD() {
        int[] decrement = new int[size()];

        decrement[decrement.length - 1] = 1;
        return subtract(new DigitArray(decrement, true));
    }

    public DigitArray incrementLSD() {
        int[] increment = new int[size()];

        increment[increment.length - 1] = 1;
        return add(new DigitArray(increment, true));
    }

    private DigitArray divideByTwo() {
        int[] copyArray = new int[digitArray.length + 1];

        System.arraycopy(digitArray, 0, copyArray, 0, digitArray.length);
//        DigitArray result = new DigitArray(copyArray, positive);
        int carry = 0;

        for (int i = 0; i < copyArray.length; i++) {
            int positionResult = (int) Math.floor(copyArray[i] / 2. + carry);

            carry = (int) ((copyArray[i] / 2. + carry - positionResult) * 10);
            copyArray[i] = positionResult;
        }
        return new DigitArray(copyArray, positive);
    }

    public boolean isGreaterThan(DigitArray other) {
        if (negative() && other.positive())
            return false;
        if (positive() && other.negative() && !other.isZero())
            return true;
        for (int i = 0; i < Math.max(digitArray.length, other.size()); i++) {
            if (i >= digitArray.length) {
                if (other.digitArray[i] > 0)
                    return false;
                else if (other.digitArray[i] < 0)
                    return true;
            } else if (i >= other.size()) {
                if (digitArray[i] > 0)
                    return true;
                else if (digitArray[i] < 0)
                    return false;
            } else if (digitArray[i] > other.digitArray[i]) {
                return true;
            } else if (digitArray[i] < other.digitArray[i]) {
                return false;
            }
        }
        return false;
    }

    private boolean isZero() {
        for (int i = 0; i < digitArray.length; i++) {
            if (digitArray[i] != 0)
                return false;
        }
        return true;
    }

    public DigitArray add(DigitArray other) {
        if (negative() && other.positive())
            return other.subtract(new DigitArray(this, true));
        if (positive() && other.negative())
            return subtract(new DigitArray(other, true));
        int[] result = new int[Math.max(digitArray.length, other.digitArray.length)];
        int temp;
        boolean carry = false;

        for (int i = result.length - 1; i >= 0; i--) {
            if (i >= digitArray.length) {
                result[i] = other.digitArray[i];
                carry = false;
            } else if (i >= other.size()) {
                result[i] = digitArray[i];
                carry = false;
            } else {
                temp = digitArray[i] + other.digitArray[i] + (carry ? 1 : 0);
                carry = temp > 9;
                result[i] = temp % 10;
            }
        }
        if (negative() && other.negative())
            return new DigitArray(result, false);
        return new DigitArray(result, true);
    }

    private boolean positive() {
        return positive;
    }

    private boolean negative() {
        return !positive;
    }

    public DigitArray subtract(DigitArray other) {
        if (other.negative())
            return add(new DigitArray(other, true));
        if (negative() && other.positive) {
            DigitArray result = other.add(new DigitArray(this, true));

            result.setPositivity(false);
            return result;
        }
        if (other.isGreaterThan(this)) {
            DigitArray result = other.subtract(this);

            result.setPositivity(false);
            return result;
        }
        int[] result = new int[Math.max(digitArray.length, other.digitArray.length)];
        int temp;
        boolean carry = false;

        for (int i = result.length - 1; i >= 0; i--) {
            if (i >= digitArray.length) {
                temp = -other.digitArray[i] - (carry ? 1 : 0);
                carry = temp < 0;
                result[i] = Math.floorMod(temp, 10);
            } else if (i >= other.digitArray.length) {
                result[i] = digitArray[i];
                carry = false;
            } else {
                temp = digitArray[i] - other.digitArray[i] - (carry ? 1 : 0);
                carry = temp < 0;
                result[i] = Math.floorMod(temp, 10);
            }
        }
        return new DigitArray(result, true);
    }

    public DigitArray getReducedPrecisionDigitArray(int precision) {
        return new DigitArray(Arrays.copyOf(digitArray, precision), positive);
    }

    public int get(int index) {
        return digitArray[index];
    }

    public void set(int index, int value) {
        digitArray[index] = value;
        assert value >= 0 && value <= 9;
    }

    public int size() {
        return digitArray.length;
    }

    public IntStream stream() {
        return Arrays.stream(digitArray);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DigitArray)) return false;
        DigitArray that = (DigitArray) o;

        if (positive != that.positive)
            return isZero() && that.isZero();
        for (int i = 0; i < Math.max(digitArray.length, that.digitArray.length); i++) {
            if (digitArray.length <= i) {
                if (that.digitArray[i] != 0)
                    return false;
            } else if (that.digitArray.length <= i) {
                if (digitArray[i] != 0)
                    return false;
            } else if (digitArray[i] != that.digitArray[i])
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return (positive ? "" : "-") + Arrays.toString(digitArray);
    }

    public int[] getArray() {
        return digitArray;
    }

    public void setPositivity(boolean positivity) {
        this.positive = positivity;
    }
}
