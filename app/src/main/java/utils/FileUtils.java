package utils;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 文件操作工具类
 */
public class FileUtils {
    /**
     * 判断指定的文件（或目录）是否存在（完整路径文件名）
     */
    public static boolean exists(String path) {
        return !TextUtils.isEmpty(path) && new File(path).exists();
    }

    /**
     * 判断指定的文件是否存在（完整路径文件名）
     */
    public static boolean existsFile(String path) {
        if (TextUtils.isEmpty(path))
            return false;
        File f = new File(path);
        return f.exists() && !f.isDirectory();
    }

    /**
     * 判断指定的目录是否存在（完整路径目录名）
     */
    public static boolean existsDirectory(String path) {
        if (TextUtils.isEmpty(path))
            return false;
        File f = new File(path);
        return f.exists() && f.isDirectory();
    }

    /**
     * 判断指定的文件（或目录）是否存在
     *
     * @param path     文件所在目录
     * @param filename 文件名
     */
    public static boolean exists(String path, String filename) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(filename))
            return false;
        String fn = filename;
        while (!TextUtils.isEmpty(fn) && fn.startsWith("/"))
            fn = fn.substring(1);

        if (path.endsWith("/"))
            return exists(path + filename);
        else
            return exists(path + "/" + filename);
    }

    /**
     * 判断指定的文件是否存在
     *
     * @param path     文件所在目录
     * @param filename 文件名
     */
    public static boolean existsFile(String path, String filename) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(filename))
            return false;
        String fn = filename;
        while (!TextUtils.isEmpty(fn) && fn.startsWith("/"))
            fn = fn.substring(1);

        if (path.endsWith("/"))
            return existsFile(path + filename);
        else
            return existsFile(path + "/" + filename);
    }

    /**
     * 创建指定的目录
     *
     * @param path    需要创建的目录
     * @param delfile 当存在同名文件（非目录）时，是否将文件删除
     * @return 目录是否创建成功
     */
    public static boolean mkdir(String path, boolean delfile) {
        if (TextUtils.isEmpty(path))
            return false;
        File f = new File(path);
        if (!f.exists())
            return f.mkdirs();
        else if (f.isDirectory())
            return true;
        else if (delfile && f.delete())
            return f.mkdir();
        return false;
    }

    /**
     * 强制删除指定的目录
     */
    public static boolean rmdir(String path) {
        if (TextUtils.isEmpty(path))
            return true;
        File f = new File(path);
        if (f.exists()) {
            if (f.isDirectory()) {
                File[] fs = f.listFiles();
                if (fs != null && fs.length > 0) {
                    for (File subf : fs) {
                        if (subf != null && subf.exists()) {
                            if (subf.isDirectory()) {
                                if (!rmdir(subf.getAbsolutePath()))
                                    return false;
                            } else if (!subf.delete())
                                return false;
                        }
                    }
                }
                return f.delete();
            }
            return false;
        }
        return true;
    }


    /**
     * 删除指定的文件
     */
    public static boolean delete(String filename) {
        if (TextUtils.isEmpty(filename))
            return true;
        File f = new File(filename);
        return !f.exists() || f.delete();
    }

    /**
     * 写文本文件
     *
     * @param filename 要写入的文件名（完整路径）
     * @param data     要写入的信息
     * @param append   是否追加保存
     */
    public static boolean write(String filename, String data, boolean append) {
        if (TextUtils.isEmpty(filename) || data == null)
            return false;

        File f = new File(filename);
        if (!f.getParentFile().exists()) {
            File fp = f.getParentFile();
            if (!fp.mkdirs() && !fp.isDirectory()) {
                return false;
            }
        }

        try {
            FileWriter writer = new FileWriter(f, append);
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 把数据写入到指定的文件中
     *
     * @param filename  要写入的文件名（完整路径）
     * @param data      要写入的数据
     * @param overwrite 是否覆盖原文件
     */
    public static boolean write(String filename, byte[] data, boolean overwrite) {
        if (TextUtils.isEmpty(filename) || data == null)
            return false;

        File f = new File(filename);
        if (!f.getParentFile().exists()) {
            File fp = f.getParentFile();
            if (!fp.mkdirs() && !fp.isDirectory()) {
                return false;
            }
        }
        if (f.exists()) {
            if (overwrite) {
                if (!f.delete())
                    return false;
            } else
                return false;
        }

        try {
            OutputStream os = new FileOutputStream(f);
            os.write(data, 0, data.length);
            os.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 将文件流写入到指定文件夹下的文件(覆盖原文件)
     *
     * @param inputStream 源文件
     * @param outputFile  目标文件
     */
    public static boolean write(InputStream inputStream, File outputFile) {
        if (inputStream != null && outputFile != null) {
            // 判断目标文件父目录是否存在，如不存在则创建
            if (!outputFile.getParentFile().exists()) {
                File fp = outputFile.getParentFile();
                if (!fp.mkdirs() && !fp.isDirectory()) {
                    return false;
                }
            }
            try {
                OutputStream os = new FileOutputStream(outputFile);
                byte[] dat = new byte[1024];
                int len;
                while ((len = inputStream.read(dat)) > 0) {
                    os.write(dat, 0, len);
                }
                inputStream.close();
                os.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 将文件流写入到指定文件夹下的文件(覆盖原文件)
     *
     * @param inputStream 源文件
     * @param outputFile  目标文件
     */
    public static boolean write(InputStream inputStream, String outputFile) {
        return inputStream != null && !TextUtils.isEmpty(outputFile) && write(inputStream, new File(outputFile));
    }

    /**
     * 读取指定的文件数据
     *
     * @param file 文件信息
     * @return 文件数据
     */
    public static byte[] read(File file) {
        if (file != null && file.exists() && !file.isDirectory() && file.length() > 0) {
            try {
                RandomAccessFile randomFile = new RandomAccessFile(file, "r");
                int fileLength = (int) randomFile.length();
                if (fileLength > 0) {
                    byte[] data = new byte[fileLength];
                    if (data.length == fileLength) {
                        randomFile.seek(0);
                        int readed = randomFile.read(data, 0, fileLength);
                        randomFile.close();
                        if (readed == fileLength) {
                            return data;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 复制文件(覆盖原文件)
     *
     * @param srcFilename  源文件
     * @param destFilename 目标文件
     */
    public static boolean copy(String srcFilename, String destFilename) {
        if (!existsFile(srcFilename) || TextUtils.isEmpty(destFilename))
            return false;
        try {
            return write(new FileInputStream(srcFilename), new File(destFilename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 复制文件到指定的目录中（返回目标文件带完整路径文件名，复制失败返回空）
     *
     * @param srcFilename 源文件
     * @param destPath    指定的目录
     * @param autoRename  是否对目标文件重命名（当false时，将覆盖原文件）
     */
    public static String copy(String srcFilename, String destPath, boolean autoRename) {
        if (!existsFile(srcFilename) || StringUtils.isEmpty(destPath))
            return null;

        String path = destPath;
        if (!destPath.endsWith("/"))
            path = destPath + "/";

        String destFile;
        if (autoRename) {
            String ext = getExt(srcFilename);
            int xh = 0;
            String fn = StringUtils.formatDate(new Date(System.currentTimeMillis()), StringUtils.DATE_TEMPLATE.yyyyMMddHHmmss).trim();
            do {
                destFile = path + fn + String.format(Locale.SIMPLIFIED_CHINESE, "%04d", xh++).trim() + ext;
            } while (exists(destFile));
        } else {
            destFile = path + getFileName(srcFilename);
        }
        if (copy(srcFilename, destFile))
            return destFile;
        else
            return null;
    }

    /**
     * 把Assets目录下的文件复制到指定的位置
     *
     * @param context      上下文对象
     * @param assetFile    文件名（assets目录下的相对路径）
     * @param destFilename 目标文件
     * @return 是否复制成功
     */
    public static boolean copyAssetFile(Context context, String assetFile, String destFilename) {
        if (context == null || TextUtils.isEmpty(assetFile) || TextUtils.isEmpty(destFilename))
            return false;

        InputStream assetIS;
        try {
            assetIS = context.getAssets().open(assetFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return write(assetIS, destFilename);
    }

    /**
     * 获取指定目录（包括子目录）中的所有文件（返回完整路径的文件列表）
     */
    public static ArrayList<String> getFilesAll(String path) {
        ArrayList<String> fns = new ArrayList<>();
        fns.clear();
        if (!StringUtils.isEmpty(path)) {
            File dirs = new File(path);
            if (dirs.exists() && dirs.isDirectory()) {
                File[] fs = dirs.listFiles();
                if (fs != null && fs.length > 0) {
                    for (File f : fs) {
                        if (f != null) {
                            if (f.isDirectory())
                                fns.addAll(getFilesAll(f.getAbsolutePath()));
                            else
                                fns.add(f.getAbsolutePath());
                        }
                    }
                }
            }
        }
        return fns;
    }

    /**
     * 获取指定目录中的文件列表（包含子目录）
     */
    public static ArrayList<String> getFiles(String path) {
        return getFiles(path, false);
    }

    /**
     * 获取指定目录中的文件列表
     */
    public static ArrayList<String> getFiles(String path, boolean excptDirectory) {
        ArrayList<String> fns = new ArrayList<>();
        fns.clear();
        if (!StringUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                File[] fs = file.listFiles();
                if (fs != null && fs.length > 0) {
                    for (File f : fs) {
                        if (f != null && (!excptDirectory || !f.isDirectory())) {
                            fns.add(f.getName());
                        }
                    }
                }
            }
        }
        return fns;
    }

    /**
     * 获取指定目录中的文件列表
     *
     * @param path   目录完路径
     * @param filter 过滤条件
     */
    public static ArrayList<String> getFiles(String path, FileFilter filter) {
        if (filter == null)
            return getFiles(path);

        ArrayList<String> fns = new ArrayList<>();
        fns.clear();
        if (!StringUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                File[] fs = file.listFiles(filter);
                if (fs != null && fs.length > 0) {
                    for (File f : fs) {
                        if (f != null) {
                            fns.add(f.getName());
                        }
                    }
                }
            }
        }
        return fns;
    }

    /**
     * 获取指定目录中的文件列表
     *
     * @param path                    目录完路径
     * @param filterRegularExpression 过滤条件(正则表达式，文件名都为小写字母)
     * @param excptDirectory          是否排除子目录
     */
    public static ArrayList<String> getFiles(String path, final String filterRegularExpression, final boolean excptDirectory) {
        if (StringUtils.isEmpty(filterRegularExpression))
            return getFiles(null);

        return getFiles(path, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                try {
                    return (!excptDirectory || !pathname.isDirectory()) && pathname.getName().toLowerCase().matches(filterRegularExpression);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });
    }

    /**
     * 获取指定目录中的文件列表（不包含子目录）
     *
     * @param path 目录完路径
     * @param exts 包含的文件扩展名（不区分大小写，只返回指定扩展名的文件，当置为null时，返回所有文件）
     */
    public static ArrayList<String> getFiles(String path, String[] exts) {
        return getFiles(path, exts, true);
    }

    /**
     * 获取指定目录中的文件列表
     *
     * @param path           目录完路径
     * @param exts           包含的文件扩展名（不区分大小写，只返回指定扩展名的文件，当置为null时，返回所有文件）
     * @param excptDirectory 是否排除子目录
     */
    public static ArrayList<String> getFiles(String path, String[] exts, boolean excptDirectory) {
        return getFiles(path, "", exts, excptDirectory);
    }

    /**
     * 获取指定目录中的文件列表
     *
     * @param path           目录完路径
     * @param prefix         文件名前缀
     * @param exts           包含的文件扩展名（不区分大小写，只返回指定扩展名的文件，当置为null时，返回所有文件）
     * @param excptDirectory 是否排除子目录
     */
    public static ArrayList<String> getFiles(String path, String prefix, String[] exts, boolean excptDirectory) {
        if ((exts == null || exts.length < 1) && StringUtils.isEmpty(prefix))
            return getFiles(path, excptDirectory);

        StringBuilder sb = new StringBuilder(StringUtils.isEmpty(prefix) ? ".*" : "^" + prefix.toLowerCase() + ".*");
        boolean had = false;
        if (exts != null && exts.length > 0) {
            sb.append("\\.(");
            for (String ext : exts) {
                if (!StringUtils.isEmpty(ext)) {
                    if (had)
                        sb.append("|");
                    if (ext.startsWith("."))
                        sb.append(ext.substring(1).toLowerCase());
                    else
                        sb.append(ext.toLowerCase());
                    had = true;
                }
            }
            sb.append(")");
        }
        sb.append("$");
        return getFiles(path, sb.toString(), excptDirectory);
    }

    /**
     * 获取指定应用Asset下的文件列表
     */
    public static ArrayList<String> getAssetFiles(Context context) {
        ArrayList<String> fns = new ArrayList<>();
        fns.clear();
        if (context != null) {
            try {
                fns.addAll(Arrays.asList(context.getAssets().list("")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fns;
    }

    /**
     * 用于收集未能被全局异常捕获的异常堆栈信息
     * 通常在发现程序崩溃而引发崩溃的异常未被捕获时，手动利用try-catch触发
     *
     * @param path 文件存放路径(不包括文件名)
     * @param ex   异常
     */
    public static void writeExceptionStackInfo(String path, Exception ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        StringBuilder sb = new StringBuilder();
        sb.append(writer.toString());
        OutputStream out = null;
        try {
            long timestamp = System.currentTimeMillis();
            String time = StringUtils.formatDateNow("yyyy-MM-dd-HH-mm-ss");
            String fullName = "ex-" + time + "-" + timestamp + ".txt";
            mkdir(path, true);
            out = new FileOutputStream(path + fullName);
            out.write(sb.toString().getBytes("UTF-8"));
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 重新命名文件名
     *
     * @param srcFilename  原文件名
     * @param destFilename 新文件名
     */
    public static boolean rename(String srcFilename, String destFilename) {
        if (!exists(srcFilename) || exists(destFilename))
            return false;
        File f = new File(srcFilename);
        return f.renameTo(new File(destFilename));
    }

    /**
     * 根据指定的完整路径文件名，获取无路径的文件名
     *
     * @param path 完整路径文件名
     * @return 无路径的文件名
     */
    public static String getFileName(String path) {
        if (StringUtils.isEmpty(path))
            return "";
        int pos1 = path.lastIndexOf("/");
        int pos2 = path.lastIndexOf("\\");
        int pos = (pos1 > pos2) ? pos1 : pos2;
        if (pos >= 0)
            return path.substring(pos + 1);
        else
            return path;
    }

    /**
     * 根据指定的完整路径文件名，获取无路径的文件名
     *
     * @param path      完整路径文件名
     * @param removeExt 是否去掉文件扩展名
     * @return 无路径的文件名
     */
    public static String getFileName(String path, boolean removeExt) {
        if (StringUtils.isEmpty(path))
            return "";
        if (removeExt) {
            String ext = getExt(path);
            if (!StringUtils.isEmpty(ext)) {
                String fn = getFileName(path);
                if (!StringUtils.isEmpty(fn)) {
                    if (fn.length() > ext.length()) {
                        return fn.substring(0, fn.length() - ext.length());
                    }
                    return "";//正常情况下不可能为空
                }
            }
        }
        return getFileName(path);
    }

    /**
     * 获取指定文件名的扩展名（返回值带点“.”）
     */
    public static String getExt(String filename) {
        if (StringUtils.isEmpty(filename))
            return "";
        int pos1 = filename.lastIndexOf("/");
        int pos2 = filename.lastIndexOf("\\");
        int pos = (pos1 > pos2) ? pos1 : pos2;
        int dot = filename.lastIndexOf(".");
        if (dot > pos && dot >= 0)
            return filename.substring(dot);
        else
            return "";
    }

    /**
     * 获取文件的修改时间
     */
    public static Date getModifyTime(String filename, Date defaultValue) {
        if (existsFile(filename)) {
            File f = new File(filename);
            return new Date(f.lastModified());
        }
        return defaultValue;
    }

    /**
     * 获取指定文件的长度（文件大小、字节数）
     */
    public static long getLength(String filename) {
        if (TextUtils.isEmpty(filename))
            return 0;
        File f = new File(filename);
        if (f.exists() && !f.isDirectory())
            return f.length();
        return 0;
    }

    /**
     * /**
     * 解压ZIP文件(覆盖原文件)
     *
     * @param srcFilename 源文件
     * @param destPath    目标目录
     */
    public static boolean unZip(String srcFilename, String destPath) {
        if (!existsFile(srcFilename) || TextUtils.isEmpty(destPath))
            return false;

        //下面两个文件列表用于解压失败后恢复原文件
        List<String> bakFile = new ArrayList<>();
        List<String> oldFile = new ArrayList<>();
        bakFile.clear();
        oldFile.clear();

        try {
            ZipFile zfile = new ZipFile(srcFilename);
            Enumeration zList = zfile.entries();
            ZipEntry ze = null;
            while (zList.hasMoreElements()) {
                ze = (ZipEntry) zList.nextElement();
                if (ze == null || ze.isDirectory())
                    continue;
                // new String(ze.getName().getBytes("8859_1"), "GB2312")
                String fn = destPath + ze.getName();
                if (existsFile(fn)) {
                    String bakFn = getBakFilename(fn);
                    if (rename(fn, bakFn)) {
                        bakFile.add(bakFn);
                        oldFile.add(fn);
                    } else {
                        zfile.close();
                        rollbackBakFile(bakFile, oldFile);
                        return false;
                    }
                }
                InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
                if (!write(is, fn)) {
                    zfile.close();
                    rollbackBakFile(bakFile, oldFile);
                    return false;
                }
            }
            zfile.close();
        } catch (Exception e) {
            rollbackBakFile(bakFile, oldFile);
            return false;
        }

        if (bakFile.size() > 0) {
            for (String f : bakFile) {
                delete(f);
            }
        }

        return true;
    }

    /**
     * 在同一目录生成备份文件名（加bak后缀）
     *
     * @param filename 原文件名
     * @return 备份文件名
     */
    private static String getBakFilename(String filename) {
        String bakFile, bakFileInit;
        if (StringUtils.isEmpty(filename))
            bakFileInit = "tmp.bak";
        else
            bakFileInit = filename + ".bak";

        bakFile = bakFileInit;
        int xh = 1;
        while (exists(bakFile)) {
            bakFile = bakFileInit + String.valueOf(xh).trim();
        }

        return bakFile;
    }

    /**
     * 根据原文件和备份文件名，恢复原文件
     *
     * @param bakFiles 备份文件名列表
     * @param oldFiles 原始文件名列表
     * @return 是否完成回滚
     */
    private static boolean rollbackBakFile(List<String> bakFiles, List<String> oldFiles) {
        if (bakFiles != null && oldFiles != null) {
            for (int i = 0; i < bakFiles.size() && i < oldFiles.size(); i++) {
                if (existsFile(bakFiles.get(i)) && !StringUtils.isEmpty(oldFiles.get(i))) {
                    if (existsFile(oldFiles.get(i)))
                        delete(oldFiles.get(i));

                    if (!rename(bakFiles.get(i), oldFiles.get(i)))
                        return false;
                }
            }
        }
        return true;
    }
}
