package cz.agents.gtlibrary.domain.clairvoyance;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.io.GambitEFG;

public class ClairvoyanceGameState extends GameStateImpl {

    public static void main(String[] args) {
        GambitEFG writer = new GambitEFG();
        GameState root = new ClairvoyanceGameState();
        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> expander = new ClairvoyanceExpander<>(config);

        writer.write("ClairvoyanceGame.efg", root, expander);
    }

    private Boolean winningCard;
    private int currentPlayerIndex;
    private double p1Money;
    private double p2Money;
    private boolean isGameEnd;
    private boolean fold;

    public ClairvoyanceGameState() {
        super(ClairvoyanceInfo.ALL_PLAYERS);
        currentPlayerIndex = 2;
        p1Money = 0.5;
        p2Money = 0.5;
        isGameEnd = false;
        fold = false;
    }

    public ClairvoyanceGameState(ClairvoyanceGameState gameState) {
        super(gameState);
        this.winningCard = gameState.winningCard;
        this.currentPlayerIndex = gameState.currentPlayerIndex;
        this.p1Money = gameState.p1Money;
        this.p2Money = gameState.p2Money;
        this.isGameEnd = gameState.isGameEnd;
        this.fold = gameState.fold;
    }

    @Override
    public Player getPlayerToMove() {
        return players[currentPlayerIndex];
    }

    @Override
    public GameState copy() {
        return new ClairvoyanceGameState(this);
    }

    @Override
    public double[] getUtilities() {
        if (winningCard || fold)
            return new double[]{p2Money, -p2Money};
        return new double[]{-p1Money, p1Money};
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 0.5;
    }

    @Override
    public boolean isGameEnd() {
        return isGameEnd;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return currentPlayerIndex == 2;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (currentPlayerIndex == 0)
            return new PerfectRecallISKey(winningCard.hashCode(), getSequenceForPlayerToMove());
        if (currentPlayerIndex == 1)
            return new PerfectRecallISKey((int) (2 * p1Money), getSequenceForPlayerToMove());
        return null;
    }

    public void setWinningCard(boolean winningCard) {
        this.winningCard = winningCard;
        currentPlayerIndex = 0;
    }

    public void addP1Money(double bet) {
        p1Money += bet;
        currentPlayerIndex = 1;
    }

    public void addP2Money(double bet) {
        p2Money += bet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClairvoyanceGameState)) return false;

        ClairvoyanceGameState that = (ClairvoyanceGameState) o;

        if (currentPlayerIndex != that.currentPlayerIndex) return false;
        if (p1Money != that.p1Money) return false;
        if (p2Money != that.p2Money) return false;
        if (winningCard != null ? !winningCard.equals(that.winningCard) : that.winningCard != null) return false;
        if (!history.equals(that.history))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = winningCard != null ? winningCard.hashCode() : 0;

        result = 31 * result + currentPlayerIndex;
        result = 31 * result + (int) (2 * p1Money);
        result = 31 * result + (int) (2 * p2Money);
        result = 31 * result + history.hashCode();
        return result;
    }

    public void call() {
        addP2Money(p1Money - 0.5);
        isGameEnd = true;
    }

    public void fold() {
        isGameEnd = true;
        fold = true;
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        return Rational.ONE;
    }

    public Boolean isWinningCard() {
        return winningCard;
    }

    public double getP2Money() {
        return p2Money;
    }

    public double getP1Money() {
        return p1Money;
    }
}
