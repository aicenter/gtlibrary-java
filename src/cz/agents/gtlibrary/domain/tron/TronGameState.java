package cz.agents.gtlibrary.domain.tron;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;

import java.util.Collections;

public class TronGameState extends GameStateImpl {

    // lanctot: Note: maybe need to change this
    private static final long serialVersionUID = -1885423234236725674L;

    protected List<Action> sequenceForAllPlayers;
    private TronAction faceUpCard;

    private static int[] rowOffsets = { -1,  0, +1,  0 };
    private static int[] colOffsets = {  0, +1,  0, -1 };

    protected char[][] board;
    protected int[] playerRows;
    protected int[] playerCols;
    protected int[] playerActions;
    private int currentPlayerIndex;

    protected Pair<Integer, Sequence> key;
    private int hashCode = -1;

    public TronGameState() {
        super(TronGameInfo.ALL_PLAYERS);
        sequenceForAllPlayers = new ArrayList<Action>();
        currentPlayerIndex = 0;

        playerRows = new int[2];
        playerCols = new int[2];
        playerActions = new int[2];
        board = new char[TronGameInfo.ROWS][TronGameInfo.COLS]; 

        initBoard(TronGameInfo.BOARDTYPE);
    }

    public TronGameState(Sequence natureSequence) {
        super(TronGameInfo.ALL_PLAYERS);
        sequenceForAllPlayers = new ArrayList<Action>();
        currentPlayerIndex = 0;
        
        playerRows = new int[2];
        playerCols = new int[2];
        playerActions = new int[2];
        board = new char[TronGameInfo.ROWS][TronGameInfo.COLS]; 

        initBoard(TronGameInfo.BOARDTYPE);
    }

    private void initBoard(char boardType) {
        if (boardType == 'A') { 
          // place in the corner
          playerRows[0] = 0; 
          playerCols[0] = 0;
          playerRows[1] = TronGameInfo.ROWS-1;
          playerCols[1] = TronGameInfo.COLS-1;
          playerActions[0] = 0;
          playerActions[1] = 0;

          for (int r = 0; r < TronGameInfo.ROWS; r++) { 
            for (int c = 0; c < TronGameInfo.COLS; c++) { 
              board[r][c] = '.';
              
              if (r == playerRows[0] && c == playerCols[0]) 
                board[r][c] = '0';
              else if (r == playerRows[1] && c == playerCols[1]) 
                board[r][c] = '1';
            }
          }
        }
        else if (boardType == 'B') {
          if (TronGameInfo.ROWS != 3 || TronGameInfo.COLS != 5) 
            throw new RuntimeException("This board must have 3 rows and 5 columns!"); 
          
          // this is a situation where the only optimal strategy is in mixed strategies
          //
          // .....
          // .#0#1
          // .....
          
          for (int r = 0; r < TronGameInfo.ROWS; r++) { 
            for (int c = 0; c < TronGameInfo.COLS; c++) { 
              board[r][c] = '.';
            }
          }

          board[1][1] = '#';
          board[1][2] = '0';
          board[1][3] = '#';
          board[1][4] = '1';

          playerRows[0] = 1; 
          playerCols[0] = 2;
          playerRows[1] = 1;
          playerCols[1] = 4;
          playerActions[0] = 0;
          playerActions[1] = 0;
        }
        else { 
          throw new RuntimeException("Unsupported board type: " + boardType);
        }
    }

    private boolean inBounds(int r, int c) {
      return (r >= 0 && c >= 0 && r < TronGameInfo.ROWS && c < TronGameInfo.COLS); 
    }

    private Sequence createRandomSequence() {
        return null;
    }

    public TronGameState(TronGameState gameState) {
        super(gameState);

        this.board = new char[TronGameInfo.ROWS][TronGameInfo.COLS];
        for (int r = 0; r < TronGameInfo.ROWS; r++) { 
          for (int c = 0; c < TronGameInfo.COLS; c++) { 
            board[r][c] = gameState.board[r][c];
          }
        }

        this.currentPlayerIndex = gameState.currentPlayerIndex;
        this.playerRows = Arrays.copyOf(gameState.playerRows, gameState.playerRows.length);
        this.playerCols = Arrays.copyOf(gameState.playerCols, gameState.playerCols.length);
        this.playerActions = Arrays.copyOf(gameState.playerActions, gameState.playerActions.length);

        this.sequenceForAllPlayers = new ArrayList<Action>(gameState.sequenceForAllPlayers);
    }

    public Sequence getNatureSequence() {
        return null;
    }

    @Override
    public Player getPlayerToMove() {
        return players[currentPlayerIndex];
    }

    private void addActionToSequenceForAllPlayers(TronAction action) {
        sequenceForAllPlayers.add(action);
    }

    public void performFirstPlayerAction(TronAction action) {
        cleanCache();

        playerActions[0] = action.getValue();
        currentPlayerIndex = 1 - currentPlayerIndex;
    }

    public void performSecondPlayerAction(TronAction action) {
        cleanCache();

        playerActions[1] = action.getValue();

        // add p1's action to the sequence
        addActionToSequenceForAllPlayers((TronAction) history.getSequenceOf(TronGameInfo.FIRST_PLAYER).getLast());
        addActionToSequenceForAllPlayers(action);

        int[] nextPlayerRows = new int[2];
        int[] nextPlayerCols = new int[2];

        // get next locations
        for (int player = 0; player < 2; player++) { 
          nextPlayerRows[player] = playerRows[player] + rowOffsets[playerActions[player]-1];
          nextPlayerCols[player] = playerCols[player] + colOffsets[playerActions[player]-1];
        }

        // check for head-on collision
        if (nextPlayerRows[0] == nextPlayerRows[1] && nextPlayerCols[0] == nextPlayerCols[1]) {
          board[nextPlayerRows[0]][nextPlayerCols[0]] = 'X'; 
        }
        else {
          // move to new locations on the board
          board[nextPlayerRows[0]][nextPlayerCols[0]] = '0';
          board[nextPlayerRows[1]][nextPlayerCols[1]] = '1';
        }

        // put a wall in old locations
        board[playerRows[0]][playerCols[0]] = '#';
        board[playerRows[1]][playerCols[1]] = '#';

        // update locations
        playerRows[0] = nextPlayerRows[0];
        playerCols[0] = nextPlayerCols[0];
        playerRows[1] = nextPlayerRows[1];
        playerCols[1] = nextPlayerCols[1];

        // reset player actions
        playerActions[0] = playerActions[1] = 0;

        currentPlayerIndex = 1 - currentPlayerIndex;
    }

    public void performNatureAction(TronAction action) {
        cleanCache();
    }

    private void cleanCache() {
        key = null;
        hashCode = -1;
    }

    public ArrayList<Integer> getActionsForPlayerToMove() {
        ArrayList<Integer> list = new ArrayList<Integer>();

        for (int dir = 0; dir < 4; dir++) {
          int rp = playerRows[currentPlayerIndex] + rowOffsets[dir];
          int cp = playerCols[currentPlayerIndex] + colOffsets[dir];

          if (inBounds(rp,cp) && board[rp][cp] == '.')
            list.add(dir+1);  
        }

        return list;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return currentPlayerIndex == 2;
    }

    public boolean playerSurrounded(int player) { 
      for (int dir = 0; dir < 4; dir++) {
        int rp = playerRows[player] + rowOffsets[dir];
        int cp = playerCols[player] + colOffsets[dir];

        if (inBounds(rp,cp) && board[rp][cp] == '.')
          return false;
      }

      return true;
    }

    @Override
    public double[] getUtilities() {
        if (isGameEnd()) {
          if (playerRows[0] == playerRows[1] && playerCols[0] == playerCols[1]) 
            return new double[]{0, 0, 0};
          
          boolean p0surrounded = playerSurrounded(0);
          boolean p1surrounded = playerSurrounded(1);

          if (p0surrounded && p1surrounded) 
            return new double[]{0, 0, 0};
          else if (p0surrounded && !p1surrounded)
            return new double[]{-1, 1, 0};
          else if (!p0surrounded && p1surrounded)
            return new double[]{1, -1, 0};
          else 
            throw new RuntimeException("should not get here");
        }
        return new double[]{0, 0, 0};
    }

    @Override
    public boolean isGameEnd() {
        if (playerRows[0] == playerRows[1] && playerCols[0] == playerCols[1]) 
          return true;  // head-on collision
          
        boolean p0surrounded = playerSurrounded(0);
        boolean p1surrounded = playerSurrounded(1);

        return (p0surrounded || p1surrounded);
    }

    @Override
    public GameState copy() {
        return new TronGameState(this);
    }

    public List<Action> getSequenceForAllPlayers() {
        return sequenceForAllPlayers;
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 1;
    }

    @Override
    public int hashCode() {
        if (hashCode == -1)
            hashCode = history.hashCode();
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
        TronGameState other = (TronGameState) obj;

        if (!(currentPlayerIndex == other.currentPlayerIndex
                && playerRows[0] == other.playerRows[0]
                && playerCols[0] == other.playerCols[0]
                && playerRows[1] == other.playerRows[1]
                && playerCols[1] == other.playerCols[1]
                && playerActions[0] == other.playerActions[0]
                && playerActions[1] == other.playerActions[1]))
            return false;

        for (int r = 0; r < TronGameInfo.ROWS; r++) { 
          for (int c = 0; c < TronGameInfo.COLS; c++) { 
            if (board[r][c] != other.board[r][c])
              return false;
          }
        }

        if (!history.equals(other.history))
            return false;

        return true;
    }

    @Override
    public Pair<Integer, Sequence> getISKeyForPlayerToMove() {
        if (key == null) {
            if (isPlayerToMoveNature())
                key = new Pair<Integer, Sequence>(0, history.getSequenceOf(getPlayerToMove()));
            else
                key = new Pair<Integer, Sequence>(sequenceForAllPlayers.hashCode(), getSequenceForPlayerToMove());
        }
        return key;
    }

    @Override
    public String toString() {
        //return history.toString();
        String str = "";
        for (int r = 0; r < TronGameInfo.ROWS; r++) { 
          for (int c = 0; c < TronGameInfo.COLS; c++) { 
            str += board[r][c];
          }
          str += "\n";
        }

        return str;
    }
}


