package cz.agents.gtlibrary.domain.testGame.gameDefs;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jakub Cerny on 03/11/2017.
 */
public class TestGame1 implements GameDefinition {

    protected static final boolean USE_TL = false;

        // State -> States
    public static final HashMap<Integer, ArrayList<Integer>> successors = new HashMap<Integer, ArrayList<Integer>>() {{
        put(1,new ArrayList<Integer>(){{add(2); add(3);}});
        put(2,new ArrayList<Integer>(){{add(4); add(5);}});
        put(3,new ArrayList<Integer>(){{add(6); add(7);}});
        put(5,new ArrayList<Integer>(){{add(8); add(9);}});
        if (!USE_TL) put(7,new ArrayList<Integer>(){{add(10); add(11);}});
        put(8,new ArrayList<Integer>(){{add(12); add(13);}});
        put(9,new ArrayList<Integer>(){{add(14); add(15);}});
        put(10,new ArrayList<Integer>(){{add(16); add(17);}});
        put(11,new ArrayList<Integer>(){{add(18); add(19);}});
//        put(19,new ArrayList<Integer>(){{add(20); add(21);}});
    }};

    // State -> IS
    public static final HashMap<Integer, Integer> iss = new HashMap<Integer,Integer>() {{
        put(1,1); put(2,2); put(3,2); put(5,3); put(7,4);
        put(8,5); put(9,5); put(10,5); put(11,5); put(19,6);
    }};

    // IS -> Player
    public static final HashMap<Integer, Integer> players = new HashMap<Integer,Integer>() {{
        put(1,0); put(2,1); put(3,0); put(4,0); put(5,1); put(6,0);
    }};

    // State -> utility
    public static final HashMap<Integer, Double[]> utilities = new HashMap<Integer,Double[]>() {{
        put(4,new Double[]{-21.0, -30.0});
        put(6,new Double[]{-22.0, -30.0});
        put(12,new Double[]{-11.0, 0.0});
        put(13,new Double[]{-10.0, -1.0});
        put(14,new Double[]{-8.0, -1.0});
        put(15,new Double[]{-7.0, -0.5});
        put(16,new Double[]{-6.0, 1.0});
        put(17,new Double[]{-12.0, 1.0});
        put(18,new Double[]{-5.0, 1.0});
        put(19,new Double[]{-6.0, 1.0});
        put(20,new Double[]{-6.0, 0.0});
        put(21,new Double[]{-7.0, 0.0});
        if (USE_TL) put(7,new Double[]{-6.0, 1.0});
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
