package net.base;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import net.OnResponseListener;
import net.RequestPackage;
import net.ResponseResult;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * 通过平台访问后台服务
 */
public class NetRequestAzc {
    private String LOG_TAG = "";
    private int timeout = 10000;
    private String url = "";
    //Handler 用于向主线程抛送信息
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private List<RequestPackage> reqList = null;

    private String mXtlb = "01";//接口类型
    private String mJkxlh = "";//需要向安之畅申请
    private String mJkid = "88Z01";//接口标识ID

    private final static String ENVHEADER = "<?xml version='1.0' encoding='utf-8'?>\n"
            + "<soap:Envelope xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>\n"
            + "<SOAP-ENV:Header xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
            + "" + "</SOAP-ENV:Header>\n" + "<soap:Body>\n";
    private final static String ENVTAIL = "</soap:Body>\n</soap:Envelope>";
    private final static String PARAMMASK = "<in%d>%s</in%d>\n";
    private final static String RESPONSEHEADER = "<ns1:out>";
    private final static String RESPONSETAIL = "</ns1:out>";

    /**
     * 本机ip地址
     */
    private String localIpAddress = "";

    /**
     * 接口类型（默认为：01）
     */
    public void setXtlb(String xtlb) {
        this.mXtlb = xtlb;
    }

    /**
     * 接口类型
     */
    public String getXtlb() {
        return mXtlb;
    }

    /**
     * 接口序列号（需要向安之畅申请）
     */
    public void setJkxlh(String jkxlh) {
        this.mJkxlh = jkxlh;
    }

    /**
     * 接口序列号
     */
    public String getJkxlh() {
        return mJkxlh;
    }

    /**
     * 接口标识ID
     */
    public void setJkid(String jkid) {
        this.mJkid = jkid;
    }

    /**
     * 接口标识ID
     */
    public String getJkid() {
        return mJkid;
    }

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
    public NetRequestAzc(String logTag) {
        this.LOG_TAG = logTag;
        this.url = "";
        reqList = new ArrayList<>();

        if (TextUtils.isEmpty(localIpAddress)) {
            localIpAddress = getLocalIpAddress();
        }
    }

    /**
     * 网络请求类构造函数
     *
     * @param logTag Logcat日志的标签
     * @param url    网络地址
     */
    public NetRequestAzc(String logTag, String url) {
        this.LOG_TAG = logTag;
        this.url = url;
        reqList = new ArrayList<>();

        if (TextUtils.isEmpty(localIpAddress)) {
            localIpAddress = getLocalIpAddress();
        }
    }

    /**
     * 网络请求类构造函数
     *
     * @param logTag  Logcat日志的标签
     * @param url     网络地址
     * @param timeout 超时时间（毫秒）
     */
    public NetRequestAzc(String logTag, String url, int timeout) {
        this.LOG_TAG = logTag;
        this.url = url;
        this.timeout = timeout;
        reqList = new ArrayList<>();

        if (TextUtils.isEmpty(localIpAddress)) {
            localIpAddress = getLocalIpAddress();
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
        RequestPackage req = new RequestPackage(context);
        if (hadUuid)
            req.setUuid(java.util.UUID.randomUUID().toString());
        else
            req.setUuid("0");
        req.setRequestName(requestName);
        try {
            req.setPostData(getPostString(mXtlb, mJkxlh, mJkid, postData, req.getRequestName()).getBytes("UTF-8"));
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
        if (TextUtils.isEmpty(requestName) || TextUtils.isEmpty(uuid)) {
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

    /**
     * 生成发送给安之畅转发服务的数据
     *
     * @param xtlb     系统类别(如：01)
     * @param jkxlh    接口序列号(向安之畅申请获得)
     * @param jkid     接口标识
     * @param postData 发送的数据
     */
    private String getPostString(String xtlb, String jkxlh, String jkid, String postData, String methodName) {
        String methodAzc = "queryObjectOut";

        // 加入methodName和zdbs属性
        postData = "{\"methodName\":\"" + methodName + "\", \"zdbs\":\"" + localIpAddress + "\"," + postData.substring(1);
        //String[] params = new String[]{xtlb, jkxlh, jkid, "","","","", localIpAddress, cn.sunlandgroup.tools.Encode.urlEncode(postData, "UTF-8")};
        String[] params = new String[]{xtlb, jkxlh, jkid, "", "", "", "", localIpAddress, postData};

        //String[] params = new String[]{xtlb, jkxlh, jkid, cn.sunlandgroup.tools.Encode.urlEncode(postData, "UTF-8")};
        StringBuilder sb = new StringBuilder();
        sb.append(ENVHEADER);
        sb.append("<");
        sb.append(methodAzc);
        sb.append(">\n");
        for (int i = 0; i < params.length; i++) {
            String format = String.format(PARAMMASK, i, params[i], i);
            sb.append(format);
        }
        sb.append("</");
        sb.append(methodAzc);
        sb.append(">\n");
        sb.append(ENVTAIL);

        return sb.toString();
    }

    /**
     * 获取本机IP地址
     */
    private String getLocalIpAddress() {
        String hostIp = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = inetAddress.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = inetAddress.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            // Log.i("SocketException--->", ""+e.getLocalizedMessage());
            return "ip is error";
        }

        return hostIp;
    }

    private String getResponseString(String src) {
        if (src == null || !src.contains(RESPONSEHEADER) || !src.contains(RESPONSETAIL))
            return "";

        int start = src.indexOf(RESPONSEHEADER) + RESPONSEHEADER.length();
        if (start > 0) {
            int end = src.indexOf(RESPONSETAIL);
            if (start < end)
                return src.substring(start, end);
        }
        return "";
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
                //System.setProperty("http.keepAlive", "false");
                conn = (HttpURLConnection) pkg.getUrl().openConnection();
                conn.setConnectTimeout(this.timeoutRequest);
                conn.setReadTimeout(this.timeoutRequest);
                conn.setDoOutput(true);// 允许输出
                conn.setDoInput(true);
                conn.setUseCaches(false);// 不使用Cache
                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
//                conn.setRequestProperty("Accept", "text/xml");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setRequestProperty("Pragma:", "no-cache");
                conn.setRequestProperty("Cache-Control", "no-cache");

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
                if (code == 200) {
                    InputStream is = conn.getInputStream();// 获取返回数据
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(is));

                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        sb.append("\n");
                    }

                    returnData = getResponseString(sb.toString());

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
    public ResponseResult postRequest(String postData, String methodName) {
        return postRequest(postData, this.timeout, methodName);
    }

    /**
     * 同步发送数据请求(不能在主线程上调用)
     *
     * @param postData 发送的数据
     * @param timeout  超时时间（毫秒）
     * @return 请求结果
     */
    public ResponseResult postRequest(String postData, int timeout, String methodName) {
        ResponseResult r = new ResponseResult();
        r.setErrorCode(-1);
        HttpURLConnection conn;
        try {
//            System.setProperty("http.keepAlive", "false");
            conn = (HttpURLConnection) (new URL(this.url)).openConnection();
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.setDoOutput(true);// 允许输出
            conn.setDoInput(true);
            conn.setUseCaches(false);// 不使用Cache
            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            conn.setRequestProperty("Accept", "text/xml");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Pragma:", "no-cache");
            conn.setRequestProperty("Cache-Control", "no-cache");

            DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
            byte[] reqDat = getPostString(mXtlb, mJkxlh, mJkid, postData, methodName).getBytes("UTF-8");
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
            if (code == 200) {
                InputStream is = conn.getInputStream();// 获取返回数据
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));

                StringBuilder sbRead = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sbRead.append(line);
                    sbRead.append("\n");
                }
                final String respData = getResponseString(sbRead.toString());
                r.setReturnText(respData);
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
