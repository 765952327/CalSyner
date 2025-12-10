package com.calsync.web;

import com.calsync.domain.ServiceType;
import com.calsync.service.ParamService;
import com.calsync.sync.Param;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/param")
public class ParamController {
    @Autowired
    private ParamService paramService;
    
    @GetMapping("/query")
    public List<Param> getParams(Long taskId, ServiceType serviceType) {
        return paramService.getParams(taskId, serviceType);
    }
}
