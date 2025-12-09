package com.calsync.sync.jira;

import com.calsync.domain.SyncTask;
import com.calsync.service.SyncTaskService;
import java.util.List;
import net.rcarz.jiraclient.JiraException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
//@ActiveProfiles("dev")
class JiraMangerTest {
    @Autowired
    private JiraManger jiraManger;
    @Autowired
    private SyncTaskService syncTaskService;
    
    @Test
    void fetch() throws JiraException {
        SyncTask task = syncTaskService.getTask(3L);
    }
}
