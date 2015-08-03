package cz.agents.gtlibrary.algorithms.stackelberg.iterativelp;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;

import java.util.HashMap;
import java.util.Map;

public class ExpValEnforcingStackelbergLP extends SumEnforcingStackelbergLP {
    public ExpValEnforcingStackelbergLP(Player leader, GameInfo info) {
        super(leader, info);
    }

    @Override
    protected Map<InformationSet, Map<Sequence, Double>> getBehavioralStrategy(LPData lpData, Player player) {
        Map<InformationSet, Map<Sequence, Double>> strategy = new HashMap<>();

        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Pair) {
                Pair varKey = (Pair) entry.getKey();

                if (varKey.getLeft() instanceof Sequence && varKey.getRight() instanceof Sequence) {
                    Sequence playerSequence = player.equals(leader) ? (Sequence) varKey.getLeft() : (Sequence) varKey.getRight();
                    Map<Sequence, Double> isStrategy = strategy.get(playerSequence.getLastInformationSet());
                    Double currentValue = getValueFromCplex(lpData, entry);

                    if (currentValue > eps)
                        if (isSequenceFrom(player.equals(leader) ? (Sequence) varKey.getRight() : (Sequence) varKey.getLeft(), playerSequence.getLastInformationSet()))
                            if (isStrategy == null) {
                                if (currentValue > eps) {
                                    isStrategy = new HashMap<>();
//                                    double behavioralStrat = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                                    isStrategy.put(playerSequence, getExpectedValue(lpData, playerSequence));
                                    strategy.put(playerSequence.getLastInformationSet(), isStrategy);
                                }
                            } else {
                                double behavioralStrategy = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                                if (behavioralStrategy > eps) {
                                    isStrategy.put(playerSequence, getExpectedValue(lpData, playerSequence));
                                }
                            }
                }
            }
        }
        return strategy;
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
}
