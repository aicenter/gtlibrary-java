/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.improvedBR;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleSequenceFormLP;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.cplex.IloCplex;

import java.util.HashSet;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: kail
 * Date: 5/27/13
 * Time: 10:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class BestMinmaxCoreLP extends DoubleOracleSequenceFormLP {

    private GameState rootState;

    public BestMinmaxCoreLP(Player[] players, GameState rootState) {
        super(players);
        this.rootState = rootState;
    }

    public Double calculateMinmaxImprovement(Sequence newSequence,SequenceFormConfig algConfig) {
        Player firstPlayer = newSequence.getPlayer();
        Player secondPlayer = players[(firstPlayer.getId() + 1) % 2];

        IloCplex cplex = modelsForPlayers.get(firstPlayer);
        if (cplex == null || variables.isEmpty()) return null;

        double oldValue = getResultForPlayer(firstPlayer);
        double newValue = Double.NEGATIVE_INFINITY;

        if (sequences.get(firstPlayer).contains(newSequence)) return Double.NEGATIVE_INFINITY;

        try {
            createConstraintForSequence(cplex, newSequence, algConfig);

            cplex.solve();

            if (cplex.getCplexStatus() != IloCplex.CplexStatus.Optimal) {
                assert false;
            }

            newValue = cplex.getValue(objectiveForPlayers.get(firstPlayer));
            cplex.remove(constraints.get(newSequence));
            constraints.remove(newSequence);
        } catch (IloException e) {
            e.printStackTrace();
        }

        return (newValue - oldValue);
    }

    protected IloNumExpr computeSumGR(IloCplex cplex, Sequence firstPlayerSequence, SequenceFormConfig<SequenceInformationSet> algConfig, Player firstPlayer) throws IloException {
        boolean isBR = !sequences.get(firstPlayer).contains(firstPlayerSequence);

        IloNumExpr sumGR = cplex.constant(0);
        HashSet<Sequence> secondPlayerSequences = new HashSet<Sequence>();

        if (isBR) {
            secondPlayerSequences.addAll(sequences.get(players[(firstPlayer.getId() + 1) % 2]));
        } else {
            if (algConfig.getCompatibleSequencesFor(firstPlayerSequence) != null)
                secondPlayerSequences.addAll(algConfig.getCompatibleSequencesFor(firstPlayerSequence));
        }

        for (Sequence secondPlayerSequence : secondPlayerSequences) {
            IloNumExpr prob = variables.get(secondPlayerSequence);

            if (prob == null)
                continue;
            Map<Player, Sequence> actions = createActions(firstPlayerSequence, secondPlayerSequence);

            double utility;
            if (isBR) {
                utility = executeTwoActions(actions);
            } else {
                utility = getUtility(algConfig, actions);
            }

            utility = utility * ((firstPlayer.getId() == 1) ? -1 : 1);
            sumGR = cplex.sum(sumGR, cplex.prod(utility, prob));
        }
        return sumGR;
    }

    private double executeTwoActions(Map<Player, Sequence> actions) {
        double utility = 0;

        if (actions.get(rootState.getPlayerToMove()).size() == 0 ||
                actions.get(players[(rootState.getPlayerToMove().getId() + 1) % 2]).size() == 0) return utility;

        GameState newState = rootState.performAction(actions.get(rootState.getPlayerToMove()).getFirst());
        newState = newState.performAction(actions.get(newState.getPlayerToMove()).getFirst());

        assert (newState.isGameEnd());

        utility = newState.getUtilities()[0];

        return utility;
    }
}
