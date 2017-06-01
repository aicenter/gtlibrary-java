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


package cz.agents.gtlibrary.experimental.stochastic.characteristics;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kail
 * Date: 12/9/13
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class CharCalculator {

    public static int DEPTH = 2;
    public static int discretizations = 10;
    public static int nodes = 3;
    public static Map<Double, Integer> howManyCharsOfThatValue = new HashMap<Double, Integer>();
    public static HashSet<Characteristic> characteristics = new HashSet<Characteristic>();


    public static void main(String args[]) {
        for (int n=0; n<nodes; n++) {

        }
    }

    private static Set<Characteristic> calculateCharacteristic(int startNode, int depth, int discr) {
        Set<Characteristic> result = new HashSet<Characteristic>();




        return result;
    }
}
