package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Qubo on 2016/10/1.
 * 封装联网操作
 */
public class HttpUtils {
    /**
     * 发送get请求 获取服务端返回的输入流
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static InputStream get(String path) throws IOException {
        // 1.创建URL对象
        URL url = new URL(path);
        // 2.获取HttpURLConnection对象
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // 3.设置请求方法为GET
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);
        // 4.直接获取输入流
        InputStream is = connection.getInputStream();
        return is;
    }

    public static InputStream post(String path, Map<String, String> paramMap) throws IOException {
        // 1.创建URL对象
        URL url = new URL(path);
        // 2.获取HttpURLConnection对象
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // 3.设置请求方法为POST
        connection.setRequestMethod("POST");
        // 4.设置输入输出流(post)
        connection.setDoInput(true);
        connection.setDoOutput(true);
        // 5.设置不使用缓存(post)
        connection.setUseCaches(false);
        // 6.设置请求头信息,告诉服务端将我的请求参数转换 为键值对形式
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        // 7.准备传递的参数
        StringBuffer params = new StringBuffer();//线程安全的可变字符序列
        if (paramMap != null) {
            // Set一个不包含重复元素的 collection
            Set<String> keys = paramMap.keySet();//返回此映射中包含的键的 Set 视图
            Iterator<String> iterator = keys.iterator();// 返回在此 set 中的元素上进行迭代的迭代器
            while (iterator.hasNext()) {//如果仍有元素可以迭代，则返回 true。
                String key = iterator.next();//返回迭代的下一个元素
                String value = paramMap.get(key);
                //把key=value拼接到params字符串中
                params.append(key + "=" + value + "&");
            }
            //key=value&key=value&key=value&
            params.deleteCharAt(params.length() - 1);
            //把params作为参数输出给服务端
            // 8.获取输出流
            OutputStream os = connection.getOutputStream();
            // 9.给服务器写数据
            os.write(params.toString().getBytes("utf-8"));
            // 10.流的 冲刷
            os.flush();
        }
        // 11.获取输入流
        InputStream is = connection.getInputStream();
        return is;
    }

    /**
     * 把输入流 按照utf-8编码解析为字符串
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String isToString(InputStream inputStream) throws IOException {
        StringBuffer sb = new StringBuffer();
        String line;
        //创建一个使用默认大小输入缓冲区的缓冲字符输入流
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        while ((line = reader.readLine()) !=null) {
            sb.append(line);
        }
        return sb.toString();
    }


}
