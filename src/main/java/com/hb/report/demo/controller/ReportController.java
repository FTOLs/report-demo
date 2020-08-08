package com.hb.report.demo.controller;

import com.github.abel533.echarts.Option;
import com.github.abel533.echarts.axis.CategoryAxis;
import com.github.abel533.echarts.axis.ValueAxis;
import com.github.abel533.echarts.code.MarkType;
import com.github.abel533.echarts.code.Trigger;
import com.github.abel533.echarts.data.PointData;
import com.github.abel533.echarts.json.GsonUtil;
import com.github.abel533.echarts.series.Bar;
import com.hb.report.demo.utils.EchartsUtils;
import com.hb.report.demo.utils.HtmlConverter;
import com.hb.report.demo.utils.RedisOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

@Controller
public class ReportController {

    @Autowired
    RedisOperator redisOperator;

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @RequestMapping("test")
    public ModelAndView test(){
        Option option = new Option();
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
        String base64Img = EchartsUtils.generateEChart(format,800,900, "/Users/huangbing/Desktop/echartsImages/", true);
        return new ModelAndView("test").addObject("img", base64Img);
    }

    @RequestMapping("report")
    public ModelAndView report(){
        return new ModelAndView("report");
    }

    @RequestMapping("ckEditor")
    public String ckEditor(){
        return "ckEditor";
    }

    @PostMapping("saveReport")
    @ResponseBody
    public String saveReport(String html){
        redisOperator.set("ckEditor", html);
        return "ok";
    }

    @GetMapping("viewReport")
    public ModelAndView viewReport(){
        String html = redisOperator.get("ckEditor");
        String path = genEchartImg();
        html = html.replaceAll("\\[img1\\]", "<img src=\""+path+"\" />");
        return new ModelAndView("reportTemplate").addObject("html", html);
    }

    private String genEchartImg() {
        Option option = new Option();
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
        String realPath = ReportController.class.getResource("/").getPath() + "static/echartsImages/";

        String filePath = EchartsUtils.generateEChart(format,800,900, realPath, false);

        return filePath.replace(realPath, "/static/echartsImages/");
    }

    @GetMapping("dowonloadReport")
    @ResponseBody
    public void dowonloadReport(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String url = "http://192.168.20.56:8080/viewReport";
        File file = new HtmlConverter.Builder().saveFilePath("/Users/huangbing/Desktop/echartsImages/")
                .builder()
                .saveUrlToDocx(url, "test");
        String aFileName = "报告.docx";
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try{
            request.setCharacterEncoding("UTF-8");
            String agent = request.getHeader("User-Agent").toUpperCase();
            if ((agent.indexOf("MSIE") > 0) || ((agent.indexOf("RV") != -1) && (agent.indexOf("FIREFOX") == -1)))
                aFileName = URLEncoder.encode(aFileName, "UTF-8");
            else {
                aFileName = new String(aFileName.getBytes("UTF-8"), "ISO8859-1");
            }
            response.setContentType("application/x-msdownload;");
            response.setHeader("Content-disposition", "attachment; filename=" + aFileName);
            response.setHeader("Content-Length", String.valueOf(file.length()));
            bis = new BufferedInputStream(new FileInputStream(file));
            bos = new BufferedOutputStream(response.getOutputStream());
            byte[] buff = new byte[2048];
            int bytesRead;
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length)))
                bos.write(buff, 0, bytesRead);
            System.out.println("success");
            bos.flush();
        }catch (Exception e) {
            // TODO: handle exception
            System.out.println("导出文件失败！");
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
                file.delete();
            } catch (Exception e) {
            }
        }
    }


}
