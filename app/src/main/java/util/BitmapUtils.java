package util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
     * 通过一个网络的路径加载一张图片
     * @param path
     */
    public static void loadBitmap(Context context, final String path, final BitmapCallback callback) {
        //先去文件中找找  看看有没有下载过
        String fileName = path.substring(path.lastIndexOf("/")+1);
        File file = new File(context.getCacheDir(),fileName);
        Bitmap bitmap = loadBitmap(file.getAbsolutePath());
        if (bitmap!=null) {
            callback.onBitmapLoaded(bitmap);
            return;
        }
        //文件中没有图片   则去下载
        new AsyncTask<String, String, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... params) {
                try {
                    InputStream is = HttpUtils.get(path);
                    Bitmap bitmap = loadBitmap(is, 50, 50);
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
            baos.write(buffer,0,length);// 将buffer中的数据写入到输出流
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
        options.inPreferredConfig = Bitmap.Config.ARGB_4444;//ARGB_4444:每个像素占用2byte内存
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
    public interface BitmapCallback{
        void onBitmapLoaded(Bitmap bitmap);
    }


}
