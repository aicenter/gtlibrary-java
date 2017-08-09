package cz.agents.gtlibrary.domain.goofspiel.ir;

import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielAction;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.iinodes.*;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Observation;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.HashSet;
import java.util.Set;

public class IRGoofSpielGameState extends IIGoofSpielGameState {

    public static int REMEMBERED_MOVES = 2;

    public IRGoofSpielGameState(GoofSpielGameState gameState) {
        super(gameState);
    }

    public IRGoofSpielGameState(Sequence natureSequence, int sequenceIndex) {
        super(natureSequence, sequenceIndex);
    }

    public IRGoofSpielGameState() {
        super();
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (key == null) {
            Player player = getPlayerToMove();

            if (player.equals(GSGameInfo.FIRST_PLAYER)) {
                Observations observations = new Observations(player, player);

                observations.add(new GSObservation(playerScore[player.getId()], new HashSet<>(getCardsFor(player))));
                for (int i = 1; i <= REMEMBERED_MOVES; i++) {
                    if (getSequenceForPlayerToMove().size() >= i)
                        observations.add(new ObservationImpl(((GoofSpielAction) getSequenceForPlayerToMove().get(getSequenceForPlayerToMove().size() - i)).getValue()));
                }
                key = new ImperfectRecallISKey(observations, null, null);
            } else {
                Observations observations = new Observations(player, player);

                observations.add(new PerfectRecallObservation((PerfectRecallISKey) super.getISKeyForPlayerToMove()));
                key = new ImperfectRecallISKey(observations, null, null);
            }
        }
        return key;
    }

    @Override
    public GameState copy() {
        return new IRGoofSpielGameState(this);
    }

    class GSObservation implements Observation {
        int score;
        Set<Integer> cards;

        public GSObservation(int score, Set<Integer> cards) {
            this.score = score;
            this.cards = cards;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GSObservation)) return false;

            GSObservation that = (GSObservation) o;

            if (score != that.score) return false;
            return cards.equals(that.cards);

        }

        @Override
        public int hashCode() {
            int result = score;
            result = 31 * result + cards.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return score + " " + cards;
        }
    }

}