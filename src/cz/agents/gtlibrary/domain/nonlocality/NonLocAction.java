package cz.agents.gtlibrary.domain.nonlocality;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class NonLocAction extends ActionImpl {

    private String type;

    public NonLocAction(InformationSet informationSet, String type) {
        super(informationSet);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public void perform(GameState gameState) {
        //intentionally empty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NonLocAction)) return false;
        if (!super.equals(o)) return false;

        NonLocAction that = (NonLocAction) o;

        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }

    @Override
    public String toString() {
        return type;
    }
    
    
}
