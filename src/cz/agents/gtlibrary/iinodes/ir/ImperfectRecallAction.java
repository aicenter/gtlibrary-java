package cz.agents.gtlibrary.iinodes.ir;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.InformationSet;

@Deprecated
public abstract class ImperfectRecallAction extends ActionImpl {
    protected String type;

    public ImperfectRecallAction(InformationSet informationSet, String type) {
        super(informationSet);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
