package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.bound;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.number.DigitArray;
import cz.agents.gtlibrary.interfaces.Action;

import java.util.Map;

public class UpperBound extends Bound {

    public UpperBound() {
        super();
    }

    public UpperBound(Map<? extends SequenceFormIRInformationSet, ? extends Map<Action, DigitArray>> m) {
        super(m);
    }

    @Override
    public void put(Action action, DigitArray digitArray) {
        if (digitArray.equals(DigitArray.ONE))
            return;
        super.put(action, digitArray);
    }
}
