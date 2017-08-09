package cz.agents.gtlibrary.algorithms.sequenceform.gensum.quantalresponse;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.List;
import java.util.Map;

public class QREResult {
    public List<Map<Player, Map<Sequence, Double>>> quantalResponses;
    public List<Double> lambdas;

    public QREResult(List<Map<Player, Map<Sequence, Double>>> quantalResponses, List<Double> lambdas) {
        this.quantalResponses = quantalResponses;
        this.lambdas = lambdas;
    }
}
