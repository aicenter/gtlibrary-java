package cz.agents.gtlibrary.algorithms.stackelberg.iterativelp;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AllShallowestBrokenCplexStackelbergLP extends ShallowestAllCplexStackelbergLP {

    public AllShallowestBrokenCplexStackelbergLP(Player leader, GameInfo info) {
        super(leader, info);
    }

    protected Set<Sequence> getBrokenStrategyCauses(Map<InformationSet, Map<Sequence, Double>> strategy, LPData lpData) {
        Set<Sequence> shallowestBrokenStrategyCause = null;
        Set<Sequence> isSequences = new HashSet<>();

        for (Map.Entry<InformationSet, Map<Sequence, Double>> isStrategy : strategy.entrySet()) {
            if (isStrategy.getValue().size() > 1) {
                if(!isContinuation(((PerfectRecallInformationSet)isStrategy.getKey()).getPlayersHistory(), isSequences)) {
                    isSequences.add(((PerfectRecallInformationSet)isStrategy.getKey()).getPlayersHistory());
                    if (shallowestBrokenStrategyCause == null)
                        shallowestBrokenStrategyCause = new HashSet<>(isStrategy.getValue().keySet());
                    else
                        shallowestBrokenStrategyCause = new HashSet<>(isStrategy.getValue().keySet());
                }
            }
        }
        return shallowestBrokenStrategyCause;
    }

    private boolean isContinuation(Sequence playersHistory, Set<Sequence> isSequences) {
        for (Sequence sequence : isSequences) {
            if(sequence.isPrefixOf(playersHistory))
                return true;
        }
        return false;
    }
}
