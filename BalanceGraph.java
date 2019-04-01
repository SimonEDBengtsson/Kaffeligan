import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.text.*;
public class BalanceGraph extends JPanel{
    static int width=1920,height=1080;
    Transaction[] transactions;
    long startDate,endDate;
    int startBalance,endBalance;
    public static void test(){
        JFrame frame=new JFrame();
        frame.add(new BalanceGraph("/home/simon/Downloads/Kontohandelser2019-04-01.csv",CustomerData.Bank.ICA));
        frame.pack();
        frame.setVisible(true);
    }
    public BalanceGraph(String path,CustomerData.Bank bank){
        readTransactions(path,bank);
        long duration=endDate-startDate;
        int balanceChange=endBalance-startBalance<0?startBalance-endBalance:endBalance-startBalance;// |delta balance|
        double pixperms=width/duration;
        double pixpercsek=height/balanceChange;
        setPreferredSize(new Dimension(width,height));
        Graphics g=getGraphics();
        int[] x=new int[transactions.length];
        int[] y=new int[transactions.length];
        for(int i=0;i<transactions.length;i++){
            x[i]=(int)(pixperms*(transactions[i].date-startDate)+0.5);
            y[i]=(int)(pixpercsek*(transactions[i].balance-startBalance)+0.5);
        }
        g.drawPolyline(x,y,x.length);
    }
    public void readTransactions(String path,CustomerData.Bank bank){
        switch(bank){
            case ICA:   readTransactionsICA(path);
            default:    transactions=null;
        }
        startDate=transactions[0].date;
        endDate=transactions[transactions.length-1].date;
        startBalance=transactions[0].balance;
        endBalance=transactions[transactions.length-1].balance;
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