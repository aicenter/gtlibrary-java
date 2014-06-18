package cz.agents.gtlibrary.utils;

// a cache-optimized approximation for Tanh (useful for eval functions)

public class FastTanh {
    // Stores a number of pre-computed logarithms
    private final static int N_TANHS = 200001;
    private final static double[] tanhs = new double[N_TANHS];

    static {
        for (int i = 0; i < tanhs.length; i++) {
            double x = (i - 100000) / 1000.0;
            tanhs[i] = Math.tanh(x);
        }
    }

    public static double tanh(double x) {
        if (x >= -100.000 && x < 100.000) {
            int index = (int) Math.round(x * 1000 + 100000);
            return tanhs[index];
        } else {
            return Math.tanh(x);
        }
    }
}
