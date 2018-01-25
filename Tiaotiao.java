import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Tiaotiao {
	
	private static String IMAGE_PATH = "/Users/gavin/Downloads/screenshot.png";
	private static String IMAGE_OUT_PATH = "/Users/gavin/Downloads/jump/screenshot_out";
	private static String mImageOutPath = "";
	private static int mStartY = 450;
	private static int mEndY = 1500;
	private static int mBasicR = 0;
	private static int mBasicG = 0;
	private static int mBasicB = 0;
	private static int mColorOffset = 30;
	private static int mPersonX = 0;
	private static int mPersonY = 0;
	private static int mTargetX = 0;
	private static int mTargetY = 0;
	
	private static int mJumpTime = 0;
	private static int mTargetHeight = 250;
	
	public static void main(String args[]){
		double time = 0;
		mJumpTime = 0;
		while(true){
			try {
				mImageOutPath = IMAGE_OUT_PATH + mJumpTime + ".png";
				getScreenshot();
				getPosition();
				time = Math.sqrt((mTargetX - mPersonX) * (mTargetX - mPersonX) + (mTargetY - mPersonY) * (mTargetY - mPersonY));
				time = time * 1.35;
				System.out.println("Need swipe time = " + time + "\n");
				swipeTime((long) time);
				deleteOldScreenshotOut();
				Thread.sleep(3000); 
				mJumpTime ++;
				if (mJumpTime % 10 == 0){
					mTargetHeight = mTargetHeight - 5;
				}
			} catch (Exception ex){
				
			}
			
		}
		
	}
	
	private static void swipeTime(long ms) {  
        try {  
            Runtime.getRuntime()  
            .exec("/Users/gavin/adt-bundle-mac/sdk/platform-tools/adb shell input swipe 400 400 600 600 " + ms);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
          
    }  
	
	private static void getScreenshot() {  
        try {  
            // 获取手机截图
            Runtime.getRuntime()  
            .exec("/Users/gavin/adt-bundle-mac/sdk/platform-tools/adb shell /system/bin/screencap -p /sdcard/screenshot.png");  
            Thread.sleep(1000);  

            // 上传手机截图到电脑
            Runtime.getRuntime()  
            .exec("/Users/gavin/adt-bundle-mac/sdk/platform-tools/adb pull /sdcard/screenshot.png /Users/gavin/Downloads/screenshot.png");
            Thread.sleep(1000);  
            System.out.print("Get screenshot success!\n");
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
          
    }  
	
	public static void getPosition() throws Exception {  
        int[] rgb = new int[3];  
        File file = new File(IMAGE_PATH);  
        BufferedImage bi = null;  
        try {  
            bi = ImageIO.read(file);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  

        // 只搜索屏幕中间矩形区域，可根据分辨率配置高度
        int width = bi.getWidth();  
        int height = mEndY; 
        int minx = bi.getMinX();  
        int miny = mStartY;  

        int personStartX = 0;
        int personStartY = 0;
        int personEndX = 0;
        int personEndY = 0;
        
        int targetStartX = 0;
        int targetStartY = 0;
        int targetEndX = 0;
        int targetEndY = 0;
        int targetR = 0;
        int targetG = 0;
        int targetB = 0;
        
        // 获取背景色值，这里直接选取坐标（500, 500）这个点的颜色值，不同分辨率手机要依据实际情况修改
        int pixel = bi.getRGB(500, 500);  
        mBasicR = (pixel & 0xff0000) >> 16;  
        mBasicG = (pixel & 0xff00) >> 8;  
        mBasicB = (pixel & 0xff);  
        System.out.println("mBasicR = " + mBasicR + ", mBasicG = " + mBasicG + ", mBasicB = " + mBasicB);
        
        // 获取小人区域和中心点
        for (int j = miny; j < height; j++) {  
        	for (int i = minx; i < width; i++) {  
                pixel = bi.getRGB(i, j); 
                rgb[0] = (pixel & 0xff0000) >> 16;  
                rgb[1] = (pixel & 0xff00) >> 8;  
                rgb[2] = (pixel & 0xff);  

                // 背景颜色跳过
                if (getColorOffset(mBasicR, mBasicG, mBasicB, rgb[0], rgb[1], rgb[2]) < mColorOffset){
                	continue;
                }
               
                // 小人颜色接近点，用取色器取到的小人底部中心点颜色大致为R:55, G:55, B:93，所以可以搜索目标区域的该颜色相近的区域
                // 然后取X, Y坐标最小和最大的两个值
                // 最后计算最大最小值的中间值
                if (rgb[0] >= 50 && rgb[0] <= 60 && rgb[1] >= 50 && rgb[1] <= 60 && rgb[2] >= 90 && rgb[2] <= 95 ){
                	if (personStartX == 0){
                		personStartX = i;
                		personStartY = j;
                		personEndX = i;
                		personEndY = j;
                	}
                	
                	if (i <= personStartX){
                		personStartX = i;
                	}
                	if (j <= personStartY){
                		personStartY = j;
                	}
                	
                	if (i >= personEndX){
                		personEndX = i;
                	}
                	if (j >= personEndY){
                		personEndY = j;
                	}
                }
            }  
        }  
        
        mPersonX = personStartX + ((personEndX - personStartX) / 2) - 15;
        mPersonY = personEndY - 20;
        
        
        
        // 获取下一个物体位置
        for (int j = miny; j < height; j++) {  
        	for (int i = minx; i < width; i++) {  
                pixel = bi.getRGB(i, j); 
                rgb[0] = (pixel & 0xff0000) >> 16;  
                rgb[1] = (pixel & 0xff00) >> 8;  
                rgb[2] = (pixel & 0xff);  

                // 背景颜色跳过
                if (getColorOffset(mBasicR, mBasicG, mBasicB, rgb[0], rgb[1], rgb[2]) < mColorOffset){
                	continue;
                }
                
                // 过滤小人干扰，通过调试发现有时候小人会干扰判断所以要过滤小人所在的纵向区域
                if (Math.abs(i - mPersonX) < 50){
                	continue;
                }
                
                // 从上至下横向便利每个点（排除上面的背景色和小人纵向区域），获取到的第一个其他颜色点即为物体上边缘点
                if (targetStartX == 0){
                	targetStartX = i;
                	targetStartY = j + 10; // 加点偏移量，使其定位到物体较大面积区域的颜色
                	pixel = bi.getRGB(targetStartX, targetStartY); 
                    rgb[0] = (pixel & 0xff0000) >> 16;  
                    rgb[1] = (pixel & 0xff00) >> 8;  
                    rgb[2] = (pixel & 0xff);  
                	targetR = rgb[0];
                	targetG = rgb[1];
                	targetB = rgb[2];
                }
                
                // 根据上面取到的上边缘点，纵向向下搜索相同颜色的点中Y坐标最大的点，注意要根据跳跃的进度修改纵向搜索区域
                // 这里的mTargetHeight 默认值设为250，即搜索纵向250个像素点，然后在主函数那个每10次递减10直到最小值为20
                // 可根据调试结果修改
                if (targetStartX != 0 &&targetStartY != 0){
                	if (i >= targetStartX - 25 && i < targetStartX + 25 && j < targetStartY + mTargetHeight && getColorOffset(targetR, targetG, targetB, rgb[0], rgb[1], rgb[2]) <= 3){
                		targetEndX = i;
                		targetEndY = j;
                	}
                }
            }  
        }  
        
        mTargetX = targetStartX;
        mTargetY = targetStartY + ((targetEndY - targetStartY) / 2) - 10;
        
        System.out.println("mPersonX = " + mPersonX + ", mPsersonY = " + mPersonY + ", mTargetX = " + mTargetX + ", mTargetY = " + mTargetY);

        // 将处理完后的图片，如小人位置，物体上下边缘点和中心点、搜索矩形区域绘制好后保存，以便调试调整参数
        drawPoint(IMAGE_PATH, Color.green, mPersonX, mPersonY);
        drawPoint(mImageOutPath, Color.red, targetStartX, targetStartY);
        drawPoint(mImageOutPath, Color.red, targetEndX, targetEndY);
        drawPoint(mImageOutPath, Color.red, mTargetX, mTargetY);
        drawRect(mImageOutPath, 0, mStartY, width, mEndY - mStartY);
        
    }  
	
	public static void drawRect(String path, int x, int y, int width, int height) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        Graphics g = image.getGraphics();
//        Graphics2D d2 = (Graphics2D) g;
        g.setColor(Color.RED);//画笔颜色
        g.drawRect(x, y, width, height);
        FileOutputStream out = new FileOutputStream(mImageOutPath);//输出图片的地址
        ImageIO.write(image, "png", out);
    }
	
	public static void drawPoint(String path, Color color, int x, int y) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        Graphics g = image.getGraphics();
//        Graphics2D d2 = (Graphics2D) g;
        g.setColor(color);//画笔颜色
        g.fillOval(x, y, 20, 20);
        FileOutputStream out = new FileOutputStream(mImageOutPath);//输出图片的地址
        ImageIO.write(image, "png", out);
    }
	
	public static int getColorOffset(int r0, int g0, int b0, int r1, int g1, int b1){
		return Math.abs(r0 - r1) + Math.abs(g0 - g1) + Math.abs(b0 - b1);
	}
	
	public static void deleteOldScreenshotOut(){
		try {
			File file = new File(IMAGE_OUT_PATH + (mJumpTime - 50) + ".png");
			if (file.exists()){
				file.delete();
			}
		} catch (Exception ex){
			
		}
	}
}
