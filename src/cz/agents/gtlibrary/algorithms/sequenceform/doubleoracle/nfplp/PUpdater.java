package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;
import java.util.Set;

public class PUpdater extends InitialPBuilder {

    private Set<Sequence> lastItSeq;
    private Map<Sequence, Double> explSeqSum;

    public PUpdater(Player[] players, SequenceFormConfig<SequenceInformationSet> config, RecyclingNFPTable table) {
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
    public void initObjective(Sequence p2EmptySequence) {
        lpTable.setObjective("t", 1);
    }

    @Override
    protected void updateForP1(Sequence p1Sequence) {

    }

    @Override
    protected void updateForP2(Sequence p2Sequence) {
        if (lastItSeq.contains(p2Sequence))
            lpTable.setConstraintIfNotPresent(p2Sequence, "t", 1);
        else
            lpTable.removeFromConstraint(p2Sequence, "t");
        Double value = explSeqSum.get(p2Sequence);

        if (value != null)
            lpTable.setConstant(p2Sequence, -value);
    }

    @Override
    protected void addUtilities(Iterable<Sequence> p1Sequences, Iterable<Sequence> p2Sequences) {
    }
}
