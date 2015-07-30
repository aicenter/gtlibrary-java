package cz.agents.gtlibrary.algorithms.stackelberg.iterativelp;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

import java.util.Set;

public class EnforcingStackelbergLP extends ForbiddingStackelbergLP {
    public EnforcingStackelbergLP(Player leader, GameInfo info) {
        super(leader, info);
    }

    @Override
    protected void restrictFollowerPlay(Sequence brokenStrategyCause, Set<Sequence> brokenStrategyCauses, LPData lpData) {
        System.out.println(brokenStrategyCause + " enforced");
        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Pair) {
                if (((Pair) varKey).getLeft() instanceof Sequence && ((Pair) varKey).getRight() instanceof Sequence) {
                    Pair<Sequence, Sequence> p = (Pair<Sequence, Sequence>) varKey;

                    if (p.getRight().equals(brokenStrategyCause)) {
                        Pair<String, Pair<Sequence, Sequence>> eqKey = new Pair<>("restr", p);
                        Pair<Sequence, Sequence> prefixKey =  new Pair<>(p.getLeft(), brokenStrategyCause.getSubSequence(brokenStrategyCause.size() - 1));

                        assert lpData.getWatchedPrimalVariables().containsKey(prefixKey);
                        lpTable.setConstraint(eqKey, p, 1);
                        lpTable.setConstraint(eqKey, prefixKey, -1);
                        lpTable.setConstraintType(eqKey, 1);
                    }
                }
            }
        }
    }

    @Override
    protected void removeRestriction(Sequence brokenStrategyCause, Set<Sequence> brokenStrategyCauses, LPData lpData) {
        System.out.println(brokenStrategyCause + " released");
        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Pair) {
                if (((Pair) varKey).getLeft() instanceof Sequence && ((Pair) varKey).getRight() instanceof Sequence) {
                    Pair<Sequence, Sequence> p = (Pair<Sequence, Sequence>) varKey;

                    if (p.getRight().equals(brokenStrategyCause)) {
                        Pair<String, Pair<Sequence, Sequence>> eqKey = new Pair<>("restr", p);
                        Pair<Sequence, Sequence> prefixKey =  new Pair<>(p.getLeft(), brokenStrategyCause.getSubSequence(brokenStrategyCause.size() - 1));

                        lpTable.removeFromConstraint(eqKey, p);
                        lpTable.removeFromConstraint(eqKey, prefixKey);
                        lpTable.removeConstant(eqKey);
                    }
                }
            }
        }
    }
}
