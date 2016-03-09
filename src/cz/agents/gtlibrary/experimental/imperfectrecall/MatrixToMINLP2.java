package cz.agents.gtlibrary.experimental.imperfectrecall;

import Jama.Matrix;
import cz.agents.gtlibrary.utils.CombinationGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by bosansky on 8/11/14.
 */
public class MatrixToMINLP2 {
    Matrix matrix;
    int XVarsNum;
    int YVarsNum;
    int origObjectiveTerms;
    int origEqConstraints;

    int YVarsNew;
    int hatYVarsNum;
    int lambdaVars;
    int newObjectiveTerms;
    int newXEqConstraints;
    int newKKTEqConstraints;
    int newIneqConstraints;


    Matrix newMatrix;

    public MatrixToMINLP2(double[][] matrix, int XVarsNum, int YVarsNum, int objectiveTerms, int eqConstraints) {
        this.matrix = new Matrix(matrix);
        this.XVarsNum = XVarsNum;
        this.YVarsNum = YVarsNum;
        this.origObjectiveTerms = objectiveTerms;
        this.origEqConstraints = eqConstraints;

        assert (XVarsNum+YVarsNum) == matrix.length;
        assert (objectiveTerms + eqConstraints) == this.matrix.getColumnDimension();
    }

    public void reduceYVars() {
        int colShift = origObjectiveTerms;
        int rowShift = XVarsNum;
//        ArrayList<ArrayList<Integer>> newObjectiveTerms = new ArrayList<>();
        ArrayList<Integer> yVarsToRemove = new ArrayList<>();
        ArrayList<Integer> eqsToRemove = new ArrayList<>();
        Map<Integer, ArrayList<Integer>> yVarsToReplace = new HashMap<>();
        for (int eq=0; eq<origEqConstraints; eq++) {
            ArrayList<Integer> thisYVars = new ArrayList<>();
            for (int yVar=0; yVar<YVarsNum; yVar++) {
                if (matrix.get(rowShift + yVar, colShift + eq) > 0) {
                    thisYVars.add(yVar);
                }
            }

            if (!thisYVars.isEmpty()) {
                int lastYIdx = thisYVars.size()-1;
                int yIDX = thisYVars.get(lastYIdx);
                yVarsToRemove.add(yIDX);
                thisYVars.remove(lastYIdx);
                yVarsToReplace.put(yIDX, thisYVars);
                eqsToRemove.add(eq);
            }

        }

        YVarsNew = YVarsNum - yVarsToRemove.size();
        hatYVarsNum = (int)Math.pow(2,YVarsNew) - 1;
        lambdaVars = (int)Math.pow(2,YVarsNew);
        newXEqConstraints = origEqConstraints - yVarsToRemove.size();
        newKKTEqConstraints = hatYVarsNum;
        newIneqConstraints = (int)Math.pow(2,YVarsNew);
        newObjectiveTerms = 0;
        for (int obj =0 ; obj<origObjectiveTerms; obj++) {
            int thisObj = 1;
            for (int yVar=0; yVar<YVarsNum; yVar++) {
                if (matrix.get(rowShift + yVar, obj) != 0 && yVarsToRemove.contains(yVar)) {
                    thisObj *= yVarsToReplace.get(yVar).size() + 1;
                }
            }
            newObjectiveTerms += thisObj;
        }
        newMatrix = new Matrix(XVarsNum + hatYVarsNum + lambdaVars + 1, newObjectiveTerms + newIneqConstraints + newXEqConstraints + newKKTEqConstraints);

        System.out.println("New Matrix Size: " + newMatrix.getRowDimension() + "x" + newMatrix.getColumnDimension());


        int newObjectivesAdded = 0;
        for (int obj=0; obj<origObjectiveTerms; obj++) {
            Matrix column = matrix.getMatrix(0, XVarsNum + YVarsNum - 1, obj, obj);

            ArrayList<Integer> yInThisObj = new ArrayList<>();
            ArrayList<Integer> yNewThisObj = new ArrayList<>();
            ArrayList<Integer> yToRemoveInThisObj = new ArrayList<>();
            double value = getValueFromObjVector(column);
            for (int y=0; y<YVarsNum; y++) {
                if (matrix.get(XVarsNum+y, obj) != 0) {
                    yInThisObj.add(y);
                }
            }
            for (int y : yVarsToRemove) {
                if (yInThisObj.contains(y)) {
                    yToRemoveInThisObj.add(y);
                }
            }
            yInThisObj.removeAll(yToRemoveInThisObj);
            for (int y : yInThisObj) {
                yNewThisObj.add(yIndexTransformation(yVarsToRemove, y));
            }

            Matrix newColumnBase = new Matrix(XVarsNum + hatYVarsNum + lambdaVars + 1,1);
            newColumnBase.setMatrix(0, XVarsNum - 1, 0, 0, column.getMatrix(0, XVarsNum - 1, 0, 0));
            for (int y : yNewThisObj) {
                newColumnBase.set(XVarsNum + y, 0, value);
            }
            int sizeOfObjectives = 1;
            for (int y : yToRemoveInThisObj) {
                sizeOfObjectives *= yVarsToReplace.get(y).size() + 1;
                Matrix newColumns = new Matrix(newColumnBase.getRowDimension(),sizeOfObjectives);
                for (int col=0; col<newColumnBase.getColumnDimension(); col++) {
                    newColumns.setMatrix(0,XVarsNum + hatYVarsNum + lambdaVars,2*col,2*col+1, multiplyWithOneMinusIndexes(newColumnBase.getMatrix(0,XVarsNum + hatYVarsNum + lambdaVars,col,col),yVarsToReplace.get(y), XVarsNum, yVarsToRemove));
                }
                newColumnBase = newColumns;
            }
            newMatrix.setMatrix(0, XVarsNum + hatYVarsNum + lambdaVars, newObjectivesAdded, newObjectivesAdded + sizeOfObjectives-1, newColumnBase);
            newObjectivesAdded += sizeOfObjectives;

        }
//        System.out.println(matrixToFancyString(newMatrix)); //*/
    }

    public void replaceYHat() {
        assert  (newMatrix != null);
        ArrayList<Integer> allYs = new ArrayList<>();
        for (int i=0; i<YVarsNew; i++) allYs.add(i);
        for (int obj=0; obj<newObjectiveTerms; obj++) {
            ArrayList<Integer> yInThisObj = new ArrayList<>();
            for (int y=0; y<YVarsNew; y++) {
                if (newMatrix.get(XVarsNum+y, obj) != 0) {
                    yInThisObj.add(y);
                }
            }
            if (yInThisObj.size() > 1) {
                int newIdx = fromListToYHatIdx(allYs,yInThisObj);
                newMatrix.set(XVarsNum+newIdx,obj,newMatrix.get(XVarsNum+yInThisObj.get(0),obj));
                for (int y : yInThisObj) {
                    newMatrix.set(XVarsNum+y,obj,0);
                }
            }
        }
//        System.out.println(matrixToFancyString(newMatrix)); //*/
    }

    public void generateReformulationInequalities() {
        Matrix eqs = new Matrix(newMatrix.getRowDimension(), newIneqConstraints);
        int currentCol = 0;

        int[] allYs = new int[YVarsNew];
        ArrayList<Integer> listAllYs = new ArrayList<>(YVarsNew);
        for (int i=0; i<YVarsNew; i++) {
            listAllYs.add(i);
            allYs[i]=i;
        }

        for (int sizeY1=0; sizeY1<=YVarsNew; sizeY1++) {
            int sizeY2=YVarsNew - sizeY1;
            for (int c=0; c<CombinationGenerator.binomCoef(YVarsNew, sizeY1); c++) {
                int[] tmp = CombinationGenerator.generateCombinationWithoutRepeating(allYs, sizeY1, c);
                ArrayList<Integer> currentY1 = new ArrayList<>(YVarsNew);
                ArrayList<Integer> currentY2 = new ArrayList<>(sizeY2);
                for (int y : allYs) {
                    if (Arrays.binarySearch(tmp, y) < 0) {
                        currentY2.add(y);
                    }  else {
                        currentY1.add(y);
                    }
                }
                int currentSign = 1;

                getAllYComb(listAllYs, currentY1, currentY2, eqs, currentCol, currentSign);
                currentCol++;
            }
        }

        newMatrix.setMatrix(0,newMatrix.getRowDimension()-1,newObjectiveTerms,newObjectiveTerms+newIneqConstraints-1,eqs);
//        System.out.println(matrixToFancyString(newMatrix)); //*/
    }

    public void generateKKTEQS() {
        for (int yHat = 0; yHat < hatYVarsNum; yHat++) {
                ArrayList<Matrix> whichOBJs = new ArrayList<>();
                for (int obj=0;obj<newObjectiveTerms;obj++) {
                    if (newMatrix.get(XVarsNum+yHat,obj) != 0) {
                        whichOBJs.add(newMatrix.getMatrix(0,XVarsNum+hatYVarsNum-1,obj,obj));
                    }
                }
                Matrix sum = new Matrix(XVarsNum+hatYVarsNum,1);
                for (Matrix m : whichOBJs) {
                    sum.plusEquals(m);
                }
                boolean atLeastOneX = false;
                for (int x=0; x<XVarsNum; x++) {
                    if (sum.get(x,0) != 0) {
                        atLeastOneX = true;
                        break;
                    }
                }
                if (!atLeastOneX) {
                    newMatrix.set(newMatrix.getRowDimension()-1,newObjectiveTerms+newIneqConstraints+newXEqConstraints+yHat,sum.get(yHat,0));
                } else {
                    newMatrix.setMatrix(0,XVarsNum-1,newObjectiveTerms+newIneqConstraints+newXEqConstraints+yHat,newObjectiveTerms+newIneqConstraints+newXEqConstraints+yHat,sum.getMatrix(0,XVarsNum-1,0,0));
                }

            for (int lambda = 0; lambda < lambdaVars; lambda++) {
                double value = newMatrix.get(XVarsNum+yHat,newObjectiveTerms+lambda);
                if (value != 0) {
                    newMatrix.set(XVarsNum+hatYVarsNum+lambda, newObjectiveTerms+newIneqConstraints+newXEqConstraints+yHat, -value);
                }
            }
        }
    }

    public void generateXEQs() {
        // this assumes that in the original matrix there are first all X-related equations, following by the Y-related equations (that has been removed)
        for (int xEQ=0; xEQ < newXEqConstraints; xEQ++) {
            newMatrix.setMatrix(0,XVarsNum,newObjectiveTerms+newIneqConstraints+xEQ,newObjectiveTerms+newIneqConstraints+xEQ,matrix.getMatrix(0,XVarsNum,origObjectiveTerms+xEQ,origObjectiveTerms+xEQ));
            newMatrix.set(XVarsNum + hatYVarsNum + lambdaVars, newObjectiveTerms + newIneqConstraints + xEQ, -1);
        }
        System.out.println(matrixToFancyString(newMatrix)); //*/
    }

    public void saveMatrix() {
        String fileName = "game-"+XVarsNum+"-"+YVarsNew+"-"+(hatYVarsNum-YVarsNew)+"-"+newObjectiveTerms+"-"+newIneqConstraints+"-"+(newXEqConstraints+newKKTEqConstraints);
        try {
            File f = new File(fileName);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new PrintStream(f)));
            bw.write(matrixToFancyString(newMatrix));
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAllYComb(ArrayList<Integer> listAllYs, List<Integer> Y1, List<Integer> Y2, Matrix eqs, int column, int sign) {
        if (Y1.isEmpty()) {
            eqs.set(XVarsNum+hatYVarsNum+lambdaVars, column, sign);
        } else {
            int IDX = fromListToYHatIdx(listAllYs, Y1);
            eqs.set(XVarsNum + IDX, column, sign);
        }
        if (Y2.isEmpty()) return;
        ArrayList<Integer> newY2 = new ArrayList<>(Y2);
        Integer y2 = newY2.remove(0);
        ArrayList<Integer> newY1 = new ArrayList<>(Y1);
        getAllYComb(listAllYs, newY1, newY2, eqs, column, sign);
        newY1.add(y2);
        Integer[] tmp = newY1.toArray(new Integer[] {});
        Arrays.sort(tmp);
        getAllYComb(listAllYs, Arrays.asList(tmp), newY2, eqs, column, sign*-1);
    }

    public static String matrixToFancyString(Matrix matrix) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<matrix.getRowDimension(); i++) {
            for (int j=0; j<matrix.getColumnDimension(); j++) {
                if (j>0) sb.append(",");
                sb.append(matrix.get(i, j));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static Matrix multiplyWithOneMinusIndexes(Matrix vector, ArrayList<Integer> indexes, int rowShift, ArrayList<Integer> yToRemove) {
        double value = getValueFromObjVector(vector);
        Matrix newVector = new Matrix(vector.getRowDimension(), indexes.size()+1);

        newVector.setMatrix(0,vector.getRowDimension()-1,0,0,vector.copy());

        for (int c=0; c<indexes.size(); c++) {
            int i  = yIndexTransformation(yToRemove, indexes.get(c));
            newVector.setMatrix(0, vector.getRowDimension() - 1, c + 1, c + 1, vector.copy().uminus());
            newVector.set(rowShift+i, c + 1, -value);
        }

        return newVector;
    }

    public static int yIndexTransformation(ArrayList<Integer> yVarsToRemove, int oldIndex) {
        int newIndex = oldIndex;

        for (int i : yVarsToRemove) {
            if (i < oldIndex) newIndex--;
        }

        return newIndex;
    }

    public static double getValueFromObjVector(Matrix vector) {
        double value = Double.NEGATIVE_INFINITY;
        for (int r=0; r<vector.getRowDimension(); r++) {
            if (vector.get(r,0) != 0) {
                value = vector.get(r,0);
                break;
            }
        }
        assert (value > Double.NEGATIVE_INFINITY);
        return value;
    }

    static public int fromListToYHatIdx(List<Integer> allYs, List<Integer> multipliedYs) {
        int newIdx = 0;
        for (int i=1; i<multipliedYs.size(); i++) newIdx += CombinationGenerator.binomCoef(allYs.size(),i);
        newIdx += CombinationGenerator.whichCombinationItIs(allYs,multipliedYs);
        return newIdx;
    }
}
