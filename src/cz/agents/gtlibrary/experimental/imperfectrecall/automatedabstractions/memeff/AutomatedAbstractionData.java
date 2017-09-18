package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.iinodes.ISKey;

import java.io.Serializable;
import java.util.Map;

public class AutomatedAbstractionData implements Serializable {
    public Map<ISKey, MemEffAbstractedInformationSet> currentAbstractionInformationSets;
    public InformationSetKeyMap currentAbstractionISKeys;
    public int iteration;
    public int isKeyCounter;

    public AutomatedAbstractionData(Map<ISKey, MemEffAbstractedInformationSet> currentAbstractionInformationSets,
                                    InformationSetKeyMap currentAbstractionISKeys, int iteration, int isKeyCounter) {
        this.currentAbstractionISKeys = currentAbstractionISKeys;
        this.currentAbstractionInformationSets = currentAbstractionInformationSets;
        this.iteration = iteration;
        this.isKeyCounter = isKeyCounter;
    }
}
