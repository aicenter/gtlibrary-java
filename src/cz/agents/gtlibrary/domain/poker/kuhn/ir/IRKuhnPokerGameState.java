package cz.agents.gtlibrary.domain.poker.kuhn.ir;

import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.Observations;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Observation;
import cz.agents.gtlibrary.interfaces.Sequence;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Iterator;

public class IRKuhnPokerGameState extends KuhnPokerGameState {
    public IRKuhnPokerGameState() {
        super();
    }

    public IRKuhnPokerGameState(KuhnPokerGameState gameState) {
        super(gameState);
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (isPlayerToMoveNature()) {
            Observations natureObs = new Observations(KPGameInfo.NATURE, KPGameInfo.NATURE);

            natureObs.add(new PerfectPokerObservation(new ArrayListSequenceImpl(getSequenceForPlayerToMove())));
            return new ImperfectRecallISKey(null, null, natureObs);
        }

        HashCodeBuilder hcb = new HashCodeBuilder(17, 31);
        Iterator<PokerAction> iterator = sequenceForAllPlayers.iterator();
        int moveNum = 0;

        hcb.append(playerCards[getPlayerToMove().getId()].getActionType());
        while (iterator.hasNext()) {
//            hcb.append(iterator.next().getActionType());
            hcb.append(moveNum++);
        }
        hcb.append(isGameEnd());
        Observations ownObservation = new Observations(getPlayerToMove(), getPlayerToMove());

//        ownObservation.add(new PerfectPokerObservation(new ArrayListSequenceImpl(getSequenceForPlayerToMove())));
        Observations rest = new Observations(getPlayerToMove(), players[1 - getPlayerToMove().getId()]);

        rest.add(new IntPokerObservation(hcb.toHashCode()));
        return new ImperfectRecallISKey(ownObservation, rest, null);
    }

//    @Override
//    public ISKey getISKeyForPlayerToMove() {
//        Observations observations = new Observations(getPlayerToMove(), getPlayerToMove());
//
//        observations.add(new PerfectRecallObservation((PerfectRecallISKey) super.getISKeyForPlayerToMove()));
//        return new ImperfectRecallISKey(observations, null, null);
//    }

    @Override
    public GameState copy() {
        return new IRKuhnPokerGameState(this);
    }

    public class PerfectPokerObservation implements Observation {
        private Sequence sequence;

        public PerfectPokerObservation(Sequence sequence) {
            this.sequence = sequence;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PerfectPokerObservation)) return false;

            PerfectPokerObservation that = (PerfectPokerObservation) o;

            return sequence.equals(that.sequence);

        }

        @Override
        public int hashCode() {
            return sequence.hashCode();
        }

        @Override
        public String toString() {
            return sequence.toString();
        }
    }

    public class IntPokerObservation implements Observation {
        private int hashCode;

        public IntPokerObservation(int hashCode) {
            this.hashCode = hashCode;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public String toString() {
            return "" + hashCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof IntPokerObservation)) return false;

            IntPokerObservation that = (IntPokerObservation) o;

            return hashCode == that.hashCode;

        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
