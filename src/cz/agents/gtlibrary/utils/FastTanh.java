/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


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
