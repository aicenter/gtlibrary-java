package cz.agents.gtlibrary.experimental.imperfectrecall;

import ilog.concert.*;
import ilog.cplex.IloCplex;

/**
 * Created by bosansky on 11/4/15.
 */
public class BilinearTermsMDT {

    public static void main(String[] args) {
        try {

            IloCplex cplex = new IloCplex();
            IloNumVar V = cplex.numVar(Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY, IloNumVarType.Float,"V");

            IloNumVar x = cplex.numVar(0,1,IloNumVarType.Float,"x");
            IloNumVar y = cplex.numVar(0,1,IloNumVarType.Float,"y");
            IloNumVar z = cplex.numVar(0,1,IloNumVarType.Float,"z");



            cplex.addLe(V, cplex.sum(cplex.prod(-1.2, x), cplex.prod(-1, y), cplex.prod(1,z)));
            cplex.addGe(z,0.5);
//            cplex.addLe(x, 0.9);

//            cplex.addGe(z,cplex.sum(x,cplex.sum(y,-1)));
            cplex.addLe(z,x);
            cplex.addLe(z,y);

            cplex.addMaximize(V);

            final int precision = 1;
            addMDTConstraints(cplex,x,y,z,precision);

            cplex.exportModel("IR2.lp");
            cplex.solve();

            if (cplex.getStatus() == IloCplex.Status.Optimal) {
                System.out.println("Objective: " + cplex.getValue(V));
                System.out.println("z: " + cplex.getValue(z));
                System.out.println("y: " + cplex.getValue(y));
                System.out.println("x: " + cplex.getValue(x));
                System.out.println("Inaccuracy: " + Math.abs(cplex.getValue(z) - cplex.getValue(x)*cplex.getValue(y)));
            }


        } catch (IloException e) {
            e.printStackTrace();
        }

    }

    public static void addMDTConstraints(IloCplex cplex, IloNumVar x, IloNumVar y, IloNumVar xy, int precision) throws IloException{
        final int digits = 10;
        IloNumVar[][] w = new IloNumVar[digits][precision];
        IloNumVar[][] xHat = new IloNumVar[digits][precision];

        IloNumExpr zSum = cplex.numExpr();
        IloNumExpr ySum = cplex.numExpr();
        for (int l=0; l<precision; l++) {
            IloNumExpr xSum = cplex.numExpr();
            for (int k = 0; k < digits; k++) {
                w[k][l] = cplex.numVar(0,1,IloNumVarType.Bool);
                xHat[k][l] = cplex.numVar(0,1,IloNumVarType.Float);

                zSum = cplex.sum(zSum, cplex.prod(cplex.constant(Math.pow(10, -(l+1)) * k), xHat[k][l]));
                xSum = cplex.sum(xSum, xHat[k][l]);
                ySum = cplex.sum(ySum, cplex.prod(cplex.constant(Math.pow(10, -(l+1)) * k), w[k][l]));

                cplex.addLe(xHat[k][l],w[k][l]);
            }
            cplex.addEq(x,xSum);
        }

        cplex.addEq(y,ySum);
        cplex.addEq(xy,zSum);
    }
}
