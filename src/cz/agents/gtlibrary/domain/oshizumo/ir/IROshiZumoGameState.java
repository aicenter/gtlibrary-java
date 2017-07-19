package cz.agents.gtlibrary.domain.oshizumo.ir;

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielAction;
import cz.agents.gtlibrary.domain.oshizumo.*;
import cz.agents.gtlibrary.domain.randomgameimproved.io.BasicGameBuilder;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.iinodes.*;
import cz.agents.gtlibrary.interfaces.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IROshiZumoGameState extends OshiZumoGameState {

    public static int REMEMBERED_MOVES = 1;

    public static void main(String[] args) {
        GameState prGameState = new IIOshiZumoGameState();
        Expander<SequenceInformationSet> prExpander = new OshiZumoExpander<>(new SequenceFormConfig<>());
        new FullSequenceEFG(prGameState, prExpander, new OZGameInfo(), (SequenceFormConfig<SequenceInformationSet>) prExpander.getAlgorithmConfig()).generateCompleteGame();
        System.out.println(prExpander.getAlgorithmConfig().getAllInformationSets().size() + " " + ((SequenceFormConfig) prExpander.getAlgorithmConfig()).getSequencesFor(OZGameInfo.FIRST_PLAYER).size() + " " + ((SequenceFormConfig) prExpander.getAlgorithmConfig()).getSequencesFor(OZGameInfo.SECOND_PLAYER).size());

        GameState irGameState = new IROshiZumoGameState();
        Expander<? extends InformationSet> irExpander = new OshiZumoExpander<>(new SequenceFormIRConfig(new OZGameInfo()));
        BasicGameBuilder.build(irGameState, irExpander.getAlgorithmConfig(), irExpander);
        System.out.println(irExpander.getAlgorithmConfig().getAllInformationSets().size() + " " + ((SequenceFormIRConfig) irExpander.getAlgorithmConfig()).getSequencesFor(OZGameInfo.FIRST_PLAYER).size() + " " + ((SequenceFormIRConfig) irExpander.getAlgorithmConfig()).getSequencesFor(OZGameInfo.SECOND_PLAYER).size());
    }

    public IROshiZumoGameState() {
        super();
    }

    public IROshiZumoGameState(IROshiZumoGameState gameState) {
        super(gameState);
    }

    @Override
    public GameState copy() {
        return new IROshiZumoGameState(this);
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (key == null) {
            if (getPlayerToMove().equals(OZGameInfo.FIRST_PLAYER)) {
                Observations observations = new Observations(getPlayerToMove(), getPlayerToMove());

                observations.add(new OshiZumoBoardObservation(wrestlerLoc, currentPlayerIndex == 0 ? p1Coins : p2Coins));
                Sequence sequence = getSequenceForPlayerToMove();
                Sequence opponentSequence = getSequenceFor(players[1 - getPlayerToMove().getId()]);
                List<Integer> wins = new ArrayList(sequence.size());

                for (int i = 0; i < sequence.size(); i++) {
                    wins.add((int) Math.signum(((OshiZumoAction) sequence.get(i)).compareTo((OshiZumoAction) opponentSequence.get(i))));
                }
                for (int i = 1; i <= REMEMBERED_MOVES; i++) {
                    if (getSequenceForPlayerToMove().size() >= i)
                        observations.add(new ObservationImpl(((OshiZumoAction) getSequenceForPlayerToMove().get(getSequenceForPlayerToMove().size() - i)).getValue()));
                }
                Observations opponentObservations = new Observations(getPlayerToMove(), players[1 - currentPlayerIndex]);

                opponentObservations.add(new OpponentObservation(wins, isGameEnd()));
                key = new ImperfectRecallISKey(observations, opponentObservations, null);
            } else {

//                Observations observations = new Observations(getPlayerToMove(), getPlayerToMove());
//
//                observations.add(new OshiZumoBoardObservation(wrestlerLoc, currentPlayerIndex == 0 ? p1Coins : p2Coins));
//                Sequence sequence = getSequenceForPlayerToMove();
//                Sequence opponentSequence = getSequenceFor(players[1 - getPlayerToMove().getId()]);
//                List<Integer> wins = new ArrayList(sequence.size());
//
//                for (int i = 0; i < sequence.size(); i++) {
//                    wins.add((int) Math.signum(((OshiZumoAction) sequence.get(i)).compareTo((OshiZumoAction) opponentSequence.get(i))));
//                }
//                Observations opponentObservations = new Observations(getPlayerToMove(), players[1 - currentPlayerIndex]);
//
//                opponentObservations.add(new OpponentObservation(wins, isGameEnd()));
//                key = new ImperfectRecallISKey(observations, opponentObservations, null);
//            }
//
                Observations observations = new Observations(players[currentPlayerIndex], players[1 - currentPlayerIndex]);

                observations.add(new PerfectRecallObservation((PerfectRecallISKey) super.getISKeyForPlayerToMove()));
                key = new ImperfectRecallISKey(observations, null, null);
            }
        }
        return key;
    }

    private class OshiZumoBoardObservation implements Observation {
        private int wrestlerLoc;
        private int playerCoins;
        private int opponentCoins;

        public OshiZumoBoardObservation(int wrestlerLoc, int playerCoins) {
            this.wrestlerLoc = wrestlerLoc;
            this.playerCoins = playerCoins;
        }

        public OshiZumoBoardObservation(int wrestlerLoc, int playerCoins, int opponentCoins) {
            this.wrestlerLoc = wrestlerLoc;
            this.playerCoins = playerCoins;
            this.opponentCoins = opponentCoins;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OshiZumoBoardObservation)) return false;

            OshiZumoBoardObservation that = (OshiZumoBoardObservation) o;

            if (wrestlerLoc != that.wrestlerLoc) return false;
            if (playerCoins != that.playerCoins) return false;
            return opponentCoins == that.opponentCoins;

        }

        @Override
        public int hashCode() {
            int result = wrestlerLoc;
            result = 31 * result + playerCoins;
            result = 31 * result + opponentCoins;
            return result;
        }
    }

    private class OpponentObservation implements Observation {
        protected List<Integer> wins;
        protected boolean isGameEnd;

        public OpponentObservation(List<Integer> wins, boolean isGameEnd) {
            this.wins = wins;
            this.isGameEnd = isGameEnd;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OpponentObservation)) return false;

            OpponentObservation that = (OpponentObservation) o;

            if (isGameEnd != that.isGameEnd) return false;
            return wins.equals(that.wins);

        }

        @Override
        public int hashCode() {
            int result = wins.hashCode();
            result = 31 * result + (isGameEnd ? 1 : 0);
            return result;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
