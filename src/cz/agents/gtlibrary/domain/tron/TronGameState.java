/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


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
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.iinodes.*;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FastTanh;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;

import java.util.Collections;

public class TronGameState extends SimultaneousGameState {

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

    protected ISKey key;
    private int hashCode = -1;

    // needed for efficient computation of floodfill eval func
    private static int[] open1 = null;
    private static int[] open2 = null;
    private static int si1, ei1, si2, ei2;
    private static int area1, area2;
    private static int[][] tmpBoard = null;
    private static int[][] toSingle;
    private static int[] toRow;
    private static int[] toCol;

    public TronGameState() {
        super(TronGameInfo.ALL_PLAYERS);
        sequenceForAllPlayers = new ArrayList<Action>();
        currentPlayerIndex = 0;

        playerRows = new int[2];
        playerCols = new int[2];
        playerActions = new int[2];
        board = new char[TronGameInfo.ROWS][TronGameInfo.COLS]; 

        if (tmpBoard == null) 
          buildStatic(); 

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

        if (tmpBoard == null) 
          buildStatic(); 

        initBoard(TronGameInfo.BOARDTYPE);
    }

    private void buildStatic() { 
      tmpBoard = new int[TronGameInfo.ROWS][TronGameInfo.COLS]; 
      open1 = new int[TronGameInfo.ROWS*TronGameInfo.COLS];
      open2 = new int[TronGameInfo.ROWS*TronGameInfo.COLS];
    
      toSingle = new int[TronGameInfo.ROWS][TronGameInfo.COLS];
      toRow = new int[TronGameInfo.ROWS*TronGameInfo.COLS];
      toCol = new int[TronGameInfo.ROWS*TronGameInfo.COLS];

      for (int r = 0; r < TronGameInfo.ROWS; r++) {
        for (int c = 0; c < TronGameInfo.COLS; c++) { 
          int sc = r*TronGameInfo.COLS + c;
          toSingle[r][c] = sc;
          toRow[sc] = r;
          toCol[sc] = c; 
        }
      }
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
    public double[] getEndGameUtilities() {
        if (isActualGameEnd()) {
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
    public boolean isActualGameEnd() {
        if (playerRows[0] == playerRows[1] && playerCols[0] == playerCols[1]) 
          return true;  // head-on collision
          
        boolean p0surrounded = playerSurrounded(0);
        boolean p1surrounded = playerSurrounded(1);

        return (p0surrounded || p1surrounded);
    }

    private void flood() {

      int width = TronGameInfo.ROWS*TronGameInfo.COLS;
      int wi = 0;      

      for (int r = 0; r < TronGameInfo.ROWS; r++) {
        for (int c = 0; c < TronGameInfo.COLS; c++) {
          tmpBoard[r][c] = 0;
          open1[wi] = open2[wi] = -1; 
          wi++;
        }
      }

      // player 1, round 0 and player 2 round 0
      tmpBoard[playerRows[0]][playerCols[0]] = 1000;
      tmpBoard[playerRows[1]][playerCols[1]] = 2000;

      area1 = area2 = 1; 

      open1[0] = toSingle[playerRows[0]][playerCols[0]];
      open2[0] = toSingle[playerRows[1]][playerCols[1]];

      si1 = 0; ei1 = 1;
      si2 = 0; ei2 = 1; 

      int round = 0;

      // both open lists are not empty
      while (si1 < ei1 && si2 < ei2) { 
        round++; 

        // spread 1 first
        if (si1 < ei1) {
          int roundEnd1 = ei1; 

          for (; si1 < roundEnd1; si1++) { 
            int coord = open1[si1];
            int r = toRow[coord], c = toCol[coord]; 

            // if blocked due to being reached simultaneously, no longer expand from here
            if (tmpBoard[r][c] == 3000)
              continue;
           
            for (int dir = 0; dir < 4; dir++) { 
              int rp = r + rowOffsets[dir], cp = c + colOffsets[dir]; 
              
              if (inBounds(rp,cp) && tmpBoard[rp][cp] == 0 && board[rp][cp] == '.') {
                tmpBoard[rp][cp] = 1000 + round; 
                open1[ei1] = toSingle[rp][cp]; 
                ei1++;
                area1++; 
              }
            }
          }
        }

        // spread 2 
        if (si2 < ei2) { 
          int roundEnd2 = ei2; 

          for (; si2 < roundEnd2; si2++) { 
            int coord = open2[si2];
            int r = toRow[coord], c = toCol[coord]; 
           
            // expand in all directions
            for (int dir = 0; dir < 4; dir++) { 
              int rp = r + rowOffsets[dir], cp = c + colOffsets[dir]; 
              
              if (inBounds(rp,cp) && (tmpBoard[rp][cp] == 0 || tmpBoard[rp][cp] == (1000+round)) && board[rp][cp] == '.') {
                if (tmpBoard[rp][cp] == 0) {                
                  tmpBoard[rp][cp] = 2000 + round; 
                  open2[ei2] = toSingle[rp][cp]; 
                  ei2++;
                  area2++; 
                }
                else {
                  tmpBoard[rp][cp] = 3000; // blocked from reaching simultaneously; p1 got here this round
                  area1--; 
                }
              }
            }
          }
        }
      }
    }

    @Override
    public double[] evaluate() {
      if (isActualGameEnd())
        return getEndGameUtilities();

      // temporary
      //return new double[]{0, 0, 0};

      flood();

      double delta = area1 - area2; 

      // now scale and pass through tanh;
      // tanh(1) = 0.762
      // tanh(2) = 0.964
      double p1eval = FastTanh.tanh(delta / 5.0);

      return new double[]{p1eval, -p1eval, 0};
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
    public ISKey getISKeyForPlayerToMove() {
        if (key == null) {
            if (isPlayerToMoveNature())
                key = new PerfectRecallISKey(0, history.getSequenceOf(getPlayerToMove()));
            else
                key = new PerfectRecallISKey(sequenceForAllPlayers.hashCode(), getSequenceForPlayerToMove());
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

    @Override
    public boolean isDepthLimit() {
        return Math.min(history.getSequenceOf(players[0]).size(), history.getSequenceOf(players[1]).size()) >= depth;
    }

    @Override
    public void setDepth(int depth) {
        this.depth = depth + Math.min(history.getSequenceOf(players[0]).size(), history.getSequenceOf(players[1]).size());
    }

}


