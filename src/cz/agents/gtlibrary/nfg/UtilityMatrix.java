package cz.agents.gtlibrary.nfg;

/**
 * Basic class wrapping utility given by a matrix.  TODO: redesign.
 * Author: Ondrej Vanek
 * Date: 11/22/11
 * Time: 1:14 PM
 */
public class UtilityMatrix extends Utility<UtilityMatrix.IndexOfPlayer, UtilityMatrix.IndexOfPlayer> {

    private double[][][] matrix;

    public UtilityMatrix(double[][][] matrix) {
        this.matrix = matrix;
    }

    public UtilityMatrix(int rows, int columns) {
        this.matrix = new double[rows][columns][2];
    }

    public void set(int row, int col, double value1, double value2) {
        matrix[row][col][0] = value1;
        matrix[row][col][1] = value2;
    }

    public double[][][] set(double[][][] matrix) {
        double[][][] old = this.matrix;
        this.matrix = matrix;
        return old;
    }

    /**
     * @param row
     * @param col
     * @param player is 0 or 1;
     * @return
     */
    public double get(int row, int col, int player) {
        if (player < 0 || player > 1) throw new IllegalArgumentException("Player can be either 0 or 1!");
        return matrix[row][col][player];
    }


    @Override
    public double getUtility(IndexOfPlayer rowPlayer, IndexOfPlayer colPlayer) {
        return matrix[rowPlayer.index][colPlayer.index][rowPlayer.player];
    }

    public PlayerStrategySet<IndexOfPlayer> getStrategies(int player) {
        if (player < 0 || player > 1) throw new IllegalArgumentException("Player can be either 0 or 1!");
        PlayerStrategySet<IndexOfPlayer> set = new PlayerStrategySet<IndexOfPlayer>();

        int strategySize = matrix.length;
        if (player == 1) strategySize = matrix[0].length;
        for (int i = 0; i < strategySize; i++) {
            IndexOfPlayer iop = new IndexOfPlayer(i, player);
            set.add(iop);
        }
        return set;
    }

    public static final class IndexOfPlayer implements PureStrategy {
        private int index;
        private int player;

        public IndexOfPlayer(int value, int player) {
            if (player < 0 || player > 1) throw new IllegalArgumentException("Player can be either 0 or 1!");
            this.index = value;
            this.player = player;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("{index=").append(index);
            sb.append('}');
            return sb.toString();
        }

        public int getIndex() {
            return index;
        }

        public int getPlayer() {
            return player;
        }
    }
}
