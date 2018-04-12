package cz.agents.gtlibrary.domain.bpg.imperfectrecall;

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.bpg.data.BorderPatrollingGraph;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.iinodes.*;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Observation;
import cz.agents.gtlibrary.utils.BasicGameBuilder;
import cz.agents.gtlibrary.utils.graph.Node;

public class AttackerIRBPGGameState extends BPGGameState {

    public static void main(String[] args) {
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BPGGameInfo());
        GameState root = new AttackerIRBPGGameState();
        Expander<SequenceFormIRInformationSet> expander = new BPGExpander<>(config);

        BasicGameBuilder.build(root, config, expander);

        System.out.println(config.getAllInformationSets().size());
        System.out.println(config.getSequencesFor(BPGGameInfo.DEFENDER).size());

        root = new BPGGameState();
        SequenceFormConfig<SequenceInformationSet> config1 = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> expander1 = new BPGExpander<>(config1);
        FullSequenceEFG efg = new FullSequenceEFG(root, expander1, new BPGGameInfo(), config1);
        efg.generateCompleteGame();
        System.out.println(config1.getAllInformationSets().size());
        System.out.println(config1.getSequencesFor(BPGGameInfo.DEFENDER).size());
//        GambitEFG exporter = new GambitEFG();
//        exporter.write("IRBPG.gbt", root, expander);
    }

    public AttackerIRBPGGameState() {
        super();
    }

    public AttackerIRBPGGameState(BorderPatrollingGraph graph) {
        super(graph);
    }

    public AttackerIRBPGGameState(BPGGameState gameState) {
        super(gameState);
    }

    @Override
    public GameState copy() {
        return new AttackerIRBPGGameState(this);
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (key != null)
            return key;
        if (playerToMove.equals(BPGGameInfo.ATTACKER)) {
            Observations attackerObservations = new Observations(BPGGameInfo.ATTACKER, BPGGameInfo.ATTACKER);

            attackerObservations.add(new BPGAttackerAttackerObservation(getAttackerPosition(), isSlowAttackerMovement(), getSequenceForPlayerToMove().size()));
            key = new ImperfectRecallISKey(attackerObservations, null, null);
        } else {
            Observations observations = new Observations(BPGGameInfo.DEFENDER, BPGGameInfo.ATTACKER);

            observations.add(new PerfectRecallObservation((PerfectRecallISKey) super.getISKeyForPlayerToMove()));
            key = new ImperfectRecallISKey(observations, null, null);
        }
        return key;
    }

    class BPGAttackerAttackerObservation implements Observation {
        protected Node attackerPosition;
        protected boolean slowMove;
        protected int moveNumber;

        public BPGAttackerAttackerObservation(Node attackerPosition, boolean slowMove, int moveNumber) {
            this.attackerPosition = attackerPosition;
            this.slowMove = slowMove;
            this.moveNumber = moveNumber;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BPGAttackerAttackerObservation)) return false;

            BPGAttackerAttackerObservation that = (BPGAttackerAttackerObservation) o;

            if (slowMove != that.slowMove) return false;
            if (moveNumber != that.moveNumber) return false;
            return attackerPosition.equals(that.attackerPosition);

        }

        @Override
        public int hashCode() {
            int result = attackerPosition.hashCode();
            result = 31 * result + (slowMove ? 1 : 0);
            result = 31 * result + moveNumber;
            return result;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
