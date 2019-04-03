import java.io.*;
import java.util.*;
public class CustomerData{// contains various data about incoming transactions
    static int initialCapital=200000;// the amount of money we started with
    static int dateIndex=0,nameIndex=1,typeIndex=2,paidIndex=4,balanceIndex=5;// indeces for icabanken
    Customer[] customers;
    Transaction[] transactions;
    int startBalance;// CSEK
    int endBalance;
    int maxBalance;
    int minBalance;
    int netProfit;
    long startDate;// ms
    long endDate;
    public enum Bank{// a bunch of banks i found on the internet
        ICA("ICA Banken"),HANDELSBANKEN("Svenska Handelsbanken"),NORDEA("Nordea"),SEB("Svenska Enskilda Banken"),
        SWEDBANK("Swedbank"),OKQ8("OK-Q8 Bank"),SKANDIA("Skandiabanken"),DANSKE("Danske Bank"),LÄNS("Länsförsäkringar Bank"),
        SANTANDER("Santander Consumer Bank"),VOLVO("Volvofinans Bank"),IKANO("Ikano Bank"),KAUPTHING("Ålandsbanken");
        private String name;// name for UI purposes
        private Bank(String name){
            this.name=name;
        }
        @Override
        public String toString(){
            return name;
        }
    }
    public CustomerData(String path,Bank bank)throws Exception{
        switch(bank){
            case ICA:   readICA(path);
                        break;
            default:    throw new Exception("Bank not supported");
        }
    }
    public void readICA(String path)throws IOException{// Customer is a wrapper class for a name and paid amount with the Comparable interface
        BufferedReader in=new BufferedReader(new FileReader(path));// read in the csv file
        ArrayList<Customer> customers=new ArrayList<Customer>(3);
        ArrayList<Transaction> transactions=new ArrayList<Transaction>(3);
        String line=in.readLine();// this line is the header and can thus be discarded
        line=in.readLine();// the first line has the newest entry
        startDate=extractDateICA(line);
        startBalance=extractBalanceICA(line);
        maxBalance=startBalance;// initialize to compare later
        minBalance=startBalance;
        String mem=line;
        do{
            transactions.add(readTransactionICA(line));
            int balance=extractBalanceICA(line);
            maxBalance=maxBalance<balance?balance:maxBalance;// if "line"s balance is higher/lower than max/min, update
            minBalance=balance<minBalance?balance:minBalance;
            if(extractTypeICA(line).equals("Insättning")){// only deposits are counted as customers, need more specifications for enum
                String name=extractNameICA(line);
                int paid=extractPaidICA(line);
                boolean exists=false;
                for(Customer c:customers){// check if the customer exists, if so add paid to their total otherwise create them and add to the arraylist
                    if(c.name.equals(name)){
                        c.paid+=paid;
                        exists=true;
                        break;
                    }
                }
                if(!exists){
                    customers.add(new Customer(paid,name));
                }
                mem=line;
            }
        }while((line=in.readLine())!=null);
        endDate=extractDateICA(mem);// the last line has the newest entry
        endBalance=extractBalanceICA(mem);
        netProfit=initialCapital-endBalance;
        this.customers=customers.toArray(new Customer[1]);// turn the arraylist into an array for sort to work
        Arrays.sort(this.customers);// the Comparable interface is implemented to put the highest paid in the beginning of the list
        this.transactions=transactions.toArray(new Transaction[1]);
        Arrays.sort(this.transactions);// needs flipping, .sort() uses quicksort so should be linear time
    }
    public Transaction readTransactionICA(String line){
        return new Transaction(extractDateICA(line),extractBalanceICA(line));
    }
    public static String extractNameICA(String line){// name of the person, designed for Swish payments
        String temp=line.split(";")[nameIndex];
        temp=temp.replaceAll("Swish\\s*","");// remove "Swish"
        temp=temp.replaceAll("([^,]+),([^,]+)","$2 $1");// put first- before last name
        temp=temp.replaceAll("^\\s*","");// remove leading spaces
        return temp;
    }
    public static String extractTypeICA(String line){
        return line.split(";")[typeIndex];
    }
    public static int extractPaidICA(String line){// finds amount paid in CSEK
        String temp=line.split(";")[paidIndex];
        return Integer.parseInt(temp.replaceAll("\\D",""));// remove all but digits
    }
    public static long extractDateICA(String line){// date in milliseconds since the epoch
        String[] date=line.split(";")[dateIndex].split("-");// yyyy-mm-dd;other;stuff;...
        return new GregorianCalendar(Integer.parseInt(date[0]),Integer.parseInt(date[1]),Integer.parseInt(date[2])).getTimeInMillis();
    }
    public static int extractBalanceICA(String line){// finds balance in CSEK
        String temp=line.split(";")[balanceIndex];
        return Integer.parseInt(temp.replaceAll("\\D",""));// remove all but digits
    }
    private void undefined(){
        customers=null;
        startBalance=-1;
        endBalance=-1;
        startDate=-1;
        endDate=-1;
    }
    public static class Customer implements Comparable<Customer>{// wrapper for name and paid amount, lowest paid amount is biggest for compareTo()
        int paid;
        String name;
        public Customer(int p,String n){
            paid=p;
            name=n;
        }
        public int compareTo(Customer c){// want highest paying first
            if(c.paid<paid){
                return -1;
            }
            else if(c.paid==paid){
                return 0;
            }
            return 1;
        }
    }
    public static class Transaction implements Comparable<Transaction>{// wrapper for date and balance
        long date;
        int balance;
        public Transaction(long date,int balance){
            this.date=date;
            this.balance=balance;
        }
        public int compareTo(Transaction t){// want the oldest transactions to come first
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