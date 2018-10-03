package cz.agents.gtlibrary.domain.banditGame;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by kail on 11/11/17.
 */
public class BanditGameAgentAction extends ActionImpl {

    private int toRow = -1;
    private int toCol = -1;
    private int fromRow = -1;
    private int fromCol = -1;

    private int hashCode = -1;


    public BanditGameAgentAction(InformationSet informationSet, int fromRow, int fromCol, int toRow, int toCol) {
        super(informationSet);
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toCol = toCol;
        this.toRow = toRow;
    }

    @Override
    public void perform(GameState gameState) {
        ((BanditGameState)gameState).executeAgentAction(this);
    }

    @Override
    public int hashCode() {
        if (hashCode == -1) {
            hashCode = (new HashCodeBuilder(17, 31)).append((informationSet == null) ? 0 : informationSet).append(toRow).append(toCol).append(fromRow).append(fromCol).toHashCode();
        }
        return hashCode;
    }

    public int getToRow() {
        return toRow;
    }

    public int getToCol() {
        return toCol;
    }

    public int getFromRow() {
        return fromRow;
    }

    public int getFromCol() {
        return fromCol;
    }

    @Override
    public String toString() {
        return "AA[" + fromRow + ',' + fromCol  + " -> " + toRow + ',' + toCol + "]";
    }
}
