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


package cz.agents.gtlibrary.experimental.stochastic.characteristics;

import java.util.*;

/**
 * Created by kail on 2/25/14.
 */
public class SumConstraint {
    ArrayList<Variable> leftSide = new ArrayList<Variable>();
    ArrayList<Variable> rightSide = new ArrayList<Variable>();
    Integer sumValue;

    public SumConstraint(ArrayList<Variable> leftSide, ArrayList<Variable> rightSide) {
        this(leftSide, rightSide, null);
    }

    public SumConstraint(ArrayList<Variable> leftSide, ArrayList<Variable> rightSide, Integer sumValue) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
        this.sumValue = sumValue;
    }

    public ArrayList<Variable> getLeftSide() {
        return leftSide;
    }

    public void addToLeftSide(Variable leftSideVariable) {
        this.leftSide.add(leftSideVariable);
    }

    public ArrayList<Variable> getRightSide() {
        return rightSide;
    }

    public void addToRightSide(Variable rightSideVariable) {
        this.rightSide.add(rightSideVariable);
    }

    public boolean makeConstraintConsistent(Variable lastFixedVariable) {
        if (sumValue == null) {
            assert (leftSide.size() == 1);

            Variable lsVar = leftSide.get(0);
            Set<Integer> toRemoveFromDomain = new HashSet<Integer>();
            for (int d : lsVar.getDomain()) {
                if (!makeConstraintConsistent(rightSide, d, true, lastFixedVariable)) {
                    toRemoveFromDomain.add(d);
                }
            }
            lsVar.removeFromDomain(toRemoveFromDomain, lastFixedVariable);

            return !lsVar.hasEmptyDomain();
        } else {
            return makeConstraintConsistent(leftSide, sumValue, true, lastFixedVariable) & makeConstraintConsistent(rightSide, sumValue, true, lastFixedVariable);
        }
    }

    public static boolean makeConstraintConsistent(ArrayList<Variable> vars, Integer sumValue, boolean first, Variable lastFixedVariable) {
        if (vars.isEmpty()) return true;

        //test
        int sumMins = 0;
        int sumMaxs = 0;
        for (Variable v : vars) {
            sumMins += Collections.min(v.getDomain());
            sumMaxs += Collections.max(v.getDomain());
        }
        if (sumMins > sumValue || sumMaxs < sumValue) return false;


        if (vars.size() > 1) {
            boolean changed = true;
            while (changed) {
                changed = false;
                for (Variable v : vars) {
                    Set<Integer> toRemoveFromDomain = new HashSet<Integer>();
                    for (int d : v.getDomain()) {
                        int remainingSum = sumValue - d;
                        ArrayList<Variable> remaining = new ArrayList<Variable>(vars);
                        remaining.remove(v);
                        if (!makeConstraintConsistent(remaining, remainingSum, false, lastFixedVariable)) {
                            if (first) {
                                toRemoveFromDomain.add(d);
                            } else {
                                return false;
                            }
                        }

                    }
                    if (!toRemoveFromDomain.isEmpty()) {
                        v.removeFromDomain(toRemoveFromDomain, lastFixedVariable);
                        if (v.hasEmptyDomain()) return false;
                        changed = true;
                    }
                }
            }
        }
        return true;
    }

    public static void enumerateAllSolutions(Map<Variable, Integer> partialSolution, Set<Variable> allVariables, Set<SumConstraint> allConstraints, Map<Variable, Set<SumConstraint>> mappingVarToCon) {

        Set<Variable> tmp = new HashSet<Variable>();
        for (Variable v : allVariables) {
            if (partialSolution.containsKey(v)) continue;
            tmp.add(v);
        }

        if (tmp.isEmpty()) {
            System.out.println(partialSolution);
            return;
        }

        Variable currentVariable = tmp.iterator().next();
        for (int d : new ArrayList<Integer>(currentVariable.getDomain())) {
            boolean fail = false;
            currentVariable.commitTo(d);
            Set<SumConstraint> toCheck = new HashSet<SumConstraint>();
            toCheck.addAll(mappingVarToCon.get(currentVariable));
            while (!toCheck.isEmpty()) {
                SumConstraint con = toCheck.iterator().next();
                toCheck.remove(con);
                if (!con.makeConstraintConsistent(currentVariable)) {
                    fail = true;
                    break;
                }
            }
            if (!fail) {
                partialSolution.put(currentVariable, d);
                enumerateAllSolutions(partialSolution, allVariables, allConstraints, mappingVarToCon);
                partialSolution.remove(currentVariable);
            }
            currentVariable.uncommit();
            for (Variable v : allVariables) {
                v.undeleteValuesForVariable(currentVariable);
            }
        }
    }

    @Override
    public String toString() {
        return "SumConstraint{" +
                "leftSide=" + leftSide +
                ", rightSide=" + rightSide +
                ", sumValue=" + sumValue +
                '}';
    }

    public Integer getSumValue() {
        return sumValue;
    }
}
