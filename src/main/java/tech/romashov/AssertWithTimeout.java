package tech.romashov;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vladimir Popov
 */
public class AssertWithTimeout {
    private static final Logger logger = LoggerFactory.getLogger(AssertWithTimeout.class);
    private static Duration delay = Duration.ofMillis(300);

    public static <T> T assertThat(Callable<T> getter, Matcher<? super T> matcher, Duration timeout) throws Throwable {
        return assertThat("", getter, matcher, timeout);
    }


    public static <T> T assertThat(Callable<T> getter, Matcher<? super T> matcher, Duration timeout, Duration delay) throws Throwable {
        return assertThat("", getter, matcher, timeout, delay);
    }

    public static <T> T assertThat(String reason, Callable<T> getter, Matcher<? super T> matcher, Duration timeout) throws Throwable {
        return assertThat(reason, getter, matcher, timeout, AssertWithTimeout.delay);
    }

    /**
     * Утверждает, что выражение, возвращенное {@code getter}-ом, максимум  за время {@code timeout}, будет соответствовать {@code matcher}-у.
     *
     * @param reason  комментарий на случай провала утверждения.
     * @param getter  инструмент для получения текущего значения проверяемой величины.
     * @param matcher описание требования к проверяемой величине.
     * @param timeout максимальное время ожидания "ожидаемого" значения в миллисекундах.
     * @param delay   период перезапроса значения проверяемой величины в миллисекундах.
     * @param <T>     тип проверяемого значения.
     * @throws Throwable
     */
    public static <T> T assertThat(String reason, Callable<T> getter, Matcher<? super T> matcher, Duration timeout, Duration delay) throws Throwable {
        T actual = waitAndGetActualValue(getter, matcher, timeout, delay);
        if (!matcher.matches(actual)) {
            Description description = new StringDescription();
            description.appendText(reason)
                    .appendText("\nExpected: ")
                    .appendDescriptionOf(matcher)
                    .appendText("\n but: ");
            matcher.describeMismatch(actual, description);
            throw new AssertionError(description.toString());
        }
        return actual;
    }

    private static <T> T waitAndGetActualValue(Callable<T> getter, Matcher<? super T> matcher, Duration timeout, Duration delay) throws Throwable {
        Instant begin = Instant.now();
        T value = getter.call();
        while (Duration.between(begin, Instant.now()).toMillis() < timeout.toMillis() && !matcher.matches(value)) {
            try {
                Thread.sleep(delay.toMillis());
            } catch (InterruptedException e) {
                logger.warn(e.getMessage(), e);
            }
            value = getter.call();
        }
        return value;
    }
}
