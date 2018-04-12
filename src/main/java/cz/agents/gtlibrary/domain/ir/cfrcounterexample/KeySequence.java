package cz.agents.gtlibrary.domain.ir.cfrcounterexample;

import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class KeySequence extends ArrayListSequenceImpl {
    public KeySequence(Player player) {
        super(player);
    }

    public KeySequence(Sequence sequence) {
        super(sequence);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof KeySequence))
            return false;
        KeySequence other = (KeySequence) obj;

        if (super.equals(other))
            return true;
        if (isEmpty()) {
            if (other.isEmpty()) {
                return true;
            } else {
                CCAction lastAction = (CCAction) other.getLast();

                return other.size() == 2 && (lastAction.getActionType().equals("u") || lastAction.getActionType().equals("v"));
            }
        }
        if (other.isEmpty()) {
            CCAction lastAction = (CCAction) getLast();

            return size() == 2 && (lastAction.getActionType().equals("u") || lastAction.getActionType().equals("v"));
        }
        return false;
    }


}
