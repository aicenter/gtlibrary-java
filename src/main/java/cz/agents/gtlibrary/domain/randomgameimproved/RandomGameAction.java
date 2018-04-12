package cz.agents.gtlibrary.domain.randomgameimproved;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class RandomGameAction extends ActionImpl {

    private static final long serialVersionUID = -3743050233407643384L;

    private final String value;
    private final int order;
    private int hashCode = -1;

    public RandomGameAction(InformationSet informationSet, String value, int order) {
        super(informationSet);
        this.value = value;
        this.order = order;
    }

    @Override
    public void perform(GameState gameState) {
        if (informationSet != null && !informationSet.getPlayer().equals(gameState.getPlayerToMove())) {
            throw new IllegalArgumentException("Incorrect player.");
        }

        ((RandomGameState)gameState).evaluateAction(this);
    }

    @Override
    public int hashCode() {
        if(hashCode == -1)
            hashCode = new HashCodeBuilder(17,31).append(value).append(order).append(informationSet).toHashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RandomGameAction)) return false;
        if (!super.equals(o)) return false;

        RandomGameAction that = (RandomGameAction) o;

        if (order != that.order) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }
    public String getValue() {
        return value;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public String toString() {
        if (informationSet == null) return "[Pl2, " + value.substring(value.indexOf("_") + 1) + "]";
        return "[" + informationSet.getPlayer() + ", " + informationSet.hashCode() + ", " + value.substring(value.indexOf("_") + 1) + "]";
    }

}
