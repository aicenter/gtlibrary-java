package cz.agents.gtlibrary.algorithms.cr;

import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSSimulator;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

import static cz.agents.gtlibrary.algorithms.cr.CRExperiments.buildCompleteTree;

public class OOSFixedStrategy extends OOSAlgorithm {
    private InnerNode trackNode;
    public double expValue;

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
        if(rootNode instanceof ChanceNode && rootNode.getGameState() instanceof IIGoofSpielGameState) {
            trackNode = (InnerNode) rootNode.getChildFor(rootNode.getActions().get(0));
        } else {
            trackNode = rootNode;
        }
    }

    @Override
    protected void updateInfosetRegrets(InnerNode in, Player expPlayer, OOSAlgorithmData data,
                                        int ai, double pai,
                                        double u_z, double u_x, double u_h,
                                        double rm_h_cn, double rm_h_opp, double rm_zha_all, double s_h_all) {
        // by not updating regrets, RM strategy will stay the same, and thus also the avg strategy.
    }

    protected double orig_mean = 0.;
    protected double orig_var = 0.;
    protected double vr_mean = 0.;
    protected double vr_var = 0.;

    @Override
    protected void updateHistoryExpectedValue(Player expPlayer,
                                              InnerNode updateNode,
                                              double u_h,
                                              double rm_h_pl,
                                              double rm_h_opp,
                                              double rm_h_cn,
                                              double s_h_all) {
        super.updateHistoryExpectedValue(expPlayer, updateNode, u_h, rm_h_pl, rm_h_opp, rm_h_cn, s_h_all);

        if(updateNode.equals(trackNode)) {
            double n = numSamplesDuringRun() + 1;

            double vr_x = (expPlayer.getId() == 0 ? 1 : -1) * u_h;
            double old_vr_mean = vr_mean;
            vr_mean += (vr_x - old_vr_mean) / n;
            vr_var += (vr_x - old_vr_mean)*(vr_x - vr_mean);

            double orig_x = (expPlayer.getId() == 0 ? 1 : -1) * u_z;
            double old_orig_mean = orig_mean;
            orig_mean += (orig_x - old_orig_mean) / n;
            orig_var += (orig_x - old_orig_mean)*(orig_x - orig_mean);

            if(n % 1000 == 0)
            System.out.println(
                    n + ";" +
                    orig_x +";"+orig_mean+";"+Math.sqrt(orig_var /n) + ";"+(orig_mean-expValue)+";"+
                    vr_x +";"+ vr_mean +";"+Math.sqrt(vr_var /n)+";"+(vr_mean-expValue));
//                    n + ";" +
//                    (orig_mean-expValue)+";"+
//                    (vr_mean-expValue));
        }
    }
}
