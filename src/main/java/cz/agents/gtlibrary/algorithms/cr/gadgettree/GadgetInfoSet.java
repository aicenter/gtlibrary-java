package cz.agents.gtlibrary.algorithms.cr.gadgettree;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.GameState;

public class GadgetInfoSet extends MCTSInformationSet {

    private final GadgetISKey isKey;
    private int terminateCnt = 0;
    private int followCnt = 0;

    public GadgetInfoSet(GameState state, GadgetISKey isKey) {
        super(state);
        this.playerHistory = isKey.getSequence();
        this.isKey = isKey;
        this.hashCode = isKey.getHash();
    }

    public double getIsCFV(int expUtilityIterations) {
        double isCFV = 0; // let's keep 0 by default (if the games are balanced at public states)
        for (InnerNode in : getAllNodes()) {
            GadgetInnerNode n = (GadgetInnerNode) in;
            InnerNode o = n.getOriginalNode();
            assert o.getReachPrPlayerChance() > 0;
            // todo: iterations in "keep" version of resolving
            isCFV += o.getReachPrPlayerChance() * o.getExpectedValue(expUtilityIterations);
        }
        return isCFV * 2;
    }

    public double getIsReach() {
        return getAllNodes().stream()
                .map(in -> ((GadgetInnerNode) in).getOriginalNode().getReachPrPlayerChance())
                .reduce(0.0, Double::sum);
    }

    public int getFollowCnt() {
        return followCnt;
    }

    public void setFollowCnt(int followCnt) {
        this.followCnt = followCnt;
    }

    public int getTerminateCnt() {
        return terminateCnt;
    }

    public void setTerminateCnt(int terminateCnt) {
        this.terminateCnt = terminateCnt;
    }

    public void incrFollowCnt() {
        followCnt++;
    }

    public void incrTerminateCnt() {
        terminateCnt++;
    }

    @Override
    public String toString() {
        return "Gadget " + super.toString();
    }

    @Override
    public ISKey getISKey() {
        return isKey;
    }
}
