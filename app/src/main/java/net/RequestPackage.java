package net;

import android.content.Context;

import java.net.URL;

/**
 * 网络请求包
 */
public class RequestPackage {
    private Context context = null;
    private String requestName = "";
    private byte[] postData = null;
    private OnResponseListener onResponseListener = null;
    private URL url = null;
    private String uuid = "";
    private Object tag = null;

    public RequestPackage(Context context) {
        this.context = context;
    }

    /**
     * 上下文对象
     */
    public Context getContext() {
        return context;
    }

    /**
     * 上下文对象
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * 网络请求地址
     */
    public URL getUrl() {
        return url;
    }

    /**
     * 网络请求地址
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    /**
     * 请求接口名称
     */
    public String getRequestName() {
        return requestName;
    }

    /**
     * 请求接口名称
     */
    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    /**
     * 发送的JSON数据
     */
    public byte[] getPostData() {
        return postData;
    }

    /**
     * 发送的JSON数据
     */
    public void setPostData(byte[] postData) {
        this.postData = postData;
    }

    /**
     * 结果返回监听
     */
    public OnResponseListener getOnResponseListener() {
        return onResponseListener;
    }

    /**
     * 结果返回监听
     */
    public void setOnResponseListener(
            OnResponseListener onResponseListener) {
        this.onResponseListener = onResponseListener;
    }

    /**
     * 标签（存放异步服务请求对象）
     */
    public Object getTag() {
        return tag;
    }

    /**
     * 标签（存放异步服务请求对象）
     */
    public void setTag(Object tag) {
        this.tag = tag;
    }

    /**
     * 接口请求ID(用来区分同名接口的哪一次请求)
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * 接口请求ID(用来区分同名接口的哪一次请求)
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
