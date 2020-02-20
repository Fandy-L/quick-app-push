package com.chuang.qapp.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * @author fandy.lin
 */
@Component
@Slf4j
public class HttpUtils {
    /**
     * 读取超时时间
     */
    @Value("${socket.time.out:3000}")
    private int soTimeout;

    /**
     * 连接超时时间
     */
    @Value("${connection.time.out:3000}")
    private int connectionTimeout;

    /**
     * 连接请求超时时间
     */
    private int connectionRequestTimeout = 10000;

    public String doGet(String url, Map<String,String> headers) throws IOException {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String result;
        try {
            // 通过址默认配置创建一个httpClient实例
            httpClient = HttpClients.createDefault();
            // 创建httpGet远程连接实例
            HttpGet httpGet = new HttpGet(url);
            Set<String> keySet = headers.keySet();
            for(String key:keySet){
                httpGet.addHeader(key, headers.get(key));
            }
            // 设置配置请求参数
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connectionTimeout)
                    .setConnectionRequestTimeout(connectionRequestTimeout)
                    .setSocketTimeout(soTimeout)
                    .build();
            // 为httpGet实例设置配置
            httpGet.setConfig(requestConfig);
            // 执行get请求得到返回对象
            response = httpClient.execute(httpGet);
            // 通过返回对象获取返回数据
            HttpEntity entity = response.getEntity();
            // 通过EntityUtils中的toString方法将结果转换为字符串
            result = EntityUtils.toString(entity);

            if (response.getStatusLine().getStatusCode() == 200) {
                return result;
            } else {
                log.error("HttpGetException http-get,request={}，状态码:{},内容：{}", url, response.getStatusLine().getStatusCode(), result);
                return null;
            }
        } catch (Exception e) {
            log.warn("HttpGetException http-get执行时出现异常 url = {}, error.e=", url, e);
            throw e;
        } finally {
            // 关闭资源
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.error("HttpGetException http-get关闭response出现异常 url = {}, error.e=", url, e);
                    throw e;
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    log.error("HttpGetException http-get关闭httpClient出现异常 url = {}, error.e = ", url,
                            e);
                    throw e;
                }
            }
        }
    }

    public String doPost(String url, String requestBody) throws IOException {
        CloseableHttpClient httpClient;
        CloseableHttpResponse httpResponse = null;
        String result;
        // 创建httpClient实例
        httpClient = HttpClients.createDefault();
        // 创建httpPost远程连接实例
        HttpPost httpPost = new HttpPost(url);
        // 配置请求参数实例
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connectionTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .setSocketTimeout(soTimeout)
                .build();
        // 为httpPost实例设置配置
        httpPost.setConfig(requestConfig);
        // 设置请求头
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        // 为httpPost设置封装好的请求参数
        httpPost.setEntity(new StringEntity(requestBody, "UTF-8"));
        try {
            // httpClient对象执行post请求,并返回响应参数对象
            httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                log.error("HttpPostException http-post连接服务出现异常 url = {}", url);
            }
            // 从响应对象中获取响应内容
            HttpEntity entity = httpResponse.getEntity();
            result = EntityUtils.toString(entity);
        } catch (Exception e) {
            log.error("HttpPostException http-post执行时出现异常 url = {}, error.e = ", url, e);
            throw e;
        } finally {
            // 关闭资源
            if (null != httpResponse) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    log.error("HttpPostException http-post关闭httpResponse出现异常 url = {}, error.e = ",
                            url, e);
                    throw e;
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    log.error("HttpPostException http-post关闭httpClient出现异常 url = {}, error.e = ", url,
                            e);
                    throw e;
                }
            }
        }
        return result;
    }

}
