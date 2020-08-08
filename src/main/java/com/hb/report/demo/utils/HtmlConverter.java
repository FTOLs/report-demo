package com.hb.report.demo.utils;

import org.docx4j.Docx4J;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.PageSizePaper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.RFonts;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStream;
import java.net.URL;

import static com.hb.report.demo.utils.HtmlConverter.RemoveTag.*;

/**
 * html转换工具类
 *
 * 图片长宽乘积不能太大，不然会导致内存溢出
 *
 * HtmlConverter
 * @author: huangbing
 * @date: 2020/8/7 2:32 下午
 */
public class HtmlConverter {

    /**
     * 页面大小
     */
    public enum PageSize {
        /** 大小*/
        LETTER("letter"),
        LEGAL("legal"),
        A3("A3"),
        A4("A4"),
        A5("A5"),
        B4JIS("B4JIS");

        PageSize(String code){
            this.code = code;
        }
        private String code;

        public String getCode() {
            return code;
        }
    }

    /**
     * 移除的标签
     */
    enum RemoveTag {
        /** 移除的标签*/
        SCRIPT("script"), A("a"), LINK("link"), HREF("href");

        RemoveTag(String code){
            this.code = code;
        }
        private String code;

        public String getCode() {
            return code;
        }
    }

    /**
     * 参数类
     */
    private static class Params {

        /** 默认字体库*/
        private final static String DEFAULT_FONT_FAMILY = "STSongStd-Light";
        /** 默认字体库路径*/
        private final static String DEFAULT_FONT_PATH = "/static/fonts/STSongStd-Light.ttf";
        /** 默认是否横版*/
        private final static boolean DEFAULT_LAND_SCAPE = false;
        /** 默认页面尺寸*/
        private final static String DEFAULT_PAGE_SIZE = PageSize.A4.getCode();
        /** 字体库*/
        private String fontFamily = DEFAULT_FONT_FAMILY;
        /** 字体库路径*/
        private String fontPath = DEFAULT_FONT_PATH;
        /** 页面尺寸*/
        private String pageSize = DEFAULT_PAGE_SIZE;
        /** 是否横版*/
        private boolean isLandScape = DEFAULT_LAND_SCAPE;
        /** 保存的文件的路径 */
        private String saveFilePath = HtmlConverter.class.getResource("/").getPath() + "output/";

    }

    private final Logger logger = LoggerFactory.getLogger(HtmlConverter.class);

    private Builder builder;

    public HtmlConverter(Builder builder) {
        this.builder = builder;
    }

    /**
     * 构建类
     */
    public static class Builder {

        private Params params;

        public Builder() {
            this.params = new Params();
            this.params.fontFamily = Params.DEFAULT_FONT_FAMILY;
            this.params.fontPath = Params.DEFAULT_FONT_PATH;
            this.params.pageSize = Params.DEFAULT_PAGE_SIZE;
            this.params.isLandScape = Params.DEFAULT_LAND_SCAPE;
        }

        public Builder fontFamily(String fontFamily) {
            this.params.fontFamily = fontFamily;
            return this;
        }

        public Builder fontPath(String fontPath) {
            this.params.fontPath = fontPath;
            return this;
        }

        public Builder pageSize(String pageSize) {
            this.params.pageSize = pageSize;
            return this;
        }

        public Builder isLandScape(boolean isLandScape) {
            this.params.isLandScape = isLandScape;
            return this;
        }

        public Builder saveFilePath(String saveFilePath) {
            this.params.saveFilePath = saveFilePath;
            return this;
        }

        /**
         * 数据处理完毕之后处理逻辑放在构造函数里面
         *
         * @return
         */
        public HtmlConverter builder() {
            return new HtmlConverter(this);
        }

    }

    /**
     * 将页面保存为 docx
     *
     * @param url
     * @param fileName
     * @return
     * @throws Exception
     */
    public File saveUrlToDocx(String url, String fileName) throws Exception {
        return saveDocx(url2word(url), fileName);
    }

    /**
     * 将页面保存为 pdf
     *
     * @param url
     * @param fileName
     * @return
     * @throws Exception
     */
    public File saveUrlToPdf(String url, String fileName) throws Exception {
        return savePdf(url2word(url), fileName);
    }

    /**
     * 将页面转为 {@link org.docx4j.openpackaging.packages.WordprocessingMLPackage}
     *
     * @param url
     * @return
     * @throws Exception
     */
    public WordprocessingMLPackage url2word(String url) throws Exception {
        return xhtml2word(url2xhtml(url));
    }

    /**
     * 将 {@link org.docx4j.openpackaging.packages.WordprocessingMLPackage} 存为 docx
     *
     * @param wordMLPackage
     * @param fileName
     * @return
     * @throws Exception
     */
    public File saveDocx(WordprocessingMLPackage wordMLPackage, String fileName) throws Exception {
        File file = new File(genFilePath(fileName) + ".docx");
        //保存到 docx 文件
        wordMLPackage.save(file);

        if (logger.isDebugEnabled()) {
            logger.debug("Save to [.docx]: {}", file.getAbsolutePath());
        }

        return file;
    }

    /**
     * 将 {@link org.docx4j.openpackaging.packages.WordprocessingMLPackage} 存为 pdf
     *
     * @param wordMLPackage
     * @param fileName
     * @return
     * @throws Exception
     */
    public File savePdf(WordprocessingMLPackage wordMLPackage, String fileName) throws Exception {

        File file = new File(genFilePath(fileName) + ".pdf");

        OutputStream os = new java.io.FileOutputStream(file);

        Docx4J.toPDF(wordMLPackage, os);

        os.flush();
        os.close();

        if (logger.isDebugEnabled()) {
            logger.debug("Save to [.pdf]: {}", file.getAbsolutePath());
        }
        return file;
    }

    /**
     * 将 {@link org.jsoup.nodes.Document} 对象转为 {@link org.docx4j.openpackaging.packages.WordprocessingMLPackage}
     * xhtml to word
     *
     * @param doc
     * @return
     * @throws Exception
     */
    protected WordprocessingMLPackage xhtml2word(Document doc) throws Exception {
        //A4纸，//横版:true
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage(PageSizePaper.valueOf(this.builder.params.pageSize), this.builder.params.isLandScape);

        //配置中文字体
        configSimSunFont(wordMLPackage);

        XHTMLImporterImpl xhtmlImporter = new XHTMLImporterImpl(wordMLPackage);

        //导入 xhtml
        wordMLPackage.getMainDocumentPart().getContent().addAll(
                xhtmlImporter.convert(doc.html(), doc.baseUri()));

        return wordMLPackage;
    }

    /**
     * 将页面转为{@link org.jsoup.nodes.Document}对象，xhtml 格式
     *
     * @param url
     * @return
     * @throws Exception
     */
    protected Document url2xhtml(String url) throws Exception {
        // 添加头部授权参数防止被过滤
        String token = AESEncryptUtils.aesEncryptToString("html2File");

        Document doc = Jsoup.connect(url).header("Authorization", token).get();

        if (logger.isDebugEnabled()) {
            logger.debug("baseUri: {}", doc.baseUri());
        }
        //除去所有 script
        for (Element script : doc.getElementsByTag(SCRIPT.getCode())) {
            script.remove();
        }

        //除去 a 的 onclick，href 属性
        for (Element a : doc.getElementsByTag(A.getCode())) {
            a.removeAttr("onclick");
            a.removeAttr("href");
        }
        //将link中的地址替换为绝对地址
        Elements links = doc.getElementsByTag(LINK.getCode());
        for (Element element : links) {
            String href = element.absUrl(HREF.getCode());

            if (logger.isDebugEnabled()) {
                logger.debug("href: {} -> {}", element.attr(HREF.getCode()), href);
            }

            element.attr(HREF.getCode(), href);
        }

        //转为 xhtml 格式
        doc.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)
                .escapeMode(Entities.EscapeMode.xhtml);

        if (logger.isDebugEnabled()) {
            String[] split = doc.html().split("\n");
            for (int c = 0; c < split.length; c++) {
                logger.debug("line {}:\t{}", c + 1, split[c]);
            }
        }
        return doc;
    }

    /**
     * 为 {@link org.docx4j.openpackaging.packages.WordprocessingMLPackage} 配置中文字体
     *
     * @param wordMLPackage
     * @throws Exception
     */
    protected void configSimSunFont(WordprocessingMLPackage wordMLPackage) throws Exception {
        Mapper fontMapper = new IdentityPlusMapper();
        wordMLPackage.setFontMapper(fontMapper);

        //加载字体文件（解决linux环境下无中文字体问题）
        URL simsunUrl = this.getClass().getResource(this.builder.params.fontPath);
        PhysicalFonts.addPhysicalFont(simsunUrl);
        PhysicalFont simsunFont = PhysicalFonts.get(this.builder.params.fontFamily);
        fontMapper.put(this.builder.params.fontFamily, simsunFont);
        //设置文件默认字体
        RFonts rfonts = Context.getWmlObjectFactory().createRFonts();
        rfonts.setAsciiTheme(null);
        rfonts.setAscii(this.builder.params.fontFamily);
        wordMLPackage.getMainDocumentPart().getPropertyResolver()
                .getDocumentDefaultRPr().setRFonts(rfonts);
    }

    /**
     * 生成文件位置
     *
     * @return
     */
    protected String genFilePath(String fileName) {
        return this.builder.params.saveFilePath + fileName;
    }

    public static void main(String[] args) throws Exception {
//        //输入要转换的网址
//        String url = "http://192.168.20.56:8080/viewReport";
//        new Builder().saveFilePath("/Users/huangbing/Desktop/echartsImages/")
//                     .builder()
//                     .saveUrlToDocx(url, "test");

        String s = "[img1] [img1] [img1]";
        String s1 = s.replaceAll("\\[img1\\]", "22");
        System.out.println(s1);
    }
}