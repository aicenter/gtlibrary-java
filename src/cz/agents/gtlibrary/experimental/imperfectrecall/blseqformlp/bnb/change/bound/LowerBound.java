package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.bound;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.number.DigitArray;
import cz.agents.gtlibrary.interfaces.Action;

import java.util.Map;

public class LowerBound extends Bound {

    public LowerBound() {
        super();
    }

    public LowerBound(Map<? extends SequenceFormIRInformationSet, ? extends Map<Action, DigitArray>> m) {
        super(m);
    }

    @Override
    public void put(Action action, DigitArray digitArray) {
        if(digitArray.equals(DigitArray.ZERO))
            return;
        super.put(action, digitArray);
    }
}
