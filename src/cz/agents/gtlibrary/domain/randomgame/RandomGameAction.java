package cz.agents.gtlibrary.domain.randomgame;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class RandomGameAction extends ActionImpl{

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
        if (!informationSet.getPlayer().equals(gameState.getPlayerToMove())) {
            throw new IllegalArgumentException("Incorrect player.");
        }

        ((RandomGameState)gameState).evaluateAction(this);
    }

    @Override
    public int hashCode() {
        if(hashCode == -1)
            hashCode = new HashCodeBuilder(17,31).append(value).append(informationSet).toHashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;

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
        StringBuilder builder = new StringBuilder();
        builder.append("[" + informationSet.getPlayer() + ", " + informationSet.hashCode() + ", ");
        builder.append(value.substring(value.indexOf("_")+1));
        builder.append("]");
        return builder.toString();
    }

}
