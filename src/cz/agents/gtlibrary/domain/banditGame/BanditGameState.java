package cz.agents.gtlibrary.domain.banditGame;

import cz.agents.gtlibrary.domain.observationGame.ObsGameInfo;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.Pair;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kail on 11/11/17.
 */
public class BanditGameState extends GameStateImpl{


    protected List<Pair<Integer,Integer>> history;
    protected int timeStep;
    protected Player playerToMove;

    protected int GOLD_PICKED = 0;
    protected boolean END_REACHED = false;
    protected List<Pair<Integer,Integer>> attacks;
    protected List<Pair<Integer,Integer>> emptyDGR;
    protected List<Pair<Integer,Integer>> bandits;

    protected int agentRow = -1;
    protected int agentCol = -1;

    protected int hashCode = -1;
    protected boolean gameEnd = false;

    protected Pair<Integer,Integer> observed = new Pair<>(-1,-1);
    protected int[][] banditMove = null;

    public BanditGameState() {
        super(ObsGameInfo.ALL_PLAYERS);
        history = new ArrayList<>();
        attacks = new ArrayList<>();
        bandits = new ArrayList<>();
        emptyDGR = new ArrayList<>();
        timeStep = 0;
        playerToMove = BanditGameInfo.BANDIT;
    }

    public BanditGameState(GameStateImpl gameState) {
        super(gameState);
        BanditGameState obs = (BanditGameState)gameState;
        history = new ArrayList<>(obs.history);
        attacks = new ArrayList<>(obs.attacks);
        bandits = new ArrayList<>(obs.bandits);
        emptyDGR = new ArrayList<>(obs.emptyDGR);
        timeStep = obs.timeStep;
        playerToMove = obs.playerToMove;
        GOLD_PICKED = obs.GOLD_PICKED;
        END_REACHED = obs.END_REACHED;
        agentRow = obs.agentRow;
        agentCol = obs.agentCol;
        observed = new Pair<>(obs.observed.getLeft(),obs.observed.getRight());
        if (obs.banditMove != null) {
            banditMove = new int[2][2];
            banditMove[0][0] = obs.banditMove[0][0];
            banditMove[0][1] = obs.banditMove[0][1];
            banditMove[1][0] = obs.banditMove[1][0];
            banditMove[1][1] = obs.banditMove[1][1];
        }
    }

    protected void executeBanditAction(BanditGameBanditAction action) {
        assert (playerToMove.equals(BanditGameInfo.BANDIT));
        if (action.getType().equals(BanditGameBanditAction.BanditActionType.INIT)) {
            assert bandits.size() < BanditGameInfo.BANDIT_NUM;
            bandits.add(new Pair<>(action.getToRow(),action.getToCol()));
            if (bandits.size() == BanditGameInfo.BANDIT_NUM) {
                switchPlayers();
            }
        } else if (action.getType().equals(BanditGameBanditAction.BanditActionType.RELOCATE)) {
            assert (observed.getLeft() != -1 && observed.getRight() != -1);
            Pair<Integer, Integer> from = new Pair<>(action.getFromRow(),action.getFromCol());
            Pair<Integer, Integer> to = new Pair<>(action.getToRow(),action.getToCol());
            int i = bandits.indexOf(from);
            bandits.remove(from);
            bandits.add(i,to);
//            emptyDGR.remove(to);
//            emptyDGR.add(from);
            banditMove = new int[2][2];
            banditMove[0][0] = from.getLeft();
            banditMove[0][1] = from.getRight();
            banditMove[1][0] = to.getLeft();
            banditMove[1][1] = to.getRight();
            switchPlayers();
        }  else if (action.getType().equals(BanditGameBanditAction.BanditActionType.NOA)) {
            switchPlayers();
        } else assert false;

        clearCache();
    }

    protected void executeAgentAction(BanditGameAgentAction action) {
        assert (playerToMove.equals(BanditGameInfo.AGENT));
        assert (agentRow == action.getFromRow());
        assert (agentCol == action.getFromCol());
        agentRow = action.getToRow();
        agentCol = action.getToCol();
        Pair<Integer,Integer> p = new Pair<>(agentRow,agentCol);
        history.add(p);
        if (bandits.contains(p)) {
            observed = new Pair<>(-2, -2);
            attacks.add(p);
        } else {
            for (int i=0; i<BanditGameInfo.DGRS.size(); i++) {
                if (BanditGameInfo.DGRS.get(i).equals(p)) {
                    emptyDGR.add(p);
                    if (observed.getLeft() == -1 && observed.getRight() == -1) {
                        switchPlayers();
                        observed = new Pair<>(p.getLeft(), p.getRight());
                    }
                }
            }

        }
        if (BanditGameInfo.GOLD.contains(p)) {
            GOLD_PICKED++;
        }
        if (BanditGameInfo.END.equals(p)) {
            END_REACHED = true;
            gameEnd = true;
        }

        if (!gameEnd) {
            boolean cont = false;
            for (int i = -1; i <= 1; i++)
                for (int j = -1; j <= 1; j++) {
                    if (i*j != 0) continue;
                    Pair<Integer, Integer> a = new Pair<>(agentRow + i, agentCol + j);
                    if (!history.contains(a) && BanditGameInfo.map[a.getLeft()][a.getRight()] != '#')
                        cont = true;
                }
            gameEnd = !cont;
        }
        timeStep++;
        clearCache();
    }


    @Override
    public Player getPlayerToMove() {
        return playerToMove;
    }

    @Override
    public GameState copy() {
        return new BanditGameState(this);
    }

    @Override
    public double[] getUtilities() {
        if (isGameEnd()) {
            double result = 0;
            if (END_REACHED) {
                result += 10;
                result += 1*GOLD_PICKED;
            }
            result = result*Math.pow((1-BanditGameInfo.ATTACK_PROB),attacks.size());
            return new double[] {result, -result};
        }
        else return new double[] {0,0};
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 0;
    }

    @Override
    public boolean isGameEnd() {
        return gameEnd;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (getPlayerToMove().equals(BanditGameInfo.AGENT)) {
            return new PerfectRecallISKey(new HashCodeBuilder(17, 31).append(timeStep).append(history).append(emptyDGR).append(attacks).append(observed).append(GOLD_PICKED).append(END_REACHED).toHashCode(), getSequenceForPlayerToMove());
        } else if (getPlayerToMove().equals(BanditGameInfo.BANDIT)) {
            return new PerfectRecallISKey(new HashCodeBuilder(17, 31).append(timeStep).append(attacks).append(bandits).append(observed).append(banditMove).toHashCode(), getSequenceForPlayerToMove());
        } else throw new IllegalStateException("something is wrong");
    }

    @Override
    public int hashCode() {
        if (hashCode == -1) {
            hashCode = new HashCodeBuilder(17, 31).append(history).append(timeStep).append(bandits).append(attacks).append(emptyDGR).append(GOLD_PICKED).append(END_REACHED).append(observed).append(banditMove).toHashCode();
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BanditGameState other = (BanditGameState) obj;
        if (this.hashCode() != obj.hashCode())
            return false;
        if (this.timeStep != other.timeStep || this.GOLD_PICKED != other.GOLD_PICKED || this.END_REACHED != other.END_REACHED || !this.observed.equals(other.observed))
            return false;
        if (!history.equals(other.history))
            return false;
        if (!bandits.equals(other.bandits))
            return false;
        if (!attacks.equals(other.attacks))
            return false;
        if (!emptyDGR.equals(other.emptyDGR))
            return false;
        if ((banditMove == null && other.banditMove != null) || (banditMove != null && other.banditMove == null))
            return false;
        if (banditMove != null) {
            if (banditMove[0][0] != other.banditMove[0][0] || banditMove[0][1] != other.banditMove[0][1] || banditMove[1][0] != other.banditMove[1][0] || banditMove[1][1] != other.banditMove[1][1])
                return false;
        }
        return true;
    }

    public void clearCache() {
        hashCode = -1;
    }

    protected void switchPlayers() {
        int currentPlayerIndex = 1 - playerToMove.getId();
        playerToMove = BanditGameInfo.ALL_PLAYERS[currentPlayerIndex];
    }

    @Override
    public String toString() {
        return "[" +
//                "history=" + history +
//                ", timeStep=" + timeStep +
//                ", attacks=" + attacks +
//                ", emptyDGR=" + emptyDGR +
//                ", bandits=" + bandits +
                ']';
    }
}
