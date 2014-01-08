package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;
import java.util.Set;

public class P2PUpdater extends InitialP2PBuilder {

    private Set<Sequence> lastItSeq;
    private Map<Sequence, Double> explSeqSum;

    public P2PUpdater(Player[] players, SequenceFormConfig<SequenceInformationSet> config, RecyclingNFPTable table) {
        super(players, config);
        lpTable = table;
    }

    public void buildLP(QResult data) {
        this.lastItSeq = data.getLastItSeq();
        this.explSeqSum = data.getExplSeqSum();

        super.buildLP();
    }

    @Override
    public void initTable() {
    }

    @Override
    protected void updateForP1(Sequence p1Sequence) {
        if (lastItSeq.contains(p1Sequence))
            lpTable.setConstraint(p1Sequence, "t", 1);
        else
            lpTable.removeFromConstraint(p1Sequence, "t");
        Double value = explSeqSum.get(p1Sequence);

        if (value != null)
            lpTable.setConstant(p1Sequence, -value);
    }

    @Override
    protected void updateForP2(Sequence p2Sequence) {
    }

    @Override
    protected void addUtilities(Iterable<Sequence> p1Sequences, Iterable<Sequence> p2Sequences) {
    }
}
