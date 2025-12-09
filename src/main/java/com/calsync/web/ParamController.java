package com.calsync.web;

import com.calsync.domain.ServiceType;
import com.calsync.sync.Param;
import com.calsync.sync.ParamsSource;
import com.calsync.sync.jira.JiraParam;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/param")
public class ParamController {
    
    @Autowired
    private ParamsSource<JiraParam> paramsSource;
    
    
    @GetMapping("/query")
    public List<Param> getParams(Long taskId, ServiceType serviceType) {
        switch (serviceType) {
            case JIRA:
                return new ArrayList<>(paramsSource.getParams(taskId));
            case RADICALE:
            case CUSTOM:
            default:
                return new ArrayList<>();
        }
    }
}
