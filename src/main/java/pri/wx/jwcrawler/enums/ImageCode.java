package pri.wx.jwcrawler.enums;

import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by WX on 2017/6/28 0028.
 * Changed by WX on 2018/7/24
 * 识别验证码程序(限于嘉庚教务系统验证码)
 * <p>全局单例</p>
 *
 * @author wx
 */

public enum ImageCode {

    INSTANCE;

    private BufferedImage image = null;

    ImageCode() { }

    //获得RGB数据数组
    private int[][] getRGBS(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[][] a = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++)
                a[x][y] = img.getRGB(x, y);
        }
        return a;
    }

    //是否为干扰点
    private boolean isWhite(int[][] rgbs, int x, int y) {
        int result = 0;
        if (rgbs[x][y] == Color.WHITE.getRGB())
            return false;
        if (x + 1 < rgbs.length && rgbs[x + 1][y] == rgbs[x][y])
            result += 1;
        if (x - 1 > 0 && rgbs[x - 1][y] == rgbs[x][y])
            result += 1;
        if (y + 1 < rgbs[0].length && rgbs[x][y + 1] == rgbs[x][y])
            result += 1;
        if (y - 1 > 0 && rgbs[x][y - 1] == rgbs[x][y])
            result += 1;

        return result == 0;
    }

    //是否为列背景
    private boolean isBackcol(int[][] rgbs, int x) {
        for (int i = 0; i < rgbs[0].length; i++) {
            if (rgbs[x][i] != Color.WHITE.getRGB())
                return false;
        }
        return true;
    }

    //是否为行背景
    private boolean isBackline(int[][] rgbs, int y) {
        for (int i = 0; i < rgbs.length; i++) {
            if (rgbs[i][y] != Color.WHITE.getRGB())
                return false;
        }
        return true;
    }

    //根据base64下载图片(BufferedImage)
    private boolean generateImage(String imgStr) {
        //对字节数组字符串进行Base64解码并生成图片
        if (imgStr == null || "".equals(imgStr)) //图像数据为空
            return false;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            //Base64解码
            byte[] b = decoder.decodeBuffer(imgStr);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {//调整异常数据
                    b[i] += 256;
                }
            }

            ByteArrayInputStream in = new ByteArrayInputStream(b);
            //image = ImageIO.read(in);

            //生成jpeg图片
            /*
            String imgFilePath = "./ccccccccccccc.jpg";// + imageName;//新生成的图片
            FileOutputStream out = new FileOutputStream(imgFilePath);

            out.write(b,0,b.length);
            out.flush();
            out.close();*/
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //移除背景
    private BufferedImage removeBackground(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[][] rgbs = new int[width][height];
        int pointcolor = 0;

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                rgbs[x][y] = img.getRGB(x, y);
            }
        }

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                if (isWhite(rgbs, x, y)) {
                    pointcolor = rgbs[x][y];
                    x = width;
                    y = height;
                }
            }
        }
        if (pointcolor != 0) {
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    if (rgbs[x][y] == pointcolor) {
                        img.setRGB(x, y, Color.WHITE.getRGB());
                    } else if (rgbs[x][y] != Color.WHITE.getRGB()) {
                        img.setRGB(x, y, Color.BLACK.getRGB());
                    }
                }
            }
        }
        return img;
    }

    //切割
    private List<BufferedImage> splitImage(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[][] rgbs = getRGBS(img);

        List<BufferedImage> subImgs = new ArrayList<>();
        List<Integer> weightlist = new ArrayList<>();

        //横向记录坐标
        for (int x = 0; x < width - 1; ++x) {

            if (x == 0 && !isBackcol(rgbs, x)) {
                weightlist.add(x);
            } else if (isBackcol(rgbs, x) && !isBackcol(rgbs, x + 1)) {
                if (x - 1 < 0 || isBackcol(rgbs, x - 1))
                    //if(x-2 < 0 || isBackcol(rgbs,x-2))
                    weightlist.add(x + 1);
            }
            if (x + 1 == width - 1 && !isBackcol(rgbs, x + 1)) {
                weightlist.add(x + 2);
            } else if (isBackcol(rgbs, x + 1) && !isBackcol(rgbs, x)) {
                if (x + 2 >= width || isBackcol(rgbs, x + 2))
                    //if(x+3>=width||isBackcol(rgbs,x+3))
                    weightlist.add(x + 1);
            }
        }

        for (int i = 0; i < 4; i++) {
            //横向切割
            BufferedImage vimg = img.getSubimage(weightlist.get(2 * i), 0, weightlist.get(2 * i + 1) - weightlist.get(2 * i), height);
            int[][] vrgbs = getRGBS(vimg);
            //纵向记录坐标
            int y1 = 0, y2 = 0;

            for (int y = 0; y < vimg.getHeight() - 1; ++y) {
                if (y == 0 && !isBackline(vrgbs, y))
                    y1 = 0;
                else if (isBackline(vrgbs, y) && !isBackline(vrgbs, y + 1))
                    y1 = y;
                if (!isBackline(vrgbs, vimg.getHeight() - 1))
                    y2 = vimg.getHeight() - 1;
                else if (!isBackline(vrgbs, y) && isBackline(vrgbs, y + 1)) {
                    if (y + 2 >= vimg.getHeight() || isBackline(vrgbs, y + 2))
                        y2 = y;
                }
            }
            //纵向切割
            subImgs.add(vimg.getSubimage(0, y1 + 1, vimg.getWidth(), y2 - y1));
        }
        return subImgs;

    }

    //相似度计算
    private int isLike(int[][] a, int[][] b) {
        //a模版 b样本
        int width = a.length;
        int height = a[0].length;
        int like = 0, all = 0, all2 = 0;
        if (b.length < width)
            width = b.length;
        if (b[0].length < height)
            height = b[0].length;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (b[x][y] != Color.WHITE.getRGB()) {
                    all++;
                    if (a[x][y] == b[x][y])
                        like++;
                }
                if (a[x][y] != Color.WHITE.getRGB())
                    all2++;
            }
        }
        if (all < all2)
            all = all2;
        return like * 1000 / all;
    }

    //匹配且返回字符
    private char getCode(BufferedImage img) {
        char result = 'A';
        int max = 0;
        for (int i = 0; i < 26; i++) {

            if (i == 8 || i == 14 || i == 16)
                ;
            else {
                char now = (char) (65 + i);
                //System.out.println(letterPath + now + ".jpg");
                int[][] temrgbs = Alphabet.getABC(now).getAlpha();
                int[][] rgbs = getRGBS(img);
                int likenum = isLike(temrgbs, rgbs);
                if (likenum > max) {
                    max = likenum;
                    result = now;
                }
            }
        }

        return result;
    }

    public void setImage(byte[] bytes) {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);    //将b作为输入流;
        try {
            image = ImageIO.read(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回验证码字符串
     * @return 验证码
     */
    public String getImageCode() {
        String imgstr = "";
        if (image == null && !generateImage(imgstr)) {
            return "error";
        } else {
            BufferedImage img = removeBackground(image);
            //ImageIO.write(img, "PNG", new File(path +"/result.jpg"));
            List<BufferedImage> imgss = splitImage(img);
            StringBuilder str = new StringBuilder();
            for (BufferedImage imgs : imgss) {
                str.append(getCode(imgs));
                //System.out.println(getCode(imgss.get(i)));
                //ImageIO.write(imgss.get(i), "PNG", new File(path + "/split" + i + ".jpg"));
            }
            return str.toString();
        }
    }
}
