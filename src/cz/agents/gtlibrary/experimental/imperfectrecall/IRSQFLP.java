package cz.agents.gtlibrary.experimental.imperfectrecall;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

/**
 * Created by kail on 11/9/15.
 */
public class IRSQFLP {
    public static void main(String[] args) {
        try {

            final int precision = 4;
            final int M = 20;

            double[] utilities = new double[] {-1,2,-10,-10,2,-1};
            int leafs = utilities.length;
            int nodes = 11;


            IloCplex cplex = new IloCplex();
            IloNumVar V = cplex.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, IloNumVarType.Float, "V");
            cplex.addMaximize(V);

            IloNumVar[] p = new IloNumVar[leafs];
            IloNumVar[] v = new IloNumVar[nodes];
            IloNumVar[] x = new IloNumVar[2];
            IloNumVar[] y = new IloNumVar[2];
            IloNumVar[] sl = new IloNumVar[2];
            IloNumVar[] r = new IloNumVar[9];

            IloNumVar VI = cplex.numVar(Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,IloNumVarType.Float,"VI1");

            for (int i = 0; i < nodes; i++) {
                if (i < p.length) p[i] = cplex.numVar(0,1,IloNumVarType.Float,"P"+i);
                v[i] = cplex.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, IloNumVarType.Float, "v"+i);
                if (i < r.length) r[i] = cplex.numVar(0,1,IloNumVarType.Float,"r"+i);
                if (i < x.length) x[i] = cplex.numVar(0,1,IloNumVarType.Float,"x"+i);
                if (i < y.length) y[i] = cplex.numVar(0,1,IloNumVarType.Bool,"y"+i);
                if (i < sl.length) sl[i] = cplex.numVar(0,Double.POSITIVE_INFINITY,IloNumVarType.Float,"sl"+i);
            }

            IloNumExpr vSum = cplex.numExpr();
            IloNumExpr pSum = cplex.numExpr();
            for (int i=0; i<leafs; i++) {
                vSum = cplex.sum(vSum,cplex.prod(p[i],utilities[i]));
                pSum = cplex.sum(pSum,p[i]);
            }
            cplex.addEq(V,vSum);
            cplex.addEq(1,pSum);

            cplex.addEq(cplex.sum(x[0],x[1]),1);
            cplex.addEq(cplex.sum(y[0],y[1]),1);

            cplex.addEq(r[0],1);
            cplex.addEq(cplex.sum(r[1],r[2]),r[0]);
            cplex.addEq(cplex.sum(r[3],r[4]),r[0]);
            cplex.addEq(cplex.sum(r[5],r[6]),r[2]);
            cplex.addEq(cplex.sum(r[7],r[8]),r[3]);

            cplex.addLe(p[0],r[1]);cplex.addLe(p[0],y[0]);
            cplex.addLe(p[1],r[5]);cplex.addLe(p[1],y[0]);
            cplex.addLe(p[2],r[6]);cplex.addLe(p[2],y[0]);
            cplex.addLe(p[3],r[7]);cplex.addLe(p[3],y[1]);
            cplex.addLe(p[4],r[8]);cplex.addLe(p[4],y[1]);
            cplex.addLe(p[5],r[4]);cplex.addLe(p[5],y[1]);

            cplex.addEq(v[0],cplex.prod(1,r[1]));
            cplex.addEq(v[1],cplex.prod(-2,r[5]));
            cplex.addEq(v[2],cplex.prod(10,r[6]));
            cplex.addEq(v[3],cplex.prod(10,r[7]));
            cplex.addEq(v[4],cplex.prod(-2,r[8]));
            cplex.addEq(v[5],cplex.prod(1,r[4]));

            cplex.addEq(cplex.sum(v[1],v[2]),v[6]);
            cplex.addEq(cplex.sum(v[3],v[4]),v[7]);
            cplex.addEq(cplex.sum(v[0],v[6]),v[8]);
            cplex.addEq(cplex.sum(v[7],v[5]),v[9]);

            cplex.addLe(cplex.prod(cplex.diff(y[0],1),M),cplex.diff(v[10],v[8]));
            cplex.addGe(cplex.prod(cplex.diff(1,y[0]),M),cplex.diff(v[10],v[8]));
            cplex.addLe(cplex.prod(cplex.diff(y[1],1),M),cplex.diff(v[10],v[9]));
            cplex.addGe(cplex.prod(cplex.diff(1, y[1]), M), cplex.diff(v[10], v[9]));

            cplex.addEq(cplex.diff(v[8],sl[0]),VI);
            cplex.addEq(cplex.diff(v[9],sl[1]),VI);
            cplex.addLe(sl[0],cplex.prod(cplex.diff(1,y[0]),M));
            cplex.addLe(sl[1],cplex.prod(cplex.diff(1,y[1]),M));

            BilinearTermsMDT.addMDTConstraints(cplex,r[2],x[0],r[5],precision);
            BilinearTermsMDT.addMDTConstraints(cplex,r[2],x[1],r[6],precision);
            BilinearTermsMDT.addMDTConstraints(cplex,r[3],x[0],r[7],precision);
            BilinearTermsMDT.addMDTConstraints(cplex,r[3],x[1],r[8],precision);

            cplex.exportModel("IR_example.lp");
            cplex.solve();

            if (cplex.getStatus() == IloCplex.Status.Optimal) {
                System.out.println("Objective: " + cplex.getValue(V));
                for (int i = 0; i < 8; i++) {
                    System.out.println("R"+i+":"+cplex.getValue(r[i]));
                }
            }

            } catch (IloException e) {
            e.printStackTrace();
        }
    }
}