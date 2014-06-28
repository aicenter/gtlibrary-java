package cz.agents.gtlibrary.algorithms.sequenceform.refinements.undominated;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;

public class UndominatedTable extends LPTable {


    public void addToObjective(Object varKey, double value) {
        Double oldValue = objective.get(varKey);

        if (oldValue == null)
            objective.put(varKey, value);
        else
            objective.put(varKey, oldValue + value);
        updateVariableIndices(varKey);
    }

    public void clearObjective() {
        objective.clear();
    }
}
