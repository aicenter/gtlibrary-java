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

public class QUpdater extends InitialQBuilder {

    private Map<Sequence, Double> explSeqSum;

    public QUpdater(Player[] players, RecyclingNFPTable lpTable, GameInfo info) {
        super(players, info);
        this.lpTable = lpTable;
    }

//    @Override
//    protected void updateForP2(Sequence p2Sequence) {
//        Double value = explSeqSum.get(p2Sequence);
//
//        if (value != null)
//            lpTable.setConstraint(p2Sequence, "s", explSeqSum.get(p2Sequence));
//    }

    @Override
    public void initTable() {
    }

//    public void buildLP(QResultReuse data, double gameValue, DoubleOracleConfig<DoubleOracleInformationSet> config, double initialValue) {
//        this.explSeqSum = getSum(data.getLastItSeq(), data.getExplSeqSum(), gameValue);
//        super.buildLP(config, initialValue, p1SequencesToAdd);
//    }

//    @Override
//    protected void updateForP1(Sequence p1Sequence) {
//    }
//
//    @Override
//    protected void addUtilities(Iterable<Sequence> p1Sequences, Iterable<Sequence> p2Sequences) {
//    }

    @Override
    protected Map<Sequence, Double> getSum(Set<Sequence> exploitableSequences, Map<Sequence, Double> explSeqSum, double valueOfGame) {
        Map<Sequence, Double> updatedSum = new HashMap<Sequence, Double>(explSeqSum);

        for (Sequence sequence : exploitableSequences) {
            Double value = updatedSum.get(sequence);

            updatedSum.put(sequence, (value == null ? 0 : value) + valueOfGame);
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
