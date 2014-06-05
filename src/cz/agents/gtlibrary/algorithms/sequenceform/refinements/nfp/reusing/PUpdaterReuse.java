package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.reusing;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;
import java.util.Set;

public class PUpdaterReuse extends InitialPBuilderReuse {

    private Set<Sequence> lastItSeq;
    private Map<Sequence, Double> explSeqSum;

    public PUpdaterReuse(Player[] players, RecyclingNFPTable table, GameInfo info) {
        super(players, info);
        lpTable = table;
    }

    public void update(QResultReuse qResult, SequenceFormConfig<? extends SequenceInformationSet> config) {
        removeSlackVariables(config.getSequencesFor(players[1]));
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

    @Override
    public void initTable() {
    }

    @Override
    public void initObjective(Sequence p2EmptySequence) {
        lpTable.setObjective("t", 1);
    }

//    @Override
//    protected void updateForP1(Sequence p1Sequence) {
//
//    }
//
//    @Override
//    protected void updateForP2(Sequence p2Sequence) {
//        if (lastItSeq.contains(p2Sequence))
//            lpTable.setConstraintIfNotPresent(p2Sequence, "t", 1);
//        else
//            lpTable.removeFromConstraint(p2Sequence, "t");
//        Double value = explSeqSum.get(p2Sequence);
//
//        if (value != null)
//            lpTable.setConstant(p2Sequence, -value);
//    }
//
//    @Override
//    protected void addUtilities(Iterable<Sequence> p1Sequences, Iterable<Sequence> p2Sequences) {
//    }

}
