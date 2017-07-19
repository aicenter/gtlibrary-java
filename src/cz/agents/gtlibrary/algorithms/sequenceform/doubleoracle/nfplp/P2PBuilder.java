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


package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;
import java.util.Set;

public class P2PBuilder extends InitialP2PBuilder {

    private Set<Sequence> lastItSeq;
    private Map<Sequence, Double> explSeqSum;

    public P2PBuilder(Player[] players, GameInfo info) {
        super(players, info);
    }

    public void update(QResult data, double initialValueOfGame, SequenceFormConfig<? extends SequenceInformationSet> config) {
        this.lastItSeq = data.getLastItSeq();
        this.explSeqSum = data.getExplSeqSum();
        addPreviousItConstraints(initialValueOfGame);
        clearSlacks(config.getSequencesFor(players[0]));
        for (Sequence sequence : lastItSeq) {
            addSlackVariable(sequence);
        }
        for (Map.Entry<Sequence, Double> entry : explSeqSum.entrySet()) {
            addSlackConstant(entry);
        }
    }

    private void addSlackConstant(Map.Entry<Sequence, Double> entry) {
        lpTable.setConstant(entry.getKey(), -entry.getValue());
    }

    private void addSlackVariable(Sequence sequence) {
        lpTable.setConstraint(sequence, "t", 1);
    }

    private void addPreviousItConstraints(double initialValueOfGame) {
        lpTable.setConstraint("prevIt", players[0], 1);
        lpTable.setConstant("prevIt", initialValueOfGame);
        lpTable.setConstraintType("prevIt", 1);
    }

//    @Override
//    public void buildLP(DoubleOracleConfig<DoubleOracleInformationSet> config) {
//        clearSlacks(config.getSequencesFor(players[0]));
//        super.buildLP(config);
//    }

    private void clearSlacks(Iterable<Sequence> sequences) {
        for (Sequence sequence : sequences) {
            lpTable.removeFromConstraint(sequence, "t");
            lpTable.removeConstant(sequence);
        }
    }

    @Override
    public void initObjective(Sequence p2EmptySequence) {
        lpTable.setObjective("t", 1);
    }

//    @Override
//    protected void updateForP1(Sequence p1Sequence) {
//        super.updateForP1(p1Sequence);
//
//        if (lastItSeq.contains(p1Sequence))
//            lpTable.setConstraint(p1Sequence, "t", 1);
//        Double reward = explSeqSum.get(p1Sequence);
//
//        if (reward != null)
//            lpTable.setConstant(p1Sequence, -reward);
//    }

//    public void updateSolver() {
//        try {
//            lpTable.toCplex();
//        } catch (IloException e) {
//            e.printStackTrace();
//        }
//    }

}
