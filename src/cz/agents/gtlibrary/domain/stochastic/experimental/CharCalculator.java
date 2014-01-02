package cz.agents.gtlibrary.domain.stochastic.experimental;

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
