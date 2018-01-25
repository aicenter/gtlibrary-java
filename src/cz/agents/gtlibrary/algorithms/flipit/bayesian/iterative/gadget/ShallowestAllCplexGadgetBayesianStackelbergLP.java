package cz.agents.gtlibrary.algorithms.flipit.bayesian.iterative.gadget;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.domain.flipit.types.FollowerType;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Jakub Cerny on 17/01/2018.
 */
public class ShallowestAllCplexGadgetBayesianStackelbergLP extends ShallowestBrokenCplexGadgetBayesianStackelbergLP {

    public ShallowestAllCplexGadgetBayesianStackelbergLP(Player leader, GameInfo info) {
        super(leader, info);
    }

    @Override
    protected Pair<Map<Sequence, Double>, Double> handleBrokenStrategyCause(double lowerBound, double upperBound, LPData lpData, double value, Iterable<Pair<FollowerType,Sequence>> brokenStrategyCauses) {
        bnbBranchingCount++;
        SequenceInformationSet lastSet = (SequenceInformationSet) brokenStrategyCauses.iterator().next().getRight().getLastInformationSet();
        Set<Sequence> outgoingSequences = lastSet.getOutgoingSequences();
        FollowerType type = brokenStrategyCauses.iterator().next().getLeft();

        HashSet<Pair<FollowerType, Sequence>> typeSeqPairs = new HashSet<>();
        for (Sequence outgoingSequence : outgoingSequences) {
            Pair<FollowerType, Sequence> pair = new Pair<>(type,outgoingSequence);
            addEqualityToBinaryVariableFor(pair, lpData);
            typeSeqPairs.add(pair);
        }
        controlBinaryVariables(typeSeqPairs);
        Pair<Map<Sequence, Double>, Double> currentBest = solve(lowerBound, upperBound);

        if (CLEAN_MILP) removeBinaryConstraints(typeSeqPairs, lpData);
        return currentBest;
    }
}
