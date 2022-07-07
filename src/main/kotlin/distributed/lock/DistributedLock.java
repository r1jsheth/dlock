package distributed.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    String DEFAULT_KEY = "DEFAULT_KEY";
    // TODO: 30 seconds is this okay?
    int DEFAULT_TIMEOUT = 30;
    TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;

    String key() default DEFAULT_KEY;

    int timeout() default DEFAULT_TIMEOUT;

//    TimeUnit timeoutUnit() default DEFAULT_TIMEOUT_UNIT;
//    ^ line doesn't work! while `TimeUnit.SECONDS` works fine!!

    TimeUnit timeoutUnit() default TimeUnit.SECONDS;

}
