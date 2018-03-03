package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

/**
 * 图像处理工具类
 */
public class BitmapUtils {
    /**
     * 创建一个指定大小的缩略图（不回收原图资源）
     *
     * @param source 原图
     * @param width  缩略图宽度
     * @param height 缩略图高度
     * @return 缩略图
     */
    public static Bitmap getThumbnail(Bitmap source, int width, int height) {
        return getThumbnail(source, width, height, false);
    }

    /**
     * 创建一个指定大小的缩略图
     *
     * @param source            原图
     * @param width             缩略图宽度
     * @param height            缩略图高度
     * @param autoRecycleSource 是否回收原图资源
     * @return 缩略图
     */
    public static Bitmap getThumbnail(Bitmap source, int width, int height, boolean autoRecycleSource) {
        if (source == null || width < 1 || height < 1)
            return null;
        int bmpHeight, bmpWidth;
        try {
            bmpHeight = source.getHeight();
            bmpWidth = source.getWidth();
        } catch (Exception e) {
            e.printStackTrace();
            return source;
        }

        if (bmpHeight <= height && bmpWidth <= width)
            return source;

        //计算缩小比例
        double ratioHeight = bmpHeight * 1.0 / height;
        double ratioWidth = bmpWidth * 1.0 / width;
        double ratio = (ratioHeight > ratioWidth) ? ratioHeight : ratioWidth;
        int destHeight = (int) Math.floor(bmpHeight / ratio);
        int destWidth = (int) Math.floor(bmpWidth / ratio);

        //避免出现近似值
        if (destHeight > height)
            destHeight = height;
        if (destWidth > width)
            destWidth = width;

        if (autoRecycleSource)
            return ThumbnailUtils.extractThumbnail(source, destWidth, destHeight, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        else
            return ThumbnailUtils.extractThumbnail(source, destWidth, destHeight);
    }

    /**
     * 获取指定图片文件的缩略图（自动根据旋转角度摆正图片）
     *
     * @param fileName 完整的图片文件名
     * @param width    缩略图宽度
     * @param height   缩略图高度
     * @return 缩略图
     */
    public static Bitmap getThumbnail(String fileName, int width, int height) {
        return getThumbnail(fileName, width, height, true);
    }

    /**
     * 获取指定图片文件的缩略图
     *
     * @param fileName   完整的图片文件名
     * @param width      缩略图宽度
     * @param height     缩略图高度
     * @param autoRotate 是否自动摆正图片
     * @return 缩略图
     */
    public static Bitmap getThumbnail(String fileName, int width, int height, boolean autoRotate) {
        if (FileUtils.existsFile(fileName) && width > 0 && height > 0) {
            try {
                int sw = width;
                int sh = height;

                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds = true;//只获取图片的实际宽度和高度，不进行具体解码操作
                BitmapFactory.decodeFile(fileName, opt);
                int degree = getDegree(fileName);
                if (autoRotate && degree != 0 && degree % 180 != 0) {
                    sw = height;
                    sh = width;
                }
                opt.inSampleSize = 1;//初始值，表示不进行缩放
                if (sw > 0 && sh > 0 && (opt.outWidth > sw || opt.outHeight > sh)) {
                    int scaleWidth = (int) Math.round(opt.outWidth * 1.0 / sw);//计算宽度的缩放比
                    int scaleHeight = (int) Math.round(opt.outHeight * 1.0 / sh);//计算高度的缩放比
                    opt.inSampleSize = scaleWidth > scaleHeight ? scaleWidth : scaleHeight;//选择缩放比最大的值进行缩放
                }
                //以下代码为了进一步减少出现OOM（内存溢出）的可能性
                opt.inDither = false;//不进行图片抖动处理
                opt.inPreferredConfig = Bitmap.Config.RGB_565;//降低空间占用

                opt.inJustDecodeBounds = false;//开始解码
                Bitmap bitmap = BitmapFactory.decodeFile(fileName, opt);
                if (autoRotate && degree != 0 && bitmap != null) {
                    Matrix matrix = new Matrix();
                    int w = bitmap.getWidth();
                    int h = bitmap.getHeight();
                    matrix.postRotate(degree);

                    return getThumbnail(Bitmap.createBitmap(bitmap, 0, 0, w, h,
                            matrix, true), width, height, true);
                }

                return getThumbnail(bitmap, width, height, true);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /**
     * 获取指定图片流的缩略图
     *
     * @param bmpStream 图片流数据
     * @param width     缩略图宽度
     * @param height    缩略图高度
     * @return 缩略图
     */
    public static Bitmap getThumbnail(byte[] bmpStream, int width, int height) {
        if (bmpStream != null && bmpStream.length > 0 && width > 0 && height > 0) {
            try {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds = true;//只获取图片的实际宽度和高度，不进行具体解码操作
                BitmapFactory.decodeByteArray(bmpStream, 0, bmpStream.length, opt);
                opt.inSampleSize = 1;//初始值，表示不进行缩放
                if (width > 0 && height > 0 && (opt.outWidth > width || opt.outHeight > height)) {
                    int scaleWidth = (int) Math.round(opt.outWidth * 1.0 / width);//计算宽度的缩放比
                    int scaleHeight = (int) Math.round(opt.outHeight * 1.0 / height);//计算高度的缩放比
                    opt.inSampleSize = scaleWidth > scaleHeight ? scaleWidth : scaleHeight;//选择缩放比最大的值进行缩放
                }
                //以下代码为了进一步减少出现OOM（内存溢出）的可能性
                opt.inDither = false;//不进行图片抖动处理
                opt.inPreferredConfig = Bitmap.Config.RGB_565;//降低空间占用

                opt.inJustDecodeBounds = false;//开始解码
                Bitmap bitmap = BitmapFactory.decodeByteArray(bmpStream, 0, bmpStream.length, opt);
                return getThumbnail(bitmap, width, height, true);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /**
     * 根据指定的照片文件名获取图像数据（自动根据旋转角度摆正图片）
     *
     * @param fileName 照片完整文件名
     * @return 图像数据
     */
    public static Bitmap getBitmap(String fileName) {
        if (!FileUtils.existsFile(fileName))
            return null;

        try {
            int degree = getDegree(fileName);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 1;//初始值，表示不进行缩放

            //以下代码为了进一步减少出现OOM（内存溢出）的可能性
            opt.inDither = false;//不进行图片抖动处理
            opt.inPreferredConfig = Bitmap.Config.RGB_565;//降低空间占用

            opt.inJustDecodeBounds = false;//开始解码
            Bitmap bitmap = BitmapFactory.decodeFile(fileName, opt);
            if (degree != 0 && bitmap != null) {
                Matrix matrix = new Matrix();
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
                matrix.postRotate(degree);

                return Bitmap.createBitmap(bitmap, 0, 0, w, h,
                        matrix, true);
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 把图片按JPG格式进行压缩，并存放在指定的目录中（成功后返回带完整路径的文件名，并回收原图资源）
     *
     * @param bitmap   图片信息
     * @param quality  图片压缩质量（推荐80）
     * @param destPath 指定的目录
     * @param filename 指定的文件名
     */
    public static String compressJpg(Bitmap bitmap, int quality, String destPath, String filename) {
        return compress(bitmap, Bitmap.CompressFormat.JPEG, quality, destPath, filename);
    }

    /**
     * 把图片按JPG格式进行压缩，并存放在指定的目录中（成功后返回带完整路径的文件名，格式：yyyyMMddHHmmss+4位序号，并回收原图资源）
     *
     * @param bitmap   图片信息
     * @param quality  图片压缩质量（推荐80）
     * @param destPath 指定的目录
     */
    public static String compressJpg(Bitmap bitmap, int quality, String destPath) {
        return compress(bitmap, Bitmap.CompressFormat.JPEG, quality, destPath);
    }

    /**
     * 把图片按指定参数进行压缩，并存放在指定的目录中（成功后返回带完整路径的文件名，并回收原图资源）
     *
     * @param bitmap   图片信息
     * @param format   图片压缩格式
     * @param quality  图片压缩质量（推荐80）
     * @param destPath 指定的目录
     * @param filename 指定的文件名
     */
    public static String compress(Bitmap bitmap, Bitmap.CompressFormat format, int quality, String destPath, String filename) {
        if (bitmap == null || format == null || StringUtils.isEmpty(destPath) || StringUtils.isEmpty(filename))
            return null;

        String path = destPath;
        if (!destPath.endsWith("/"))
            path = destPath + "/";
        String destFile = path + filename;
        File f = new File(destFile);
        if (!f.getParentFile().exists()) {
            if (!f.getParentFile().mkdirs())
                return null;
        }

        FileOutputStream fos = null;
        boolean ret = false;
        try {
            fos = new FileOutputStream(f);
            ret = bitmap.compress(format, quality, fos);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
            FileUtils.delete(destFile);
            return null;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            bitmap.recycle();
        }
        if (ret)
            return destFile;
        else
            return null;
    }

    /**
     * 把图片按指定参数进行压缩，并存放在指定的目录中（成功后返回带完整路径的文件名，格式：yyyyMMddHHmmss+4位序号，并回收原图资源）
     *
     * @param bitmap   图片信息
     * @param format   图片压缩格式
     * @param quality  图片压缩质量（推荐80）
     * @param destPath 指定的目录
     */
    public static String compress(Bitmap bitmap, Bitmap.CompressFormat format, int quality, String destPath) {
        if (bitmap == null || format == null || StringUtils.isEmpty(destPath))
            return null;

        String path = destPath;
        if (!destPath.endsWith("/"))
            path = destPath + "/";

        String ext = "";
        switch (format) {
            case JPEG:
                ext = ".jpg";
                break;
            case PNG:
                ext = ".png";
                break;
            case WEBP:
                ext = ".webp";
                break;
        }

        int xh = 0;
        String fnPre = StringUtils.formatDateNow(StringUtils.DATE_TEMPLATE.yyyyMMddHHmmss).trim();
        String fn;
        do {
            fn = fnPre + String.format(Locale.SIMPLIFIED_CHINESE, "%04d", xh++).trim() + ext;//用4位后缀，为了跟毫秒进行区分
        } while (FileUtils.exists(path + fn));

        return compress(bitmap, format, quality, path, fn);
    }

    /**
     * 通过缩小图片尺寸（高、宽），使用图片文件符合指定的大小（回收原图资源）
     *
     * @param filename 完整路径的图片文件名
     * @param quality  图片压缩质量（推荐80）
     * @param size     指定的图片文件大小（字节数）
     * @return 缩小后的图片数据
     */
    public static Bitmap compressJpgBySize(String filename, int quality, int size) {
        return compressJpgBySize(getBitmap(filename), quality, size);
    }

    /**
     * 通过缩小图片尺寸（高、宽），使用图片文件符合指定的大小（回收原图资源）
     *
     * @param bitmap  图片数据信息
     * @param quality 图片压缩质量（推荐80）
     * @param size    指定的图片文件大小（字节数）
     * @return 缩小后的图片数据
     */
    public static Bitmap compressJpgBySize(Bitmap bitmap, int quality, int size) {
        if (bitmap == null || size < 1)
            return null;
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        Bitmap newBmp = bitmap;
        double ratio;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
        try {
            int bosLen = bos.toByteArray().length;
            while (bosLen > size) {
                bos.reset();
                ratio = size * 1.0 / bosLen;
                if (ratio >= 1.0)//两者已无限接近，无法计算比率
                    break;

                height = (int) Math.floor(height * ratio);
                width = (int) Math.floor(width * ratio);
                if (height < 1 || width < 1)
                    break;

                newBmp = getThumbnail(newBmp, width, height, true);
                if (newBmp != null) {
                    newBmp.compress(Bitmap.CompressFormat.JPEG, quality, bos);
                    bosLen = bos.toByteArray().length;
                } else {
                    height = 0;
                    width = 0;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                bos.close();
            } catch (IOException eio) {
                eio.printStackTrace();
            }
            newBmp.recycle();
            return null;
        }

        try {
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (height < 1 || width < 1) {
            if (newBmp != null)
                newBmp.recycle();
            return null;
        }
        return newBmp;
    }

    /**
     * 通过降低图片质量，使用图片文件符合指定的大小（回收原图资源）
     *
     * @param bitmap 图片数据信息
     * @param size   指定的图片文件大小（字节数）
     * @return 缩小后的图片数据
     */
    public static Bitmap compressJpgByQuality(Bitmap bitmap, int size) {
        if (bitmap == null || size < 1)
            return null;

        int quality = 80;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);
        try {
            int diff = os.toByteArray().length - size;
            while (diff > 0) {
                os.reset();
                if (diff > size && quality > 40)
                    quality -= 20;
                else if (diff > size / 2 && quality > 20)
                    quality -= 10;
                else
                    quality -= 2;

                if (quality < 1)
                    break;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);
                diff = os.toByteArray().length - size;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                os.close();
            } catch (IOException eio) {
                eio.printStackTrace();
            }
            bitmap.recycle();
            return null;
        }
        if (quality < 1) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bitmap.recycle();
            return null;
        }
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        Bitmap newBmp = BitmapFactory.decodeStream(is, null, null);
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bitmap.recycle();
        return newBmp;
    }

    /**
     * 读取图片旋转的角度
     *
     * @param filename 完整路径的图片文件名
     * @return degree旋转的角度
     */
    public static int getDegree(String filename) {
        int degree = 0;
        if (FileUtils.existsFile(filename)) {
            try {
                ExifInterface exifInterface = new ExifInterface(filename);
                int orientation = exifInterface.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return degree;
    }

    /**
     * 获取图片的拍照日期
     *
     * @param filename     完整路径的图片文件名
     * @param defaultValue 当无法取到日期时，默认返回值
     */
    public static Date getExifDate(String filename, Date defaultValue) {
        if (FileUtils.existsFile(filename)) {
            try {
                ExifInterface exifInterface = new ExifInterface(filename);
                String time = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                if (StringUtils.isEmpty(time))
                    return defaultValue;

                if (time.length() == 19) {
                    if (time.contains("-"))
                        return StringUtils.getDateByTemplate(time, "yyyy-MM-dd HH:mm:ss", defaultValue);
                    if (time.contains("/"))
                        return StringUtils.getDateByTemplate(time, "yyyy/MM/dd HH:mm:ss", defaultValue);
                }

                if (time.length() == 16) {
                    if (time.contains("-"))
                        return StringUtils.getDateByTemplate(time, "yyyy-MM-dd HH:mm", defaultValue);
                    if (time.contains("/"))
                        return StringUtils.getDateByTemplate(time, "yyyy/MM/dd HH:mm", defaultValue);
                }

                return StringUtils.getDateByCompactDate(time, defaultValue);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return defaultValue;
    }

    /**
     * 根据分辨率从dp（设备独立像素）的单位转成为px（设备实际像素）
     */
    public static int dip2px(Context context, float dpValue) {
        if (context == null)
            return 0;
        float scale = context.getResources().getDisplayMetrics().density;
        if (dpValue > 0)
            return (int) (dpValue * scale + 0.5f);
        else
            return (int) (dpValue * scale - 0.5f);
    }

    /**
     * 根据分辨率从px（设备实际像素）的单位转成为dp（设备独立像素）
     */
    public static int px2dip(Context context, float pxValue) {
        if (context == null)
            return 0;
        float scale = context.getResources().getDisplayMetrics().density;
        if (pxValue > 0)
            return (int) (pxValue / scale + 0.5f);
        else
            return (int) (pxValue / scale - 0.5f);
    }
}
