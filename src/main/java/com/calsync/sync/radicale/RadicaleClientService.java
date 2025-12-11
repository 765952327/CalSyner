package com.calsync.sync.radicale;

import com.calsync.domain.ServiceConfig;
import com.calsync.service.ServiceConfigService;
import com.calsync.sync.ClientService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;

public class RadicaleClientService implements ClientService<RadicaleClient> {
    private static final Map<Long, RadicaleClient> clientMap = new ConcurrentHashMap<>();
    @Autowired
    private ServiceConfigService serviceConfigService;
    
    @Override
    public RadicaleClient getClient(Long serviceId) {
        RadicaleClient radicaleClient = clientMap.get(serviceId);
        if (radicaleClient != null) {
            return radicaleClient;
        }
        ServiceConfig config = serviceConfigService.getConfig(serviceId);
        radicaleClient = new RadicaleClient(config.getBaseUrl(), config.getUsername(), config.getPassword());
        clientMap.put(serviceId, radicaleClient);
        return radicaleClient;
    }
    
    @Override
    public boolean test(Long serviceId) {
        RadicaleClient radicaleClient = getClient(serviceId);
        return radicaleClient.ping();
    }
}
