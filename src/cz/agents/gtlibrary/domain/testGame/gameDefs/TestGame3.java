package cz.agents.gtlibrary.domain.testGame.gameDefs;

import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jakub Cerny on 03/11/2017.
 */
public class TestGame3 implements GameDefinition {

    protected static final boolean MAKE_4_TL = false;
    protected static final boolean MAKE_5_TL = false;
    protected static final boolean MAKE_6_TL = false;
    protected static final boolean MAKE_7_TL = false;

    protected static final int seed = 17;

    // State -> States
    public static final HashMap<Integer, ArrayList<Integer>> successors = new HashMap<Integer, ArrayList<Integer>>() {{
        put(1,new ArrayList<Integer>(){{add(2); add(3);}});
        put(2,new ArrayList<Integer>(){{add(4); add(5);}});
        put(3,new ArrayList<Integer>(){{add(6); add(7);}});
        if (MAKE_4_TL)
            put(4,new ArrayList<Integer>(){{add(16); add(17); add(18); add(19);}});
        else
            put(4,new ArrayList<Integer>(){{add(8); add(9);}});
        if (MAKE_5_TL)
            put(5,new ArrayList<Integer>(){{add(20); add(21); add(22); add(23);}});
        else
            put(5,new ArrayList<Integer>(){{add(10); add(11);}});
        if (MAKE_6_TL)
            put(6,new ArrayList<Integer>(){{add(24); add(25); add(26); add(27);}});
        else
            put(6,new ArrayList<Integer>(){{add(12); add(13);}});
        if (MAKE_7_TL)
            put(7,new ArrayList<Integer>(){{add(28); add(29); add(30); add(31);}});
        else
            put(7,new ArrayList<Integer>(){{add(14); add(15);}});

        put(8,new ArrayList<Integer>(){{add(16); add(17);}});
        put(9,new ArrayList<Integer>(){{add(18); add(19);}});
        put(10,new ArrayList<Integer>(){{add(20); add(21);}});
        put(11,new ArrayList<Integer>(){{add(22); add(23);}});
        put(12,new ArrayList<Integer>(){{add(24); add(25);}});
        put(13,new ArrayList<Integer>(){{add(26); add(27);}});
        put(14,new ArrayList<Integer>(){{add(28); add(29);}});
        put(15,new ArrayList<Integer>(){{add(30); add(31);}});
    }};

    // State -> IS
    public static final HashMap<Integer, Integer> iss = new HashMap<Integer,Integer>() {{
        put(1,1); put(2,2); put(3,2); put(4,3); put(5,4); put(6,5); put(7,6); put(8,7);
        put(9,7); put(10,8); put(11,8); put(12,9); put(13,9); put(14,10); put(15,10);
    }};

    // IS -> Player
    public static final HashMap<Integer, Integer> players = new HashMap<Integer,Integer>() {{
        put(1,0); put(2,1); put(3,0); put(4,0); put(5,0); put(6,0); put(7,1); put(8,1); put(9,1); put(10,1);
    }};

    // State -> utility
    public static final HashMap<Integer, Double[]> utilities = new HashMap<Integer,Double[]>() {{
        HighQualityRandom r = new HighQualityRandom(seed);
        double MAX_UTILITY = 10;
        for (int i = 16; i <= 31; i++) put(i,new Double[]{2*r.nextDouble()*MAX_UTILITY - MAX_UTILITY, 2*r.nextDouble()*MAX_UTILITY  - MAX_UTILITY});
//        put(16,new Double[]{-100.0, -100.0});
//        put(17,new Double[]{-100.0, -100.0});
//        put(18,new Double[]{-100.0, -100.0});
//        put(19,new Double[]{-100.0, -100.0});
//        put(20,new Double[]{-100.0, -100.0});
//        put(21,new Double[]{-100.0, -100.0});
//        put(22,new Double[]{-100.0, -100.0});
//        put(23,new Double[]{-100.0, -100.0});
//        put(24,new Double[]{-100.0, -100.0});
//        put(25,new Double[]{-100.0, -100.0});
//        put(26,new Double[]{-100.0, -100.0});
//        put(27,new Double[]{-100.0, -100.0});
//        put(28,new Double[]{-100.0, -100.0});
//        put(29,new Double[]{-100.0, -100.0});
//        put(30,new Double[]{-100.0, -100.0});
//        put(31,new Double[]{-100.0, -100.0});
    }};

    @Override
    public HashMap<Integer, ArrayList<Integer>> getSuccessors() {
        return successors;
    }

    @Override
    public HashMap<Integer, Integer> getISs() {
        return iss;
    }

    @Override
    public HashMap<Integer, Integer> getPlayersForISs() {
        return players;
    }

    @Override
    public HashMap<Integer, Double[]> getUtilities() {
        return utilities;
    }
}
