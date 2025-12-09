package com.calsync.web;

import com.calsync.domain.ServiceType;
import com.calsync.sync.Param;
import com.calsync.sync.ParamsSource;
import com.calsync.sync.jira.JiraParam;
import com.calsync.sync.radicale.RadicaleParam;
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
    private ParamsSource<JiraParam> jiraParamParamsSource;
    @Autowired
    private ParamsSource<RadicaleParam> radicaleParamsSource;
    
    
    @GetMapping("/query")
    public List<Param> getParams(Long taskId, ServiceType serviceType) {
        switch (serviceType) {
            case JIRA:
                return new ArrayList<>(jiraParamParamsSource.getParams(taskId));
            case RADICALE:
                return new ArrayList<>(radicaleParamsSource.getParams(taskId));
            case CUSTOM:
            default:
                return new ArrayList<>();
        }
    }
}
