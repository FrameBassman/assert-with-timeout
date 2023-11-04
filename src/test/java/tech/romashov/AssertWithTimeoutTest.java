package tech.romashov;


import org.junit.Test;

import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class AssertWithTimeoutTest {
    @Test
    public void assertWithTimeoutShouldBeWithSpecifiedTimeout() throws Throwable {
        Instant begin = Instant.now();
        Duration testTimeout = Duration.ofSeconds(1);
        try {
            AssertWithTimeout.assertThat(() -> 1, equalTo(2), testTimeout);
        } catch (AssertionError error) {
            // nothing to do
        }
        assertThat(Duration.between(begin, Instant.now()), greaterThanOrEqualTo(testTimeout));
    }

    @Test
    public void assertWithTimeoutShouldBeWithSpecifiedFailureMessage() throws Throwable {
        Duration testTimeout = Duration.ofSeconds(1);
        AssertionError catchedError = null;
        try {
            AssertWithTimeout.assertThat("reason", () -> 1, equalTo(2), testTimeout);
        } catch (AssertionError error) {
            catchedError = error;
        }
        assertThat(catchedError, not(nullValue()));
        assertThat(catchedError.getMessage(), equalTo("reason\nExpected: <2>\n but: was <1>"));
    }
}
