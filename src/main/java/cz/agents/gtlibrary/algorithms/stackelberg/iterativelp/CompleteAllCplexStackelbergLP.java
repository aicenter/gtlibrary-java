package cz.agents.gtlibrary.algorithms.stackelberg.iterativelp;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CompleteAllCplexStackelbergLP extends CompleteBrokenCplexStackelbergLP {

    public CompleteAllCplexStackelbergLP(Player leader, GameInfo info) {
        super(leader, info);
    }

    protected Set<Sequence> getBrokenStrategyCauses(Map<InformationSet, Map<Sequence, Double>> strategy, LPData lpData) {
        Set<Sequence> shallowestBrokenStrategyCause = null;

        for (Map<Sequence, Double> isStrategy : strategy.values()) {
            if (isStrategy.size() > 1) {
                if (shallowestBrokenStrategyCause == null)
                    shallowestBrokenStrategyCause = new HashSet<>(isStrategy.keySet());
                else
                    shallowestBrokenStrategyCause.addAll(isStrategy.keySet());
            }
        }
        return shallowestBrokenStrategyCause;
    }
}
