package com.calsync.sync.jira;

import com.calsync.sync.EventSpec;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@ActiveProfiles("dev")
class JiraMangerTest {
    @Autowired
    private JiraManger jiraManger;
    @Autowired
    private JdbcTemplate jdbc;

    private Properties loadProps() {
        Properties p = new Properties();
        try (InputStream in = JiraMangerTest.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (Exception ignored) {}
        return p;
    }

    private String getenvOrProp(String env, Properties p, String key) {
        String v = System.getenv(env);
        if (v == null || v.trim().isEmpty()) {
            v = p.getProperty(key);
        }
        return v;
    }

    @Test
    void fetch_taskId_real() {
        List<EventSpec> list = jiraManger.fetch(1L);
    }
}
