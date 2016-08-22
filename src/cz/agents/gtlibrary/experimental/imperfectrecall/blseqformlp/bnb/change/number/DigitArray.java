package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.number;

import java.util.Arrays;
import java.util.stream.IntStream;

public class DigitArray {

    public static DigitArray ONE = new DigitArray(new int[]{1});
    public static DigitArray ZERO = new DigitArray(new int[]{0});

    private int[] digitArray;

    public DigitArray(int[] digitArray) {
        this.digitArray = digitArray;
    }

    public DigitArray(DigitArray other) {
        this.digitArray = new int[other.digitArray.length];
        System.arraycopy(other.digitArray, 0, this.digitArray, 0, this.digitArray.length);
    }

    public boolean isGreaterThan(DigitArray other) {
        return isGreaterThan(other.digitArray);
    }

    public boolean isGreaterThan(int[] other) {
        for (int i = 0; i < Math.max(digitArray.length, other.length); i++) {
            if (i >= digitArray.length) {
                if (other[i] > 0)
                    return false;
                else if (other[i] < 0)
                    return true;
            } else if (i >= other.length) {
                if (digitArray[i] > 0)
                    return true;
                else if (digitArray[i] < 0)
                    return false;
            } else if (digitArray[i] > other[i]) {
                return true;
            } else if (digitArray[i] < other[i]) {
                return false;
            }
        }
        return false;
    }

    public DigitArray add(DigitArray other) {
        return add(other.digitArray);
    }

    public DigitArray add(int[] other) {
        int[] result = new int[Math.max(digitArray.length, other.length)];
        int temp;
        boolean carry = false;

        for (int i = result.length - 1; i >= 0; i--) {
            if (i >= digitArray.length) {
                result[i] = other[i];
                carry = false;
            } else if (i >= other.length) {
                result[i] = digitArray[i];
                carry = false;
            } else {
                temp = digitArray[i] + other[i] + (carry ? 1 : 0);
                carry = temp > 9;
                result[i] = temp % 10;
            }
        }
        return new DigitArray(result);
    }

    public DigitArray subtract(DigitArray other) {
        return subtract(other.digitArray);
    }

    public DigitArray subtract(int[] other) {
        int[] result = new int[Math.max(digitArray.length, other.length)];
        int temp;
        boolean carry = false;

        for (int i = result.length - 1; i >= 0; i--) {
            if (i >= digitArray.length) {
                result[i] = (10 - other[i]) % 10 - (carry ? 1 : 0);
                carry = (other[i] != 0);
            } else if (i >= other.length) {
                result[i] = digitArray[i];
                carry = false;
            } else {
                temp = digitArray[i] - other[i] - (carry ? 1 : 0);
                carry = temp < 0;
                result[i] = i == 0 ? temp % 10 : Math.floorMod(temp, 10);
            }
        }
        return new DigitArray(result);
    }

    public DigitArray getReducedPrecisionDigitArray(int precision) {
        return new DigitArray(Arrays.copyOf(digitArray, precision));
    }

    public int get(int index) {
        return digitArray[index];
    }

    public void set(int index, int value) {
        digitArray[index] = value;
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
        return Arrays.toString(digitArray);
    }

    public int[] getArray() {
        return digitArray;
    }
}
