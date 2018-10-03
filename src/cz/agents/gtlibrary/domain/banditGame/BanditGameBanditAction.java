package cz.agents.gtlibrary.domain.banditGame;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by kail on 11/11/17.
 */
public class BanditGameBanditAction extends ActionImpl {

    public enum BanditActionType {INIT, RELOCATE, NOA};
    private BanditActionType type;
    private int toRow = -1;
    private int toCol = -1;
    private int fromRow = -1;
    private int fromCol = -1;

    private int hashCode = -1;

    public BanditGameBanditAction(InformationSet informationSet) {
        super(informationSet);
        this.type = BanditActionType.NOA;
    }

    public BanditGameBanditAction(InformationSet informationSet, int fromRow, int fromCol, int toRow, int toCol) {
        super(informationSet);
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toCol = toCol;
        this.toRow = toRow;
        this.type = BanditActionType.RELOCATE;
    }

    public BanditGameBanditAction(InformationSet informationSet, int toRow, int toCol) {
        super(informationSet);
        this.toCol = toCol;
        this.toRow = toRow;
        this.type = BanditActionType.INIT;
    }

    @Override
    public void perform(GameState gameState) {
        ((BanditGameState)gameState).executeBanditAction(this);
    }

    @Override
    public int hashCode() {
        if(hashCode == -1) {
            hashCode = (new HashCodeBuilder(17,31)).append((informationSet == null) ? 0 : informationSet).append(type).append(toRow).append(toCol).append(fromRow).append(fromCol).toHashCode();
        }
        return hashCode;
    }

    public BanditActionType getType() {
        return type;
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
        if (type == BanditActionType.INIT) {
            return "BA[" + toRow + ',' + toCol + "]";
        } else if (type == BanditActionType.RELOCATE) {
            return "BA[" + fromRow + "," + fromCol + " -> " + toRow + ',' + toCol + "]";
        } else if (type == BanditActionType.NOA) {
            return "BA[]";
        } else return "";

    }
}
