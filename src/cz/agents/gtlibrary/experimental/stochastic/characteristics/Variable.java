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
public class Variable {
    private Path cesta;
    private Set<Integer> domain;
    private Set<Integer> backupDomain = null;
    private Map<Variable, Set<Integer>> removedValues = new HashMap<Variable, Set<Integer>>();

    public Variable(Path cesta, Collection<Integer> domain) {
        this.cesta = cesta;
        this.domain = new HashSet<Integer>();
        this.domain.addAll(domain);
    }

    public Path getCesta() {
        return cesta;
    }

    public boolean hasInDomain(Integer v) {
        return this.domain.contains(v);
    }

    public void removeFromDomain(Collection v,Variable lastFixedVariable) {
        this.domain.removeAll(v);
        Set<Integer> removedValuesForThisVar = removedValues.get(lastFixedVariable);
        if (removedValuesForThisVar == null) removedValuesForThisVar = new HashSet<Integer>();
        removedValuesForThisVar.addAll(v);
        removedValues.put(lastFixedVariable, removedValuesForThisVar);
    }

    public void undeleteValuesForVariable(Variable uncommitingVar) {
        Set<Integer> tmp = removedValues.get(uncommitingVar);
        if (tmp != null || tmp.isEmpty())
            return;
        domain.addAll(tmp);
        removedValues.remove(uncommitingVar);
    }

    public boolean hasEmptyDomain() {
        return domain.isEmpty();
    }

    public Set<Integer> getDomain() {
        return domain;
    }

    public void commitTo(Integer value) {
        assert (domain.contains(value));
        backupDomain = domain;
        domain = new HashSet<Integer>();
        domain.add(value);
    }

    public void uncommit() {
        assert (backupDomain != null);
        domain = backupDomain;
        backupDomain = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Variable)) return false;

        Variable variable = (Variable) o;

        if (cesta != null ? !cesta.equals(variable.cesta) : variable.cesta != null) return false;
//        if (domain != null ? !domain.equals(variable.domain) : variable.domain != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = cesta != null ? cesta.hashCode() : 0;
//        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Variable{" +
                "cesta=" + cesta +
                ", domain=" + domain +
//                ", backupDomain=" + backupDomain +
//                ", removedValues=" + removedValues +
                '}';
    }
}
