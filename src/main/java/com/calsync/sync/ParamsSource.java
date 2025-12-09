package com.calsync.sync;

import com.calsync.sync.jira.JiraParam;
import java.util.List;
import net.rcarz.jiraclient.JiraException;

/**
 * 参数源
 */
public interface ParamsSource {
    List<JiraParam> getParams(Long taskId);
}
