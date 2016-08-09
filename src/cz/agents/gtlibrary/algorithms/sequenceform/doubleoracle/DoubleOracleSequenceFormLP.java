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


package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle;

import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import ilog.concert.IloException;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormLP;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DoubleOracleSequenceFormLP extends SequenceFormLP implements DoubleOracleLPSolver {

    protected Map<Player, Set<Sequence>> newSequencesSinceLastLPCalculation = new FixedSizeMap<Player, Set<Sequence>>(2);
    protected Player[] players;
    private long overallGenerationTime = 0;
    private long overallComputationTime = 0;

    private double EPS = 0.00;
    private double boundSize = -1;
    private int updates = 0;

	public DoubleOracleSequenceFormLP(Player[] players) {
		super(players);
        this.players = players;
        for (Player p : players) {
            newSequencesSinceLastLPCalculation.put(p, new HashSet<Sequence>());
        }
	}

	public void calculateStrategyForPlayer(int secondPlayerIndex, GameState root, DoubleOracleConfig algConfig, double currentBoundSize) {
		try {

//            if (this.boundSize < 0) {
//                this.boundSize = currentBoundSize;
//            } else {
//                if (1.0 > currentBoundSize && updates == 0) {
//                    EPS = 0;
//                    updateSequenceVariables();
//                    updates++;
////                } else if (2.0 > currentBoundSize && updates == 0) {
////                    EPS = 0.001;
////                    updateSequenceVariables();
////                    updates++;
//                }
//            }

			int firstPlayerIndex = (1 + secondPlayerIndex) % 2;
			createVariables(algConfig, root.getAllPlayers());

            for (Player p : players) {
                    newSequencesSinceLastLPCalculation.get(p).addAll(algConfig.getNewSequences());
            }
            long currentTime = System.currentTimeMillis();
            setSequencesAndISForGeneratingConstraints(algConfig, players[firstPlayerIndex], players[secondPlayerIndex]);
            overallGenerationTime += (System.currentTimeMillis()) - currentTime;
            currentTime = System.currentTimeMillis();
			double v = calculateOnePlStrategy(algConfig, root, players[firstPlayerIndex], players[secondPlayerIndex]);
            overallComputationTime += (System.currentTimeMillis()) - currentTime;
            newSequencesSinceLastLPCalculation.get(players[secondPlayerIndex]).clear();
//            return v;
		} catch (IloException e) {
			e.printStackTrace();
//			return null;
		}
	}

    private void setSequencesAndISForGeneratingConstraints(DoubleOracleConfig algConfig, Player firstPlayer, Player secondPlayer) {
        sequences.get(firstPlayer).clear();
        informationSets.get(secondPlayer).clear();

        debugOutput.println("Starting identification of Sequences/IS");

        for (Sequence s : newSequencesSinceLastLPCalculation.get(secondPlayer)) {
            if (s.getPlayer().equals(firstPlayer)) {
                sequences.get(firstPlayer).add(s);
                if (s.size() > 0)
                    sequences.get(firstPlayer).add(((SequenceInformationSet)s.getLast().getInformationSet()).getPlayersHistory());
            } else if (s.getPlayer().equals(secondPlayer)) {
                sequences.get(firstPlayer).addAll(algConfig.getCompatibleSequencesFor(s));
                if (s.size() > 0)
                    informationSets.get(secondPlayer).add((SequenceInformationSet) s.getLast().getInformationSet());
                if (algConfig.getReachableSets(s) != null) {
                    for (SequenceInformationSet i : (Set<SequenceInformationSet>)algConfig.getReachableSets(s)) {
                        if (i.getPlayer().equals(secondPlayer))
                            informationSets.get(secondPlayer).add(i);
                    }
                }
            } else assert false;
        }

        debugOutput.println("Ending identification of Sequences/IS");
    }

    public long getOverallGenerationTime() {
        return overallGenerationTime;
    }

    @Override
    public Set<Sequence> getNewSequencesSinceLastLPCalc(Player player) {
        return newSequencesSinceLastLPCalculation.get(player);
    }

    public long getOverallComputationTime() {
        return overallComputationTime;
    }

    @Override
    protected IloNumVar createVariableForSequence(IloCplex cplex, Sequence sequence) throws IloException {
        IloNumVar r = cplex.numVar(Math.pow(EPS, sequence.size()), 1, IloNumVarType.Float, "R" + sequence.toString());

        if (sequence.size() == 0)
            r.setLB(1d);
        variables.put(sequence, r);
        return r;
    }

    protected void updateSequenceVariables() throws IloException {
        for (Object o : variables.keySet()) {
            if (o instanceof  Sequence) {
                    variables.get(o).setLB(Math.pow(EPS, ((Sequence) o).size()));
            }
        }
    }

    @Override
    protected Map<Sequence, Double> createSolution(SequenceFormConfig<SequenceInformationSet> algConfig, Player secondPlayer, IloCplex cplex) throws IloException {
        Map<Sequence, Double> result = super.createSolution(algConfig, secondPlayer, cplex);

//        for (Player p : players)  {
//            Set<Sequence> seqs = (Set)((DoubleOracleConfig)algConfig).getFullBRSequences().get(p);
//            for (Sequence s : seqs) {
//                if (result.containsKey(s) || s.size() > 0) continue;
//                Sequence ss = new LinkedListSequenceImpl(s);
//                HashSet<Sequence> toAdd = new HashSet<Sequence>();
//                while (!result.containsKey(ss) && ss.size() > 0) {
//                    toAdd.add(new LinkedListSequenceImpl(ss));
//                    ss.removeLast();
//                }
//                if (ss.size() > 0) {
//                    for (Sequence sss : toAdd)
//                        result.put(sss, result.get(ss));
//                }
//            }
//        }
        return result;
    }
}
