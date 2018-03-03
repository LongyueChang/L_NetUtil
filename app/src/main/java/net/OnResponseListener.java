package net;

/**
 * 网络请求结果返回接口
 */
public interface OnResponseListener {
    /**
     * 网络请求结果返回数据
     *
     * @param errorCode  请求错误代码
     * @param errorText  请求错误信息
     * @param returnText 返回的信息（数据）
     * @param uuid       接口请求编号（当重复请求时有效）
     */
    void onResponse(int errorCode, String errorText, String returnText, String uuid);
}
