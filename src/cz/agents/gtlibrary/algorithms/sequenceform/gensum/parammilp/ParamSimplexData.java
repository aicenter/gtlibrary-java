package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp;

import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.EpsilonPolynomial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParamSimplexData {
    public EpsilonPolynomial[][] tableau;
    public List<Integer> basis;
    public Indices firstPhaseSlacks;
    public Map<Object, Integer> variableIndices;

    public ParamSimplexData(EpsilonPolynomial[][] tableau, List<Integer> basis, Indices firstPhaseSlacks, Map<Object, Integer> variableIndices) {
        this.tableau = tableau;
        this.basis = basis;
        this.firstPhaseSlacks = firstPhaseSlacks;
        this.variableIndices = variableIndices;
    }

    public ParamSimplexData copy() {
        return new ParamSimplexData(deepCopy(tableau), new ArrayList<>(basis),
                firstPhaseSlacks == null ? null : firstPhaseSlacks.copy(), new HashMap<>(variableIndices));
    }

    private EpsilonPolynomial[][] deepCopy(EpsilonPolynomial[][] tableau) {
        EpsilonPolynomial[][] tableauCopy = new EpsilonPolynomial[tableau.length][tableau[0].length];

        for (int i = 0; i < tableau.length; i++) {
            for (int j = 0; j < tableau[0].length; j++) {
                tableauCopy[i][j] =  tableau[i][j];
            }
        }
        return tableauCopy;
    }
}
