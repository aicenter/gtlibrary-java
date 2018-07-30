package cz.agents.gtlibrary.domain.testGame.gameDefs;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jakub Cerny on 31/01/2018.
 */
public class CostsCounterExampleGame implements GameDefinition {
    // State -> States
    public static final HashMap<Integer, ArrayList<Integer>> successors = new HashMap<Integer, ArrayList<Integer>>() {{
        put(1,new ArrayList<Integer>(){{add(2); add(3);}});
        put(2,new ArrayList<Integer>(){{add(4); add(5);}});
        put(3,new ArrayList<Integer>(){{add(6); add(7);}});
        put(4,new ArrayList<Integer>(){{add(8); add(9);}});
        put(6,new ArrayList<Integer>(){{add(10); add(11);}});
        put(7,new ArrayList<Integer>(){{add(12); add(13);}});
        put(8,new ArrayList<Integer>(){{add(14); add(15);}});
        put(9,new ArrayList<Integer>(){{add(16); add(17);}});
        put(10,new ArrayList<Integer>(){{add(18); add(19);}});
        put(11,new ArrayList<Integer>(){{add(20); add(21);}});
    }};

    // State -> IS
    public static final HashMap<Integer, Integer> iss = new HashMap<Integer,Integer>() {{
        put(1,1); put(2,2); put(3,2); put(4,3); put(6,4); put(7,6);
        put(8,5); put(9,5); put(10,5); put(11,5);
    }};

    // IS -> Player
    public static final HashMap<Integer, Integer> players = new HashMap<Integer,Integer>() {{
        put(1,0); put(2,1); put(3,0); put(4,0); put(5,1); put(6,0);
    }};

    // State -> utility
    static Double[] type1Utility = new Double[]{ 1.0,  1.0};
    static Double[] type2Utility = new Double[]{0.0, 0.0};//{-1.0, -1.0};
    static Double[] type3Utility = new Double[]{ 0.5,  0.0};
    public static final HashMap<Integer, Double[]> utilities = new HashMap<Integer,Double[]>() {{
        put(5,type2Utility);
        put(12,type3Utility);
        put(13,type3Utility);
        put(14,type1Utility);
        put(15,type1Utility);
        put(16,type1Utility);
        put(17,type1Utility);
        put(18,type2Utility);
        put(19,type2Utility);
        put(20,type2Utility);
        put(21,type2Utility);
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
