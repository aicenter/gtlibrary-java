package cz.agents.gtlibrary.algorithms.bestresponse;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;

public interface ImperfectRecallBestResponse {
    public Map<Action, Double> getBestResponse(Map<Action, Double> opponentStrategy);
    public Map<Action, Double> getBestResponseSequence(Map<Sequence, Double> opponentStrategy);
    public double getValue();
    public long getExpandedNodes();
}
