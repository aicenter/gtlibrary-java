package cz.agents.gtlibrary.domain.banditGame;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by kail on 11/11/17.
 */
public class BanditGameInfo implements GameInfo {

    public static int seed = 1;

    public static Player AGENT = new PlayerImpl(0);
    public static Player BANDIT = new PlayerImpl(1);
    public static Player[] ALL_PLAYERS = { AGENT, BANDIT };

    public static int BANDIT_NUM = -1;
    public static char[][] map;
    public static int ROWS_COUNT = -1;
    public static int COLS_COUNT = -1;
    public static double ATTACK_PROB = -1;

    public static Pair<Integer,Integer> START;
    public static Pair<Integer,Integer> END;

    public static ArrayList<Pair<Integer,Integer>> GOLD = new ArrayList<>();
    public static ArrayList<Pair<Integer,Integer>> DGRS = new ArrayList<>();

    final public static char START_CH = 'S';
    final public static char DGRS_CH = 'E';
    final public static char END_CH = 'D';
    final public static char GOLD_CH = 'G';

    public BanditGameInfo(String file) {
        readGraphFromFile(file);
    }

    private void readGraphFromFile(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String thisLine = null;
            String nString = null;

            if ((nString = br.readLine()) != null) {
                ROWS_COUNT = Integer.parseInt(nString);
            }

            if ((nString = br.readLine()) != null) {
                COLS_COUNT = Integer.parseInt(nString);
            }

            map = new char[ROWS_COUNT][COLS_COUNT];

            for (int i = 0; i < ROWS_COUNT; i++) {
                thisLine = br.readLine();
                assert thisLine != null;

                map[i] = thisLine.toCharArray();

                for (int j=0; j<map[i].length; j++) {
                    char c = map[i][j];
                    switch (c) {
                        case START_CH:
                            START = new Pair<>(i,j);
                            break;
                        case DGRS_CH:
                            DGRS.add(new Pair<>(i,j));
                            break;
                        case END_CH:
                            END = new Pair<>(i,j);
                            break;
                        case GOLD_CH:
                            GOLD.add(new Pair<>(i,j));
                            break;
                    }
                }
            }

            if ((nString = br.readLine()) != null) {
                BANDIT_NUM = Integer.parseInt(nString);
            }

            if ((nString = br.readLine()) != null) {
                ATTACK_PROB = new Double(nString);
            }

            br.close();
        } catch (IOException e) {
            System.err.println("Error: " + e);
        }
    }


        @Override
    public double getMaxUtility() {
        return GOLD.size()*10+1;
    }

    @Override
    public Player getFirstPlayerToMove() {
        return BANDIT;
    }

    @Override
    public Player getOpponent(Player player) {
        return player.equals(AGENT) ? BANDIT : AGENT;
    }

    @Override
    public String getInfo() {
        return "BanditGame : S[" + START + "]  D[" + END + "]  G[" + GOLD + "]";
    }

    @Override
    public int getMaxDepth() {
        return ROWS_COUNT*COLS_COUNT;
    }

    @Override
    public Player[] getAllPlayers() {
        return ALL_PLAYERS;
    }

    @Override
    public double getUtilityStabilizer() {
        return 0;
    }
}
