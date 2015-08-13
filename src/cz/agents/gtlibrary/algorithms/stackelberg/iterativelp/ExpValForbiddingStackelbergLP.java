package cz.agents.gtlibrary.algorithms.stackelberg.iterativelp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExpValForbiddingStackelbergLP extends SumForbiddingStackelbergLP {

    public ExpValForbiddingStackelbergLP(Player leader, GameInfo info) {
        super(leader, info);
    }

//    @Override
//    protected Map<InformationSet, Map<Sequence, Double>> getSequenceEvaluation(LPData lpData, Player player) {
//        Map<InformationSet, Map<Sequence, Double>> strategy = new HashMap<>();
//
//        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
//            if (entry.getKey() instanceof Pair) {
//                Pair varKey = (Pair) entry.getKey();
//
//                if (varKey.getLeft() instanceof Sequence && varKey.getRight() instanceof Sequence) {
//                    Sequence playerSequence = player.equals(leader) ? (Sequence) varKey.getLeft() : (Sequence) varKey.getRight();
//                    Map<Sequence, Double> isStrategy = strategy.get(playerSequence.getLastInformationSet());
//                    Double currentValue = getValueFromCplex(lpData, entry);
//
//                    if (currentValue > eps)
//                        if (isSequenceFrom(player.equals(leader) ? (Sequence) varKey.getRight() : (Sequence) varKey.getLeft(), playerSequence.getLastInformationSet()))
//                            if (isStrategy == null) {
//                                if (currentValue > eps) {
//                                    isStrategy = new HashMap<>();
////                                    double behavioralStrat = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);
//
//                                    isStrategy.put(playerSequence, getExpectedValue(lpData, playerSequence));
//                                    strategy.put(playerSequence.getLastInformationSet(), isStrategy);
//                                }
//                            } else {
//                                double behavioralStrategy = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);
//
//                                if (behavioralStrategy > eps) {
//                                    isStrategy.put(playerSequence, getExpectedValue(lpData, playerSequence));
//                                }
//                            }
//                }
//            }
//        }
//        return strategy;
//    }

    protected Map<InformationSet, Map<Sequence, Double>> getSequenceEvaluation(LPData lpData, Player player) {
        return getBehavioralStrategy(lpData, player);
    }

//    protected Map<InformationSet, Map<Sequence, Double>> getBehavioralStrategy(LPData lpData, Player player) {
//        Map<InformationSet, Map<Sequence, Double>> strategy = new HashMap<>();
//
//        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
//            if (entry.getKey() instanceof Pair) {
//                Pair varKey = (Pair) entry.getKey();
//
//                if (varKey.getLeft() instanceof Sequence && varKey.getRight() instanceof Sequence) {
//                    Sequence playerSequence = player.equals(leader) ? (Sequence) varKey.getLeft() : (Sequence) varKey.getRight();
//                    Map<Sequence, Double> isStrategy = strategy.get(playerSequence.getLastInformationSet());
//                    Double currentValue = getValueFromCplex(lpData, entry);
//
//                    if (currentValue > eps)
//                        if (isSequenceFrom(player.equals(leader) ? (Sequence) varKey.getRight() : (Sequence) varKey.getLeft(), playerSequence.getLastInformationSet()))
//                            if (isStrategy == null) {
//                                if (currentValue > eps) {
//                                    isStrategy = new HashMap<>();
//                                    double behavioralStrat = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);
//
//                                    isStrategy.put(playerSequence, behavioralStrat);
//                                    strategy.put(playerSequence.getLastInformationSet(), isStrategy);
//                                }
//                            } else {
//                                double behavioralStrategy = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);
//
//                                if (behavioralStrategy > eps)
//                                    isStrategy.put(playerSequence, behavioralStrategy);
//                            }
//                }
//            }
//        }
//        return strategy;
//    }

    @Override
    protected Iterable<Sequence> getBrokenStrategyCauses(Map<InformationSet, Map<Sequence, Double>> strategy, LPData lpData) {
        SequenceInformationSet set = null;
        Map<Sequence, Double> shallowestBrokenStrategyCause = null;

        for (Map.Entry<InformationSet, Map<Sequence, Double>> isStrategy : strategy.entrySet()) {
            if (isStrategy.getValue().size() > 1) {
                if (shallowestBrokenStrategyCause == null) {
                    shallowestBrokenStrategyCause = new HashMap<>(isStrategy.getValue());
                    set = (SequenceInformationSet) isStrategy.getKey();
                } else {
                    Sequence candidate = isStrategy.getValue().keySet().iterator().next();
                    Sequence bestSoFar = shallowestBrokenStrategyCause.keySet().iterator().next();

                    if (candidate.size() < bestSoFar.size()) {
                        shallowestBrokenStrategyCause = new HashMap<>(isStrategy.getValue());
                        set = (SequenceInformationSet) isStrategy.getKey();
                    }
                }
            }
        }
        if (set == null)
            return null;
        Map<Sequence, Double> leaderExpectedValues = getLeaderExpectedValues(behavioralToRealizationPlan(getBehavioralStrategy(lpData, leader)), behavioralToRealizationPlan(getBehavioralStrategy(lpData, follower)), shallowestBrokenStrategyCause);

        return sort(leaderExpectedValues, leaderExpectedValues.keySet());
    }

    private Map<Sequence, Double> getLeaderExpectedValues(Map<Sequence, Double> leaderRealPlan, Map<Sequence, Double> followerRealPlan, Map<Sequence, Double> shallowestBrokenStrategyCause) {
        Map<Sequence, Double> expectedValues = new HashMap<>(shallowestBrokenStrategyCause.size());

        for (Sequence sequence : shallowestBrokenStrategyCause.keySet()) {
            Map<Sequence, Double> modifiedFollowerRP = modify(new HashMap<>(followerRealPlan), sequence);

            expectedValues.put(sequence, getExpectedValue(leaderRealPlan, modifiedFollowerRP));
        }
        return expectedValues;
    }

    private Map<Sequence, Double> modify(Map<Sequence, Double> followerRealPlan, Sequence sequence) {
        assert followerRealPlan.containsKey(sequence) && followerRealPlan.get(sequence) > 1e-8;
        Sequence prefix = sequence.getSubSequence(sequence.size() - 1);

        assert followerRealPlan.containsKey(prefix) && followerRealPlan.get(prefix) > 1e-8;
        followerRealPlan.put(sequence, followerRealPlan.get(prefix));

        for (SequenceInformationSet informationSet : algConfig.getReachableSets(sequence)) {
            update(followerRealPlan, informationSet);
        }
        for (Sequence outgoingSequence : ((SequenceInformationSet) sequence.getLastInformationSet()).getOutgoingSequences()) {
            if (outgoingSequence.equals(sequence))
                continue;
            followerRealPlan.put(outgoingSequence, 0d);
            for (SequenceInformationSet informationSet : algConfig.getReachableSets(outgoingSequence)) {
                update(followerRealPlan, informationSet);
            }
        }
        return followerRealPlan;
    }

    private void update(Map<Sequence, Double> followerRealPlan, SequenceInformationSet informationSet) {
        Double prefixProbability = followerRealPlan.get(informationSet.getPlayersHistory());

        if(prefixProbability == null)
            return;
        double probSum = getProbabilitySum(followerRealPlan, informationSet);

        for (Sequence outgoingSequence : informationSet.getOutgoingSequences()) {
            Double oldValue = followerRealPlan.get(outgoingSequence);

            if(oldValue != null)
                followerRealPlan.put(outgoingSequence, oldValue*prefixProbability/probSum);
            for (SequenceInformationSet reachableSet : algConfig.getReachableSets(outgoingSequence)) {
                update(followerRealPlan, reachableSet);
            }
        }
    }

    private double getProbabilitySum(Map<Sequence, Double> followerRealPlan, SequenceInformationSet informationSet) {
        double sum = 0;

        for (Sequence outgoingSequence : informationSet.getOutgoingSequences()) {
            sum += get(followerRealPlan, outgoingSequence);
        }
        return sum;
    }

    private double getExpectedValue(Map<Sequence, Double> leaderRealPlan, Map<Sequence, Double> modifiedFollowerRP) {
        double expVal = 0;

        for (Map.Entry<Map<Player, Sequence>, Double[]> mapEntry : algConfig.getUtilityForSequenceCombinationGenSum().entrySet()) {
            double leaderProb = get(leaderRealPlan, mapEntry.getKey().get(leader));
            double followerProb = get(modifiedFollowerRP, mapEntry.getKey().get(follower));

            expVal += leaderProb * followerProb * mapEntry.getValue()[leader.getId()];
        }
        return expVal;
    }

    private double get(Map<Sequence, Double> leaderRealPlan, Sequence sequence) {
        Double value = leaderRealPlan.get(sequence);

        return value == null ? 0 : value;
    }

    private double getExpectedValue(LPData lpData, Sequence playerSequence) {
        double value = Double.NaN;

        try {
            value = lpData.getSolver().getValue(lpData.getWatchedPrimalVariables().get(new Pair<>("v", playerSequence)));
        } catch (IloException e) {
            e.printStackTrace();
        }
        return value;
    }

//    @Override
//    protected Iterable<Sequence> getBrokenStrategyCauses(Map<InformationSet, Map<Sequence, Double>> strategy, LPData lpData) {
//        SequenceInformationSet set = null;
//        Map<Sequence, Double> chosenBrokenStrategyCause = null;
//        double bestValue = Double.NEGATIVE_INFINITY;
//
//        for (Map.Entry<InformationSet, Map<Sequence, Double>> isStrategy : strategy.entrySet()) {
//            if (isStrategy.getValue().size() > 1) {
//                if (chosenBrokenStrategyCause == null) {
//                    chosenBrokenStrategyCause = new HashMap<>(isStrategy.getValue());
//                    set = (SequenceInformationSet) isStrategy.getKey();
//                } else {
//                    double currentValue = Collections.min(isStrategy.getValue().values());
//
//                    if (currentValue < bestValue) {
//                        chosenBrokenStrategyCause = new HashMap<>(isStrategy.getValue());
//                        set = (SequenceInformationSet) isStrategy.getKey();
//                    }
//                }
//            }
//        }
//        if(set == null)
//            return null;
//        return sort(chosenBrokenStrategyCause, chosenBrokenStrategyCause.keySet());
//    }
}
