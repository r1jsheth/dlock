package distributed.lock.repository;

import org.springframework.context.annotation.Bean;
import org.springframework.integration.support.locks.LockRegistry;

public interface LockRepository {

    @Bean
    LockRegistry getLockRegistry();

}

