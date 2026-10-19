package com.chen.blog.common.util;

import com.alibaba.fastjson.JSON;
import eu.bitwalker.useragentutils.UserAgent;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

/**
 * ip工具类
 *
 * @author chenfuyun
 */
@SuppressWarnings("all")
public class IpUtils {

    /**
     * 归属地解析接口连接超时时间(毫秒)
     */
    private static final int IP_SOURCE_CONNECT_TIMEOUT = 1000;

    /**
     * 归属地解析接口读取超时时间(毫秒)
     */
    private static final int IP_SOURCE_READ_TIMEOUT = 1500;

    /**
     * 获取用户ip地址
     *
     * @param request 请求
     * @return ip地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = null;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if ("127.0.0.1".equals(ipAddress)) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    ipAddress = inet.getHostAddress();
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) {
                // = 15
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress = "";
        }
        return ipAddress;
    }

    /**
     * 解析ip地址
     *
     * @param ipAddress ip地址
     * @return 解析后的ip地址
     */
    public static String getIpSource(String ipAddress) {
        // 内网/本机地址无需查询,直接跳过以避免无谓的同步外网调用
        if (isInnerIp(ipAddress)) {
            return "";
        }
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http://opendata.baidu.com/api.php?query=" + ipAddress + "&co=&resource_id=6006&oe=utf8");
            connection = (HttpURLConnection) url.openConnection();
            // 归属地仅为日志展示用的附属信息,设置超时避免解析接口异常时拖慢业务请求
            connection.setConnectTimeout(IP_SOURCE_CONNECT_TIMEOUT);
            connection.setReadTimeout(IP_SOURCE_READ_TIMEOUT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line = null;
            StringBuffer result = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
            Map map = JSON.parseObject(result.toString(), Map.class);
            List<Map<String, String>> data = (List) map.get("data");
            return data.get(0).get("location");
        } catch (Exception e) {
            return "";
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 判断是否为空或内网地址
     *
     * @param ipAddress ip地址
     * @return true 表示为空或内网地址
     */
    private static boolean isInnerIp(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return true;
        }
        String ip = ipAddress.trim();
        if ("localhost".equalsIgnoreCase(ip) || "0:0:0:0:0:0:0:1".equals(ip) || ip.startsWith("0:")) {
            return true;
        }
        if (ip.startsWith("127.") || ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("169.254.")) {
            return true;
        }
        // 172.16.0.0 ~ 172.31.255.255
        if (ip.startsWith("172.")) {
            String[] segments = ip.split("\\.");
            if (segments.length > 1) {
                try {
                    int second = Integer.parseInt(segments[1]);
                    return second >= 16 && second <= 31;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 获取访问设备
     *
     * @param request 请求
     * @return {@link UserAgent} 访问设备
     */
    public static UserAgent getUserAgent(HttpServletRequest request){
        return UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
    }

}
