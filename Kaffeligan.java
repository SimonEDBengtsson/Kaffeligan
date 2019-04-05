import java.io.*;
import java.util.*;
import java.awt.Image;
import java.awt.image.*;
import javax.imageio.*;
public class Kaffeligan{
    final static int winners=3;
    static String backgroundImagePath="resources/background.png";
    static String logoImagePath="resources/logo.png";
    static String bronzeImagePath="resources/bronze.png";
    static String silverImagePath="resources/silver.png";
    static String goldImagePath="resources/gold.png";
    static String lp="LP1";
    public static void create(String path,FinancialData fd,GUI caller)throws Exception{// writes either a png or jpg at "path", generated from "fd"
        lp=caller.requestDataFromUser("\"Kaffeligan \"+","Kaffeligan version",null,null);
        if(path.matches(".*\\.png$")){// check the path to see what image type to use
            writePNG(path,fd);
        }
        else if(path.matches(".*\\.jpg$")){
            writeJPG(path,fd);
        }
        else{
            throw new Exception("Filetype not supported");
        }
    }
    public static void writePNG(String path,FinancialData fd)throws java.io.IOException{// sends the array of sorted customers onward, gets a BufferedImage and writes it to a png file
        ImageIO.write(createBufferedImage(fd.customers),"png",new File(path));
    }
    public static void writeJPG(String path,FinancialData fd)throws java.io.IOException{// sends the array of sorted customers onward, gets a BufferedImage and writes it to a jpg file
        BufferedImage argb=createBufferedImage(fd.customers);// OpenJDK doesn't play nice with jpg, can't handle the alpha channel
        BufferedImage bgr=new BufferedImage(argb.getWidth(),argb.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
        bgr.getGraphics().drawImage(argb,0,0,null);
        ImageIO.write(bgr,"jpeg",new File(path));
    }
    private static BufferedImage createBufferedImage(FinancialData.Customer[] ca)throws java.io.IOException{
        int width=GUI.imageWidth,height=GUI.imageHeight;// resolution
        int logoLeftMargin=100,logoRightPadding=40;// graphical design parameters
        int medalLeftMargin=190,medalRightPadding=40,medalTopPadding=20;
        int topMargin=40,amountWidth=500;
        int logoSize=300,medalWidth=120,medalHeight=200,headerSize,textSize;
        int headerFontSize=160,fontSize=90,headerDownShift=45,textDownShift=35;
        
        ca=decideWinners(ca);// decide which three are the winners and their order
        
        BufferedImage background=ImageIO.read(GUI.load(backgroundImagePath));// read in the graphical assets
        BufferedImage logo=ImageIO.read(GUI.load(logoImagePath));
        Image bronze=ImageIO.read(GUI.load(bronzeImagePath)).getScaledInstance(medalWidth,medalHeight,Image.SCALE_SMOOTH);
        Image silver=ImageIO.read(GUI.load(silverImagePath)).getScaledInstance(medalWidth,medalHeight,Image.SCALE_SMOOTH);
        Image gold=ImageIO.read(GUI.load(goldImagePath)).getScaledInstance(medalWidth,medalHeight,Image.SCALE_SMOOTH);
        Image[] medals=new Image[]{gold,silver,bronze};// create array too let same payment give same medal
        int i=0;
        BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        
        java.awt.Graphics g=image.getGraphics();// start composing the picture, background, logo, header
        g.drawImage(background.getScaledInstance(width,height,Image.SCALE_SMOOTH),0,0,null);
        g.drawImage(logo.getScaledInstance(logoSize,logoSize,Image.SCALE_SMOOTH),logoLeftMargin,topMargin,null);
        shadowText(g,"Kaffeligan "+lp,logoLeftMargin+logoSize+logoRightPadding,topMargin+headerDownShift,headerFontSize);
        
        int amountOffset=width-amountWidth;// vertical baseline for paid amount
        int x=medalLeftMargin+medalWidth+medalRightPadding;// vertical baseline for names
        int y=topMargin+logoSize+medalTopPadding;// horizontal baseline for gold medal
        if(ca.length>0){// add medal, name and paid amount for first place
            g.drawImage(medals[i],medalLeftMargin,y,null);
            outlinedText(g,ca[0].name,x,y+textDownShift,fontSize);
            outlinedText(g,CSEKtoString(ca[0].paid),amountOffset,y+textDownShift,fontSize);
        }
        y+=medalHeight+medalTopPadding;// move horizontal baseline down to silver medal and repeat above steps for runner-up
        if(ca.length>1){// second place
            i=ca[1].paid<ca[0].paid?i+1:i;// if second place has payed as much as first, he gets a gold medal too
            g.drawImage(medals[i],medalLeftMargin,y,null);
            outlinedText(g,ca[1].name,x,y+textDownShift,fontSize);
            outlinedText(g,CSEKtoString(ca[1].paid),amountOffset,y+textDownShift,fontSize);
        }
        y+=medalHeight+medalTopPadding;// finally do the bronze medalist
        if(ca.length>2){// third place
            i=ca[2].paid<ca[1].paid?i+1:i;
            g.drawImage(medals[i],medalLeftMargin,y,null);
            outlinedText(g,ca[2].name,x,y+textDownShift,fontSize);
            outlinedText(g,CSEKtoString(ca[2].paid),amountOffset,y+textDownShift,fontSize);
        }
        return image;
    }
    public static String CSEKtoString(int csek){// formats int representing CSEK (öre), ex 1906 -> 19,06:-
        boolean negative=false;
        if(csek<0){// handle negative values as positive, then put a minus in front at the end
            negative=true;
            csek*=-1;
        }
        String kr=""+csek/100;
        if((csek%=100)<10){// %100 leaves just the öre
            kr+=",0"+csek;
        }
        else{
            kr+=","+csek;
        }
        if(negative){
            kr="-"+kr;
        }
        return kr+":-";
    }
    public static void shadowText(java.awt.Graphics g,String text,int x,int y,int fontSize){// writes white text with shadow
        java.awt.Font font=new java.awt.Font("Impact",java.awt.Font.BOLD,fontSize);
        g.setFont(font);
        y+=g.getFontMetrics().getAscent();
        g.setColor(new java.awt.Color(20,20,20));
        g.drawString(text,x+fontSize/10,y+fontSize/10);// write black text diagonlly down to the right
        g.setColor(new java.awt.Color(250,250,250));
        g.drawString(text,x,y);// write white text on top
    }
    public static void outlinedText(java.awt.Graphics g,String text,int x,int y,int fontSize){// writes white text with black outline
        java.awt.Font font=new java.awt.Font("Impact",java.awt.Font.BOLD,fontSize);
        y+=g.getFontMetrics(font).getAscent();
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
    private static FinancialData.Customer[] decideWinners(FinancialData.Customer[] ca){// picks out the customers who have paid the most, randomizes ties
        FinancialData.Customer[] result=new FinancialData.Customer[winners<ca.length?winners:ca.length];
        int decided=0;
        int i=0;
        while(decided<winners && decided<ca.length){
            int mem=i;
            try{
                while(ca[i].paid==ca[i+1].paid){// mem is the index of the first in the rank, i is the last
                    i++;
                }
            }
            catch(IndexOutOfBoundsException x){}// can happen for short arrays, or arrays with low spread
            FinancialData.Customer[] temp=new FinancialData.Customer[i-mem+1];// array for all customers of same rank
            for(int j=mem;j<=i;j++){
                temp[j-mem]=ca[j];
            }
            shuffle(temp);// randomize their order
            try{
                for(int j=decided;j<temp.length+decided;j++){// enter into winners
                    result[j]=temp[j-decided];
                }
            }
            catch(IndexOutOfBoundsException x){// winners is full, the work is done
                break;
            }
            decided+=temp.length;// everyone in the rank is a winner go to next
            i++;// increment i to next rank
        }
        return result;
    }
    private static void shuffle(Object[] array){// randomizes order of array
        int index;
        Object temp;
        Random random=new Random();
        for (int i=array.length-1;i>0;i--){
            index=random.nextInt(i+1);
            temp=array[index];
            array[index]=array[i];
            array[i]=temp;
        }
    }
}
