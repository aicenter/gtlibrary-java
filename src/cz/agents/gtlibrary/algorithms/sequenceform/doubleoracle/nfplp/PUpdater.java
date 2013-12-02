package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.bothplayerslp.LPTable;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.bothplayerslp.RecyclingLPTable;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;
import java.util.Set;

public class PUpdater extends InitialPBuilder {

    private Set<Sequence> lastItSeq;
    private Map<Sequence, Double> explSeqSum;

    public PUpdater(Player[] players, DoubleOracleConfig<DoubleOracleInformationSet> config, RecyclingNFPTable table) {
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

}
