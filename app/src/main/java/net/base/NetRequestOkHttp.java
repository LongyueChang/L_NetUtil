package net.base;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import net.OnResponseListener;
import net.RequestPackage;
import net.ResponseResult;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 通过OkHttp框架进行网络请求
 * https://search.maven.org/remote_content?g=com.squareup.okhttp3&a=okhttp&v=LATEST
 * https://search.maven.org/remote_content?g=com.squareup.okio&a=okio&v=LATEST
 */
public class NetRequestOkHttp {
    private static OkHttpClient mHttpClient = null;
    private static OkHttpClient mHttpClientSync = null;
    private String LOG_TAG = "";
    private int timeout = 10000;
    private String url = "";
    //Handler 用于向主线程抛送信息
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private List<RequestPackage> reqList = null;

    /**
     * 超时时间
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * 超时时间
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
        if (mHttpClient != null && timeout != mHttpClient.connectTimeoutMillis()) {
            mHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(this.timeout, TimeUnit.MILLISECONDS)
                    .readTimeout(this.timeout, TimeUnit.MILLISECONDS)
                    .writeTimeout(this.timeout, TimeUnit.MILLISECONDS)
                    .build();

            mHttpClientSync = mHttpClient.newBuilder().build();
        }
    }

    /**
     * 网络地址
     */
    public String getUrl() {
        return url;
    }

    /**
     * 网络地址
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 网络请求类构造函数
     *
     * @param logTag Logcat日志的标签
     */
    public NetRequestOkHttp(String logTag) {
        this.LOG_TAG = logTag;
        this.url = "";
        reqList = new ArrayList<>();
        reqList.clear();
        if (mHttpClient == null) {
            mHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(this.timeout, TimeUnit.MILLISECONDS)
                    .readTimeout(this.timeout, TimeUnit.MILLISECONDS)
                    .writeTimeout(this.timeout, TimeUnit.MILLISECONDS)
                    .build();

            mHttpClientSync = mHttpClient.newBuilder().build();
        }
    }

    /**
     * 网络请求类构造函数
     *
     * @param logTag Logcat日志的标签
     * @param url    网络地址
     */
    public NetRequestOkHttp(String logTag, String url) {
        this.LOG_TAG = logTag;
        this.url = url;
        reqList = new ArrayList<>();
        reqList.clear();
        if (mHttpClient == null) {
            mHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(this.timeout, TimeUnit.MILLISECONDS)
                    .readTimeout(this.timeout, TimeUnit.MILLISECONDS)
                    .writeTimeout(this.timeout, TimeUnit.MILLISECONDS)
                    .build();

            mHttpClientSync = mHttpClient.newBuilder().build();
        }
    }

    /**
     * 网络请求类构造函数
     *
     * @param logTag  Logcat日志的标签
     * @param url     网络地址
     * @param timeout 超时时间（毫秒）
     */
    public NetRequestOkHttp(String logTag, String url, int timeout) {
        this.LOG_TAG = logTag;
        this.url = url;
        this.timeout = timeout;
        reqList = new ArrayList<>();
        reqList.clear();
        if (mHttpClient == null) {
            mHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(this.timeout, TimeUnit.MILLISECONDS)
                    .readTimeout(this.timeout, TimeUnit.MILLISECONDS)
                    .writeTimeout(this.timeout, TimeUnit.MILLISECONDS)
                    .build();

            mHttpClientSync = mHttpClient.newBuilder().build();
        }
    }

    /**
     * 创建请求数据包
     *
     * @param context            上下文对象
     * @param requestName        请求接口名称(用于在请求队列中区分是哪个接口)
     * @param postData           请求参数（JSON数据）
     * @param onResponseListener 结果返回监听
     * @param hadUuid            是否创建唯一ID号
     * @return 请求数据包实例对象
     */
    public RequestPackage createRequest(Context context, String requestName,
                                        String postData, OnResponseListener onResponseListener, boolean hadUuid) {
        StringBuilder sb = new StringBuilder();
        sb.append("request=?");
        RequestPackage req = new RequestPackage(context);
        if (hadUuid)
            req.setUuid(java.util.UUID.randomUUID().toString());
        else
            req.setUuid("0");
        req.setRequestName(requestName);
        try {
            req.setPostData(sb.toString().getBytes("UTF-8"));
        } catch (Exception e) {
            Log.d(LOG_TAG, "RequestPackage createRequest Exception:"
                    + e.getMessage());
            req.setPostData(null);
        }
        try {
            req.setUrl(new URL(this.url));
        } catch (Exception e) {
            Log.d(LOG_TAG, "RequestPackage createRequest Exception:"
                    + e.getMessage());
            req.setPostData(null);
        }
        req.setOnResponseListener(onResponseListener);

        return req;
    }

    /**
     * 取消请求
     *
     * @param requestName 需要取消请求的接口名
     * @param uuid        接口的UUID信息
     * @return true为成功, false为失败
     */
    public boolean cancelRequest(String requestName, String uuid) {
        if (requestName == null || requestName.equals("") || uuid == null || uuid.equals("")) {
            return false;
        } else {
            try {
                for (int i = 0; i < reqList.size(); i++) {
                    if (reqList.get(i).getRequestName().equalsIgnoreCase(requestName)
                            && reqList.get(i).getUuid().equalsIgnoreCase(uuid)) {
                        if (reqList.get(i).getTag() != null) {
                            Call req = (Call) reqList.get(i).getTag();
                            reqList.remove(i);
                            if (!req.isCanceled())
                                req.cancel();
                        }
                    }
                }
                if (mHttpClient != null && reqList.size() < 1) {
                    mHttpClient.dispatcher().cancelAll();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * 请求网络接口
     *
     * @param pkg 网络请求数据包
     * @return 请求成功与否（返回空说明发送失败）
     */
    public String postRequest(RequestPackage pkg) {
        if (pkg == null) {
            Log.d(LOG_TAG, "postRequest pkg is null");
            return null;
        }
        return postRequest(pkg, timeout);
    }

    /**
     * 请求网络接口
     *
     * @param pkg     网络请求数据包
     * @param timeout 请求超时时间
     * @return 请求成功与否（返回空说明发送失败）
     */
    public String postRequest(final RequestPackage pkg, int timeout) {
        if (pkg == null)
            return null;
        if (pkg.getUrl() == null)
            return null;
        if (pkg.getOnResponseListener() == null)
            return null;
        if (mHttpClient == null) {
            mHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                    .readTimeout(timeout, TimeUnit.MILLISECONDS)
                    .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                    .build();
        }
        for (int i = 0; i < reqList.size(); i++) {
            if (reqList.get(i).getRequestName().equalsIgnoreCase(pkg.getRequestName())
                    && reqList.get(i).getUuid().equalsIgnoreCase(pkg.getUuid())) {
                Log.e(LOG_TAG, "postRequest 错误！相同请求已经存在。");
                return null;
            }
        }

        //POST方式
        Request request;
        try {
            request = new Request.Builder().url(this.url)
                    .header("Accept", "text/xml")
                    .addHeader("Connection", "close")//默认为Keep-Alive
                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=utf-8"), new String(pkg.getPostData())))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "OkHttp Request Builder Exception:" + (e.getMessage() == null ? e.toString() : e.getMessage()));
            return null;
        }

        Call req = mHttpClient.newCall(request);
        pkg.setTag(req);
        reqList.add(pkg);

        req.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String error = null;
                boolean isNotBack = true;
                if (e != null) {
                    if (e.getCause() instanceof SocketTimeoutException
                            || e.toString().contains("java.net.SocketTimeoutException")) {
                        //李发根 2017.5.17  由于实际测试发现连接超时e.getCause()的值是null，所以增加这个字符判断，以免返回纯英文错误描述
                        error = "连接超时" + (e.getMessage() != null ? ("\n" + e.getMessage()) : "");
                    } else if (e.getCause() instanceof ConnectException
                            || e.toString().contains(("java.net.ConnectException"))) {
                        //李发根 2017.5.17  增加连接失败错误描述
                        error = "连接失败" + (e.getMessage() != null ? ("\n" + e.getMessage()) : "");
                    } else {
                        if (e.getMessage() != null)
                            error = e.getMessage();
                        else
                            error = e.toString();
                        //李发根 2017.5.17  替换纯英文错误描述
                        if (error.contains("Unexpected status line: <html><head><meta HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=gb2312\" />")) {
                            error = "网络发生阻塞\n" + error;
                        }
                    }
                }
                if (error != null) {
                    Log.e(LOG_TAG, "接口" + pkg.getRequestName() + "异常：" + error, e);
                } else {
                    error = "未知错误：" + call.toString();
                }
                if (reqList.contains(pkg)) {
                    try {
                        reqList.remove(pkg);// 目的：支持递归
                        if (pkg.getOnResponseListener() != null) {
                            isNotBack = false;
                            if (Looper.myLooper() != Looper.getMainLooper()) {
                                final String errStr = error;
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            pkg.getOnResponseListener().onResponse(-1, errStr, "", pkg.getUuid());
                                        } catch (Exception exc) {
                                            Log.d(LOG_TAG, pkg.getRequestName() + " Request back Exception:"
                                                    + exc.getMessage().trim());
                                        } finally {
                                            pkg.setContext(null);
                                            pkg.setOnResponseListener(null);
                                            pkg.setPostData(null);
                                            pkg.setTag(null);
                                            pkg.setUrl(null);
                                        }
                                    }
                                });
                            } else {
                                try {
                                    pkg.getOnResponseListener().onResponse(-1, error, "", pkg.getUuid());
                                } catch (Exception exc) {
                                    Log.d(LOG_TAG, pkg.getRequestName() + " Request back Exception:"
                                            + exc.getMessage().trim());
                                } finally {
                                    pkg.setContext(null);
                                    pkg.setOnResponseListener(null);
                                    pkg.setPostData(null);
                                    pkg.setTag(null);
                                    pkg.setUrl(null);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        Log.e(LOG_TAG, pkg.getRequestName() + " Request back Exception:"
                                + ex.getMessage().trim());
                    }
                }
                if (isNotBack) {
                    //结果因某种原因未反馈给前端
                    pkg.setContext(null);
                    pkg.setOnResponseListener(null);
                    pkg.setPostData(null);
                    pkg.setTag(null);
                    pkg.setUrl(null);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean isNotBack = true;
                if (reqList.contains(pkg)) {
                    try {
                        reqList.remove(pkg);// 目的：支持递归
                        if (pkg.getOnResponseListener() != null) {
                            isNotBack = false;
                            if (response.isSuccessful()) {
                                final String data = response.body().string();
                                if (Looper.myLooper() != Looper.getMainLooper()) {
                                    mainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                pkg.getOnResponseListener().onResponse(0, "", data, pkg.getUuid());
                                            } catch (Exception exc) {
                                                Log.e(LOG_TAG, pkg.getRequestName() + " Request back Exception:"
                                                        + exc.getMessage().trim());
                                            } finally {
                                                pkg.setContext(null);
                                                pkg.setOnResponseListener(null);
                                                pkg.setPostData(null);
                                                pkg.setTag(null);
                                                pkg.setUrl(null);
                                            }
                                        }
                                    });
                                } else {
                                    try {
                                        pkg.getOnResponseListener().onResponse(0, "", data, pkg.getUuid());
                                    } catch (Exception exc) {
                                        Log.e(LOG_TAG, pkg.getRequestName() + " Request back Exception:"
                                                + exc.getMessage().trim());
                                    } finally {
                                        pkg.setContext(null);
                                        pkg.setOnResponseListener(null);
                                        pkg.setPostData(null);
                                        pkg.setTag(null);
                                        pkg.setUrl(null);
                                    }
                                }
                            } else {
                                final String error = "请求失败：(" + String.valueOf(response.code()) + ":" + response.message() + ")";
                                if (Looper.myLooper() != Looper.getMainLooper()) {
                                    mainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                pkg.getOnResponseListener().onResponse(-1, error, "", pkg.getUuid());
                                            } catch (Exception exc) {
                                                Log.e(LOG_TAG, pkg.getRequestName() + " Request back Exception:"
                                                        + exc.getMessage().trim());
                                            } finally {
                                                pkg.setContext(null);
                                                pkg.setOnResponseListener(null);
                                                pkg.setPostData(null);
                                                pkg.setTag(null);
                                                pkg.setUrl(null);
                                            }
                                        }
                                    });
                                } else {
                                    try {
                                        pkg.getOnResponseListener().onResponse(-1, error, "", pkg.getUuid());
                                    } catch (Exception exc) {
                                        Log.e(LOG_TAG, pkg.getRequestName() + " Request back Exception:"
                                                + exc.getMessage().trim());
                                    } finally {
                                        pkg.setContext(null);
                                        pkg.setOnResponseListener(null);
                                        pkg.setPostData(null);
                                        pkg.setTag(null);
                                        pkg.setUrl(null);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, pkg.getRequestName() + " Request back Exception:"
                                + ((e.getMessage() == null) ? e.toString() : e.getMessage().trim()));
                    }
                }
                if (isNotBack) {
                    //结果因某种原因未反馈给前端
                    pkg.setContext(null);
                    pkg.setOnResponseListener(null);
                    pkg.setPostData(null);
                    pkg.setTag(null);
                    pkg.setUrl(null);
                }
            }
        });

        return pkg.getUuid();
    }

    /**
     * 同步发送数据请求(不能在主线程上调用)
     *
     * @param postData 发送的数据
     * @return 请求结果
     */
    public ResponseResult postRequest(String postData) {
        return postRequest(postData, this.timeout);
    }

    /**
     * 同步发送数据请求(不能在主线程上调用)
     *
     * @param postData 发送的数据
     * @param timeout  超时时间（毫秒）
     * @return 请求结果
     */
    public ResponseResult postRequest(final String postData, int timeout) {
        ResponseResult r = new ResponseResult();
        r.setErrorCode(-1);

        if (mHttpClientSync == null) {
            if (mHttpClient != null) {
                mHttpClientSync = mHttpClient.newBuilder().build();
            } else {
                mHttpClientSync = new OkHttpClient.Builder()
                        .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                        .readTimeout(timeout, TimeUnit.MILLISECONDS)
                        .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                        .build();
            }
        }

        //POST方式
        String reqStr = "request=?";
        Request request;
        try {
            request = new Request.Builder().url(this.url)
                    .header("Accept", "text/xml")
                    .addHeader("Connection", "close")//默认为Keep-Alive
                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=utf-8"), reqStr))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            r.setErrorText(e.getMessage() == null ? e.toString() : e.getMessage());
            Log.d(LOG_TAG, "Sync OkHttpRequestBuilder Exception:" + r.getErrorText());
            return r;
        }

        try {
            Response response = mHttpClientSync.newCall(request).execute();
            if (response.isSuccessful()) {
                r.setReturnText(response.body().string());
                r.setErrorCode(0);
            } else {
                Log.d(LOG_TAG, "Sync OkHttpRequest getResponseCode:" + response.code());
                r.setErrorText("请求失败。");
            }
        } catch (SocketTimeoutException ex) {
            r.setErrorText("连接超时");
            Log.d(LOG_TAG, "Sync Request Exception:" + r.getErrorText());
        } catch (Exception e) {
            r.setErrorText(e.getMessage() == null ? e.toString() : e.getMessage());
            Log.d(LOG_TAG, "Sync Request Exception:" + r.getErrorText());
        }
        return r;
    }
}
