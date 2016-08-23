package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.bound;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.number.DigitArray;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.HashMap;
import java.util.Map;

public class Bound extends HashMap<SequenceFormIRInformationSet, Map<Action, DigitArray>> {

    public Bound() {
        super();
    }

    public Bound(Map<? extends SequenceFormIRInformationSet, ? extends Map<Action, DigitArray>> m) {
        super(m);
    }

    public DigitArray get(Action action) {
        Map<Action, DigitArray> actionMap = get(action.getInformationSet());

        if(actionMap == null)
            return null;
        return actionMap.get(action);
    }

    public DigitArray getOrDefault(Action action, DigitArray defaultValue) {
        Map<Action, DigitArray> actionMap = get(action.getInformationSet());

        if(actionMap == null)
            return defaultValue;
        return actionMap.getOrDefault(action, defaultValue);
    }

    public void put(Action action, DigitArray digitArray) {
        Map<Action, DigitArray> actionMap = get(action.getInformationSet());

        if(actionMap == null)
            actionMap = new HashMap<>();
        actionMap.put(action, digitArray);
        put((SequenceFormIRInformationSet) action.getInformationSet(), actionMap);
    }

}
