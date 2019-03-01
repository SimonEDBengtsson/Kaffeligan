import java.io.*;
import java.util.*;
import java.awt.Image;
import java.awt.image.*;
import java.awt.Color;
import javax.imageio.*;
import javax.imageio.stream.*;
public class Civet{
    private static int balanceIndex=5;// index of balance in csv file
    private static int sadnessLimit=100000,startingBalance=200000;// amount of öre considered a significant loss and starting balance
    private static int fps=5,duration=3;// duration in seconds
    private static String civetPath="dependencies/civet.png";
    public static void writeGIF(String outPath,String inPath)throws java.io.IOException{
        BufferedReader in=new BufferedReader(new FileReader(inPath));
        String mem=null,line;
        while((line=in.readLine())!=null){
            mem=line;// find the last line of the file
        }
        String balanceString=mem.split(";")[balanceIndex];
        String[] denom=balanceString.split(",");// split into kr and öre, then parse separately and recombine
        int balance=Integer.parseInt(denom[0].replaceAll(" ",""))*100+Integer.parseInt(denom[1].replaceAll(" kr",""));
        ImageOutputStream out=new FileImageOutputStream(new File(outPath));// ready the gif writer
        BufferedImage civet=ImageIO.read(new File(civetPath));
        GifSequenceWriter gif=new GifSequenceWriter(out,civet.getType(),1000/fps,false);
        if(balance<=sadnessLimit){// significant loss
            sadCivet(gif,civet,startingBalance-balance);
        }
        else if(balance>startingBalance){// any profit
            happyCivet(gif,civet,balance-startingBalance);
        }
    }
    private static void sadCivet(GifSequenceWriter gif,BufferedImage civet,int deficit)throws java.io.IOException{
        java.awt.Graphics g=civet.getGraphics();
        Kaffeligan.outlinedText(g,"ZKK har gått "+deficit+" öre back",50,100,100);
        gif.writeToSequence(civet);
        for(int i=1;i<duration*fps;i++){
            Kaffeligan.outlinedText(g,"ZKK har gått "+deficit+" öre back",50+i,100,100);
            gif.writeToSequence(civet);
        }
        gif.close();
    }
    private static void happyCivet(GifSequenceWriter gif,BufferedImage civet,int profit){
        
    }
    private static void changeBrightness(BufferedImage image,float brightnessMultiplier){
        WritableRaster raster=image.getRaster();// get image as raster and preallocate arrays
        int[] pixel=null;
        float[] hsbvals=null;
        for(int i=0;i<image.getWidth();i++){// for each pixel get its color and up the brightness through hsb
            for(int j=0;j<image.getHeight();j++){
                pixel=raster.getPixel(i,j,pixel);
                hsbvals=Color.RGBtoHSB(pixel[0],pixel[1],pixel[2],hsbvals);
                Color c=new Color(Color.HSBtoRGB(hsbvals[0],hsbvals[1],hsbvals[2]*brightnessMultiplier));
                raster.setPixel(i,j,new int[]{c.getRed(),c.getGreen(),c.getBlue(),pixel[3]});
            }
        }
    }
}