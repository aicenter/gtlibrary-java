package cz.agents.gtlibrary.domain.randomgame;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.utils.io.GambitEFG;

public class GambitRandomExport {

    public static void main(String[] args) {
        GambitEFG writer = new GambitEFG();

        writer.buildAndWrite("test.gbt", new RandomGameState(), new RandomGameExpander<SequenceInformationSet>(new GenSumSequenceFormConfig()));
    }
}
