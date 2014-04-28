package cz.agents.gtlibrary.algorithms.mcts.selectstrat;


import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.interfaces.Action;
import java.io.Serializable;

public interface Selector extends Serializable, AlgorithmData {

    /**
     * Returns selected action index.
     */
    public int select();
    /**
    * Updates the selector with action result
    */
    public void update(int selection, double value);

}
