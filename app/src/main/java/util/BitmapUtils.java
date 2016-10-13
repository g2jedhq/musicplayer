package util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Qubo on 2016/10/3.
 * 封装图片相关工具方法
 */
public class BitmapUtils {
    /**
     * 异步在工作线程中执行图片模糊化处理
     *
     * @param bitmap
     * @param r
     * @param callback
     */
    public static void loadBluredBitmap(final Bitmap bitmap, final int r,
                                        final BitmapCallback callback) {

        new AsyncTask<String, String, Bitmap>() {
            protected Bitmap doInBackground(String... params) {
                //  Bitmap b = createBlurBitmap(bitmap, r);
                return doBlur(bitmap, r, false);
            }

            protected void onPostExecute(Bitmap b) {
                callback.onBitmapLoaded(b);
            }
        }.execute();
    }

    public static Bitmap doBlur(Bitmap sentBitmap, int radius, boolean canReuseInBitmap) {

        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        Bitmap bitmap;
        if (canReuseInBitmap) {
            bitmap = sentBitmap;
        } else {
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {
//                if (yi>r.length||yi>g.length||yi>b.length||rsum>dv.length||gsum>dv.length||bsum>dv.length) {
//                    break;
//                }TODO 已加入if判断ArrayIndexOutOfBoundsException

                if (yi > r.length-1) {
                    yi = r.length-1;
                }
                if (yi>g.length-1) {
                    yi=g.length-1;
                } if (yi>b.length-1) {
                    yi=b.length-1;
                } if (rsum>dv.length-1) {
                    rsum=dv.length-1;
                } if (gsum>dv.length-1) {
                    gsum=dv.length-1;
                } if (bsum>dv.length-1) {
                    bsum=dv.length-1;
                }
                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

    /**
     * 传递bitmap 传递模糊半径 返回一个被模糊的bitmap
     * 调用该方法会ArrayIndexOutOfBoundsException
     *
     * @param sentBitmap
     * @param radius
     * @return
     */
    public static Bitmap createBlurBitmap(Bitmap sentBitmap, int radius) {
        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        if (radius < 1) {
            return (null);
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);
        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;
        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];
        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);

        }
        yw = yi = 0;
        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;
        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                }

            }
            stackpointer = radius;
            for (x = 0; x < w; x++) {
                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];
                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];
                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);

                }
                p = pix[yw + vmin[x]];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];
                yi++;

            }
            yw += w;

        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;
                sir = stack[i + radius];
                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];
                rbs = r1 - Math.abs(i);
                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                }
                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
                        | (dv[gsum] << 8) | dv[bsum];
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];
                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];
                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;

                }
                p = x + vmin[y];
                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];
                yi += w;

            }

        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return (bitmap);
    }

    /**
     * 通过一个网络的路径加载一张图片
     *
     * @param path
     */
    public static void loadBitmap(Context context, final String path, final int width, final int height, final BitmapCallback callback) {
        if (TextUtils.isEmpty(path)) {
            callback.onBitmapLoaded(null);
            return;
        }
        //先去文件中找找  看看有没有下载过
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        final File file = new File(context.getCacheDir(), fileName);
        Bitmap bitmap = loadBitmap(file.getAbsolutePath());
        if (bitmap != null) {
            callback.onBitmapLoaded(bitmap);
            return;
        }
        //文件中没有图片   则去下载
        new AsyncTask<String, String, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... params) {
                Bitmap bitmap;
                try {
                    InputStream is = HttpUtils.get(path);
                    if (width == 0 && height == 0) {
                        bitmap = BitmapFactory.decodeStream(is);
                    } else {
                        bitmap = loadBitmap(is, width, height);
                    }
                    // 图片一旦下载成功 需要存入文件
                    saveBitmap(bitmap, file.getAbsolutePath());
                    return bitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                callback.onBitmapLoaded(bitmap);
            }
        }.execute();

    }

    /**
     * 通过输入流加载一张压缩后的Bitmap
     *
     * @param is     输入流
     * @param width  指定的宽度
     * @param height 指定的高度
     * @return 压缩后的Bitmap
     * @throws IOException
     */
    public static Bitmap loadBitmap(InputStream is, int width, int height) throws IOException {
        //通过is 读取 到一个 byte[]
        /**
         * ByteArrayOutputStream此类实现了一个输出流，其中的数据被写入一个 byte 数组。
         * 缓冲区会随着数据的不断写入而自动增长。可使用 toByteArray() 和 toString() 获取数据。
         * 创建一个新的 byte 数组输出流。缓冲区的容量最初是 32 字节，如有必要可增加其大小。
         */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        //public int read(byte[] buffer)throws IOException从输入流中读取一定数量的字节，并将其存储在缓冲区数组 buffer 中。
        //返回读入缓冲区的总字节数；如果因为已经到达流末尾而不再有数据可用，则返回 -1。
        while ((length = is.read(buffer)) != -1) {// 循环读取输入流中的数据存储到buffer
            // 将 buffer.length 个字节从指定的 byte 数组buffer写入此输出流。write(buffer) 的常规协定是：
            // 应该与调用 write(buffer, 0, buffer.length) 的效果完全相同。
            // public void write(byte[] b,int off,int len) 将指定 byte 数组中从偏移量 off 开始的 len 个字节写入此 byte 数组输出流
            baos.write(buffer, 0, length);// 将buffer中的数据写入到输出流
            baos.flush();//刷新此输出流并强制写出所有缓冲的输出字节。
        }
        // 创建一个新分配的 byte 数组。其大小是此输出流的当前大小，并且缓冲区的有效内容已复制到该数组中。
        byte[] bytes = baos.toByteArray();//返回以 byte 数组的形式返回此输出流的当前内容
        //使用BitmapFactory获取图片的原始宽和高
        BitmapFactory.Options options = new BitmapFactory.Options();
        //仅仅加载图片的边界属性,inJustDecodeBounds：如果设置这个参数为true，就不会给图片分配内存空间，但是可以获取到图片的大小等属性。
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);// 获取图片的尺寸
        //通过目标宽和高计算图片的压缩比例
        int w = options.outWidth / width; // outHeight：图片高，单位像素
        int h = options.outHeight / height; // outWidth：图片宽，单位像素
        int scale = w > h ? h : w; // 以最小比例计算
        //给Options属性设置压缩比例
        options.inJustDecodeBounds = false;
        //        options.inSampleSize = scale;//inSampleSize采样率，这个参数需要是2的幂函数
        options.inSampleSize = calculateInSampleSize(options, width, height);
        //        options.inPreferredConfig = Bitmap.Config.ARGB_4444;//ARGB_4444:每个像素占用2byte内存
        //重新解析byte[] 获取Bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        int bw = 0;
        int bh = 0;
        if (bitmap != null) {
            bw = bitmap.getWidth();
            bh = bitmap.getHeight();
        }
        Log.i("TAG", "loadBitmap: " + "bitmap.width=" + bw + "," + "bitmap.height=" + bh);
        return bitmap;
    }

    public static int calculateInSampleSize(BitmapFactory.Options op, int reqWidth, int reqheight) {
        int originalWidth = op.outWidth;
        int originalHeight = op.outHeight;
        int inSampleSize = 1;
        if (originalWidth > reqWidth || originalHeight > reqheight) {
            int halfWidth = originalWidth / 2;
            int halfHeight = originalHeight / 2;
            while ((halfWidth / inSampleSize >= reqWidth) && (halfHeight / inSampleSize >= reqheight)) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static int calculateInSampleSize2(BitmapFactory.Options options, int reqWidth, int reqheight) {
        int inSampleSize = 1;
        if (options.outHeight > reqheight || options.outWidth > reqWidth) {
            int heightRatio = Math.round((float) options.outHeight / (float) reqheight);// Math.round(num):num+0.5向下取整
            int widthRatio = Math.round((float) options.outWidth / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * 保存图片
     *
     * @param bitmap
     * @param path
     * @throws FileNotFoundException
     */
    public static void saveBitmap(Bitmap bitmap, String path) throws FileNotFoundException {
        if (bitmap == null) {
            return;
        }
        File file = new File(path);
        if (!file.getParentFile().exists()) {//父目录不存在
            file.getParentFile().mkdir();//创建父目录,mkdir()创建此抽象路径名指定的目录。
        }
        FileOutputStream fos = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
    }

    /**
     * 从某个路径下读取一个bitmap
     *
     * @param path
     * @return
     */
    public static Bitmap loadBitmap(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        return BitmapFactory.decodeFile(path);
    }

    /**
     * Bitmap回调接口
     */
    public interface BitmapCallback {
        void onBitmapLoaded(Bitmap bitmap);
    }


}
