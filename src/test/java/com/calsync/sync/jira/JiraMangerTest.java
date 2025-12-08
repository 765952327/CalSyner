package com.calsync.sync.jira;

import com.calsync.sync.EventSpec;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JiraMangerTest {
    @Autowired
    private JiraManger jiraManger;
    
    @Test
    void fetch() {
        List<EventSpec> list = jiraManger.fetch(1L);
    }
}
