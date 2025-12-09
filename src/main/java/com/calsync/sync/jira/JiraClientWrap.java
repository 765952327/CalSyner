package com.calsync.sync.jira;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.Resource;
import net.rcarz.jiraclient.RestClient;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JiraClientWrap {
    private JiraClient jiraClient;
    private RestClient restclient;
    
    public JiraClientWrap(JiraClient jiraClient) {
        this.jiraClient = jiraClient;
        this.restclient = jiraClient.getRestClient();
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
