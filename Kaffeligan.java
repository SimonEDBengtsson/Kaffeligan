import java.io.*;
import java.util.*;
import java.awt.Image;
import java.awt.image.*;
import javax.imageio.*;
public class Kaffeligan{
    final static String configPath="dependencies/kaffeligan.config";
    final static int winners=3;
    static int nameIndex=1,paidIndex=4;
    static String backgroundImagePath="dependencies/background.png";
    static String logoImagePath="dependencies/logo.png";
    static String bronzeImagePath="dependencies/bronze.png";
    static String silverImagePath="dependencies/silver.png";
    static String goldImagePath="dependencies/gold.png";
    static String lp="LP1";
    public static void main(String[] args){// args will be interpreted as: input file, output file (.png and .jpg supported) and lastly what to put after the header
        config();
        Scanner sc=new Scanner(System.in);
        String in,out;
        if(args.length>=1){// for all 3, check args otherwise ask in console
            in=args[0];
        }
        else{
            System.out.print("Full path to input file: ");
            in=sc.nextLine();
        }
        if(args.length>1){
            out=args[1];
        }
        else{
            System.out.print("Full path to output file: ");
            out=sc.nextLine();
        }
        if(args.length>2){
            lp=args[2];
        }
        else{
            System.out.print("What text would you like in the top right corner (LP or LV for instance): ");
            lp=sc.nextLine();
        }
        try{
            if(out.matches(".*\\.png$")){
                writePNG(out,read(in));
            }
            else if(out.matches(".*\\.jpg$")){
                writeJPG(out,read(in));
            }
            else if(out.matches(".*\\.gif$")){
                Civet.writeGIF(out,in);
            }
            else{
                System.err.println("Invalid file type");
            }
        }
        catch(java.io.IOException x){
            x.printStackTrace();
            System.err.println("It didn't work and it's probably your fault.");
        }
    }
    private static void config(){// the config file can be used to change global variables, \n separated values
        try{
            BufferedReader in=new BufferedReader(new FileReader(configPath));
            nameIndex=Integer.parseInt(in.readLine());
            paidIndex=Integer.parseInt(in.readLine());
            backgroundImagePath=in.readLine();
            logoImagePath=in.readLine();
            goldImagePath=in.readLine();
            silverImagePath=in.readLine();
            bronzeImagePath=in.readLine();
        }
        catch(java.io.IOException t){
            System.err.println("Config file not found, standard settings will be used.");
        }
    }
    private static Customer[] read(String path)throws java.io.IOException{// Customer is a wrapper class for a name and paid amount with the Comparable interface
        BufferedReader in=new BufferedReader(new FileReader(path));// read in the csv file
        ArrayList<Customer> customers=new ArrayList<Customer>(3);
        String line=in.readLine();// this line is the header and can thus be discarded
        while((line=in.readLine())!=null){
            String[] temp=line.split(";");
            temp[nameIndex]=temp[nameIndex].replaceAll("Swish\\s*","");// remove "Swish"
            temp[nameIndex]=temp[nameIndex].replaceAll("([^,]+),([^,]+)","$2 $1");// put first- before last name
            temp[nameIndex]=temp[nameIndex].replaceAll("^\\s*","");// remove leading spaces
            temp[paidIndex]=temp[paidIndex].replaceAll("\\D","");// remove all but digits
            int paid=Integer.parseInt(temp[paidIndex]);// the payment sum in "öre" to avoid floating point numbers
            boolean exists=false;
            for(Customer c:customers){// check if the customer exists, if so add paid to their total otherwise create them and add to the arraylist
                if(c.name.equals(temp[nameIndex])){
                    c.paid+=paid;
                    exists=true;
                    break;
                }
            }
            if(!exists){
                customers.add(new Customer(paid,temp[nameIndex]));
            }
        }
        Customer[] ca=customers.toArray(new Customer[1]);// turn the arraylist into an array for sort to work
        Arrays.sort(ca);// the Comparable interface is implemented to put the highest paid in the beginning of the list
        return ca;
    }
    private static void writePNG(String path,Customer[] ca)throws java.io.IOException{// sends the array of sorted customers onward, gets a BufferedImage and writes it to a png file
        ImageIO.write(createBufferedImage(ca),"png",new File(path));
    }
    private static void writeJPG(String path,Customer[] ca)throws java.io.IOException{// sends the array of sorted customers onward, gets a BufferedImage and writes it to a jpg file
        BufferedImage argb=createBufferedImage(ca);// OpenJDK doesn't play nice with jpg, can't handle the alpha channel
        BufferedImage bgr=new BufferedImage(argb.getWidth(),argb.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
        bgr.getGraphics().drawImage(argb,0,0,null);
        ImageIO.write(bgr,"jpeg",new File(path));
    }
    private static BufferedImage createBufferedImage(Customer[] ca)throws java.io.IOException{
        int width=1920,height=1080;// resolution
        int logoLeftMargin=100,logoRightPadding=40;// graphical design parameters
        int medalLeftMargin=190,medalRightPadding=40,medalTopPadding=20;
        int topMargin=40,amountWidth=500;
        int logoSize=300,medalWidth=120,medalHeight=200,headerSize,textSize;
        int headerFontSize=160,headerBaseline=(int)(headerFontSize*1.3),fontSize=90,textBaseline=(int)(fontSize*1.6);
        ca=decideWinners(ca);// decide which three are the winners and their rankings
        BufferedImage background=ImageIO.read(new File(backgroundImagePath));// read in the graphical assets
        BufferedImage logo=ImageIO.read(new File(logoImagePath));
        BufferedImage bronze=ImageIO.read(new File(bronzeImagePath));
        BufferedImage silver=ImageIO.read(new File(silverImagePath));
        BufferedImage gold=ImageIO.read(new File(goldImagePath));
        BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics g=image.getGraphics();// start composing the picture, background, logo, header
        g.drawImage(background.getScaledInstance(width,height,Image.SCALE_SMOOTH),0,0,null);
        g.drawImage(logo.getScaledInstance(logoSize,logoSize,Image.SCALE_SMOOTH),logoLeftMargin,topMargin,null);
        shadowText(g,"Kaffeligan "+lp,logoLeftMargin+logoSize+logoRightPadding,topMargin+headerBaseline,headerFontSize);
        int amountOffset=width-amountWidth;// vertical baseline for paid amount
        int x=medalLeftMargin+medalWidth+medalRightPadding;// vertical baseline for names
        int y=topMargin+logoSize+medalTopPadding;// horizontal baseline for gold medal
        g.drawImage(gold.getScaledInstance(medalWidth,medalHeight,Image.SCALE_SMOOTH),medalLeftMargin,y,null);// add medal, name and paid amount
        outlinedText(g,ca[0].name,x,y+textBaseline,fontSize);
        outlinedText(g,ca[0].paid/100+","+ca[0].paid%100+":-",amountOffset,y+textBaseline,fontSize);
        y+=medalHeight+medalTopPadding;// move horizontal baseline down to silver medal and repeat above steps for runner-up
        g.drawImage(silver.getScaledInstance(medalWidth,medalHeight,Image.SCALE_SMOOTH),medalLeftMargin,y,null);
        outlinedText(g,ca[1].name,x,y+textBaseline,fontSize);
        outlinedText(g,ca[1].paid/100+","+ca[1].paid%100+":-",amountOffset,y+textBaseline,fontSize);
        y+=medalHeight+medalTopPadding;// finally do the bronze medalist
        g.drawImage(bronze.getScaledInstance(medalWidth,medalHeight,Image.SCALE_SMOOTH),medalLeftMargin,y,null);
        outlinedText(g,ca[2].name,x,y+textBaseline,fontSize);
        outlinedText(g,ca[2].paid/100+","+ca[2].paid%100+":-",amountOffset,y+textBaseline,fontSize);
        return image;
    }
    public static void shadowText(java.awt.Graphics g,String text,int x,int y,int fontSize){// writes white text with shadow
        g.setFont(new java.awt.Font("Impact",java.awt.Font.BOLD,fontSize));
        g.setColor(new java.awt.Color(20,20,20));
        g.drawString(text,x+fontSize/10,y+fontSize/10);// write black text diagonlly down to the right
        g.setColor(new java.awt.Color(250,250,250));
        g.drawString(text,x,y);// write white text on top
    }
    public static void outlinedText(java.awt.Graphics g,String text,int x,int y,int fontSize){// writes white text with black outline
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
    private static Customer[] decideWinners(Customer[] ca){
        Customer[] result=new Customer[winners];
        int decided=0;
        int i=0;
        while(decided<winners){
            int mem=i;
            try{
                while(ca[i].paid==ca[i+1].paid){// mem is the index of the first in the rank, i is the last
                    i++;
                }
            }
            catch(IndexOutOfBoundsException x){}// can happen for short arrays, or arrays with low spread
            Customer[] temp=new Customer[i-mem+1];// array for all customers of same rank
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
    public static class Customer implements Comparable<Customer>{// wrapper for name and paid amount, lowest paid amount is biggest for compareTo()
        int paid;
        String name;
        public Customer(int p,String n){
            paid=p;
            name=n;
        }
        public int compareTo(Customer c){
            if(c.paid<paid){
                return -1;
            }
            else if(c.paid==paid){
                return 0;
            }
            return 1;
        }
        public void print(){
            System.out.println(name+": "+paid+" öre");
        }
        public void print(PrintWriter out){
            out.print(name+",");
        }
        public void println(PrintWriter out){
            out.println(name+";"+paid);
        }
    }
}
