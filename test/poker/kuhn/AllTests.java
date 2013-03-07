package poker.kuhn;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ GameStateTest.class, KPExpanderTest.class, KPGameInfoTest.class })
public class AllTests {

}
