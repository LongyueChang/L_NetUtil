package net.base;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import net.OnResponseListener;
import net.RequestPackage;
import net.ResponseResult;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * 网络请求类
 */
public class NetRequestGZ {
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
    public NetRequestGZ(String logTag) {
        this.LOG_TAG = logTag;
        this.url = "";
        reqList = new ArrayList<>();
    }

    /**
     * 网络请求类构造函数
     *
     * @param logTag Logcat日志的标签
     * @param url    网络地址
     */
    public NetRequestGZ(String logTag, String url) {
        this.LOG_TAG = logTag;
        this.url = url;
        reqList = new ArrayList<>();
    }

    /**
     * 网络请求类构造函数
     *
     * @param logTag  Logcat日志的标签
     * @param url     网络地址
     * @param timeout 超时时间（毫秒）
     */
    public NetRequestGZ(String logTag, String url, int timeout) {
        this.LOG_TAG = logTag;
        this.url = url;
        this.timeout = timeout;
        reqList = new ArrayList<>();
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
        sb.append("request=");
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
                            //HttpRequest req = (HttpRequest) reqList.get(i).getTag();
                            reqList.remove(i);
                            return true;
                        }
                    }
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
    public String postRequest(RequestPackage pkg, int timeout) {
        if (pkg == null)
            return null;
        if (pkg.getUrl() == null)
            return null;
        if (pkg.getOnResponseListener() == null)
            return null;

        try {
            for (int i = 0; i < reqList.size(); i++) {
                if (reqList.get(i).getRequestName().equalsIgnoreCase(pkg.getRequestName())
                        && reqList.get(i).getUuid().equalsIgnoreCase(pkg.getUuid())) {
                    Log.d(LOG_TAG, "postRequest 错误！相同请求已经存在。");
                    return null;
                }
            }

            HttpRequest req = new HttpRequest(pkg.getRequestName(), pkg.getUuid(), timeout);
            pkg.setTag(req);
            reqList.add(pkg);
            req.start();

        } catch (Exception e) {
            return null;
        }

        return pkg.getUuid();
    }

    private class HttpRequest extends Thread {
        private String requestName = "";
        private String uuid = "";
        private RequestPackage pkg = null;
        private int errCode = 0;
        private int timeoutRequest = 0;
        private String errText = "";
        private String returnData = "";

        public HttpRequest(String requestName, String uuid, int timeout) {
            this.requestName = requestName;
            this.uuid = uuid;
            this.timeoutRequest = timeout;
        }

        private void postRequestHttp() {
            try {
                for (int i = 0; i < reqList.size(); i++) {
                    if (reqList.get(i).getRequestName().equalsIgnoreCase(requestName)
                            && reqList.get(i).getUuid().equalsIgnoreCase(uuid)) {
                        pkg = reqList.get(i);
                        break;
                    }
                }
            } catch (Exception e) {
                errCode = -1;
                errText = "遍历pkg时出现异常" + e.getMessage().trim();
                returnData = "";
                Log.d(LOG_TAG, requestName + " Request Exception:" + errText);
                return;
            }
            if (pkg == null) {
                Log.d(LOG_TAG, "HttpRequest " + requestName + " not find pkg");
                return;
            }
            if (pkg.getUrl() == null || pkg.getOnResponseListener() == null) {
                Log.d(LOG_TAG, "HttpRequest " + requestName + " not find url");
                return;
            }
            HttpURLConnection conn;
            try {
                System.setProperty("http.keepAlive", "false");
                conn = (HttpURLConnection) pkg.getUrl().openConnection();
                conn.setConnectTimeout(this.timeoutRequest);
                conn.setReadTimeout(this.timeoutRequest);
                conn.setDoOutput(true);// 允许输出
                conn.setDoInput(true);
                conn.setUseCaches(false);// 不使用Cache
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "close");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");//兼容GET/POST
                conn.setRequestProperty("Accept", "text/xml");
                conn.setRequestProperty("Charset", "UTF-8");

                DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
                if (pkg.getPostData().length > 10240) {
                    //当单次数据过大时，HttpURLConnection会报sendto failed: EPIPE (Broken pipe)异常
                    int writed = 0;
                    byte[] data = null;
                    while (writed < pkg.getPostData().length) {
                        if (writed + 10240 < pkg.getPostData().length) {
                            if (data == null || data.length != 10240) {
                                data = new byte[10240];
                            }
                            System.arraycopy(pkg.getPostData(), writed, data, 0, data.length);
                            writed += 10240;
                        } else {
                            data = new byte[pkg.getPostData().length - writed];
                            System.arraycopy(pkg.getPostData(), writed, data, 0, data.length);
                            writed = pkg.getPostData().length;
                        }
                        outStream.write(data);
                    }
                } else {
                    outStream.write(pkg.getPostData());
                }
                outStream.flush();
                outStream.close();
                int code = conn.getResponseCode();
                if (code == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();// 获取返回数据
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                    StringBuilder sb = new StringBuilder();
//                    int value;
//                    while ((value = reader.read()) != -1) {
//                        sb.append((char) value);
//                    }
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        sb.append("\n");
                    }
                    returnData = sb.toString();
                } else {
                    Log.d(LOG_TAG, "HttpRequest " + requestName
                            + " getResponseCode:" + code);
                    errCode = -1;
                    errText = "请求失败。";
                    returnData = "";
                }
            } catch (SocketTimeoutException ex) {
                errCode = -1;
                errText = "连接超时";
                returnData = "";
                Log.d(LOG_TAG, requestName + " Request Exception:" + errText);
            } catch (Exception e) {
                errCode = -1;
                if (e.getMessage() != null) {
                    errText = e.getMessage().trim();
                } else {
                    errText = e.toString().trim();
                }
                returnData = "";
                Log.d(LOG_TAG, requestName + " Request Exception:" + errText);
            }
        }

        @Override
        public void run() {
            postRequestHttp();
            if (requestName != null && !requestName.equals("")
                    && pkg != null) {// 可能已经取消
                if (reqList.contains(pkg)) {
                    try {
                        reqList.remove(pkg);// 目的：支持递归
                    } catch (Exception e) {
                        Log.d(LOG_TAG, pkg.getRequestName() + " Request back remove Exception:"
                                + e.getMessage().trim());
                        pkg.setContext(null);
                        pkg.setOnResponseListener(null);
                        pkg.setPostData(null);
                        pkg.setTag(null);
                        pkg.setUrl(null);
                    }
                    if (pkg.getOnResponseListener() != null) {
                        if (Looper.myLooper() != Looper.getMainLooper()) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        pkg.getOnResponseListener().onResponse(errCode,
                                                errText, returnData, uuid);
                                    } catch (Exception e) {
                                        Log.d(LOG_TAG, requestName + " Request back Exception:"
                                                + e.getMessage().trim());
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
                                pkg.getOnResponseListener().onResponse(errCode,
                                        errText, returnData, uuid);
                            } catch (Exception e) {
                                Log.d(LOG_TAG, requestName + " Request back Exception:"
                                        + e.getMessage().trim());
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
            }
        }
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
    public ResponseResult postRequest(String postData, int timeout) {
        ResponseResult r = new ResponseResult();
        r.setErrorCode(-1);
        HttpURLConnection conn;
        try {
            String reqStr = "request=?";
            byte[] reqDat = reqStr.getBytes("UTF-8");

            System.setProperty("http.keepAlive", "false");
            conn = (HttpURLConnection) (new URL(this.url)).openConnection();
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.setDoOutput(true);// 允许输出
            conn.setDoInput(true);
            conn.setUseCaches(false);// 不使用Cache
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "close");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");//兼容GET/POST
            conn.setRequestProperty("Accept", "text/xml");
            conn.setRequestProperty("Charset", "UTF-8");

            DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
            if (reqDat.length > 10240) {
                //当单次数据过大时，HttpURLConnection会报sendto failed: EPIPE (Broken pipe)异常
                int writed = 0;
                byte[] data = null;
                while (writed < reqDat.length) {
                    if (writed + 10240 < reqDat.length) {
                        if (data == null || data.length != 10240) {
                            data = new byte[10240];
                        }
                        System.arraycopy(reqDat, writed, data, 0, data.length);
                        writed += 10240;
                    } else {
                        data = new byte[reqDat.length - writed];
                        System.arraycopy(reqDat, writed, data, 0, data.length);
                        writed = reqDat.length;
                    }
                    outStream.write(data);
                }
            } else {
                outStream.write(reqDat);
            }
            outStream.flush();
            outStream.close();
            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();// 获取返回数据
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                StringBuilder sbRead = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sbRead.append(line);
                    sbRead.append("\n");
                }
                r.setReturnText(sbRead.toString());
                r.setErrorCode(0);
            } else {
                Log.d(LOG_TAG, "Sync HttpRequest getResponseCode:" + code);
                r.setErrorText("请求失败。");
            }
        } catch (SocketTimeoutException ex) {
            r.setErrorText("连接超时");
            Log.d(LOG_TAG, "Sync Request Exception:" + r.getErrorText());
        } catch (Exception e) {
            r.setErrorText(e.getMessage() != null ? e.getMessage().trim() : e.toString().trim());
            Log.d(LOG_TAG, "Sync Request Exception:" + r.getErrorText());
        }
        return r;
    }
}
