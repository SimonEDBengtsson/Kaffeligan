import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.awt.image.*;
public class BalanceGraph extends JPanel{
    static int width=1920,height=1080,horizontalLines=4,verticalLines=6;
    Transaction[] transactions;
    long startDate,endDate;
    int maxBalance,minBalance;
    public static void test(){
        JFrame frame=new JFrame();
        frame.add(new BalanceGraph("/home/simon/Downloads/Kontohandelser2019-04-01(1).csv",CustomerData.Bank.ICA));
        frame.pack();
        frame.setVisible(true);
    }
    public static void writeGraph(String inPath,String outPath,CustomerData.Bank bank)throws Exception{
        if(!outPath.matches(".*\\.png$")){
            throw new Exception("Filetype not supported");
        }
        BalanceGraph bg=new BalanceGraph(inPath,bank);
        BufferedImage bi=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        bg.paintComponent(bi.getGraphics());
        javax.imageio.ImageIO.write(bi,"png",new File(outPath));
    }
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        long duration=endDate-startDate;
        int balanceSpan=maxBalance-minBalance;
        double pixperms=(double)width/duration;
        double pixpercsek=(double)height/balanceSpan;
        for(int i=0;i<horizontalLines;i++){
            double ratio=(double)(i+1)/(horizontalLines+1);
            int y=height-(int)(ratio*height+0.5);
            g.setColor(Color.BLACK);
            g.drawString(""+Kaffeligan.CSEKtoString(minBalance+(int)(balanceSpan*ratio+0.5)),0,y);
            g.setColor(Color.LIGHT_GRAY);
            g.drawPolyline(new int[]{0,width},new int[]{y,y},2);
        }
        for(int i=0;i<verticalLines;i++){
            double ratio=(double)(i+1)/(verticalLines+1);
            int x=(int)(width*ratio+0.5);
            g.setColor(Color.BLACK);
            g.drawString(formatEpoch(startDate+(long)(duration*ratio),"yyyy-MM-dd"),x,g.getFontMetrics().getAscent());
            g.setColor(Color.LIGHT_GRAY);
            g.drawPolyline(new int[]{x,x},new int[]{0,height},2);
        }
        {
            g.setColor(Color.DARK_GRAY);
            int[] x=new int[transactions.length];
            int[] y=new int[transactions.length];
            for(int i=0;i<transactions.length;i++){
                x[i]=(int)(pixperms*(transactions[i].date-startDate)+0.5);
                y[i]=height-(int)(pixpercsek*(transactions[i].balance-minBalance)+0.5);
            }
            g.drawPolyline(x,y,x.length);
        }
    }
    public BalanceGraph(String path,CustomerData.Bank bank){
        readTransactions(path,bank);
        setPreferredSize(new Dimension(width,height));
    }
    public void readTransactions(String path,CustomerData.Bank bank){
        switch(bank){
            case ICA:   readTransactionsICA(path);
                        break;
            default:    transactions=null;
        }
        startDate=transactions[0].date;
        endDate=transactions[transactions.length-1].date;
        maxBalance=transactions[0].balance;// initialize for comparisson to work
        minBalance=maxBalance;
        for(Transaction t:transactions){
            if(t.balance>maxBalance){
                maxBalance=t.balance;
            }
            if(t.balance<minBalance){
                minBalance=t.balance;
            }
        }
    }
    private void readTransactionsICA(String path){
        ArrayList<Transaction> transactions=new ArrayList<Transaction>();
        try{
            BufferedReader in=new BufferedReader(new FileReader(path));// read in the csv file
            String line=in.readLine();// header
            while((line=in.readLine())!=null){
                transactions.add(new Transaction(
                CustomerData.extractDateICA(line),
                CustomerData.extractBalanceICA(line)));
            }
        }
        catch(IOException x){
            this.transactions=null;
        }
        this.transactions=transactions.toArray(new Transaction[1]);
        java.util.Arrays.sort(this.transactions);
    }
    public static String formatEpoch(long epoch,String pattern){
        Date date = new Date(epoch);
        DateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        return format.format(date);
    }
    public static class Transaction implements Comparable<Transaction>{
        long date;
        int balance;
        public Transaction(long date,int balance){
            this.date=date;
            this.balance=balance;
        }
        public int compareTo(Transaction t){
            if(date<t.date){
                return -1;
            }
            else if(date==t.date){
                return 0;
            }
            return 1;
        }
    }
}