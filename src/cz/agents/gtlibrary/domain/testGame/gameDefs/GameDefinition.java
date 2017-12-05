package cz.agents.gtlibrary.domain.testGame.gameDefs;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jakub Cerny on 03/11/2017.
 */
public interface GameDefinition {

    public HashMap<Integer, ArrayList<Integer>> getSuccessors();
    public HashMap<Integer, Integer> getISs();
    public HashMap<Integer, Integer> getPlayersForISs();
    public HashMap<Integer, Double[]> getUtilities();

}
