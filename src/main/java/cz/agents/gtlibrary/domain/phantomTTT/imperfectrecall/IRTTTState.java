package cz.agents.gtlibrary.domain.phantomTTT.imperfectrecall;

import cz.agents.gtlibrary.domain.phantomTTT.TTTAction;
import cz.agents.gtlibrary.domain.phantomTTT.TTTExpander;
import cz.agents.gtlibrary.domain.phantomTTT.TTTInfo;
import cz.agents.gtlibrary.domain.phantomTTT.TTTState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.Observations;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Observation;
import cz.agents.gtlibrary.utils.BasicGameBuilder;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class IRTTTState extends TTTState {

    public static int REMEMBERED_MOVES = 0;
    public ISKey key;

    public static void main(String[] args) {
        GameState root = new IRTTTState();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new TTTInfo());
        Expander<SequenceFormIRInformationSet> expander = new TTTExpander<>(config);

        BasicGameBuilder.build(root, config, expander);

        System.out.println(config.getAllInformationSets().size());
    }

    public IRTTTState() {
    }

    public IRTTTState(BitSet s, char toMove, byte moveNum) {
        super(s, toMove, moveNum);
    }

    public IRTTTState(TTTState state) {
        super(state);
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (key == null) {
            Set<Integer> myPositions = new HashSet<>();
            Set<Integer> observed = new HashSet<>();
            char currentPlayerSymbol = toMove;
            char opponentPlayerSymbol = getOpponent();

            for (int i = 0; i < s.size() / 4; i++) {
                char currentPositionSymbol = getSymbol(i);

                if (currentPositionSymbol == currentPlayerSymbol)
                    myPositions.add(i);
                else if (currentPositionSymbol == opponentPlayerSymbol && getTried(currentPlayerSymbol, i))
                    observed.add(i);
            }

            Observations myObservations = new Observations(getPlayerToMove(), getPlayerToMove());

            myObservations.add(new TTTObservation(myPositions));
            for (int i = 1; i <= REMEMBERED_MOVES; i++) {
                if (getSequenceForPlayerToMove().size() >= i)
                    myObservations.add(new PositionObservation(((TTTAction) getSequenceForPlayerToMove().get(getSequenceForPlayerToMove().size() - i)).fieldID));
            }
            Observations oppObservations = new Observations(getPlayerToMove(), getAllPlayers()[1 - getPlayerToMove().getId()]);

            oppObservations.add(new TTTObservation(observed));
            key = new ImperfectRecallISKey(myObservations, oppObservations, null);
        }
        return key;
    }

    @Override
    public GameState copy() {
        return new IRTTTState(this);
    }

    class TTTObservation implements Observation {
        private Set<Integer> positions;
        private int hashCode = -1;

        public TTTObservation(Set<Integer> positions) {
            this.positions = positions;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TTTObservation)) return false;

            if (o.hashCode() != hashCode())
                return false;
            TTTObservation that = (TTTObservation) o;

            return positions != null ? positions.equals(that.positions) : that.positions == null;

        }

        @Override
        public int hashCode() {
            if (hashCode == -1)
                hashCode = positions != null ? positions.hashCode() : 0;
            return hashCode;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    private class PositionObservation implements Observation {
        private byte fieldID;

        public PositionObservation(byte fieldID) {
            this.fieldID = fieldID;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PositionObservation)) return false;

            PositionObservation that = (PositionObservation) o;

            return fieldID == that.fieldID;

        }

        @Override
        public int hashCode() {
            return (int) fieldID;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}

