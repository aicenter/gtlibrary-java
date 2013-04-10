package poker.generic;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ GenericPokerExpanderTest.class, GenericPokerGameStateTest.class, DomainExpanderTest.class })
public class AllTests {

}
