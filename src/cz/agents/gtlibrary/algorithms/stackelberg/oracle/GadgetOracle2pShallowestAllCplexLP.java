package cz.agents.gtlibrary.algorithms.stackelberg.oracle;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

import java.util.Map;
import java.util.Set;

/**
 * Created by Jakub Cerny on 17/01/2018.
 */
public class GadgetOracle2pShallowestAllCplexLP extends GadgetOracle2pShallowestBrokenCplexLP {

    public GadgetOracle2pShallowestAllCplexLP(Player leader, GameInfo info) {
        super(leader, info);
    }

    @Override
    protected Pair<Map<Sequence, Double>, Double> handleBrokenStrategyCause(double lowerBound, double upperBound, LPData lpData, double value, Iterable<Sequence> brokenStrategyCauses) {
        SequenceInformationSet lastSet = (SequenceInformationSet) brokenStrategyCauses.iterator().next().getLastInformationSet();
        Set<Sequence> outgoingSequences = lastSet.getOutgoingSequences();

//        bnbBranchingCount++;
        for (Sequence outgoingSequence : outgoingSequences) {
            addEqualityToBinaryVariableFor(outgoingSequence, lpData);
        }
        controlBinaryVariables(outgoingSequences);
        Pair<Map<Sequence, Double>, Double> currentBest = solve(lowerBound, upperBound);

        if (CLEAR_LP) removeBinaryConstraints(outgoingSequences, lpData);
        return currentBest;
    }

}
