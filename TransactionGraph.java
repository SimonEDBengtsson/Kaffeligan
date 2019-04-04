import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.awt.image.*;
import java.io.*;
public class TransactionGraph extends JPanel{
    static int width=GUI.imageWidth,height=GUI.imageHeight,pointRadius=3;
    static int horizontalLines=4;// horizontal lines for balance change
    FinancialData.Transaction[] ta;
    PeriodicTrade pt;
    long period;
    public static void writeGraph(String outPath,FinancialData fd)throws Exception{// creates a png at "outPath"
        if(!outPath.matches(".*\\.png$")){
            throw new Exception("Filetype not supported");
        }
        TransactionGraph tg=new TransactionGraph(fd);
        BufferedImage bi=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);// create a BufferedImage
        tg.paintComponent(bi.getGraphics());// draw the JPanel onto it
        javax.imageio.ImageIO.write(bi,"png",new File(outPath));
    }
    public TransactionGraph(FinancialData fd){
        this(fd,"");
    }
    public TransactionGraph(FinancialData fd,String options){
        this(fd,options,604800000L);// arbitrary period, one week
    }
    public TransactionGraph(FinancialData fd,String options,long period){
        switch(options){
            case "in":  ta=parseIn(fd);// removes all negative transactions
                        break;
            case "out": ta=parseOut(fd);// removes all positive transactions
                        break;
            default:    ta=parseAll(fd);// only sets the max and min transaction sizes, so do the previous two
        }
        setPeriod(period);
        setPreferredSize(new Dimension(width,height));
    }
    public void setPeriod(long period){
        this.period=period;
        pt=PeriodicTrade.periodize(ta,period);
    }
    private static FinancialData.Transaction[] parseIn(FinancialData fd){
        java.util.ArrayList<FinancialData.Transaction> in=new java.util.ArrayList<FinancialData.Transaction>();
        for(FinancialData.Transaction t:fd.transactions){
            if(t.balanceChange>0){
                in.add(t);
            }
        }
        return in.toArray(new FinancialData.Transaction[1]);
    }
    private static FinancialData.Transaction[] parseOut(FinancialData fd){
        ArrayList<FinancialData.Transaction> out=new ArrayList<FinancialData.Transaction>();
        for(FinancialData.Transaction t:fd.transactions){
            if(t.balanceChange<0){
                out.add(t);
            }
        }
        return out.toArray(new FinancialData.Transaction[1]);
    }
    private static FinancialData.Transaction[] parseAll(FinancialData fd){
        return fd.transactions;
    }
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        double xRatio=(double)width/pt.balanceChanges.length;// pixels per period
        int max=pt.maxChange(),min=pt.minChange();
        int balanceChangeSpan=max-min;
        double yRatio=(double)height/balanceChangeSpan;// pixels per CSEK
        int base=height-(int)(0.5-min*yRatio);// y coordinate zero
        for(int i=0;i<horizontalLines;i++){
            double ratio=(double)(i+1)/(horizontalLines+1);
            int y=height-(int)(ratio*height+0.5);
            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(0,y,width,y);
            g.setColor(Color.BLACK);
            int csek=min+(int)((1.0-(double)y/height)*balanceChangeSpan+0.5);
            g.drawString(Kaffeligan.CSEKtoString(csek),0,y);// wrong way around TODO: fix
        }
        g.setColor(Color.BLACK);
        for(int i=0;i<pt.balanceChanges.length;i++){
            int x=(int)(xRatio*i+0.5);
            int y=height-(int)((pt.balanceChanges[i]-min)*yRatio+0.5);
            g.drawLine(x,base,x,y>base?y-pointRadius:y+pointRadius);
            g.drawOval(x-pointRadius,y-pointRadius,pointRadius*2,pointRadius*2);
        }
        g.drawLine(0,base,width,base);// baseline
        g.drawString("T="+Civet.formatTime(pt.period),0,g.getFontMetrics().getAscent());
    }
    public static class PeriodicTrade{
        long period;
        int balanceChanges[];
        private PeriodicTrade(long period,int[] balanceChanges){
            this.period=period;
            this.balanceChanges=balanceChanges;
        }
        public static PeriodicTrade periodize(FinancialData.Transaction[] transactions,long period){
            long duration=transactions[transactions.length-1].date-transactions[0].date;
            int[] balanceChanges=new int[(int)(duration/period)+1];
            int j=0;
            for(int i=0;i<balanceChanges.length;i++){
                balanceChanges[i]=0;
                while(j<transactions.length && transactions[j].date-transactions[0].date<period*(i+1)){// divides transactions up into periods
                    balanceChanges[i]+=transactions[j++].balanceChange;// adds their balanceChange to that period
                }
            }
            return new PeriodicTrade(period,balanceChanges);
        }
        public int maxChange(){// doesn't return a number under 0
            int max=0;
            for(int bc:balanceChanges){
                max=max<bc?bc:max;
            }
            return max;
        }
        public int minChange(){// doesn't return a number over 0
            int min=0;
            for(int bc:balanceChanges){
                min=min<bc?min:bc;
            }
            return min;
        }
    }
}