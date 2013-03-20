package strategy;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ FirstActionStrategyForMissingSequencestest.class, UniformStrategyForMissingSequencesTest.class })
public class AllTests {

}
