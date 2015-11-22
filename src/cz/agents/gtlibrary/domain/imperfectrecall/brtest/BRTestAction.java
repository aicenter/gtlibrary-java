package cz.agents.gtlibrary.domain.imperfectrecall.brtest;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class BRTestAction extends ActionImpl {
    private String id;
    private int p1Observation;
    private int p2Observation;

    public BRTestAction(InformationSet informationSet, String id, int p1Observation, int p2Observation) {
        super(informationSet);
        this.id = id;
        this.p1Observation = p1Observation;
        this.p2Observation = p2Observation;
    }

    public String getId() {
        return id;
    }

    @Override
    public void perform(GameState gameState) {
        ((BRTestGameState)gameState).addP1ObservationFor(informationSet.getPlayer(), p1Observation);
        ((BRTestGameState)gameState).addP2ObservationFor(informationSet.getPlayer(), p2Observation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BRTestAction)) return false;
        if (!super.equals(o)) return false;

        BRTestAction that = (BRTestAction) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return id;
    }
}
