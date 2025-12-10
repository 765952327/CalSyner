package com.calsync.sync;

public interface ClientService<T> {
    T getClient(Long serviceId);
    
    boolean test(Long serviceId);
}
