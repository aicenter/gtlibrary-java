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
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import ilog.concert.IloException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class P2QUpdater extends InitialP2QBuilder {

    private Map<Sequence, Double> explSeqSum;

    public P2QUpdater(Player[] players, RecyclingNFPTable table, GameInfo info) {
        super(players, info);
        this.lpTable = table;
    }

//    public void buildLP(double gameValue, QResultReuse data, SequenceFormConfig<? extends SequenceInformationSet> config, double initialValue) {
//        this.explSeqSum = getSum(data.getLastItSeq(), data.getExplSeqSum(), gameValue);
//        super.buildLP(config, initialValue);
//    }

    @Override
    public void initTable() {
    }

//    @Override
//    protected void updateForP1(Sequence p1Sequence) {
//        Double reward = explSeqSum.get(p1Sequence);
//
//        if (reward != null)
//            lpTable.setConstraint(p1Sequence, "s", explSeqSum.get(p1Sequence));
//    }

//    @Override
//    protected void updateForP2(Sequence p2Sequence) {
//    }
//
//    @Override
//    protected void addUtilities(Iterable<Sequence> p1Sequences, Iterable<Sequence> p2Sequences) {
//    }

    @Override
    protected Map<Sequence, Double> getSum(Set<Sequence> exploitableSequences, Map<Sequence, Double> explSeqSum, double valueOfGame) {
        Map<Sequence, Double> updatedSum = new HashMap<Sequence, Double>(explSeqSum);

        for (Sequence sequence : exploitableSequences) {
            Double oldValue = updatedSum.get(sequence);

            updatedSum.put(sequence, (oldValue == null ? 0 : oldValue) + valueOfGame);
        }
        return updatedSum;
    }

    @Override
    protected QResult createResult(LPData lpData) throws IloException {
        Map<Sequence, Double> watchedSequenceValues = getWatchedUSequenceValues(lpData);
        Set<Sequence> exploitableSequences = getExploitableSequences(watchedSequenceValues);

        return new QResult(lpData.getSolver().getObjValue(), explSeqSum, exploitableSequences, getRealizationPlan(lpData));
    }

    public void update(double gameValue, QResult qResult, SequenceFormConfig<? extends SequenceInformationSet> config) {
        this.explSeqSum = getSum(qResult.getLastItSeq(), qResult.getExplSeqSum(), gameValue);
        for (Map.Entry<Sequence, Double> entry : explSeqSum.entrySet()) {
            updateSlackVariable(entry);
        }
    }

    private void updateSlackVariable(Map.Entry<Sequence, Double> entry) {
        lpTable.setConstraint(entry.getKey(), "s", entry.getValue());
    }
}
