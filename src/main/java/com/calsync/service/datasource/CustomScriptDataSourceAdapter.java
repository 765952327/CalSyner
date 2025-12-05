package com.calsync.service.datasource;

import com.calsync.domain.ServiceConfig;
import com.calsync.domain.SyncTask;
import com.calsync.web.dto.FieldMappingDTO;
import com.calsync.sync.custom.CustomScript;
import com.calsync.sync.EventSpec;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;

/**
 * 自定义脚本数据源：运行用户上传的 Java 源码以生成事件。
 */
public class CustomScriptDataSourceAdapter implements DataSourceAdapter {
    @Override
    public List<EventSpec> fetch(ServiceConfig srcCfg, SyncTask task, List<FieldMappingDTO> mappings) {
        if (srcCfg == null) return Collections.emptyList();
        String code = srcCfg.getApiToken();
        if (code == null || code.trim().isEmpty()) return Collections.emptyList();
        try {
            File tmpDir = new File(System.getProperty("java.io.tmpdir"), "jirasync-scripts");
            tmpDir.mkdirs();
            File src = new File(tmpDir, "CustomUserScript.java");
            try (FileWriter fw = new FileWriter(src)) { fw.write(code); }
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) return Collections.emptyList();
            int res = compiler.run(null, null, null, src.getAbsolutePath());
            if (res != 0) return Collections.emptyList();
            URLClassLoader cl = new URLClassLoader(new URL[]{tmpDir.toURI().toURL()}, Thread.currentThread().getContextClassLoader());
            Class<?> k = Class.forName("CustomUserScript", true, cl);
            Object o = k.getDeclaredConstructor().newInstance();
            if (!(o instanceof CustomScript)) return Collections.emptyList();
            CustomScript script = (CustomScript) o;
            return script.run(srcCfg, task, mappings);
        } catch (Throwable ignored) {}
        return Collections.emptyList();
    }
}
