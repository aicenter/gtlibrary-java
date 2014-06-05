package cz.agents.gtlibrary.domain.mpochm;

import cz.agents.gtlibrary.algorithms.sequenceform.numbers.Rational;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class MPoCHMGameState extends GameStateImpl {


    public enum CoinState {
        HEAD, TAIL, NOT_SET;
    }

    public enum GiftState {
        GIVEN, NOT_GIVEN, NOT_SET;
    }

    private CoinState coinState;
    private GiftState giftState;
    private CoinState guess;
    private int currentPlayerIndex;


    public MPoCHMGameState() {
        super(MPoCHMGameInfo.ALL_PLAYERS);
        currentPlayerIndex = 0;
        coinState = CoinState.NOT_SET;
        giftState = GiftState.NOT_SET;
        guess = CoinState.NOT_SET;
    }

    public MPoCHMGameState(MPoCHMGameState gameState) {
        super(gameState);
        this.currentPlayerIndex = gameState.currentPlayerIndex;
        this.giftState = gameState.giftState;
        this.coinState = gameState.coinState;
        this.guess = gameState.guess;
    }

    public CoinState getCoinState() {
        return coinState;
    }

    public void processCoinState(CoinState coinState) {
        if (this.coinState.equals(CoinState.NOT_SET))
            this.coinState = coinState;
        else
            this.guess = coinState;
    }

    public void setGiftState(GiftState giftState) {
        this.giftState = giftState;
        currentPlayerIndex = 1;
    }

    @Override
    public Player getPlayerToMove() {
        return players[currentPlayerIndex];
    }

    @Override
    public GameState copy() {
        return new MPoCHMGameState(this);
    }

    @Override
    public double[] getUtilities() {
        double value = guess.equals(coinState) ? 1 : 0;

        if (giftState.equals(GiftState.GIVEN))
            value++;
        return new double[]{-value, value};
    }

    @Override
    public Rational[] getExactUtilities() {
        int value = guess.equals(coinState) ? 1 : 0;

        if (giftState.equals(GiftState.GIVEN))
            value++;
        return new Rational[]{new Rational(-value), new Rational(value)};
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 0;
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        return Rational.ZERO;
    }

    @Override
    public boolean isGameEnd() {
        return !guess.equals(CoinState.NOT_SET);
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public Pair<Integer, Sequence> getISKeyForPlayerToMove() {
        HashCodeBuilder hcb = new HashCodeBuilder();

        if (currentPlayerIndex == 1)
            hcb.append(giftState);
        return new Pair<Integer, Sequence>(hcb.hashCode(), getSequenceForPlayerToMove());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MPoCHMGameState)) return false;

        MPoCHMGameState that = (MPoCHMGameState) o;

        if (currentPlayerIndex != that.currentPlayerIndex) return false;
        if (coinState != that.coinState) return false;
        if (giftState != that.giftState) return false;
        if (guess != that.guess) return false;
        if (!history.equals(that.history)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return history.hashCode();
    }
}
