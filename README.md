# dlock
distributed lock using redis (java/kotlin)

## Assumptions
- The business logic inside the lock should not take more than `LOCK_TIMEOUT`.
- If your business logic is modular and contains tree of function calls, the lock should be called only once - ideally at the core level. 
