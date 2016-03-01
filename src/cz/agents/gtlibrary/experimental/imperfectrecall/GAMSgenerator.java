package cz.agents.gtlibrary.experimental.imperfectrecall;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by bosansky on 8/7/14.
 */
public class GAMSgenerator {
    private Variable[] xVars;
    private Variable[] yVars;
    private Variable[] zVars;
    private Variable[] lambdaVars;
    private Variable[] etaVars;
    private Variable[] sVars;

    private int Nobj;
    private int Nineq;
    private int Neq;

    private Variable objective;
    private String U = "20";

    private double[][] matrixData;

    public static void main(String[] args) {
//        String fileName = "game-2-3-4-10-8-8";
//        GAMSgenerator g = new GAMSgenerator(2,3,4,10,8,8,fileName);

        String fileName = "game-2-3-4-14-8-8";
        GAMSgenerator g = new GAMSgenerator(2,3,4,14,8,8,fileName);

//        String fileName = "game-4-1-0-12-2-3";
//        GAMSgenerator g = new GAMSgenerator(4,1,0,12,2,3,fileName);

//        System.out.println(Arrays.deepToString(g.matrixData));

        StringBuilder gamsFile = new StringBuilder();

        gamsFile.append(g.generateVariables());
        gamsFile.append(g.generateObjectiveConstraint());
        gamsFile.append(g.generateEquations());
        gamsFile.append(g.generateMainInequalities());
        gamsFile.append(g.generateSlackInequalities());
        List<Variable> toDisplay = new ArrayList<>();
        toDisplay.addAll(Arrays.asList(g.xVars));
        toDisplay.addAll(Arrays.asList(g.yVars));
        toDisplay.addAll(Arrays.asList(g.zVars));
        gamsFile.append(g.generateTail(g.objective, toDisplay));

        g.saveGAMS(fileName + ".gams", gamsFile.toString());
    }

    public String generateSlackInequalities() {
        StringBuilder sb = new StringBuilder();

        for (int eqCounter=0; eqCounter < Nineq; eqCounter++) {
            Term RS = new DiffTerm(new UnaryTerm(lambdaVars[eqCounter]), new ProdTerm(new UnaryTerm(U),new UnaryTerm(etaVars[eqCounter])));
            Term LS = new UnaryTerm(0);
            sb.append(new GeConstraint("slackConstraintL"+eqCounter, LS, RS).toGAMSString());
        }

        for (int eqCounter=0; eqCounter < Nineq; eqCounter++) {
            Term RS = new SumTerm(new DiffTerm(new UnaryTerm(sVars[eqCounter]), new UnaryTerm(U)),new ProdTerm(new UnaryTerm(U),new UnaryTerm(etaVars[eqCounter])));
            Term LS = new UnaryTerm(0);
            sb.append(new GeConstraint("slackConstraintS"+eqCounter, LS, RS).toGAMSString());
        }

        return sb.toString();
    }


    public String generateMainInequalities() {
        StringBuilder sb = new StringBuilder();
        for (int eqCounter=0; eqCounter<Nineq; eqCounter++) {
            Term RS = new ProdTerm(new UnaryTerm("-1"),new UnaryTerm(sVars[eqCounter]));
            Term LS = new UnaryTerm(0);
            int shift = 0;
            for (int xv=0; xv<xVars.length; xv++) {
                Double v = matrixData[shift+xv][Nobj+eqCounter];
                if (v != 0) {
                    RS = new SumTerm(RS, new ProdTerm(new UnaryTerm(v), new UnaryTerm(xVars[xv])));
                }
            }
            shift += xVars.length;
            for (int yv=0; yv<yVars.length; yv++) {
                Double v = matrixData[shift+yv][Nobj+eqCounter];
                if (v != 0) {
                    RS = new SumTerm(RS, new ProdTerm(new UnaryTerm(v), new UnaryTerm(yVars[yv])));
                }
            }
            shift += yVars.length;
            for (int zv=0; zv<zVars.length; zv++) {
                Double v = matrixData[shift+zv][Nobj+eqCounter];
                if (v != 0) {
                    RS = new SumTerm(RS, new ProdTerm(new UnaryTerm(v),new UnaryTerm(zVars[zv])));
                }
            }
            shift += zVars.length;
            for (int lv=0; lv<lambdaVars.length; lv++) {
                Double v = matrixData[shift+lv][Nobj+eqCounter];
                if (v != 0) {
                    RS = new SumTerm(RS, new ProdTerm(new UnaryTerm(v),new UnaryTerm(lambdaVars[lv])));
                }
            }
            shift += lambdaVars.length;
            Double v = matrixData[shift][Nobj+eqCounter];
            if (v != 0) {
                RS = new SumTerm(RS, new UnaryTerm(v));
            }
            sb.append(new EqConstraint("ineqConstraint"+eqCounter, LS, RS).toGAMSString());
        }
        return sb.toString();
    }

    public String generateEquations() {
        StringBuilder sb = new StringBuilder();
        for (int eqCounter=0; eqCounter<Neq; eqCounter++) {
            Term RS = new UnaryTerm(0);
            Term LS = new UnaryTerm(0);
            int shift = 0;
            for (int xv=0; xv<xVars.length; xv++) {
                Double v = matrixData[shift+xv][Nineq+Nobj+eqCounter];
                if (v != 0) {
                    RS = new SumTerm(RS, new ProdTerm(new UnaryTerm(v), new UnaryTerm(xVars[xv])));
                }
            }
            shift += xVars.length;
            for (int yv=0; yv<yVars.length; yv++) {
                Double v = matrixData[shift+yv][Nineq+Nobj+eqCounter];
                if (v != 0) {
                    RS = new SumTerm(RS, new ProdTerm(new UnaryTerm(v), new UnaryTerm(yVars[yv])));
                }
            }
            shift += yVars.length;
            for (int zv=0; zv<zVars.length; zv++) {
                Double v = matrixData[shift+zv][Nineq+Nobj+eqCounter];
                if (v != 0) {
                    RS = new SumTerm(RS, new ProdTerm(new UnaryTerm(v),new UnaryTerm(zVars[zv])));
                }
            }
            shift += zVars.length;
            for (int lv=0; lv<lambdaVars.length; lv++) {
                Double v = matrixData[shift+lv][Nineq+Nobj+eqCounter];
                if (v != 0) {
                    RS = new SumTerm(RS, new ProdTerm(new UnaryTerm(v),new UnaryTerm(lambdaVars[lv])));
                }
            }
            shift += lambdaVars.length;
            Double v = matrixData[shift][Nineq+Nobj+eqCounter];
            if (v != 0) {
                if (v < 0) {
                    RS = new DiffTerm(RS, new UnaryTerm(-1*v));
                } else {
                    RS = new SumTerm(RS, new UnaryTerm(v));
                }
            }
            sb.append(new EqConstraint("eqConstraint"+eqCounter, LS, RS).toGAMSString());
        }
        return sb.toString();
    }

    public String generateObjectiveConstraint() {
        StringBuilder sb = new StringBuilder();

        Term rs = new UnaryTerm(0);


        for (int i=0; i<Nobj; i++) {
            boolean noMoreValues = false;
            int shift = 0;
            Term rsFactor = new UnaryTerm(1);
            for (int xv=0; xv<xVars.length; xv++) {
                Double v = matrixData[shift+xv][i];
                if (v != 0) {
                    if (!noMoreValues) {
                        rsFactor = new ProdTerm(rsFactor, new ProdTerm(new UnaryTerm(v), new UnaryTerm(xVars[xv])));
                        noMoreValues = true;
                    } else {
                        rsFactor = new ProdTerm(rsFactor, new UnaryTerm(xVars[xv]));
                    }
                }
            }
            shift += xVars.length;
            for (int yv=0; yv<yVars.length; yv++) {
                Double v = matrixData[shift+yv][i];
                if (v != 0) {
                    rsFactor = new ProdTerm(rsFactor, new UnaryTerm(yVars[yv]));
                }
            }
            shift += yVars.length;
            for (int zv=0; zv<zVars.length; zv++) {
                Double v = matrixData[shift+zv][i];
                if (v != 0) {
                    rsFactor = new ProdTerm(rsFactor, new UnaryTerm(zVars[zv]));
                }
            }
            shift += zVars.length;
            rs = new SumTerm(rs, rsFactor);
        }

        EqConstraint eq = new EqConstraint(objective.getName() + "_obj", new UnaryTerm(objective), rs);
        sb.append(eq.toGAMSString());

        return sb.toString();
    }

    public String generateVariables() {
        StringBuilder sb = new StringBuilder();
        sb.append(objective.toGAMSString());

        for (Variable v : xVars) {
            sb.append(v.toGAMSString() + "\n");
        }

        for (Variable v : yVars) {
            sb.append(v.toGAMSString() + "\n");
        }

        for (Variable v : zVars) {
            sb.append(v.toGAMSString() + "\n");
        }

        for (Variable v : lambdaVars) {
            sb.append(v.toGAMSString() + "\n");
        }

        for (Variable v : etaVars) {
            sb.append(v.toGAMSString() + "\n");
        }

        for (Variable v : sVars) {
            sb.append(v.toGAMSString() + "\n");
        }

        return sb.toString();
    }


    public GAMSgenerator(int NX, int NY, int NZ, int Nobj, int Nineq, int Neq, String filename) {
        xVars = new Variable[NX];
        for (int x=0; x<NX; x++) {
            xVars[x] = new Variable("x"+x, 0, 1);
        }

        yVars = new Variable[NY];
        for (int y=0; y<NY; y++) {
            yVars[y] = new Variable("y"+y, 0, 1);
        }

        zVars = new Variable[NZ];
        for (int z=0; z<NZ; z++) {
            zVars[z] = new Variable("z"+z, 0, 1);
        }

        lambdaVars = new Variable[Nineq];
        for (int l=0; l<Nineq; l++) {
            lambdaVars[l] = new PositiveVariable("lambda"+l);
        }

        sVars = new Variable[Nineq];
        for (int s=0; s<Nineq; s++) {
            sVars[s] = new PositiveVariable("s"+s);
        }

        etaVars = new Variable[Nineq];
        for (int e=0; e<Nineq; e++) {
            etaVars[e] = new BinaryVariable("n"+e);
        }

        this.Nobj = Nobj;
        this.Nineq = Nineq;
        this.Neq = Neq;

        this.objective = new Variable("V");
        this.matrixData = new double[NX+NY+NZ+Nineq+1][Nobj+Nineq+Neq];

        loadMatrixToData(filename);
    }

    public void loadMatrixToData(String fileName) {
        assert matrixData != null;
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(fileName)));
            BufferedReader in = new BufferedReader(isr);
            String line = in.readLine();
            int row = 0;
            while (line != null) {
                int column = 0;
                for (String v : line.split(",")) {
                    matrixData[row][column] = new Double(v);
                    column++;
                }
                line = in.readLine();
                row++;
            }
            isr.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void saveGAMS(String filename, String GAMS) {
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filename))));
            out.write(GAMS);
            out.close();
        }  catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }



    public String generateTail(Variable objective, List<Variable> variablesToDisplay) {
        StringBuilder sb = new StringBuilder();

        sb.append("MODEL TEST /ALL/;\n");
        sb.append("TEST.RESLIM = 10000;\n");
        sb.append("SOLVE TEST USING MINLP MAXIMIZING " + objective.name + ";\n");
        sb.append("display ");

        int size = variablesToDisplay.size();

        for (Variable v : variablesToDisplay) {
            size--;
            sb.append(v.name + ".l");
            if (size > 0) sb.append(", ");
        }
        sb.append(";\n");
        return sb.toString();
    }

    public static class Variable {
        private double UB = 10000;
        private double LB = -10000;
        final private String name;

        public Variable(String name) {
            this.name = name;
        }

        public Variable(String name, double LB, double UB) {
            this(name);
            this.UB = UB;
            this.LB = LB;

        }

        public String toGAMSString() {
            StringBuilder sb = new StringBuilder();

            sb.append("VARIABLES\n");
            sb.append("\t"+name+"\n;\n");

            sb.append("EQUATIONS\n");
            sb.append("\t"+name+"_UB\n;\n");

            sb.append(name+"_UB .. " + name + " =l= " + UB + ";\n");

            sb.append("EQUATIONS\n");
            sb.append("\t"+name+"_LB\n;\n");

            sb.append(name+"_LB .. " + name + " =g= " + LB + ";\n");

            return sb.toString();
        }

        public String getName() {
            return name;
        }
    }

    public static class BinaryVariable extends Variable {

        public BinaryVariable(String name) {
            super(name, 0, 1);
        }

        @Override
        public String toGAMSString() {
            StringBuilder sb = new StringBuilder();

            sb.append("VARIABLES\n");
            sb.append("\t"+getName()+"\n;\n");

            sb.append("binary variables " + getName() + " ;\n");

            return sb.toString();
        }
    }

    public static class PositiveVariable extends Variable {

        public PositiveVariable(String name) {
            super(name, 0, Double.POSITIVE_INFINITY);
        }

        @Override
        public String toGAMSString() {
            StringBuilder sb = new StringBuilder();

            sb.append("VARIABLES\n");
            sb.append("\t"+getName()+"\n;\n");

            sb.append("positive variables " + getName() + " ;\n");

            return sb.toString();
        }
    }

    public static abstract class Constraint{
        final protected String name;
        final protected String ls;
        final protected String rs;

        public Constraint(String name, String ls, String rs) {
            this.name = name;
            this.ls = ls;
            this.rs = rs;
        }

        public abstract String toGAMSString();
    };

    public static class GeConstraint extends Constraint{


        public GeConstraint(String name, String ls, String rs) {
              super(name, ls, rs);
        }

        public GeConstraint(String name, Term ls, Term rs) {
            super(name, ls.toString(),rs.toString());
        }

        public String toGAMSString() {
            StringBuilder sb = new StringBuilder();

            sb.append("EQUATIONS\n");
            sb.append("\t"+name+"\n;\n");

            sb.append(name+" .. " + ls + " =g= " + rs + ";\n");

            return sb.toString();
        }
    }

    public static class EqConstraint extends Constraint{
        public EqConstraint(String name, String ls, String rs) {
            super(name, ls, rs);
        }

        public EqConstraint(String name, Term ls, Term rs) {
            super(name, ls.toString(),rs.toString());
        }

        public String toGAMSString() {
            StringBuilder sb = new StringBuilder();

            sb.append("EQUATIONS\n");
            sb.append("\t"+name+"\n;\n");

            sb.append(name+" .. " + ls + " =e= " + rs + ";\n");

            return sb.toString();
        }
    }

    public static abstract class Term {
        public abstract String toString();
    }

    public static class UnaryTerm extends Term {
        String value;

        public UnaryTerm(String value) {
            this.value = value;
        }

        public UnaryTerm(Variable value) {
            this.value = value.name;
        }

        public UnaryTerm(Integer value) {
            this.value = value.toString();
        }

        public UnaryTerm(Double value) {
            this.value = value.toString();
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class SumTerm extends Term {
        Term leftSide;
        Term rightSide;

        public SumTerm(Term leftSide, Term rightSide) {
            this.leftSide = leftSide;
            this.rightSide = rightSide;
        }

        @Override
        public String toString() {
            return " (" + leftSide.toString() + " + " + rightSide.toString() + ") ";
        }
    }

    public static class DiffTerm extends Term {
        Term leftSide;
        Term rightSide;

        public DiffTerm(Term leftSide, Term rightSide) {
            this.leftSide = leftSide;
            this.rightSide = rightSide;
        }


        @Override
        public String toString() {
            return " (" + leftSide.toString() + " - " + rightSide.toString() + ") ";
        }
    }

    public static class ProdTerm extends Term {
        Term leftSide;
        Term rightSide;

        public ProdTerm(Term leftSide, Term rightSide) {
            this.leftSide = leftSide;
            this.rightSide = rightSide;
        }

        @Override
        public String toString() {
            return " (" + leftSide.toString() + " * " + rightSide.toString() + ") ";
        }
    }

    public static class DivTerm extends Term {
        Term leftSide;
        Term rightSide;

        public DivTerm(Term leftSide, Term rightSide) {
            this.leftSide = leftSide;
            this.rightSide = rightSide;
        }

        @Override
        public String toString() {
            return " (" + leftSide.toString() + " / (" + rightSide.toString() + ")) ";
        }
    }
}
