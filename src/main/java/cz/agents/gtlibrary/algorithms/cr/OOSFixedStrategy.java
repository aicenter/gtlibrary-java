package cz.agents.gtlibrary.algorithms.cr;

import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSSimulator;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

import static cz.agents.gtlibrary.algorithms.cr.CRExperiments.buildCompleteTree;

public class OOSFixedStrategy extends OOSAlgorithm {
    private InnerNode trackNode;

    public OOSFixedStrategy(Player searchingPlayer,
                            OOSSimulator simulator,
                            GameState rootState,
                            Expander expander) {
        super(searchingPlayer, simulator, rootState, expander);
        init();
    }

    public OOSFixedStrategy(Player searchingPlayer,
                            OOSSimulator simulator,
                            GameState rootState,
                            Expander expander, double delta, double epsilon) {
        super(searchingPlayer, simulator, rootState, expander, delta, epsilon);
        init();
    }

    public OOSFixedStrategy(Player searchingPlayer,
                            InnerNode rootNode,
                            double epsilon) {
        super(searchingPlayer, rootNode, epsilon);
        init();
    }

    public OOSFixedStrategy(Player searchingPlayer, InnerNode rootNode, double epsilon, double delta) {
        super(searchingPlayer, rootNode, epsilon, delta);
        init();
    }

    public OOSFixedStrategy(Player searchingPlayer,
                            GadgetChanceNode rootNode,
                            double epsilon) {
        super(searchingPlayer, rootNode, epsilon);
        init();
    }

    void init() {
        InnerNode rootNode = getRootNode();
        buildCompleteTree(rootNode);
        if(rootNode instanceof ChanceNode) {
            trackNode = (InnerNode) rootNode.getChildFor(rootNode.getActions().get(0));
        } else {
            trackNode = rootNode;
        }
    }

    @Override
    protected void updateInfosetRegrets(MCTSInformationSet is,
                                        Player expPlayer,
                                        OOSAlgorithmData data,
                                        int ai,
                                        double pai,
                                        double u_z,
                                        double rm_h_cn,
                                        double rm_h_opp,
                                        double rm_zha_all,
                                        double s_h_all) {
        // by not updating regrets, RM strategy will stay the same, and thus also the avg strategy.
    }

    protected double mean = 0.;
    protected double var = 0.;

    @Override
    protected void updateHistoryExpectedValue(boolean infosetIsExploringPlayer,
                                              InnerNode updateNode,
                                              double updateVal,
                                              double rm_h_pl,
                                              double rm_h_opp,
                                              double s_h_all) {
        super.updateHistoryExpectedValue(infosetIsExploringPlayer, updateNode, updateVal, rm_h_pl, rm_h_opp, s_h_all);

        if(updateNode.equals(trackNode)) {
            double x = (infosetIsExploringPlayer ? -1 : 1) * updateVal;

            double old_mean = mean;
            double n = numSamplesDuringRun() + 1;
            mean += (x - old_mean) / n;
            var += (x - old_mean)*(x - mean);

            System.out.println(x +";"+mean+";"+Math.sqrt(var/n));
        }
    }
}
