package cz.agents.gtlibrary.domain.bpg.imperfectrecall;

import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.bpg.data.BorderPatrollingGraph;
import cz.agents.gtlibrary.iinodes.*;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Observation;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.graph.Node;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;

public class IRBPGGameState extends BPGGameState {
    public IRBPGGameState() {
    }

    public IRBPGGameState(BorderPatrollingGraph graph) {
        super(graph);
    }

    public IRBPGGameState(BPGGameState gameState) {
        super(gameState);
    }

    @Override
    public GameState copy() {
        return new IRBPGGameState();
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (key != null)
            return key;
        if (playerToMove.equals(BPGGameInfo.ATTACKER)) {
            Observations attackerObservations = new Observations(BPGGameInfo.ATTACKER, BPGGameInfo.ATTACKER);

            attackerObservations.add(new BPGAttackerAttackerObservation(getSequenceForPlayerToMove()));
            Observations defenderObservations = new Observations(BPGGameInfo.ATTACKER, BPGGameInfo.DEFENDER);

            defenderObservations.add(new BPGAttackerDefenderObservation(new HashCodeBuilder().append(isGameEnd()).append(getHistory().getSequenceOf(playerToMove)).toHashCode()));
            key = new ImperfectRecallISKey(attackerObservations, defenderObservations, null);
        } else {
            Observations attackerObservations = new Observations(BPGGameInfo.DEFENDER, BPGGameInfo.ATTACKER);

            attackerObservations.add(new BPGDefenderAttackerObservation(flaggedNodesObservedByPatroller));
            Observations defenderObservations = new Observations(BPGGameInfo.DEFENDER, BPGGameInfo.DEFENDER);

            defenderObservations.add(new BPGDefenderDefenderObservation(p1Position, p2Position));
            key = new ImperfectRecallISKey(defenderObservations, attackerObservations, null);
        }
        return key;
    }

    class BPGDefenderAttackerObservation implements Observation {

        protected Set<Node> flaggedNodesObservedByPatroller;

        public BPGDefenderAttackerObservation(Set<Node> flaggedNodesObservedByPatroller) {
            this.flaggedNodesObservedByPatroller = new HashSet<>(flaggedNodesObservedByPatroller);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BPGDefenderAttackerObservation)) return false;

            BPGDefenderAttackerObservation that = (BPGDefenderAttackerObservation) o;

            return flaggedNodesObservedByPatroller != null ? flaggedNodesObservedByPatroller.equals(that.flaggedNodesObservedByPatroller) : that.flaggedNodesObservedByPatroller == null;
        }

        @Override
        public int hashCode() {
            return flaggedNodesObservedByPatroller != null ? flaggedNodesObservedByPatroller.hashCode() : 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    class BPGDefenderDefenderObservation implements Observation {

        protected Node p1Position;
        protected Node p2Position;

        public BPGDefenderDefenderObservation(Node p1Position, Node p2Position) {
            this.p1Position = p1Position;
            this.p2Position = p2Position;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BPGDefenderDefenderObservation)) return false;

            BPGDefenderDefenderObservation that = (BPGDefenderDefenderObservation) o;

            if (p1Position != null ? !p1Position.equals(that.p1Position) : that.p1Position != null) return false;
            if (p2Position != null ? !p2Position.equals(that.p2Position) : that.p2Position != null) return false;
            return true;

        }

        @Override
        public int hashCode() {
            int result = p1Position != null ? p1Position.hashCode() : 0;
            result = 31 * result + (p2Position != null ? p2Position.hashCode() : 0);
            return result;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    class BPGAttackerAttackerObservation implements Observation {

        protected Sequence attackerSequence;

        public BPGAttackerAttackerObservation(Sequence attackerSequence) {
            this.attackerSequence = new ArrayListSequenceImpl(attackerSequence);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            BPGAttackerAttackerObservation that = (BPGAttackerAttackerObservation) o;

            return attackerSequence.equals(that.attackerSequence);
        }

        @Override
        public int hashCode() {
            return attackerSequence.hashCode();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    class BPGAttackerDefenderObservation implements Observation {

        protected int hashcode;

        public BPGAttackerDefenderObservation(int hashcode) {
            this.hashcode = hashcode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            BPGAttackerDefenderObservation that = (BPGAttackerDefenderObservation) o;

            return hashcode == that.hashcode;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
