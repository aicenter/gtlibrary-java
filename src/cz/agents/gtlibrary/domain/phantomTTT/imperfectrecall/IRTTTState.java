package cz.agents.gtlibrary.domain.phantomTTT.imperfectrecall;

import cz.agents.gtlibrary.domain.phantomTTT.TTTExpander;
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

    public static void main(String[] args) {
        GameState root = new IRTTTState();
        SequenceFormIRConfig config = new SequenceFormIRConfig();
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
        Observations oppObservations = new Observations(getPlayerToMove(), getAllPlayers()[1 - getPlayerToMove().getId()]);

        oppObservations.add(new TTTObservation(observed));
        return new ImperfectRecallISKey(myObservations, oppObservations, null);
    }

    @Override
    public GameState copy() {
        return new IRTTTState(this);
    }

    class TTTObservation implements Observation {
        private Set<Integer> positions;

        public TTTObservation(Set<Integer> positions) {
            this.positions = positions;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TTTObservation)) return false;

            TTTObservation that = (TTTObservation) o;

            return positions != null ? positions.equals(that.positions) : that.positions == null;

        }

        @Override
        public int hashCode() {
            return positions != null ? positions.hashCode() : 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}

