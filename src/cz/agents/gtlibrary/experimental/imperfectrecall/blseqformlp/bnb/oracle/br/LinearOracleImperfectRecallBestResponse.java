package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br;

import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LinearOracleImperfectRecallBestResponse extends OracleImperfectRecallBestResponse {
    private SQFBestResponseAlgorithm sqfBR;
    private GameState root;

    public LinearOracleImperfectRecallBestResponse(Player player, GameState root, Expander<SequenceFormIRInformationSet> expander, GameInfo info) {
        super(player, expander, info);
        this.root = root;
        sqfBR = new SQFBestResponseAlgorithm(expander, player.getId(), new Player[]{new PlayerImpl(0), new PlayerImpl(1)}, expander.getAlgorithmConfig(), info);
    }

    @Override
    public Map<Action, Double> getBestResponseSequence(Map<Sequence, Double> opponentStrategy) {
        value = sqfBR.calculateBR(root, opponentStrategy);
        return toBehavioralStrategy(sqfBR.getBRSequences());
    }

    private Map<Action, Double> toBehavioralStrategy(HashSet<Sequence> sequences) {
        return sequences.stream().filter(s -> !s.isEmpty()).collect(Collectors.toMap(s -> s.getLast(), s -> 1d, (oldKey, newKey) -> newKey));
    }

    @Override
    public Map<Action, Double> getBestResponse(Map<Action, Double> opponentStrategy) {
        return getBestResponseSequence(toRealizationPlan(opponentStrategy));
    }

    private Map<Sequence, Double> toRealizationPlan(Map<Action, Double> strategy) {
        Map<Sequence, Double> rp = strategy.entrySet().stream().filter(entry -> entry.getValue() > 1e-8).map(entry -> {
            Set<Sequence> incomingSequences = ((SequenceFormIRInformationSet) entry.getKey().getInformationSet()).getOutgoingSequences().keySet();

            return incomingSequences.stream().map(s -> {
                Sequence extended = new ArrayListSequenceImpl(s);

                extended.addLast(entry.getKey());
                return extended;
            }).collect(Collectors.toSet());
        }).flatMap(set -> set.stream()).collect(Collectors.toMap(s -> s, s -> getProbability(s, strategy)));
        rp.put(new ArrayListSequenceImpl(info.getOpponent(player)), 1d);
        return rp;
    }

    private double getProbability(Sequence sequence, Map<Action, Double> strategy) {
        double product = 1;

        for (Action action : sequence) {
            product *= strategy.getOrDefault(action, 0d);
        }
        return product;
    }

    @Override
    public Map<Action, Double> getBestResponseIn(GameState state, Map<Action, Double> opponentStrategy) {
       return getBestResponseSequenceIn(state, toRealizationPlan(opponentStrategy));
    }

    @Override
    public Map<Action, Double> getBestResponseSequenceIn(GameState state, Map<Sequence, Double> opponentStrategy) {
        value = sqfBR.calculateBR(state, opponentStrategy);
        return toBehavioralStrategy(sqfBR.getBRSequences());
    }
}
