import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.awt.image.*;
public class BalanceGraph extends JPanel{// a JPanel displaying the balance trend of a CustomerData object
    static int width=GUI.imageWidth,height=GUI.imageHeight;// resolution
    static int horizontalLines=4,verticalLines=6;// horizontal lines for balance, vertical for date
    FinancialData fd;
    public static void writeGraph(String outPath,FinancialData fd)throws Exception{// creates a png at "outPath"
        if(!outPath.matches(".*\\.png$")){
            throw new Exception("Filetype not supported");
        }
        BalanceGraph bg=new BalanceGraph(fd);
        BufferedImage bi=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);// create a BufferedImage
        bg.paintComponent(bi.getGraphics());// draw the JPanel onto it
        javax.imageio.ImageIO.write(bi,"png",new File(outPath));
    }
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);// probably does something important
        long duration=fd.endDate-fd.startDate;
        int balanceSpan=fd.maxBalance-fd.minBalance;
        double pixperms=(double)width/duration;// pixels per millisecond/CSEK
        double pixpercsek=(double)height/balanceSpan;
        for(int i=0;i<horizontalLines;i++){
            double ratio=(double)(i+1)/(horizontalLines+1);
            int y=height-(int)(ratio*height+0.5);
            g.setColor(Color.BLACK);// label each line with represented balance
            g.drawString(""+Kaffeligan.CSEKtoString(fd.minBalance+(int)(balanceSpan*ratio+0.5)),0,y);
            g.setColor(Color.LIGHT_GRAY);
            g.drawPolyline(new int[]{0,width},new int[]{y,y},2);
        }
        for(int i=0;i<verticalLines;i++){
            double ratio=(double)(i+1)/(verticalLines+1);
            int x=(int)(width*ratio+0.5);
            g.setColor(Color.BLACK);// label each line with represented date TODO: change date format after duration
            g.drawString(formatEpoch(fd.startDate+(long)(duration*ratio),"yyyy-MM-dd"),x,g.getFontMetrics().getAscent());
            g.setColor(Color.LIGHT_GRAY);
            g.drawPolyline(new int[]{x,x},new int[]{0,height},2);
        }
        {
            g.setColor(Color.DARK_GRAY);// finally draw the trend curve
            int[] x=new int[fd.transactions.length];
            int[] y=new int[fd.transactions.length];
            for(int i=0;i<fd.transactions.length;i++){
                x[i]=(int)(pixperms*(fd.transactions[i].date-fd.startDate)+0.5);
                y[i]=height-(int)(pixpercsek*(fd.transactions[i].balance-fd.minBalance)+0.5);
            }
            g.drawPolyline(x,y,x.length);
        }
    }
    public BalanceGraph(FinancialData fd){
        this.fd=fd;
        setPreferredSize(new Dimension(width,height));
    }
    public static String formatEpoch(long epoch,String pattern){// takes in epoch time and outputs String formatted by SimpleDateFormat
        Date date = new Date(epoch);
        DateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        return format.format(date);
    }
    public static double[] fft(int[] values){
        return null;
    }
}