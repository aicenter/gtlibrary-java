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

public class P2PUpdater extends InitialP2PBuilder {

    private Set<Sequence> lastItSeq;
    private Map<Sequence, Double> explSeqSum;

    public P2PUpdater(Player[] players, RecyclingNFPTable table, GameInfo info) {
        super(players, info);
        lpTable = table;
    }

//    public void buildLP(QResultReuse data, SequenceFormConfig<? extends SequenceInformationSet> config) {
//        this.lastItSeq = data.getLastItSeq();
//        this.explSeqSum = data.getExplSeqSum();
//
//        super.buildLP(config);
//    }

    @Override
    public void initTable() {
    }

//    @Override
//    protected void updateForP1(Sequence p1Sequence) {
//        if (lastItSeq.contains(p1Sequence))
//            lpTable.setConstraint(p1Sequence, "t", 1);
//        else
//            lpTable.removeFromConstraint(p1Sequence, "t");
//        Double reward = explSeqSum.get(p1Sequence);
//
//        if (reward != null)
//            lpTable.setConstant(p1Sequence, -reward);
//    }

//    @Override
//    protected void updateForP2(Sequence p2Sequence) {
//    }
//
//    @Override
//    protected void addUtilities(Iterable<Sequence> p1Sequences, Iterable<Sequence> p2Sequences) {
//    }

    public void update(QResult qResult, SequenceFormConfig<? extends SequenceInformationSet> config) {
        removeSlackVariables(config.getSequencesFor(players[0]));
        for (Sequence sequence : qResult.getLastItSeq()) {
            addSlackVariable(sequence);
        }
        for (Map.Entry<Sequence, Double> entry : qResult.getExplSeqSum().entrySet()) {
            addSlackConstant(entry);
        }
    }

    private void addSlackConstant(Map.Entry<Sequence, Double> entry) {
        lpTable.setConstant(entry.getKey(), -entry.getValue());
    }

    private void removeSlackVariables(Iterable<Sequence> sequences) {
        for (Sequence sequence : sequences) {
            lpTable.removeFromConstraint(sequence, "t");
        }
    }

    private void addSlackVariable(Sequence sequence) {
        lpTable.setConstraint(sequence, "t", 1);
    }
}
