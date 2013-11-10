package cz.agents.gtlibrary.domain.poker.generic;

import java.util.Arrays;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class GPGameInfo implements GameInfo {

    public static final Player FIRST_PLAYER = new PlayerImpl(0);
    public static final Player SECOND_PLAYER = new PlayerImpl(1);
    public static final Player NATURE = new PlayerImpl(2);
    public static final Player[] ALL_PLAYERS = new Player[] {FIRST_PLAYER, SECOND_PLAYER, NATURE};

    /**
     * value of ante for one player
     */
    public static final int ANTE = 1;
    public static int MAX_RAISES_IN_ROW = 2;
    public static int MAX_DIFFERENT_BETS = 2;
    public static int MAX_DIFFERENT_RAISES = 1;
    public static int[] BETS_FIRST_ROUND;

    static {
        BETS_FIRST_ROUND = new int[MAX_DIFFERENT_BETS];
        for (int i = 0; i < MAX_DIFFERENT_BETS; i++)
            BETS_FIRST_ROUND[i] = (i + 1) * 2;
    }

    /**
     * represents value which will be added to previous aggressive action
     */
    public static int[] RAISES_FIRST_ROUND;

    static {
        RAISES_FIRST_ROUND = new int[MAX_DIFFERENT_RAISES];
        for (int i = 0; i < MAX_DIFFERENT_RAISES; i++)
            RAISES_FIRST_ROUND[i] = (i + 1) * 2;
    }

    public static int MAX_CARD_TYPES = 3;
    public static int[] CARD_TYPES;

    static {
        CARD_TYPES = new int[MAX_CARD_TYPES];
        for (int i = 0; i < MAX_CARD_TYPES; i++)
            CARD_TYPES[i] = i;
    }

    public static int MAX_CARD_OF_EACH_TYPE = 2;
    public static int[] DECK;

    static {
        DECK = new int[MAX_CARD_OF_EACH_TYPE * MAX_CARD_TYPES];
        for (int i = 0; i < MAX_CARD_TYPES; i++)
            for (int j = 0; j < MAX_CARD_OF_EACH_TYPE; j++) {
                DECK[i * MAX_CARD_OF_EACH_TYPE + j] = i;
            }
    }

    public static int[] BETS_SECOND_ROUND;
    public static int[] RAISES_SECOND_ROUND;

    static {
        BETS_SECOND_ROUND = new int[BETS_FIRST_ROUND.length];
        for (int i = 0; i < BETS_FIRST_ROUND.length; i++) {
            BETS_SECOND_ROUND[i] = 2 * BETS_FIRST_ROUND[i];
        }

        RAISES_SECOND_ROUND = new int[RAISES_FIRST_ROUND.length];
        for (int i = 0; i < RAISES_FIRST_ROUND.length; i++) {
            RAISES_SECOND_ROUND[i] = 2 * RAISES_FIRST_ROUND[i];
        }
    }

    @Override
    public double getMaxUtility() {
        double maxValue = ANTE;

        maxValue += BETS_FIRST_ROUND[BETS_FIRST_ROUND.length - 1];
        for (int i = 0; i < MAX_RAISES_IN_ROW; i++) {
            maxValue += RAISES_FIRST_ROUND[RAISES_FIRST_ROUND.length - 1];
        }

        maxValue += BETS_SECOND_ROUND[BETS_SECOND_ROUND.length - 1];
        for (int i = 0; i < MAX_RAISES_IN_ROW; i++) {
            maxValue += RAISES_SECOND_ROUND[RAISES_SECOND_ROUND.length - 1];
        }
        return maxValue;
    }

    @Override
    public Player getFirstPlayerToMove() {
        return NATURE;
    }

    @Override
    public Player getOpponent(Player player) {
        if (player.equals(FIRST_PLAYER))
            return SECOND_PLAYER;
        return FIRST_PLAYER;
    }

    @Override
    public String getInfo() {
        StringBuilder builder = new StringBuilder();

        builder.append("Generic poker\n");
        builder.append("Ante: " + ANTE + "\n");
        builder.append("Max raises in row: " + MAX_RAISES_IN_ROW + "\n");
        builder.append("Bets first round: " + Arrays.toString(BETS_FIRST_ROUND) + "\n");
        builder.append("Bets second round: " + Arrays.toString(BETS_SECOND_ROUND) + "\n");
        builder.append("Raises first round: " + Arrays.toString(RAISES_FIRST_ROUND) + "\n");
        builder.append("Raises second round: " + Arrays.toString(RAISES_SECOND_ROUND) + "\n");
        builder.append("Deck: " + Arrays.toString(DECK));
        return builder.toString();
    }

    @Override
    public int getMaxDepth() {
        return 9 + 2 * MAX_RAISES_IN_ROW;
    }

    @Override
    public Player[] getAllPlayers() {
        return ALL_PLAYERS;
    }

}