package com.calsync.sync.radicale;

import biweekly.Biweekly;
import biweekly.ICalendar;
import com.calsync.util.ServiceUtil;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;

/**
 * RadicaleClient：CalDAV/CardDAV 高级封装客户端。
 * <p>
 * 主要职责：
 * - Radicale 服务交互，完成集合资源发现（PROPFIND）、
 *   事件/待办的读取（GET）、创建或更新（PUT）以及删除（DELETE）。
 * - 为调用方提供以 UID 为主键的便捷操作，以及解析 ICS 文本为 ICalendar 对象的能力。
 * <p>
 */
public class RadicaleClient {
    /**
     * HTTP 客户端实例，用于发起与 Radicale 的网络请求。
     */
    private final static OkHttpClient client = new OkHttpClient();

    /**
     * 基础地址（集合路径），用于拼接具体资源 URL；通过 {@link ServiceUtil#ensureUrl(String)} 保证以斜杠结尾。
     */
    private final String baseUrl;

    /**
     * 认证用户名（Basic Auth）。
     */
    private final String username;

    /**
     * 认证密码（Basic Auth）。
     */
    private final String password;

    /**
     * 构造函数：初始化客户端访问信息。
     *
     * @param baseUrl Radicale 集合基础地址，建议指向具体 CalDAV/CardDAV 集合目录
     * @param username 基本认证用户名
     * @param password 基本认证密码
     */
    public RadicaleClient(String baseUrl, String username, String password) {
        this.baseUrl = ServiceUtil.ensureUrl(baseUrl);
        this.username = username;
        this.password = password;
    }

    /**
     * 连接可达性检测。
     * <p>
     * 通过对集合基础地址执行 GET 请求，判断服务是否可访问并返回内容。
     *
     * @return 当返回体非空时为 {@code true}，否则为 {@code false}
     */
    public boolean ping() {
        String body = getIcs(baseUrl);
        return body != null;
    }

    /**
     * 查询集合内所有可解析的 ICalendar 数据。
     * <p>
     * 过程：先通过 PROPFIND 获取集合中所有 `.ics` 资源 URL，再逐一 GET 并解析为 ICalendar。
     *
     * @return ICalendar 列表，若集合为空或无法访问则返回空列表
     */
    public List<ICalendar> queryAll() {
        List<String> urls = queryAllUrl();
        List<ICalendar> out = new ArrayList<>();
        for (String url : urls) {
            List<ICalendar> iCalendars = get(url);
            if (!iCalendars.isEmpty()) {
                out.addAll(iCalendars);
            }
        }
        return out;
    }

    /**
     * 通过 UID 创建或更新单个 ICS 资源。
     *
     * @param uid 事件或待办的唯一标识，用于组成资源文件名（uid.ics）
     * @param iCalendar 需写入的 ICalendar 对象，将序列化为 ICS 文本
     * @return HTTP 状态码，成功通常为 2xx，失败返回具体错误码或 500
     */
    public int upsertByUid(String uid, ICalendar iCalendar) {
        String ics = Biweekly.write(iCalendar).go();
        String url = baseUrl + uid + ".ics";
        return putCalendar(url, ics);
    }

    /**
     * 通过 UID 获取 ICS 文本。
     *
     * @param uid 资源唯一标识（uid.ics）
     * @return ICS 文本；当资源不存在或访问失败时返回 {@code null}
     */
    public String getByUid(String uid) {
        String url = baseUrl + uid + ".ics";
        return getIcs(url);
    }

    /**
     * 通过 UID 删除 ICS 资源。
     *
     * @param uid 资源唯一标识（uid.ics）
     * @return HTTP 状态码，成功通常为 2xx，失败返回具体错误码或 500
     */
    public int deleteByUid(String uid) {
        String url = baseUrl + uid + ".ics";
        return delete(url);
    }

    /**
     * 列出集合中所有 `.ics` 资源的绝对 URL。
     * <p>
     * 使用 WebDAV PROPFIND（Depth=1）获取响应 XML，并解析其中的 `href` 标签。
     * 兼容命名空间形式（如 `<D:href>`），仅保留以 `.ics` 结尾的资源。
     *
     * @return `.ics` 资源 URL 列表；访问失败或无匹配时返回空列表
     */
    private List<String> queryAllUrl() {
        String xml = propfind(baseUrl);
        List<String> result = new ArrayList<>();
        if (xml == null) return result;
        Pattern p = Pattern.compile("<(?:\\w+:)?href>(.*?)</(?:\\w+:)?href>", java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL);
        Matcher m = p.matcher(xml);
        try {
            URL bu = new URL(baseUrl);
            String origin = bu.getProtocol() + "://" + bu.getHost() + (bu.getPort() != -1 ? (":" + bu.getPort()) : "");
            while (m.find()) {
                String href = m.group(1).trim();
                if (!href.endsWith(".ics")) continue;
                String url = href.startsWith("http") ? href : origin + href;
                result.add(url);
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    /**
     * 为请求添加 Basic 认证头。
     *
     * @param b OkHttp `Request.Builder`
     * @return 带有认证头的 `Request.Builder`
     */
    private Request.Builder auth(Request.Builder b) {
        return b.header("Authorization", Credentials.basic(username, password));
    }

    /**
     * GET 指定 URL 并解析为 ICalendar 列表。
     *
     * @param url 资源地址
     * @return 解析得到的 ICalendar 列表；内容为空或解析失败时返回空列表
     */
    private List<ICalendar> get(String url) {
        String ics = getIcs(url);
        if (StringUtils.isBlank(ics)) {
            return new ArrayList<>();
        }
        return Biweekly.parse(ics).all();
    }

    /**
     * GET 指定 URL 并返回 ICS 文本。
     *
     * @param url 资源地址
     * @return ICS 文本；当响应非成功或发生异常时返回 {@code null}
     */
    private String getIcs(String url) {
        Request req = auth(new Request.Builder().url(url).get()).build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) return null;
            return resp.body() != null ? resp.body().string() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * WebDAV PROPFIND：列出集合下的资源。
     *
     * @param base 集合基础地址
     * @return 响应 XML 文本；非成功或异常返回 {@code null}
     */
    private String propfind(String base) {
        RequestBody body = RequestBody.create(MediaType.parse("text/xml"), "<propfind xmlns=\"DAV:\"><allprop/></propfind>");
        Request req = auth(new Request.Builder().url(base).header("Depth", "1").method("PROPFIND", body)).build();
        try (Response resp = client.newCall(req).execute()) {
            if (!resp.isSuccessful()) return null;
            return resp.body() != null ? resp.body().string() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 写入或替换 ICS 资源（PUT）。
     *
     * @param url 目标资源地址
     * @param ics ICS 文本内容
     * @return HTTP 状态码；失败时返回 500
     */
    private int putCalendar(String url, String ics) {
        Request req = auth(new Request.Builder().url(url).put(RequestBody.create(MediaType.parse("text/calendar"), ics))).build();
        try (Response resp = client.newCall(req).execute()) {
            return resp.code();
        } catch (Exception e) {
            return 500;
        }
    }

    /**
     * 删除 ICS 资源（DELETE）。
     *
     * @param url 目标资源地址
     * @return HTTP 状态码；失败时返回 500
     */
    private int delete(String url) {
        Request req = auth(new Request.Builder().url(url).delete()).build();
        try (Response resp = client.newCall(req).execute()) {
            return resp.code();
        } catch (Exception e) {
            return 500;
        }
    }
}
