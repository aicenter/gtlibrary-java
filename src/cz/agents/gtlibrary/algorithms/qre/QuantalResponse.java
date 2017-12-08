package cz.agents.gtlibrary.algorithms.qre;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;

public class QuantalResponse {
    public static double computeQR(Node node, double pi1, double pi2, Player expPlayer, double lambda) {
        if (pi1 == 0 && pi2 == 0) return 0;
        if (node instanceof LeafNode) {
            return ((LeafNode) node).getUtilities()[expPlayer.getId()];
        }
        if (node instanceof ChanceNode) {
            ChanceNode cn = (ChanceNode) node;
            double ev = 0;
            for (Action ai : cn.getActions()) {
                final double p = cn.getGameState().getProbabilityOfNatureFor(ai);
                double new_p1 = expPlayer.getId() == 1 ? pi1 * p : pi1;
                double new_p2 = expPlayer.getId() == 0 ? pi2 * p : pi2;
                ev += p * computeQR(cn.getChildFor(ai), new_p1, new_p2, expPlayer, lambda);
            }
            return ev;
        }
        InnerNode in = (InnerNode) node;
        MCTSInformationSet is = in.getInformationSet();
        OOSAlgorithmData data = (OOSAlgorithmData) is.getAlgorithmData();

        double[] rmProbs = data.getRMStrategy();
        double[] tmpV = new double[rmProbs.length];
        double ev = 0;

        if (is.getPlayer().equals(expPlayer)) {
            int i = -1;
            double denominator = 0;
            for (Action ai : in.getActions()) {
                i++;
                if (is.getPlayer().getId() == 0) {
                    tmpV[i] = computeQR(in.getChildFor(ai), pi1 * rmProbs[i], pi2, expPlayer, lambda);
                } else {
                    tmpV[i] = computeQR(in.getChildFor(ai), pi1, rmProbs[i] * pi2, expPlayer, lambda);
                }
                ev += Math.exp(lambda*tmpV[i])*tmpV[i];
                rmProbs[i] = Math.exp(lambda*tmpV[i]);
                denominator += Math.exp(lambda*tmpV[i]);
            }
            for (int j=0; j<rmProbs.length; j++) rmProbs[j] = rmProbs[j]/ denominator;
            ev = ev / denominator;
            data.updateAllRegrets(rmProbs,0,1);
            data.updateMeanStrategy(rmProbs,1);

        } else {
            int i = -1;
            for (Action ai : in.getActions()) {
                i++;
                if (is.getPlayer().getId() == 0) {
                    tmpV[i] = computeQR(in.getChildFor(ai), pi1 * rmProbs[i], pi2, expPlayer, lambda);
                } else {
                    tmpV[i] = computeQR(in.getChildFor(ai), pi1, rmProbs[i] * pi2, expPlayer, lambda);
                }
                ev += rmProbs[i] * tmpV[i];
            }
        }




        return ev;
    }
}
