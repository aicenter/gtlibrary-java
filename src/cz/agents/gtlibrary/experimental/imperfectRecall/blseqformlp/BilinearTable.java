package cz.agents.gtlibrary.experimental.imperfectRecall.blseqformlp;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.experimental.imperfectRecall.BilinearTermsMDT;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.IloException;

import java.util.HashMap;
import java.util.Map;

public class BilinearTable extends LPTable {
    private Map<Object, Pair<Object, Object>> bilinearVars;

    public BilinearTable() {
        bilinearVars = new HashMap<>();
    }

    public void markAsBilinear(Object bilinearVarKey, Object cause1Key, Object cause2Key) {
        bilinearVars.put(bilinearVarKey, new Pair<>(cause1Key, cause2Key));
    }

    @Override
    public LPData toCplex() throws IloException {
        LPData data = super.toCplex();

        for (Map.Entry<Object,Pair<Object,Object>> entry : bilinearVars.entrySet()) {
            BilinearTermsMDT.addMDTConstraints(data.getSolver(), data.getVariables()[getVariableIndex(entry.getValue().getLeft())],
                    data.getVariables()[getVariableIndex(entry.getValue().getRight())], data.getVariables()[getVariableIndex(entry.getKey())], 2);
        }
        return data;
    }
}
