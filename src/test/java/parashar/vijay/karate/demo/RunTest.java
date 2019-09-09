package parashar.vijay.karate.demo;

import com.intuit.karate.KarateOptions;
import com.intuit.karate.junit4.Karate;
import org.junit.runner.RunWith;

@RunWith(Karate.class)
@KarateOptions(features = {"classpath:features/hello-world.feature", "classpath:features/match.feature"})
public class RunTest {
}
