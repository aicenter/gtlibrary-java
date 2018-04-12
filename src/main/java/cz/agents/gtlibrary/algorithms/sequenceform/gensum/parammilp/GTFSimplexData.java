package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.EpsilonReal;

import java.util.ArrayList;

public class GTFSimplexData {
    Indices firtPhaseSlack;
    ArrayList<Integer> basis;
    EpsilonReal[][] tableau;

    public GTFSimplexData(EpsilonReal[][] tableau, ArrayList<Integer> basis, Indices firstPhaseSlack) {
        this.tableau = tableau;
        this.basis = basis;
        this.firtPhaseSlack = firstPhaseSlack;
    }
}
