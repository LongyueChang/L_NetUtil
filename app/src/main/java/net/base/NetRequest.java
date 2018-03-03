package net.base;

import android.content.Context;


import com.google.gson.annotations.SerializedName;

import net.OnResponseListener;
import net.ResponseResult;

import java.io.Serializable;
import java.security.Security;

import utils.StringUtils;


/**
 * 通用网络请求类
 */
public class NetRequest {
    //密钥(当进行加密传输时有效)
    private static String KEY_REQUEST = "发送密码是sunland";//请求密钥（后台解密）
    private static String KEY_RESPONSE = "接收密钥还是sunland";//接收密码（后台加密）

    private boolean isTest = false;
    private TYPE type = TYPE.HTTP;
    private Context context;
    private String logTag = "";
    private NetRequestGZ requestGZ;
    private NetRequestST requestST;
    private NetRequestAzc requestAzc;
    private NetRequestGD requestGD;
    private NetRequestOkHttp requestOkHttp;

    /**
     * 网络访问（请求）类型
     */
    public enum TYPE {
        /**
         * 使用HttpURLConnection进行网络访问
         */
        HTTP(0),
        /**
         * 使用福建（泉视通）统一接入平台进行网络访问
         */
        QST(1),
        /**
         * 通过苏州广达的市场接入平台访问后台服务
         */
        GD(2),
        /**
         * 通过山东（安之畅）平台访问后台服务
         */
        AZC(3),
        /**
         * 使用OkHttp进行网络访问
         */
        OKHTTP(4);

        private int value = 0;

        TYPE(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }

        public static TYPE valueOf(int value) {
            switch (value) {
                case 0:
                    return HTTP;
                case 1:
                    return QST;
                case 2:
                    return GD;
                case 3:
                    return AZC;
                case 4:
                    return OKHTTP;
                default:
                    return null;
            }
        }
    }

    private class GeneralRequestInfo implements Serializable {
        @SerializedName("IsTest")
        private boolean isTest = true;
        @SerializedName("IsEncrypted")
        private boolean isEncrypted = false;
        @SerializedName("Method")
        private String method = "";
        @SerializedName("Parameter")
        private Object parameter;

        /**
         * 传给接口的实际参数
         */
        public Object getParameter() {
            return parameter;
        }

        /**
         * 传给接口的实际参数
         */
        public void setParameter(Object parameter) {
            this.parameter = parameter;
        }

        /**
         * 实际接口名
         */
        public String getMethod() {
            return method;
        }

        /**
         * 实际接口名
         */
        public void setMethod(String method) {
            this.method = method;
        }

        /**
         * 是否为测试（当为true时直接返回，不做实际操作，只进行接口测试）
         */
        public boolean getIsTest() {
            return isTest;
        }

        /**
         * 是否为测试（当为true时直接返回，不做实际操作，只进行接口测试）
         */
        public void setIsTest(boolean isTest) {
            this.isTest = isTest;
        }

        /**
         * 参数parameter的信息是否为加密数据
         */
        public boolean getIsEncrypted() {
            return isEncrypted;
        }

        /**
         * 参数parameter的信息是否为加密数据
         */
        public void setIsEncrypted(boolean isEncrypted) {
            this.isEncrypted = isEncrypted;
        }
    }

    private class GeneralResponseInfo implements Serializable {
        @SerializedName("IsEncrypted")
        private boolean isEncrypted = false;
        @SerializedName("Method")
        private String method = "";
        @SerializedName("Data")
        private String data = "";

        /**
         * 返回的信息是否已加密
         */
        public boolean isEncrypted() {
            return isEncrypted;
        }

        /**
         * 返回的信息是否已加密
         */
        public void setIsEncrypted(boolean isEncrypted) {
            this.isEncrypted = isEncrypted;
        }

        /**
         * 调用（返回）的接口名
         */
        public String getMethod() {
            return method;
        }

        /**
         * 调用（返回）的接口名
         */
        public void setMethod(String method) {
            this.method = method;
        }

        /**
         * 返回的信息
         */
        public String getData() {
            return data;
        }

        /**
         * 返回的信息
         */
        public void setData(String data) {
            this.data = data;
        }
    }

    /**
     * 设置网络访问的密钥
     *
     * @param keyRequest  请求密钥（后台解密，8位有效）
     * @param keyResponse 接收密码（后台加密，8位有效）
     */
    public static void setKey(String keyRequest, String keyResponse) {
        if (keyRequest != null && keyResponse != null && keyRequest.length() > 7 && keyResponse.length() > 7) {
            KEY_REQUEST = keyRequest;
            KEY_RESPONSE = keyResponse;
        }
    }

    /**
     * 构造网络请求对象
     *
     * @param context 上下文对象
     * @param logTag  系统Catlog标签
     * @param type    网络请求类型
     */
    public NetRequest(Context context, String logTag, TYPE type) {
        this.context = context;
        this.logTag = logTag;
        this.type = type;
    }

    /**
     * 克隆连接对象
     *
     * @param url 新对象的连接地址
     * @return 对象实例
     */
    public NetRequest clone(String url) {
        if (StringUtils.isEmpty(url))
            return null;

        NetRequest obj = new NetRequest(context, logTag, type);
        obj.setUrl(url);
        obj.setIsTest(isTest);
        if (requestAzc != null)
            obj.setAzcInit(requestAzc.getXtlb(), requestAzc.getJkxlh(), requestAzc.getJkid());
        if (requestST != null)
            obj.setQstInit(requestST.getServiceCode(), requestST.isDebug(), requestST.getDebugUrl());

        return obj;
    }

    private void init() {
        switch (type) {
            case QST:
                if (requestST == null) {
                    requestST = new NetRequestST(context, logTag);
                    requestGZ = null;
                    requestGD = null;
                    requestAzc = null;
                    requestOkHttp = null;
                }
                break;
            case GD:
                if (requestGD == null) {
                    requestGD = new NetRequestGD(logTag);
                    requestST = null;
                    requestGZ = null;
                    requestAzc = null;
                    requestOkHttp = null;
                }
                break;
            case AZC:
                if (requestAzc == null) {
                    requestAzc = new NetRequestAzc(logTag);
                    requestGD = null;
                    requestST = null;
                    requestGZ = null;
                    requestOkHttp = null;
                }
                break;
            case OKHTTP:
                if (requestOkHttp == null) {
                    requestOkHttp = new NetRequestOkHttp(logTag);
                    requestGD = null;
                    requestST = null;
                    requestGZ = null;
                    requestAzc = null;
                }
                break;
            default:
                if (requestGZ == null) {
                    requestGZ = new NetRequestGZ(logTag);
                    requestST = null;
                    requestGD = null;
                    requestAzc = null;
                    requestOkHttp = null;
                }
                break;
        }
    }

    /**
     * 是否仅进行测试性的调用（不连接实际服务只测试网络接口，默认为false）
     */
    public boolean isTest() {
        return isTest;
    }

    /**
     * 是否仅进行测试性的调用（不连接实际服务只测试网络接口，默认为false）
     */
    public void setIsTest(boolean isTest) {
        this.isTest = isTest;
    }

    /**
     * 超时时间(泉视通[TYPE.QST]接口没有该属性)
     */
    public int getTimeout() {
        init();
        switch (type) {
            case HTTP:
                return requestGZ.getTimeout();
            case GD:
                return requestGD.getTimeout();
            case AZC:
                return requestAzc.getTimeout();
            case OKHTTP:
                return requestOkHttp.getTimeout();
        }
        return 0;
    }

    /**
     * 超时时间(泉视通[TYPE.QST]接口没有该属性)
     */
    public void setTimeout(int timeout) {
        init();
        switch (type) {
            case HTTP:
                requestGZ.setTimeout(timeout);
                break;
            case GD:
                requestGD.setTimeout(timeout);
                break;
            case AZC:
                requestAzc.setTimeout(timeout);
                break;
            case OKHTTP:
                requestOkHttp.setTimeout(timeout);
                break;
        }
    }

    /**
     * 网络地址(泉视通[TYPE.QST]接口没有该属性)
     */
    public String getUrl() {
        init();
        switch (type) {
            case HTTP:
                return requestGZ.getUrl();
            case GD:
                return requestGD.getUrl();
            case AZC:
                return requestAzc.getUrl();
            case OKHTTP:
                return requestOkHttp.getUrl();
        }
        return "";
    }

    /**
     * 网络地址(泉视通[TYPE.QST]接口没有该属性)
     */
    public void setUrl(String url) {
        init();
        switch (type) {
            case HTTP:
                requestGZ.setUrl(url);
                break;
            case GD:
                requestGD.setUrl(url);
                break;
            case AZC:
                requestAzc.setUrl(url);
                break;
            case OKHTTP:
                requestOkHttp.setUrl(url);
                break;
        }
    }

    /**
     * 网络地址(泉视通[TYPE.QST]接口没有该属性)
     */
    public void setUrl(String url, int timeout) {
        init();
        switch (type) {
            case HTTP:
                requestGZ.setUrl(url);
                requestGZ.setTimeout(timeout);
                break;
            case GD:
                requestGD.setUrl(url);
                requestGD.setTimeout(timeout);
                break;
            case AZC:
                requestAzc.setUrl(url);
                requestAzc.setTimeout(timeout);
                break;
            case OKHTTP:
                requestOkHttp.setUrl(url);
                requestOkHttp.setTimeout(timeout);
                break;
        }
    }

    /**
     * 获取设备编号(泉视通[TYPE.QST]接口专属)
     */
    public String getQstDeviceid() {
        init();
        if (type != TYPE.QST || requestST == null) {
            return "";
        }
        return requestST.getDeviceid(context);
    }

    /**
     * 获取警员身份证号(泉视通[TYPE.QST]接口专属)
     */
    public String getQstIdentitycard() {
        init();
        if (type != TYPE.QST || requestST == null) {
            return "";
        }
        return requestST.getIdentitycard(context);
    }

    /**
     * 获取警员登录密码(泉视通[TYPE.QST]接口专属)
     */
    public String getQstPassword() {
        init();
        if (type != TYPE.QST || requestST == null) {
            return "";
        }
        return requestST.getPassword(context);
    }

    /**
     * 获取SIM卡号(泉视通[TYPE.QST]接口专属)
     */
    public String getQstSimid() {
        init();
        if (type != TYPE.QST || requestST == null) {
            return "";
        }
        return requestST.getSimid(context);
    }

    /**
     * 获取TF卡号(泉视通[TYPE.QST]接口专属)
     */
    public String getQstTfid() {
        init();
        if (type != TYPE.QST || requestST == null) {
            return "";
        }
        return requestST.getTfid(context);
    }

    /**
     * 获取警员编号(泉视通[TYPE.QST]接口专属)
     */
    public String getQstPoliceid() {
        init();
        if (type != TYPE.QST || requestST == null) {
            return "";
        }
        return requestST.getPoliceid(context);
    }

    /**
     * Token值(泉视通[TYPE.QST]接口专属)
     */
    public String getQstToken() {
        init();
        if (type != TYPE.QST || requestST == null) {
            return "";
        }
        return requestST.getToken(context);
    }

    /**
     * 设置泉视通的服务参数
     *
     * @param serviceCode 服务ID号（默认为：00000314）
     * @param isDebug     是否在模拟（局域网）模式下运行
     * @param debugUrl    在模拟（局域网）模式下运行时的泉视通服务地址
     */
    public boolean setQstInit(String serviceCode, boolean isDebug, String debugUrl) {
        init();
        if (type != TYPE.QST || requestST == null) {
            return false;
        }
        requestST.setUrl(serviceCode, isDebug, debugUrl);
        return true;
    }

    /**
     * 设置山东安之畅申请下来的参数
     *
     * @param xtlb  系统类型（默认为：01）
     * @param jkxlh 接口序列号
     * @param jkid  接口标识（“request”方法的标识，如：88Z01）
     */
    public boolean setAzcInit(String xtlb, String jkxlh, String jkid) {
        init();
        if (type != TYPE.AZC || requestAzc == null) {
            return false;
        }
        requestAzc.setXtlb(xtlb);
        requestAzc.setJkxlh(jkxlh);
        requestAzc.setJkid(jkid);
        return true;
    }

    /**
     * 异步发送数据请求
     *
     * @param context            上下文对象
     * @param method             调用的实际接口（方法）
     * @param param              给实际接口的参数（JSON串）
     * @param encrypt            是否对数据进行加密
     * @param onResponseListener 结果返回监听
     * @return 请求是否成功发送
     */
    public boolean postRequest(Context context, String method,
                               Object param, boolean encrypt, final OnResponseListener onResponseListener) {
        String r = postRequest(context, method, param, encrypt, onResponseListener, false);
        return !StringUtils.isEmpty(r);
    }

    /**
     * 异步发送数据请求
     *
     * @param context            上下文对象
     * @param method             调用的实际接口（方法）
     * @param param              给实际接口的参数（JSON串）
     * @param encrypt            是否对数据进行加密
     * @param onResponseListener 结果返回监听
     * @param repeated           是否允许接口同时多次访问网络(当true时，返回请求接口的UUID)
     * @return 请求是否成功发送（返回空说明发送失败）
     */
    public String postRequest(Context context, String method,
                              Object param, boolean encrypt, final OnResponseListener onResponseListener, boolean repeated) {
        GeneralRequestInfo arg = new GeneralRequestInfo();
        arg.setIsTest(isTest);
        arg.setMethod(method);

        if (encrypt && param instanceof String) {
            arg.setIsEncrypted(true);
            try {
                String se = "?";
                if (se.equals("")) {
                    arg.setIsEncrypted(false);
                    arg.setParameter(param);
                } else {
                    arg.setParameter(se);
                }
            } catch (Exception e) {
                e.printStackTrace();
                arg.setIsEncrypted(false);
                arg.setParameter(param);
            }
        } else {
            arg.setIsEncrypted(false);
            arg.setParameter(param);
        }

        OnResponseListener actResponse = new OnResponseListener() {
            @Override
            public void onResponse(int errorCode, String errorText, String returnText, String uuid) {
                if (errorCode == 0) {
                    try {
                        GeneralResponseInfo response = StringUtils.fromJson(returnText, GeneralResponseInfo.class);
                        if (response == null) {
                            onResponseListener.onResponse(-2, "返回为空", returnText, uuid);
                        } else if (response.isEncrypted()) {
                            String info = "";
                            if (info.equals("")) {
                                onResponseListener.onResponse(-3, "对返回数据解密失败(看returnText信息可了解更多)", returnText, uuid);
                            } else {
                                onResponseListener.onResponse(0, errorText, info, uuid);
                            }
                        } else {
                            onResponseListener.onResponse(0, errorText, response.getData(), uuid);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        onResponseListener.onResponse(-2, "返回值JSON解析失败(看returnText信息可了解更多)", returnText, uuid);
                    }
                } else {
                    onResponseListener.onResponse(errorCode, errorText, returnText, uuid);
                }
            }
        };

        init();
        String r = "";
        switch (type) {
            case HTTP:
                r = requestGZ.postRequest(requestGZ.createRequest(context, method,
                        StringUtils.toJson(arg), actResponse, repeated));
                break;
            case QST:
                r = requestST.postRequest(requestST.createRequest(context, method,
                        StringUtils.toJson(arg), actResponse, repeated));
                break;
            case GD:
                r = requestGD.postRequest(requestGD.createRequest(context, method,
                        StringUtils.toJson(arg), actResponse, repeated));
                break;
            case AZC:
                r = requestAzc.postRequest(requestAzc.createRequest(context, method,
                        StringUtils.toJson(arg), actResponse, repeated));
                break;
            case OKHTTP:
                r = requestOkHttp.postRequest(requestOkHttp.createRequest(context, method,
                        StringUtils.toJson(arg), actResponse, repeated));
                break;
        }
        return (r == null) ? "" : r;
    }

    /**
     * 取消异步请求
     *
     * @param method 需要取消请求的接口名
     * @return true为成功, false为失败
     */
    public boolean cancelRequest(String method) {
        return cancelRequest(method, "0");
    }

    /**
     * 取消异步请求
     *
     * @param method 需要取消请求的接口名
     * @param uuid   接口的UUID信息
     * @return true为成功, false为失败
     */
    public boolean cancelRequest(String method, String uuid) {
        init();
        switch (type) {
            case HTTP:
                return requestGZ.cancelRequest(method, uuid);
            case QST:
                return requestST.cancelRequest(method, uuid);
            case GD:
                return requestGD.cancelRequest(method, uuid);
            case AZC:
                return requestAzc.cancelRequest(method, uuid);
            case OKHTTP:
                return requestOkHttp.cancelRequest(method, uuid);
        }
        return false;
    }

    /**
     * 同步发送数据请求(不能在主线程上调用)
     *
     * @param method  调用的实际接口（方法）
     * @param param   给实际接口的参数（JSON串）
     * @param encrypt 是否加密传输
     * @return 请求结果
     */
    public ResponseResult postRequest(String method, Object param, boolean encrypt) {
        GeneralRequestInfo arg = new GeneralRequestInfo();
        arg.setIsTest(isTest);
        arg.setMethod(method);

        if (encrypt && param instanceof String) {
            arg.setIsEncrypted(true);
            try {
                String se ="?";
                if (se.equals("")) {
                    arg.setIsEncrypted(false);
                    arg.setParameter(param);
                } else {
                    arg.setParameter(se);
                }
            } catch (Exception e) {
                e.printStackTrace();
                arg.setIsEncrypted(false);
                arg.setParameter(param);
            }
        } else {
            arg.setIsEncrypted(false);
            arg.setParameter(param);
        }

        init();
        ResponseResult response = null;
        switch (type) {
            case HTTP:
                response = requestGZ.postRequest(StringUtils.toJson(arg));
                break;
            case QST:
                response = requestST.postRequest(StringUtils.toJson(arg));
                break;
            case GD:
                response = requestGD.postRequest(StringUtils.toJson(arg));
                break;
            case AZC:
                response = requestAzc.postRequest(StringUtils.toJson(arg), arg.getMethod());
                break;
            case OKHTTP:
                response = requestOkHttp.postRequest(StringUtils.toJson(arg));
                break;
        }
        if (response != null) {
            if (response.getErrorCode() == 0) {
                try {
                    GeneralResponseInfo resp = StringUtils.fromJson(response.getReturnText(), GeneralResponseInfo.class);
                    if (resp == null) {
                        response.setErrorCode(-2);
                        response.setErrorText("返回为空");
                    } else if (resp.getData() == null) {
                        response.setErrorCode(-2);
                        response.setErrorText("返回值JSON解析失败(看getReturnText()信息可了解更多)");
                    } else if (resp.isEncrypted()) {
                        String info = "?";
                        if (info.equals("")) {
                            response.setErrorCode(-3);
                            response.setErrorText("对返回数据解密失败(看getReturnText()信息可了解更多)");
                        } else {
                            response.setReturnText(info);
                        }
                    } else {
                        response.setReturnText(resp.getData());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setErrorCode(-2);
                    response.setErrorText("返回值JSON解析失败(看getReturnText()信息可了解更多)");
                }
            }
        }
        return response;
    }
}
