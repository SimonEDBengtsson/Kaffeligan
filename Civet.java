import java.io.*;
import java.util.*;
import java.awt.Image;
import java.awt.image.*;
import java.awt.Color;
import javax.imageio.*;
import javax.imageio.stream.*;
public class Civet{
    protected static int balanceIndex=5;// index of balance in csv file
    protected static int sadnessLimit=100000,startingBalance=200000;// amount of öre considered a significant loss and starting balance
    protected static int fps=5,duration=5;// duration in seconds
    protected static String civetPath="dependencies/civet.png";
    protected static String tearPath="dependencies/tear.png";
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
        int width=1920,height=1080,rightEyeX=1310,rightEyeY=580,leftEyeX=886,leftEyeY=650;
        int tearSpreadX=41,tearSpreadY=21;
        java.awt.Graphics g=civet.getGraphics();
        centeredOutlinedText(g,"ZKK har gått back "+Kaffeligan.CSEKtoString(deficit),width,0,100);
        gif.writeToSequence(civet);
        ArrayList<Tear> tears=new ArrayList<Tear>();
        for(int i=1;i<duration*fps;i++){
            BufferedImage frame=new BufferedImage(civet.getWidth(),civet.getHeight(),civet.getType());
            g=frame.getGraphics();
            g.drawImage(civet,0,0,null);
            if(i%2==0){
                tears.add(new Tear(leftEyeX+(int)(Math.random()*tearSpreadX)-tearSpreadX/2,leftEyeY+(int)(Math.random()*tearSpreadY),5,10));
            }
            else{
                tears.add(new Tear(rightEyeX+(int)(Math.random()*tearSpreadX)-tearSpreadX/2,rightEyeY+(int)(Math.random()*tearSpreadY),5,10));
            }
            for(int j=0;j<tears.size();j++){
                Tear t=tears.get(j);
                if(t.outOfBounds(width,height)){
                    tears.remove(j);
                    j--;
                    continue;
                }
                t.paint(g);
                t.tick();
            }
            gif.writeToSequence(frame);
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
    public static void centeredOutlinedText(java.awt.Graphics g,String text,int width,int y,int fontSize){
        java.awt.Font font=new java.awt.Font("Impact",java.awt.Font.BOLD,fontSize);
        y+=g.getFontMetrics(font).getAscent();
        int x=(width-g.getFontMetrics(font).charsWidth(text.toCharArray(),0,text.toCharArray().length))/2;
        g.setFont(new java.awt.Font("Impact",java.awt.Font.BOLD,fontSize));
        g.setColor(new java.awt.Color(50,50,50));
        g.drawString(text,x+1,y);// write black text shifted in each direction
        g.drawString(text,x-1,y);
        g.drawString(text,x,y+1);
        g.drawString(text,x,y-1);
        g.drawString(text,x+1,y+1);
        g.drawString(text,x+1,y-1);
        g.drawString(text,x-1,y+1);
        g.drawString(text,x-1,y-1);
        g.setColor(new java.awt.Color(250,250,250));
        g.drawString(text,x,y);// put the white text on top
    }
    private static class Tear{
        private static final int tearHeight=60,tearWidth=40;
        private static BufferedImage tear;
        static{
            try{
                BufferedImage sourceTear=ImageIO.read(new File(tearPath));
                tear=new BufferedImage(tearWidth,tearHeight,sourceTear.getType());
                tear.getGraphics().drawImage(sourceTear.getScaledInstance(tearWidth,tearHeight,Image.SCALE_SMOOTH),0,0,null);
            }
            catch(java.io.IOException x){
                tear=null;
            }
        }
        int x,y,v,a;
        public Tear(int x,int y,int v0,int a){// x is middle, y is top
            this.x=x;
            this.y=y;
            v=v0;
            this.a=a;
        }
        public void paint(java.awt.Graphics g){
            g.drawImage(tear,x+tearWidth/20,y,null);
        }
        public void tick(){
            y+=v;
            v+=a;
        }
        public boolean outOfBounds(int width,int height){
            if(y<=-tearHeight || y>=height || x<=-tearWidth/2 || x>=width+tearWidth/2){
                return true;
            }
            return false;
        }
    }
}