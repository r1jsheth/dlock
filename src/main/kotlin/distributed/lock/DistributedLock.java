package distributed.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    String DEFAULT_KEY = "DEFAULT_KEY";
    // TODO: 30 seconds is this okay?
    int DEFAULT_TIMEOUT = 30;

    String key() default DEFAULT_KEY;

    int timeout() default DEFAULT_TIMEOUT;

}
