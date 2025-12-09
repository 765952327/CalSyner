package com.calsync.sync.jira;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.Resource;
import net.rcarz.jiraclient.RestClient;
import net.sf.json.JSON;

@Data
public class JiraClientWrap {
    private JiraClient jiraClient;
    private RestClient restclient;
    private Map<String,JiraParam> paramsMap;
    
    public JiraClientWrap(JiraClient jiraClient) throws JiraException {
        this.jiraClient = jiraClient;
        this.restclient = jiraClient.getRestClient();
        List<JiraParam> params = getAllFields();
        if (params != null) {
            params.forEach(param -> {
                paramsMap.put(param.getId(), param);
            });
        }
    }
    
    public List<JiraParam> getParams() {
        return new ArrayList<>(paramsMap.values());
    }
    
    public JiraParam getParam(String paramId) {
        return paramsMap.get(paramId);
    }
    
    public IssuesWrap searchIssues(String jql)
            throws JiraException {
        Issue.SearchResult result = Issue.search(restclient, jql, null, null);
        return new IssuesWrap(result.issues);
    }
    
    public List<JiraParam> getAllFields() throws JiraException {
        try {
            URI uri = restclient.buildURI(Resource.getBaseUri() + "field");
            JSON response = restclient.get(uri);
            return JSONUtil.toBean(JSONUtil.toJsonStr(response), new TypeReference<List<JiraParam>>() {
            }, true);
        } catch (Exception ex) {
            throw new JiraException(ex.getMessage(), ex);
        }
    }
    
}
