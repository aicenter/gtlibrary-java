package cz.agents.gtlibrary.domain.randomabstraction;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class RandomAbstractionGameState extends GameStateImpl {
    private Map<ISKey, ISKey> abstraction;
    private GameState wrappedGameState;

    public RandomAbstractionGameState(Map<ISKey, ISKey> abstraction, GameState wrappedGameState) {
        super(wrappedGameState.getAllPlayers());
        this.abstraction = abstraction;
        this.wrappedGameState = wrappedGameState.copy();
    }

    public RandomAbstractionGameState(RandomAbstractionGameState gameState) {
        super(gameState);
        this.abstraction = gameState.abstraction;
        this.wrappedGameState = gameState.wrappedGameState.copy();
    }

    @Override
    public void performActionModifyingThisState(Action action) {
        super.performActionModifyingThisState(action);
        wrappedGameState.performActionModifyingThisState(((RandomAbstractionAction)action).wrappedActions.get(wrappedGameState.getISKeyForPlayerToMove()));
//        if (wrappedGameState.isPlayerToMoveNature() || checkConsistency(action)) {
//            try {
//                Method m = GameStateImpl.class.getDeclaredMethod("updateNatureProbabilityFor", Action.class);
//                m.setAccessible(true);
//                m.invoke(wrappedGameState, action);
//                m = GameStateImpl.class.getDeclaredMethod("addActionToHistory", Action.class, Player.class);
//                m.setAccessible(true);
//                m.invoke(wrappedGameState, action, getPlayerToMove());
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//            action.perform(this);
//        } else {
//            throw new IllegalStateException("Inconsistent move.");
//        }
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return wrappedGameState.getProbabilityOfNatureFor(action);
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        return abstraction.get(wrappedGameState.getISKeyForPlayerToMove());
    }

    @Override
    public Player getPlayerToMove() {
        return wrappedGameState.getPlayerToMove();
    }

    @Override
    public GameState copy() {
        return new RandomAbstractionGameState(this);
    }

    @Override
    public double[] getUtilities() {
        return wrappedGameState.getUtilities();
    }

    @Override
    public boolean isGameEnd() {
        return wrappedGameState.isGameEnd();
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return wrappedGameState.isPlayerToMoveNature();
    }

    public GameState getWrappedGameState() {
        return wrappedGameState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RandomAbstractionGameState)) return false;

        RandomAbstractionGameState that = (RandomAbstractionGameState) o;

        return wrappedGameState.equals(that.wrappedGameState);
    }

    @Override
    public int hashCode() {
        return wrappedGameState.hashCode();
    }

    @Override
    public String toString() {
        return history.toString();
    }
}
