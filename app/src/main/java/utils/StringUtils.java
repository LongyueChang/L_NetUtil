package utils;

import android.util.Base64;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串判断、转换工具类
 */
public class StringUtils {
    /**
     * 把指定的对象转成JSON字段串
     */
    public static String toJson(Object src) {
        return new com.google.gson.Gson().toJson(src);
    }

    /**
     * 解析指定的JSON字段串
     *
     * @param json     需要解析的JSON字段串
     * @param classOfT 目录对象定义类
     * @throws Exception
     */
    public static <T> T fromJson(String json, Class<T> classOfT) throws Exception {
        try {
            return new com.google.gson.Gson().fromJson(json, classOfT);
        } catch (com.google.gson.JsonSyntaxException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return android.text.TextUtils.isEmpty(str);
    }

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String... str) {
        return str == null || str.length < 1;
    }

    /**
     * 按条件生成格式化的字符串
     *
     * @param format 格式参数(see {@link java.util.Formatter#format})
     * @param args   根据格式参数 {@code format} 提供数值
     * @return 格式化之后的字符串
     * @throws NullPointerException             当{@code format == null}时触发
     * @throws java.util.IllegalFormatException 当format无效时触发
     */
    public static String format(String format, Object... args) {
        return String.format(Locale.SIMPLIFIED_CHINESE, format, args);
    }

    /**
     * 判断两个字符串是否一致
     */
    public static boolean equals(String str0, String str1) {
        return str0 != null && str1 != null && str0.equals(str1);
    }

    /**
     * 判断两个字符串是否一致（不区分大小写）
     */
    public static boolean equalsIgnoreCase(String str0, String str1) {
        return str0 != null && str1 != null && str0.equalsIgnoreCase(str1);
    }

    /**
     * 判断字符串长度是否与指定数值一致(0表示相等，-1表示字符串长度小于指定的数值，1表示字符串长度大于指定的数值)
     */
    public static int compareLength(String str, int len) {
        if (isEmpty(str))
            return (len == 0) ? 0 : -1;
        else if (str.length() == len)
            return 0;
        else
            return (str.length() > len) ? 1 : -1;
    }

    /**
     * 判断指定的字符串中是否包含中文
     */
    public static boolean containChinese(String str) {
        if (isEmpty(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("[\u4e00-\u9fff]");
        return pattern.matcher(str).find();
    }

    /**
     * 判断字符串是否都是中文
     */
    public static boolean isChineseOnly(String str) {
        if (isEmpty(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("[\u4e00-\u9fff]*");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断字符串是否都是字母或数字
     */
    public static boolean isAlphaOrDigitOnly(String str) {
        if (isEmpty(str)) {
            return false;
        }
        String ps = "[a-zA-Z_0-9]*";
        Pattern pattern = Pattern.compile(ps);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 判断字符串是否都是数字
     */
    public static boolean isDigitsOnly(String str) {
        return android.text.TextUtils.isDigitsOnly(str);
    }

    /**
     * 判断字符串是否为日期型
     *
     * @param str      需要判断的字符串
     * @param template 字符串格式模版
     */
    public static boolean isDate(String str, String template) {
        if (isEmpty(str) || isEmpty(template))
            return false;

        SimpleDateFormat format = new SimpleDateFormat(template, Locale.SIMPLIFIED_CHINESE);
        format.setLenient(false);
        try {
            format.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 在字符串左边用paddingChar补足totalWidth长度
     */
    public static String padLeft(String str, int totalWidth, char paddingChar) {
        StringBuilder sb = new StringBuilder((str == null) ? "" : str);
        while (sb.length() < totalWidth)
            sb.insert(0, paddingChar);
        return sb.toString();
    }

    /**
     * 在字符串右边用paddingChar补足totalWidth长度
     */
    public static String padRight(String str, int totalWidth, char paddingChar) {
        StringBuilder sb = new StringBuilder((str == null) ? "" : str);
        while (sb.length() < totalWidth)
            sb.append(paddingChar);
        return sb.toString();
    }

    /**
     * 把分开的日期和时间合并成紧凑格式（yyyyMMddHHmmss）
     *
     * @param yyyyMMdd 日期部分
     * @param HHmmss   时间部分
     */
    public static String getCompactDate(Date yyyyMMdd, Date HHmmss) {
        try {
            SimpleDateFormat sdfRq = new SimpleDateFormat("yyyyMMdd", Locale.SIMPLIFIED_CHINESE);
            SimpleDateFormat sdfSj = new SimpleDateFormat("HHmmss", Locale.SIMPLIFIED_CHINESE);
            return sdfRq.format(yyyyMMdd) + sdfSj.format(HHmmss);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 把分开的日期和时间合并成“违法时间”的格式（yyyyMMddHHmm）
     *
     * @param yyyyMMdd 日期部分
     * @param HHmm     时间部分
     */
    public static String getWfsj(Date yyyyMMdd, Date HHmm) {
        String s = getCompactDate(yyyyMMdd, HHmm);
        if (s.length() > 12)
            return s.substring(0, 12);
        else
            return s;
    }

    /**
     * 把指定的时间（yyyyMMddHHmmss格式）转换为日期型
     *
     * @param datetime    需要转换的时间
     * @param defaultDate 当无法转换时，默认返回值
     */
    public static Date getDateByCompactDate(String datetime, Date defaultDate) {
        Date dt = getDateByCompactDate(datetime);
        return (dt != null) ? dt : defaultDate;
    }

    /**
     * 把指定的时间（yyyyMMddHHmmss格式）转换为日期型（当转换失败时返回null）
     */
    public static Date getDateByCompactDate(String datetime) {
        if (isEmpty(datetime))
            return null;
        int len = datetime.length();
        String template = "";
        switch (len) {
            case 8:
                template = "yyyyMMdd";
                break;
            case 10:
                template = "yyyyMMddHH";
                break;
            case 12:
                template = "yyyyMMddHHmm";
                break;
            case 14:
                template = "yyyyMMddHHmmss";
                break;
            case 17:
                template = "yyyyMMddHHmmssSSS";
                break;
        }
        if (!isEmpty(template)) {
            ParsePosition pos = new ParsePosition(0);
            return new SimpleDateFormat(template, Locale.SIMPLIFIED_CHINESE).parse(datetime, pos);
        }
        return null;
    }

    /**
     * 根据指定的模版格式，把字符串形式的时间转换成日期型
     */
    public static Date getDateByTemplate(String datetime, String template, Date defaultDate) {
        if (!StringUtils.isEmpty(datetime) && !StringUtils.isEmpty(template)) {
            try {
                return new SimpleDateFormat(template, Locale.SIMPLIFIED_CHINESE).parse(datetime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return defaultDate;
    }

    /**
     * 以指定的格式返回当前时间
     */
    public static String formatDateNow(DATE_TEMPLATE template) {
        return formatDate(new Date(System.currentTimeMillis()), template);
    }

    /**
     * 以指定的格式返回当前时间
     */
    public static String formatDateNow(String template) {
        return formatDate(new Date(System.currentTimeMillis()), template);
    }

    /**
     * 把日期转成指定格式的字符串
     */
    public static String formatDate(Date date, String template) {
        if (date == null || isEmpty(template))
            return "";
        else
            return new SimpleDateFormat(template, Locale.SIMPLIFIED_CHINESE).format(date);
    }

    /**
     * 把日期转成指定格式的字符串
     */
    public static String formatDate(Date date, DATE_TEMPLATE template) {
        return formatDate(date, template.name().trim());
    }

    /**
     * 把指定的时间（yyyyMMddHHmmss格式）转成指定格式的字符串
     */
    public static String formatDate(String compactTime, DATE_TEMPLATE template) {
        return formatDate(getDateByCompactDate(compactTime), template);
    }

    /**
     * 把指定的时间（yyyyMMddHHmmss格式）转成指定格式的字符串
     */
    public static String formatDate(String compactTime, String template) {
        return formatDate(getDateByCompactDate(compactTime), template);
    }

    /**
     * 把指定的时间（字符串）由一种格式转成另外一种格式（转换失败时返回空）
     *
     * @param datetime     需要转换的时间（字符串）
     * @param templateSrc  原时间格式
     * @param templateDest 新时间格式
     */
    public static String formatDate(String datetime, DATE_TEMPLATE templateSrc, DATE_TEMPLATE templateDest) {
        if (isEmpty(datetime) || templateSrc == null || templateDest == null)
            return "";
        return formatDate(datetime, templateSrc.name().trim(), templateDest.name().trim());
    }

    /**
     * 把指定的时间（字符串）由一种格式转成另外一种格式（转换失败时返回空）
     *
     * @param datetime     需要转换的时间（字符串）
     * @param templateSrc  原时间格式
     * @param templateDest 新时间格式
     */
    public static String formatDate(String datetime, String templateSrc, DATE_TEMPLATE templateDest) {
        if (isEmpty(datetime) || isEmpty(templateSrc) || templateDest == null)
            return "";
        return formatDate(datetime, templateSrc, templateDest.name().trim());
    }

    /**
     * 把指定的时间（字符串）由一种格式转成另外一种格式（转换失败时返回空）
     *
     * @param datetime     需要转换的时间（字符串）
     * @param templateSrc  原时间格式
     * @param templateDest 新时间格式
     */
    public static String formatDate(String datetime, DATE_TEMPLATE templateSrc, String templateDest) {
        if (isEmpty(datetime) || templateSrc == null || isEmpty(templateDest))
            return "";
        return formatDate(datetime, templateSrc.name().trim(), templateDest);
    }

    /**
     * 把指定的时间（字符串）由一种格式转成另外一种格式（转换失败时返回空）
     *
     * @param datetime     需要转换的时间（字符串）
     * @param templateSrc  原时间格式
     * @param templateDest 新时间格式
     */
    public static String formatDate(String datetime, String templateSrc, String templateDest) {
        return formatDate(getDateByTemplate(datetime, templateSrc, null), templateDest);
    }

    /**
     * 把数值以1024制式转换为以K、M、G单位形式的字符串（如：1024转为1K）
     */
    public static String formatSize(int size) {
        return formatSize((long) size);
    }

    /**
     * 把数值以1024制式转换为以K、M、G单位形式的字符串（如：1024转为1K）
     */
    public static String formatSize(long size) {
        return formatSize((double) size);
    }

    /**
     * 把数值以1024制式转换为以K、M、G单位形式的字符串（如：1024转为1K）
     */
    public static String formatSize(double size) {
        if (size <= 0)
            return "0";

        int dw = 0;
        double val = size;
        while (val > 1024 && dw < 6) {
            val = val / 1024;
            dw++;
        }
        String str = String.valueOf(val).trim();
        switch (dw) {
            case 1:
                return str + "K";
            case 2:
                return str + "M";
            case 3:
                return str + "G";
            case 4:
                return str + "T";
            case 5:
                return str + "P";
            case 6:
                return str + "E";
        }
        return str;
    }

    /**
     * 把金额转成大写
     */
    public static String formatMoney(float price) {
        return "?";
    }

    /**
     * 判断指定的时间是否晚于当前时间
     *
     * @param datetime     需要比较的时间
     * @param defaultValue 当无法比较时，默认返回值
     */
    public static boolean afterNow(Date datetime, boolean defaultValue) {
        if (datetime == null)
            return defaultValue;
        return datetime.after(new Date(System.currentTimeMillis()));
    }

    /**
     * 判断指定的时间（yyyyMMddHHmmss格式）是否晚于当前时间
     *
     * @param datetime     需要比较的时间
     * @param defaultValue 当无法比较时，默认返回值
     */
    public static boolean afterNow(String datetime, boolean defaultValue) {
        return afterNow(getDateByCompactDate(datetime), defaultValue);
    }

    /**
     * 解Base64码
     */
    public static byte[] decodeBase64(String data) {
        if (isEmpty(data))
            return null;
        else
            return Base64.decode(data, Base64.DEFAULT);
    }

    /**
     * 将指定的二进制字节流按特殊base64进行编码
     */
    public static String encodeBase64X(byte[] data) {
        if (data != null && data.length > 0)
           return "";
        return "";
    }

    /**
     * 获取指定的二进制字节流的哈希(SHA-1)码
     */
    public static String getSha(byte[] data) {
        if (data != null && data.length > 0)
            return "";
        return "";
    }

    /**
     * 将指定的值转换成int型，并以指定的格式返回
     *
     * @param value        需要转成int型数值的信息
     * @param defaultValue 无法转换时的返回值
     * @param format       格式参数(see {@link java.util.Formatter#format})
     * @return 格式化后的字符串
     */
    public static String getIntFormat(String value, String defaultValue, String format) {
        if (StringUtils.isEmpty(value))
            return defaultValue;

        int n;
        try {
            n = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
        try {
            return format(format, n);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * 把字符串的数字转成整型值
     */
    public static int getInt(String value, int defaultValue) {
        if (StringUtils.isEmpty(value))
            return defaultValue;

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * 把字符串的数字转成长整型值
     */
    public static long getLong(String value, long defaultValue) {
        if (StringUtils.isEmpty(value))
            return defaultValue;

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * 把字符串的数字转成double值
     */
    public static double getDouble(String value, double defaultValue) {
        if (StringUtils.isEmpty(value))
            return defaultValue;

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * 把字符串的数字转成float值
     */
    public static float getFloat(String value, float defaultValue) {
        if (StringUtils.isEmpty(value))
            return defaultValue;

        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * 把指定的值按BCD码形式转成字符串
     */
    public static String getBcd(byte[] data, int offset, int byteCount) {
        if (data == null || data.length < offset || offset < 0 || byteCount < 1)
            return "";
        StringBuilder sb = new StringBuilder("");
        for (int i = offset; i < data.length && i - offset < byteCount; i++) {
            int hV = data[i] >> 4 & 0x0F;
            int lV = data[i] & 0x0F;
            if (hV >= 0 && hV <= 9) sb.append(String.valueOf(hV).trim());
            if (lV >= 0 && lV <= 9) sb.append(String.valueOf(lV).trim());
        }
        return sb.toString();
    }

    /**
     * 把指定的值转成十六进制形式的字符串
     */
    public static String getHex(byte[] data) {
        if (data == null || data.length < 1)
            return "";

        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < data.length; i++) {
            String sTemp = Integer.toHexString(0xFF & data[i]).trim();
            if (sTemp.length() < 2)
                sb.append("0");
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 把指定的值转成十六进制形式的字符串
     */
    public static String getHex(byte[] data, int offset, int byteCount) {
        if (data == null || data.length < offset || offset < 0 || byteCount < 1)
            return "";

        StringBuilder sb = new StringBuilder("");
        for (int i = offset; i < data.length && i - offset < byteCount; i++) {
            String sTemp = Integer.toHexString(0xFF & data[i]).trim();
            if (sTemp.length() < 2)
                sb.append("0");
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 把表示十六进制值的字符串转成二进制的值
     */
    public static byte[] getHex(String value) {
        if (StringUtils.isEmpty(value))
            return null;

        char[] dat = value.trim().toCharArray();
        if (dat.length < 1)
            return null;

        byte[] data = new byte[(int) Math.ceil(dat.length / 2.0)];
        int p = dat.length - 2;
        int index = data.length - 1;
        char cH, cL;
        int iH, iL;
        while (p >= 0 && index >= 0) {
            cH = dat[p];
            cL = dat[p + 1];

            if (cH >= '0' && cH <= '9')
                iH = cH - 0x30;
            else if (cH >= 'A' && cH <= 'F')
                iH = cH - 0x40 + 9;
            else if (cH >= 'a' && cH <= 'f')
                iH = cH - 0x60 + 9;
            else
                iH = 0;

            if (cL >= '0' && cL <= '9')
                iL = cL - 0x30;
            else if (cL >= 'A' && cL <= 'F')
                iL = cL - 0x40 + 9;
            else if (cL >= 'a' && cL <= 'f')
                iL = cL - 0x60 + 9;
            else
                iL = 0;

            data[index--] = (byte) ((iH << 4 | iL) & 0xFF);
            p -= 2;
        }
        if (p > -2 && index >= 0) {
            cL = dat[0];
            if (cL >= '0' && cL <= '9')
                iL = cL - 0x30;
            else if (cL >= 'A' && cL <= 'F')
                iL = cL - 0x40 + 9;
            else if (cL >= 'a' && cL <= 'f')
                iL = cL - 0x60 + 9;
            else
                iL = 0;

            data[index--] = (byte) (iL & 0x0F);
        }
        while (index >= 0) data[index--] = 0x00;//该语句实际上无意义，仅为了逻辑上清0
        return data;
    }

    /**
     * 字符串替换
     *
     * @param str     原（替换前的）字符串
     * @param oldChar 需要被替换的字符
     * @param newChar 替换后的新字符
     * @return 替换后的字符串
     */
    public static String replace(String str, char oldChar, char newChar) {
        if (isEmpty(str) || !str.contains(String.valueOf(oldChar)))
            return str;
        return str.replace(oldChar, newChar);
    }

    /**
     * 字符串替换
     *
     * @param str    原（替换前的）字符串
     * @param oldStr 需要被替换的字符串
     * @param newStr 用于替换被替换字符串的字符串
     * @return 替换后的字符串
     */
    public static String replace(String str, String oldStr, String newStr) {
        if (isEmpty(str) || isEmpty(oldStr) || !str.contains(oldStr))
            return str;
        return str.replace(oldStr, (newStr == null) ? "" : newStr);
    }

    /**
     * 日期格式模版
     */
    public enum DATE_TEMPLATE {
        yyyyMMdd,
        yyyyMMddHHmm,
        yyyyMMddHHmmss,
        yyyyMMddHHmmssSSS,
        yyyy年MM月dd日,
        yyyy年MM月dd日HH时mm分
    }
}
