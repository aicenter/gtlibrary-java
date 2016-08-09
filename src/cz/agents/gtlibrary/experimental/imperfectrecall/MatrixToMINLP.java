package cz.agents.gtlibrary.experimental.imperfectrecall;

import java.util.ArrayList;

/**
 * Created by bosansky on 8/11/14.
 */
public class MatrixToMINLP {
    double[][] matrix;
    int XVarsNum;
    int YVarsNum;
    int origObjectiveTerms;
    int origEqConstraints;

    int YVarsReducedNum;
    int YVarsNew;

    public MatrixToMINLP(double[][] matrix, int XVarsNum, int YVarsNum, int objectiveTerms, int eqConstraints) {
        this.matrix = matrix;
        this.XVarsNum = XVarsNum;
        this.YVarsNum = YVarsNum;
        this.origObjectiveTerms = objectiveTerms;
        this.origEqConstraints = eqConstraints;

        assert (XVarsNum+YVarsNum) == matrix.length;
        assert (objectiveTerms + eqConstraints) == matrix[0].length;
    }

    public void reduceYVars() {
        int colShift = origObjectiveTerms;
        int rowShift = XVarsNum;
        ArrayList<ArrayList<Integer>> newObjectiveTerms = new ArrayList<>();
        ArrayList<Integer> yVarsToRemove = new ArrayList<>();
        ArrayList<Integer> eqsToRemove = new ArrayList<>();
        ArrayList<ArrayList<Integer>> yVarsToReplace = new ArrayList<>();
        for (int eq=0; eq<origEqConstraints; eq++) {
            ArrayList<Integer> thisYVars = new ArrayList<>();
            for (int yVar=0; yVar<YVarsNum; yVar++) {
                if (matrix[rowShift+yVar][colShift+eq] > 0) {
                    thisYVars.add(yVar);
                }
                if (!thisYVars.isEmpty()) {
                    int lastYIdx = thisYVars.size()-1;
                    yVarsToRemove.add(thisYVars.get(lastYIdx));
                    thisYVars.remove(lastYIdx);
                    yVarsToReplace.add(thisYVars);
                    eqsToRemove.add(eq);
                }
            }
        }

        for (int yVar : yVarsToRemove) {
            for (int objIdx = 0; objIdx < origObjectiveTerms; objIdx++) {
                if (matrix[rowShift+yVar][objIdx] != 0) {
                    double newValue = -matrix[rowShift+yVar][objIdx];
                    ArrayList<Integer> newObj = new ArrayList<>();
                }
            }
        }
    }

}
