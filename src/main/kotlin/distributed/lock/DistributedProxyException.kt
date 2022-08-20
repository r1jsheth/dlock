package distributed.lock

import java.lang.Exception

class DistributedProxyException(cause: Throwable): Exception(cause) {
}