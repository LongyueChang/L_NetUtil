package net.base;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.qzshitong.HttpParameter;
import com.qzshitong.STApp;
import com.qzshitong.epolice.util.SSOInfo;

import net.OnResponseListener;
import net.RequestPackage;
import net.ResponseResult;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * 通过泉视通的网络接口进行网络请求
 */
public class NetRequestST {
    // 以下信息为注册后的固定值
    private String SERVICE_CODE = "00000314";//福建交警总队移动警务

    /**
     * 仅在模拟环境下使用
     */
    private String DEBUG_URL = "192.168.0.83:8080/ishitong.mpsmp.testforws";
    private boolean IS_DEBUG = false;// 只有在模拟环境下才为true

    //Handler 用于向主线程抛送信息
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private List<RequestPackage> reqList = null;
    private String LOG_TAG = "";
    private Context context;

    public void setUrl(String serviceCode, boolean isDebug, String debugUrl) {
        SERVICE_CODE = serviceCode;
        IS_DEBUG = isDebug;
        DEBUG_URL = debugUrl;
        //在调试状态下打开全视通的测试环境
        if (isDebug) {
            openDebug();
        }
    }

    public String getServiceCode() {
        return SERVICE_CODE;
    }

    public boolean isDebug() {
        return IS_DEBUG;
    }

    public String getDebugUrl() {
        return DEBUG_URL;
    }

    /**
     * 泉视通网络接口返回信息结构
     */
    private class NetRequestResultST {
        private String code = "";
        private String data = "";

        /**
         * 错误码
         */
        public String getCode() {
            return code;
        }

        /**
         * 错误码
         */
        public void setCode(String code) {
            this.code = code;
        }

        /**
         * 数据
         */
        public String getData() {
            return data;
        }

        /**
         * 数据
         */
        public void setData(String data) {
            this.data = data;
        }
    }

    /**
     * 获取设备编号
     */
    public String getDeviceid(Context context) {
        SSOInfo info = getSSOInfo(context);
        return (info != null) ? info.getDeviceid() : "";
    }

    /**
     * 获取警员身份证号
     */
    public String getIdentitycard(Context context) {
        SSOInfo info = getSSOInfo(context);
        return (info != null) ? info.getIdentitycard() : "";
    }

    /**
     * 获取警员登录密码
     */
    public String getPassword(Context context) {
        SSOInfo info = getSSOInfo(context);
        return (info != null) ? info.getPassword() : "";
    }

    /**
     * 获取SIM卡号
     */
    public String getSimid(Context context) {
        SSOInfo info = getSSOInfo(context);
        return (info != null) ? info.getSimid() : "";
    }

    /**
     * 获取TF卡号
     */
    public String getTfid(Context context) {
        SSOInfo info = getSSOInfo(context);
        return (info != null) ? info.getTfid() : "";
    }

    /**
     * 获取警员编号
     */
    public String getPoliceid(Context context) {
        SSOInfo info = getSSOInfo(context);
        return (info != null) ? info.getPoliceid() : "";
    }

    /**
     * Token值
     */
    public String getToken(Context context) {
        SSOInfo info = getSSOInfo(context);
        return (info != null) ? info.getToken() : "";
    }

    /**
     * 获取警员信息
     */
    private SSOInfo getSSOInfo(Context context) {
        try {
            @SuppressWarnings("unchecked")
            List<SSOInfo> list = STApp.getSSOInfo(context);
            if (list != null) {
                if (list.size() > 0) {
                    return list.get(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 构造函数
     *
     * @param context 上下文对象
     * @param logTag  Logcat日志的标签
     */
    public NetRequestST(Context context, String logTag) {
        this.context = context;
        this.LOG_TAG = logTag;
        reqList = new ArrayList<>();
        if (IS_DEBUG) {
            openDebug();
        }
    }

    /**
     * 打开泉视通的网络调试模式
     */
    private boolean openDebug() {
        SSOInfo ssoInfo = new SSOInfo();
        ssoInfo.setDeviceid("0x00A1000021A6AA88");
        ssoInfo.setPassword("021266");
        ssoInfo.setPoliceid("021266");
        ssoInfo.setSimid("460036501359183");
        ssoInfo.setTfid("MFJ0017364D");
        ssoInfo.setToken("43434343");
        ssoInfo.setIdentitycard("350521111111111110");

        return (STApp.openDebug(ssoInfo, DEBUG_URL));
    }

    /**
     * 请求网络接口
     *
     * @param pkg 网络请求数据包
     * @return 请求成功与否（返回空说明发送失败）
     */
    public String postRequest(final RequestPackage pkg) {
        if (pkg == null)
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
            reqList.add(pkg);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    int errCode = 0;
                    String errText = "";
                    String returnData = "";
                    // 当 4.1 及以上系统时，由于系统限制，请求不能建立在 主线程上,需要开启线程
                    final NetRequestResultST result = netRequest(pkg.getContext(), new String(pkg.getPostData()));
                    Log.d(LOG_TAG, "postRequest return:" + result.getCode());
                    if (result.getCode().equals("000")) {
                        returnData = result.getData();
                    } else {
                        errCode = -1;
                        errText = result.getData();
                    }

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
                                            if (result.getCode().equals("000")) {
                                                pkg.getOnResponseListener().onResponse(0, "", result.getData(), pkg.getUuid());
                                            } else {
                                                pkg.getOnResponseListener().onResponse(-1, result.getData(), "", pkg.getUuid());
                                            }
                                        } catch (Exception e) {
                                            Log.d(LOG_TAG, pkg.getRequestName() + " Request back Exception:"
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
                                            errText, returnData, pkg.getUuid());
                                } catch (Exception e) {
                                    Log.d(LOG_TAG, pkg.getRequestName() + " Request back Exception:"
                                            + e.getMessage().trim());
                                } finally {
                                    pkg.setContext(null);
                                    pkg.setOnResponseListener(null);
                                    pkg.setPostData(null);
                                    pkg.setTag(null);
                                    pkg.setUrl(null);
                                }
                            }
//                          Looper.prepare();
//                          pkg.getOnResponseListener().onResponse(errCode,errText, returnData);
//                          Looper.loop();
                        }

                    }
                }
            }).start();

        } catch (Exception e) {
            return null;
        }

        return pkg.getUuid();
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
                        reqList.remove(i);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
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
        req.setOnResponseListener(onResponseListener);

        return req;
    }

    /**
     * 发送网络请求
     */
    private NetRequestResultST netRequest(Context context, String data) {
        if (IS_DEBUG) {
            openDebug();
        }

        HttpParameter httpParameter = new HttpParameter("UTF-8");
        NetRequestResultST result = new NetRequestResultST();

        httpParameter.addRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");
        httpParameter.addRequestProperty("Charset", "UTF-8");

        try {
            httpParameter.setPostData(data);
        } catch (Exception e) {
            e.printStackTrace();
        }


        String resultString = STApp.submitWsHttp(SERVICE_CODE,
                httpParameter, false, context);

        try {
            JSONObject jsonObject = new JSONObject(resultString);
            result.setCode(jsonObject.getString("code"));
            result.setData(jsonObject.getString("data"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 同步发送数据请求(不能在主线程上调用)
     *
     * @param postData 发送的数据
     * @return 请求结果
     */
    public ResponseResult postRequest(String postData) {
        ResponseResult r = new ResponseResult();

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("request=?");
            NetRequestResultST result = netRequest(context, sb.toString());
            Log.d(LOG_TAG, "postRequest return:" + result.getCode());
            if (result.getCode().equals("000")) {
                r.setErrorCode(0);
                r.setReturnText(result.getData());
            } else {
                r.setErrorCode(-1);
                r.setErrorText(result.getData());
            }
        } catch (Exception e) {
            r.setErrorCode(-1);
            r.setErrorText(e.getMessage() == null ? e.toString() : e.getMessage());
        }

        return r;
    }
}
