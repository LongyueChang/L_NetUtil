package net.base;

import android.content.Context;

/**
 * 网络接口调用的基类（仅用于继承）
 */
public class Interface {
    private int mTimeout = 0;
    private String mUrl = "";
    protected boolean mEncrypt = true;
    protected NetRequest mNet = null;
    protected NetRequest.TYPE mType = NetRequest.TYPE.HTTP;

    /**
     * 初始化实例
     *
     * @param context 上下文对象
     * @param logTag  系统Catlog标签
     * @param type    网络请求类型
     */
    public void init(Context context, String logTag, NetRequest.TYPE type, boolean encrypt) {
        mType = type;
        mEncrypt = encrypt;
        mNet = new NetRequest(context, logTag, type);
        if (mTimeout > 100)
            mNet.setTimeout(mTimeout);
        if (mUrl != null && !mUrl.equals(""))
            mNet.setUrl(mUrl);
    }

    /**
     * 设置网络访问的密钥
     *
     * @param keyRequest  请求密钥（后台解密，8位有效）
     * @param keyResponse 接收密码（后台加密，8位有效）
     */
    public static void setKey(String keyRequest, String keyResponse) {
        NetRequest.setKey(keyRequest, keyResponse);
    }

    /**
     * 超时时间(毫秒)(接口没有该属性)
     */
    public int getTimeout() throws Exception {
        if (mNet != null) {
            mTimeout = mNet.getTimeout();
            return mTimeout;
        } else {
            throw new Exception("请先调用init()方法实例化对象");
        }
    }

    /**
     * 超时时间(毫秒)(接口没有该属性)
     */
    public void setTimeout(int timeout) throws Exception {
        mTimeout = timeout;
        if (mNet != null) {
            mNet.setTimeout(timeout);
        } else {
            throw new Exception("请先调用init()方法实例化对象");
        }
    }

    /**
     * 网络请求地址(接口没有该属性)
     */
    public String getUrl() throws Exception {
        if (mNet != null) {
            mUrl = mNet.getUrl();
            return mUrl;
        } else {
            throw new Exception("请先调用init()方法实例化对象");
        }
    }

    /**
     * 网络请求地址(接口没有该属性)
     */
    public void setUrl(String url, int timeout) throws Exception {
        mUrl = url;
        mTimeout = timeout;
        if (mNet != null) {
            mNet.setUrl(url, timeout);
        } else {
            throw new Exception("请先调用init()方法实例化对象");
        }
    }

    /**
     * 网络请求地址(接口没有该属性)
     */
    public void setUrl(String url) throws Exception {
        mUrl = url;
        if (mNet != null) {
            mNet.setUrl(url);
        } else {
            throw new Exception("请先调用init()方法实例化对象");
        }
    }

    /**
     * 是否仅进行测试性的调用（不连接实际服务只测试网络接口，默认为false）
     */
    public boolean isTest() throws Exception {
        if (mNet != null) {
            return mNet.isTest();
        } else {
            throw new Exception("请先调用init()方法实例化对象");
        }
    }

    /**
     * 是否仅进行测试性的调用（不连接实际服务只测试网络接口，默认为false）
     */
    public void setIsTest(boolean isTest) throws Exception {
        if (mNet != null) {
            mNet.setIsTest(isTest);
        } else {
            throw new Exception("请先调用init()方法实例化对象");
        }
    }

    /**
     * 设置服务参数(只当“NetRequest.TYPE.QST”模式下有效)
     *
     * @param serviceCode 服务ID号（默认为：00000314）
     * @param isDebug     是否在模拟（局域网）模式下运行
     * @param debugUrl    在模拟（局域网）模式下运行时的服务地址。（在非模拟环境下，该参数不起作用）
     */
    public void setQstInit(String serviceCode, boolean isDebug, String debugUrl) throws Exception {
        if (mNet != null) {
            mNet.setQstInit(serviceCode, isDebug, debugUrl);
        } else {
            throw new Exception("请先调用init()方法实例化对象");
        }
    }

    /**
     * 设置山(只当“NetRequest.TYPE.AZC”模式下有效)
     *
     * @param xtlb  系统类型（默认为：01）
     * @param jkxlh 接口序列号
     * @param jkid  接口标识（“request”方法的标识，如：88Z01）
     */
    public void setAzcInit(String xtlb, String jkxlh, String jkid) throws Exception {
        if (mNet != null) {
            mNet.setAzcInit(xtlb, jkxlh, jkid);
        } else {
            throw new Exception("请先调用init()方法实例化对象");
        }
    }
}
