package com.hb.report.demo.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "html.convert")
public class HtmlConvertproperties {

    /** 生成的文件保存路径 */
    private String fileSavePath;

    /** echarts转换后的图片保存路径 */
    private String echartsImgSavePath;
}
