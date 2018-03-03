package net;

/**
 * 进行同步网络请求时的返回结果
 */
public class ResponseResult {
    private int errorCode = 0;
    private String errorText = "";
    private String returnText = "";

    /**
     * 服务端返回的信息（数据）
     */
    public String getReturnText() {
        return returnText;
    }

    /**
     * 服务端返回的信息（数据）
     */
    public void setReturnText(String returnText) {
        this.returnText = returnText;
    }

    /**
     * 错误信息
     */
    public String getErrorText() {
        return errorText;
    }

    /**
     * 错误信息
     */
    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    /**
     * 错误代码(0返回成功)
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * 错误代码(0返回成功)
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
