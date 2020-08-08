package com.hb.report.demo.utils;

import com.github.abel533.echarts.Option;
import com.github.abel533.echarts.axis.CategoryAxis;
import com.github.abel533.echarts.axis.ValueAxis;
import com.github.abel533.echarts.code.MarkType;
import com.github.abel533.echarts.code.Trigger;
import com.github.abel533.echarts.data.PointData;
import com.github.abel533.echarts.json.GsonUtil;
import com.github.abel533.echarts.series.Bar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Echarts工具类(将Echarts生成的图表转换为图片)
 *
 * 需要安装phantomjs服务
 *
 * @author: huangbing
 * @date: 2020/8/7 4:20 下午
 */
public class EchartsUtils {

    private static final Logger logger = LoggerFactory.getLogger(EchartsUtils.class);

    private static final String JS_PATH = EchartsUtils.class.getResource("/").getPath() + "static/js/echarts-convert1.js";

    private static final String FILE_PREFIX = "echart-";

    /**
     * 生成Echart图片
     * @param options
     * @param width
     * @param height
     * @param imagesPath
     * @param needBase64
     * @return
     */
    public static String generateEChart(String options, int width, int height, String imagesPath, boolean needBase64) {
        String fileName= FILE_PREFIX + UUID.randomUUID().toString().substring(0, 8) + ".png";

        String imgPath = imagesPath + fileName;

        //数据json
        String dataPath = writeFile(options, imagesPath);
        try {
            //文件路径（路径+文件名）
            File file = new File(imgPath);
            //文件不存在则创建文件，先创建目录
            if (!file.exists()) {
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            String cmd = "phantomjs " + JS_PATH + " -infile " + dataPath + " -outfile " + imgPath + " -width " + width + " -height " + height;
            String[] s = cmd.split(" ");
            Process process = Runtime.getRuntime().exec(s);
            if(logger.isDebugEnabled()){
                System.out.println(cmd);
            }
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = input.readLine()) != null);

            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(needBase64){
                String base64Img = ImageToBase64(imgPath);
                deleteFile(imgPath);
                // 删除数据文件
                deleteFile(dataPath);
                return base64Img.replaceAll("\\s*", "");
            }
            // 删除数据文件
            deleteFile(dataPath);
            return imgPath;

        }
    }

    public static String writeFile(String options, String imagesPath) {
        String dataPath= imagesPath + UUID.randomUUID().toString().substring(0, 8) +".json";
        try {
            /* 写入Txt文件 */
            // 相对路径，如果没有则要建立一个新的output.txt文件
            File writename = new File(dataPath);
            //文件不存在则创建文件，先创建目录
            if (!writename.exists()) {
                File dir = new File(writename.getParent());
                dir.mkdirs();
                writename.createNewFile(); // 创建新文件
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));
            // \r\n即为换行
            out.write(options);
            out.flush(); // 把缓存区内容压入文件
            out.close(); // 最后记得关闭文件
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataPath;
    }

    /**
     * 图片文件转为base64
     * @param imgPath
     */
    private static String ImageToBase64(String imgPath) {
        byte[] data = null;
        // 读取图片字节数组
        try {
            InputStream in = new FileInputStream(imgPath);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        // 返回Base64编码过的字节数组字符串
        return encoder.encode(Objects.requireNonNull(data));
    }

    /**
     * 删除文件
     *
     * @param pathname
     * @return
     * @throws IOException
     */
    public static boolean deleteFile(String pathname){
        boolean result = false;
        File file = new File(pathname);
        if (file.exists()) {
            file.delete();
            result = true;
            if(logger.isDebugEnabled()){
                System.out.println("文件已经被成功删除");
                logger.info("文件已经被成功删除");
            }
        }
        return result;
    }

    public static void main(String[] args) {
        Option option = new Option();
        option.setAnimation(false);
        option.title().text("某地区蒸发量和降水量").subtext("纯属虚构");
        option.tooltip().trigger(Trigger.axis);
        option.legend("蒸发量", "降水量");
        option.calculable(true);
        option.xAxis(new CategoryAxis().data("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"));
        option.yAxis(new ValueAxis());

        Bar bar = new Bar("蒸发量");
        bar.data(2.0, 4.9, 7.0, 23.2, 25.6, 76.7, 135.6, 162.2, 32.6, 20.0, 6.4, 3.3);
        bar.markPoint().data(new PointData().type(MarkType.max).name("最大值"), new PointData().type(MarkType.min).name("最小值"));
        bar.markLine().data(new PointData().type(MarkType.average).name("平均值"));

        Bar bar2 = new Bar("降水量");
        List<Double> list = Arrays.asList(2.6, 5.9, 9.0, 26.4, 28.7, 70.7, 175.6, 182.2, 48.7, 18.8, 6.0, 2.3);
        bar2.data(list);
        bar2.markPoint().data(new PointData("年最高", 182.2).xAxis(7).yAxis(183).symbolSize(18), new PointData("年最低", 2.3).xAxis(11).yAxis(3));
        bar2.markLine().data(new PointData().type(MarkType.average).name("平均值"));

        option.series(bar, bar2);
        String format = GsonUtil.format(option);
        String base64Img = generateEChart(format,900,600, "/Users/huangbing/Desktop/echartsImages/", true);
        System.out.println(base64Img);
    }
}