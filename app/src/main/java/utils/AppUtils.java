package utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;

/**
 * 应用相关操作工具类
 */
public class AppUtils {
    /**
     * 检测某个应用是否安装
     */
    public static boolean isInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * 运行指定包对应的APP应用
     *
     * @param context     当前上下文对象
     * @param packageName 需要运行的APP包名
     * @param options     传给APP的参数
     */
    public static boolean launch(Context context, String packageName, Bundle options) {
        if (context != null && isInstalled(context, packageName)) {
            if (options == null)
                context.startActivity(context.getPackageManager().getLaunchIntentForPackage(packageName));
            else
                context.startActivity(context.getPackageManager().getLaunchIntentForPackage(packageName), options);
            return true;
        }
        return false;
    }

    /**
     * 根据指定的文件类型打开指定的文件
     *
     * @param context 当前上下文对象
     * @param data    需要打开的文件信息
     * @param type    打开该文件的类型信息
     */
    public static boolean open(Context context, Uri data, String type) {
        if (context != null) {
            try {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(data, type);
                context.startActivity(intent);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 根据指定的文件类型打开指定的文件
     *
     * @param context  当前上下文对象
     * @param filename 需要打开的文件名（完整路径）
     * @param type     打开该文件的类型信息（由哪个应用打开该文件）
     */
    public static boolean open(Context context, String filename, String type) {
        return context != null && FileUtils.existsFile(filename) && open(context, Uri.fromFile(new File(filename)), type);
    }

    /**
     * 安装APK应用
     *
     * @param context     当前上下文对象
     * @param apkFileName APK完整文件名
     */
    public static void install(Context context, String apkFileName) {
        if (context != null && FileUtils.existsFile(apkFileName)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(new File(apkFileName)), "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }

    /**
     * 卸载应用
     *
     * @param context     当前上下文对象
     * @param packageName 需要卸载的APP包名
     */
    public static void uninstall(Context context, String packageName) {
        if (context != null && isInstalled(context, packageName)) {
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + packageName));
            context.startActivity(uninstallIntent);
        }
    }

    /**
     * 获取指定APK文件的应用包名
     *
     * @param context     当前上下文对象
     * @param apkFileName APK完整文件名
     */
    public static String getApkPackageName(Context context, String apkFileName) {
        if (context != null && FileUtils.existsFile(apkFileName)) {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkFileName, PackageManager.GET_ACTIVITIES);
            if (info != null) {
                ApplicationInfo appInfo = info.applicationInfo;
                if (appInfo != null) {
                    return appInfo.packageName;
                }
            }
        }
        return "";
    }

    /**
     * 获取指定APK文件的应用版本号
     *
     * @param context     当前上下文对象
     * @param apkFileName APK完整文件名
     */
    public static int getApkVersionCode(Context context, String apkFileName) {
        if (context != null && FileUtils.existsFile(apkFileName)) {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkFileName, PackageManager.GET_ACTIVITIES);
            if (info != null) {
                return info.versionCode;
            }
        }
        return -1;
    }

    /**
     * 获取指定应用的版本号
     *
     * @param context     当前上下文对象
     * @param packageName 已安装的APP包名
     */
    public static int getVersionCode(Context context, String packageName) {
        if (context != null) {
            try {
                return context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * 获取当前应用的版本号
     *
     * @param context 当前上下文对象
     */
    public static int getCurVersionCode(Context context) {
        if (context != null) {
            try {
                return context.getPackageManager().getPackageInfo(context.getApplicationContext().getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * 获取当前应用的版本名
     *
     * @param context 当前上下文对象
     */
    public static String getCurVersionName(Context context) {
        if (context != null) {
            try {
                return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 获取当前应用的包名
     *
     * @param context 当前上下文对象
     */
    public static String getCurPackageName(Context context) {
        if (context != null) {
            return context.getPackageName();
        }
        return "";
    }

    /**
     * 读取<meta-data>元素的数据
     *
     * @param context      当前上下文对象
     * @param name         名称（键名）
     * @param defaultValue 默认值
     */
    public static String getMetaData(Context context, String name, String defaultValue) {
        if (context != null && name != null && !name.equals("")) {
            try {
                ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                if (info != null && info.metaData != null) {
                    return info.metaData.getString(name);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return defaultValue;
    }
}
