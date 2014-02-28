package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp.reusing;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;
import java.util.Set;

public class P2PUpdaterReuse extends InitialP2PBuilderReuse {

    private Set<Sequence> lastItSeq;
    private Map<Sequence, Double> explSeqSum;

    public P2PUpdaterReuse(Player[] players, RecyclingNFPTable table) {
        super(players);
        lpTable = table;
    }

//    public void buildLP(QResultReuse data, DoubleOracleConfig<DoubleOracleInformationSet> config) {
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
//        Double value = explSeqSum.get(p1Sequence);
//
//        if (value != null)
//            lpTable.setConstant(p1Sequence, -value);
//    }

//    @Override
//    protected void updateForP2(Sequence p2Sequence) {
//    }
//
//    @Override
//    protected void addUtilities(Iterable<Sequence> p1Sequences, Iterable<Sequence> p2Sequences) {
//    }

    public void update(QResultReuse qResult, DoubleOracleConfig<DoubleOracleInformationSet> config) {
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
