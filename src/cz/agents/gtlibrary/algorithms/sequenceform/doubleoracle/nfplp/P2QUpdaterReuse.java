package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import ilog.concert.IloException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class P2QUpdaterReuse extends InitialP2QBuilderReuse {

    private Map<Sequence, Double> explSeqSum;

    public P2QUpdaterReuse(Player[] players, RecyclingNFPTable table) {
        super(players);
        this.lpTable = table;
    }

//    public void buildLP(double gameValue, QResultReuse data, DoubleOracleConfig<DoubleOracleInformationSet> config, double initialValue) {
//        this.explSeqSum = getSum(data.getLastItSeq(), data.getExplSeqSum(), gameValue);
//        super.buildLP(config, initialValue);
//    }

    @Override
    public void initTable() {
    }

//    @Override
//    protected void updateForP1(Sequence p1Sequence) {
//        Double value = explSeqSum.get(p1Sequence);
//
//        if (value != null)
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
    protected QResultReuse createResult(LPData lpData) throws IloException {
        Map<Sequence, Double> watchedSequenceValues = getWatchedUSequenceValues(lpData);
        Set<Sequence> exploitableSequences = getExploitableSequences(watchedSequenceValues);

        return new QResultReuse(lpData.getSolver().getObjValue(), explSeqSum, exploitableSequences, getRealizationPlan(lpData));
    }

    public void update(double gameValue, QResultReuse qResult, DoubleOracleConfig<DoubleOracleInformationSet> config) {
        this.explSeqSum = getSum(qResult.getLastItSeq(), qResult.getExplSeqSum(), gameValue);
        for (Map.Entry<Sequence, Double> entry : explSeqSum.entrySet()) {
            updateSlackVariable(entry);
        }
    }

    private void updateSlackVariable(Map.Entry<Sequence, Double> entry) {
        lpTable.setConstraint(entry.getKey(), "s", entry.getValue());
    }
}
