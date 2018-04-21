package cz.agents.gtlibrary.domain.testGame.gameDefs;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jakub Cerny on 14/02/2018.
 */
public class GadgetCounterExampleGame implements GameDefinition {
    // State -> States
    public static final HashMap<Integer, ArrayList<Integer>> successors = new HashMap<Integer, ArrayList<Integer>>() {{
        put(1,new ArrayList<Integer>(){{add(2); add(3);}});
        put(2,new ArrayList<Integer>(){{add(4); add(5);}});
        put(3,new ArrayList<Integer>(){{add(6); add(7);}});
        put(4,new ArrayList<Integer>(){{add(8); add(9);}});
        put(5,new ArrayList<Integer>(){{add(10); add(11);}});
        put(6,new ArrayList<Integer>(){{add(12); add(13);}});
        put(7,new ArrayList<Integer>(){{add(14); add(15);}});
    }};

    // State -> IS
    public static final HashMap<Integer, Integer> iss = new HashMap<Integer,Integer>() {{
        put(1,1); put(2,2); put(3,3); put(4,4); put(5,4);
        put(6,4); put(7,4);
    }};

    // IS -> Player
    public static final HashMap<Integer, Integer> players = new HashMap<Integer,Integer>() {{
        put(1,0); put(2,0); put(3,0); put(4,1); put(5,1); put(6,1); put(7,1);
    }};

    // State -> utility
    public static final HashMap<Integer, Double[]> utilities = new HashMap<Integer,Double[]>() {{
        put(8,new Double[]{10.0, 0.0});
        put(9,new Double[]{1.0, 1.0});
        put(10,new Double[]{1.0, 0.0});
        put(11,new Double[]{1.0, 1.0});
        put(12,new Double[]{0.0, 1.0});
        put(13,new Double[]{0.0, 0.0});
        put(14,new Double[]{0.0, 1.0});
        put(15,new Double[]{0.0, 0.0});
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