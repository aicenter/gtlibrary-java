package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.reusing;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;
import java.util.Set;

public class PBuilderReuse extends InitialPBuilderReuse {

    private Set<Sequence> lastItSeq;
    private Map<Sequence, Double> explSeqSum;

    public PBuilderReuse(Player[] players, GameInfo info) {
        super(players, info);
    }

    public void update(QResultReuse data, Double initialValueOfGame, SequenceFormConfig<? extends SequenceInformationSet> config) {
        this.lastItSeq = data.getLastItSeq();
        this.explSeqSum = data.getExplSeqSum();
        addPreviousItConstraints(initialValueOfGame);
        clearSlacks(config.getSequencesFor(players[1]));
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

//    @Override
//    public void buildLP(SequenceFormConfig<? extends SequenceInformationSet> config) {
//        clearSlacks(config.getSequencesFor(players[1]));
//        super.buildLP(config);
//    }

    private void clearSlacks(Iterable<Sequence> sequences) {
        for (Sequence sequence : sequences) {
            lpTable.removeFromConstraint(sequence, "t");
            lpTable.removeConstant(sequence);
        }
    }

    private void addPreviousItConstraints(double initialValueOfGame) {
        lpTable.setConstraint("prevIt", players[1], 1);
        lpTable.setConstant("prevIt", initialValueOfGame);
        lpTable.setConstraintType("prevIt", 1);
    }

    @Override
    public void initObjective(Sequence p2EmptySequence) {
        lpTable.setObjective("t", 1);
    }

//    @Override
//    protected void updateForP2(Sequence p2Sequence) {
//        super.updateForP2(p2Sequence);
//        if (lastItSeq.contains(p2Sequence))
//            lpTable.setConstraint(p2Sequence, "t", 1);
//        Double value = explSeqSum.get(p2Sequence);
//
//        if (value != null)
//            lpTable.setConstant(p2Sequence, -value);
//    }
//
//    public void updateSolver() {
//        try {
//            lpTable.toCplex();
//        } catch (IloException e) {
//            e.printStackTrace();
//        }
//    }
}
