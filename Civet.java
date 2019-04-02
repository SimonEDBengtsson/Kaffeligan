import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.AffineTransform;
import java.awt.Color;
import javax.imageio.*;
import javax.imageio.stream.*;
public class Civet{
    final static long msmonth=2628000000L,msweek=604800000L,msday=86400000L;// conversion constants milliseconds in a month/week/day
    protected static int width=1920,height=1080;
    protected static int rightEyeX=1334,rightEyeY=580,leftEyeX=888,leftEyeY=648;// coordinates for the eyes
    protected static int rightCheekX=1400,rightCheekY=700,leftCheekX=800,leftCheekY=780;
    protected static int smileX=1212,smileY=932,smileWidth=200,smileHeight=70;
    protected static int fps=5,duration=5;// duration in seconds
    protected static String civetPath="resources/civet.png";
    protected static String tearPath="resources/tear.png";
    protected static String sparklePath="resources/sparkle.png";
    protected static String smilePath="resources/mouth.png";
    public static void writeGIF(String outPath,CustomerData cd)throws Exception{
        if(!outPath.matches(".*\\.gif$")){
            throw new Exception("Filetype not supported");
        }
        ImageOutputStream out=new FileImageOutputStream(new File(outPath));// ready the gif writer
        BufferedImage civet=ImageIO.read(GUI.load(civetPath));
        GifSequenceWriter gif=new GifSequenceWriter(out,civet.getType(),1000/fps,true);
        if(cd.startBalance<cd.endBalance){// loss
            sadCivet(gif,civet,cd.endBalance-cd.startBalance,cd.startDate-cd.endDate);
        }
        else if(cd.startBalance>cd.endBalance){// gain
            happyCivet(gif,civet,cd.startBalance-cd.endBalance,cd.startDate-cd.endDate);
        }
    }
    private static void sadCivet(GifSequenceWriter gif,BufferedImage civet,int deficit,long time)throws java.io.IOException{
        java.awt.Graphics g=civet.getGraphics();
        centeredOutlinedText(g,"ZKK har gått back "+Kaffeligan.CSEKtoString(deficit),width,0,100);// first line
        centeredOutlinedText(g,"på "+formatTime(time),width,g.getFontMetrics().getHeight(),100);// second line
        gif.writeToSequence(civet);// base frame
        ArrayList<Tear> tears=new ArrayList<Tear>();
        for(int i=1;i<duration*fps;i++){// create a new frame and paint on the base frame
            BufferedImage frame=new BufferedImage(civet.getWidth(),civet.getHeight(),civet.getType());
            g=frame.getGraphics();
            g.drawImage(civet,0,0,null);
            tears.add(Tear.randomTear(leftEyeX,leftEyeY));// add two tears every frame
            tears.add(Tear.randomTear(rightEyeX,rightEyeY));
            for(int j=0;j<tears.size();j++){
                Tear t=tears.get(j);
                if(t.outOfBounds(width,height)){// remove tears that have left the frame
                    tears.remove(j);
                    j--;
                    continue;
                }
                t.paint(g);// paint them on the frame
                t.tick();// activate physics
            }
            gif.writeToSequence(frame);
        }
        gif.close();
    }
    private static void happyCivet(GifSequenceWriter gif,BufferedImage civet,int profit,long time)throws java.io.IOException{
        int sparkleNumber=6;
        Graphics g=civet.getGraphics();
        centeredOutlinedText(g,"ZKK har gått plus "+Kaffeligan.CSEKtoString(profit),width,0,100);
        centeredOutlinedText(g,"på "+formatTime(time),width,g.getFontMetrics().getHeight(),100);
        BufferedImage smile=ImageIO.read(GUI.load(smilePath));
        g.drawImage(smile.getScaledInstance(smileWidth,smileHeight,Image.SCALE_SMOOTH),smileX-smileWidth/2,smileY-smileHeight/2,null);
        blush(g,rightCheekX,rightCheekY,0.3F);
        blush(g,leftCheekX,leftCheekY,0.6F);
        gif.writeToSequence(civet);
        ArrayList<Sparkle> sparkles=new ArrayList<Sparkle>(sparkleNumber*2);
        for(int i=1;i<duration*fps;i++){
            BufferedImage frame=new BufferedImage(civet.getWidth(),civet.getHeight(),civet.getType());
            g=frame.getGraphics();
            g.drawImage(civet,0,0,null);
            sparkles.add(Sparkle.randomSparkle(rightEyeX,rightEyeY));
            sparkles.add(Sparkle.randomSparkle(leftEyeX,leftEyeY));
            for(Sparkle s:sparkles){
                s.paint(g);
            }
            if(i>=sparkleNumber){
                sparkles.remove(0);
                sparkles.remove(0);
            }
            gif.writeToSequence(frame);
        }
        gif.close();
    }
    public static String formatTime(long ms){// in swedish
        int monthnum=(int)(ms/msmonth);// months are not actually constant length, close enough
        ms%=msmonth;
        int weeknum=(int)(ms/msweek);
        ms%=msweek;
        int daynum=(int)(ms/msday+1);// round up
        if(daynum>6){
            daynum-=7;
            weeknum++;
        }
        String months=monthnum+(monthnum==1?" månad":" månader");
        String weeks=weeknum+(weeknum==1?" vecka":" veckor");
        String days=daynum+(daynum==1?" dag":" dagar");
        String result="";
        if(monthnum>0){
            result=months;
            if(weeknum>0 && daynum>0){
                result+=", "+weeks+" & "+days;
            }
            else if(weeknum>0 ^ daynum>0){
                result+=" & ";
                if(weeknum>0){
                    result+=weeks;
                }
                else{
                    result+=days;
                }
            }
        }
        else{
            if(weeknum>0 && daynum>0){
                result=weeks+" & "+days;
            }
            else{
                if(weeknum>0){
                    result=weeks;
                }
                else{
                    result=days;
                }
            }
        }
        return result;
    }
    public static void blush(Graphics g,int x,int y,float opacity){// make a red circle centered around (x,y)
        g.setColor(new Color(255,0,55));
        ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,opacity));
        g.fillOval(x,y,100,60);
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
        g.setFont(new java.awt.Font("Impact",java.awt.Font.BOLD,fontSize));
        y+=g.getFontMetrics().getAscent();
        int x=(width-g.getFontMetrics(font).charsWidth(text.toCharArray(),0,text.toCharArray().length))/2;
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
                BufferedImage sourceTear=ImageIO.read(GUI.load(tearPath));
                tear=new BufferedImage(tearHeight,tearHeight,sourceTear.getType());
                tear.getGraphics()
                .drawImage(
                sourceTear.getScaledInstance(tearWidth,tearHeight,Image.SCALE_SMOOTH),(tearHeight-tearWidth)/2,0,null);
            }
            catch(java.io.IOException x){
                tear=null;
            }
        }
        double x,y,vx,vy,g,ang;
        public static Tear randomTear(int x,int y){
            double ang=Math.random()*Math.PI;
            x+=tearHeight*Math.cos(ang)/2;
            y+=tearHeight*Math.sin(ang)/2;
            double v0=100.0/fps;
            double g=50.0/fps;
            return new Tear(x,y,v0*Math.cos(ang),v0*Math.sin(ang),g);
        }
        public Tear(int x,int y,double vx0,double vy0,double g){// x and y are in the middle
            this.x=x;
            this.y=y;
            vx=vx0;
            vy=vy0;
            this.g=g;
            if(vx==0){
                ang=vy>0?Math.PI/2:-Math.PI/2;
            }
            else{
                ang=vx>0?Math.atan(vy/vx):Math.atan(vy/vx)+Math.PI;
            }
        }
        public void paint(java.awt.Graphics g){
            ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.5F));
            g.drawImage(rotate(tear,ang-Math.PI/2),(int)(x+0.5)-tearHeight/2,(int)(y+0.5)-tearHeight/2,null);
        }
        public void tick(){
            x+=vx;
            y+=vy;
            vy+=g;
            ang=vx>0?Math.atan(vy/vx):Math.atan(vy/vx)+Math.PI;
        }
        public boolean outOfBounds(int width,int height){
            if(y<=-tearHeight || y>=height || x<=-tearWidth/2 || x>=width+tearWidth/2){
                return true;
            }
            return false;
        }
    }
    public static class Sparkle{
        private static BufferedImage sourceSparkle;
        static{
            try{
                sourceSparkle=ImageIO.read(GUI.load(sparklePath));
            }
            catch(IOException x){
                sourceSparkle=null;
            }
        }
        private BufferedImage sparkle;
        private int x,y;
        float time=1;
        public static Sparkle randomSparkle(int x,int y){
            double r=Math.random()*60+40;
            double ang=Math.random()*2*Math.PI;
            x+=(int)(r*Math.cos(ang)+0.5);
            y+=(int)(r*Math.sin(ang)+0.5);
            return new Sparkle(x,y,(int)(Math.random()*30+30.5),(int)(Math.random()*30+30.5));
        }
        public Sparkle(int x,int y,int width,int height){
            this.x=x-width/2;
            this.y=y-height/2;
            sparkle=new BufferedImage(width,height,sourceSparkle.getType());
            sparkle.getGraphics().drawImage(sourceSparkle.getScaledInstance(width,height,Image.SCALE_SMOOTH),0,0,null);
        }
        public void paint(Graphics g){
            ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,time));
            g.drawImage(sparkle,x,y,null);
            time-=.15;
        }
    }
    public static BufferedImage rotate(BufferedImage bi,double ang){
        AffineTransform tx=new AffineTransform();
        tx.rotate(ang,bi.getWidth()/2,bi.getHeight()/2);
        AffineTransformOp op=new AffineTransformOp(tx,AffineTransformOp.TYPE_BILINEAR);
        return op.filter(bi,null);
    }
}