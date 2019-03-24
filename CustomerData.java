import java.io.*;
import java.util.*;
public class CustomerData{
    static int initialCapital=200000;
    static int dateIndex=0,nameIndex=1,paidIndex=4,balanceIndex=5;// indeces for icabanken
    Customer[] customers;
    int startBalance;// CSEK
    int endBalance;
    int netProfit;
    long startDate;// ms
    long endDate;
    public enum Bank{
        ICA("ICA Banken"),HANDELSBANKEN("Svenska Handelsbanken"),NORDEA("Nordea"),SEB("Svenska Enskilda Banken"),
        SWEDBANK("Swedbank"),OKQ8("OK-Q8 Bank"),SKANDIA("Skandiabanken"),DANSKE("Danske Bank"),LÄNS("Länsförsäkringar Bank"),
        SANTANDER("Santander Consumer Bank"),VOLVO("Volvofinans Bank"),IKANO("Ikano Bank"),KAUPTHING("Ålandsbanken");
        private String name;
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
        String line=in.readLine();// this line is the header and can thus be discarded
        line=in.readLine();
        endDate=extractDateICA(line);
        endBalance=extractBalanceICA(line);
        String mem=line;
        do{
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
            mem=line;
        }while((line=in.readLine())!=null);
        startDate=extractDateICA(mem);
        startBalance=extractBalanceICA(mem);
        netProfit=initialCapital-endBalance;
        this.customers=customers.toArray(new Customer[1]);// turn the arraylist into an array for sort to work
        Arrays.sort(this.customers);// the Comparable interface is implemented to put the highest paid in the beginning of the list
    }
    private long extractDateICA(String line){// date in milliseconds since the epoch
        String[] date=line.split(";")[dateIndex].split("-");// yyyy-mm-dd;other;stuff;...
        return new GregorianCalendar(Integer.parseInt(date[0]),Integer.parseInt(date[1]),Integer.parseInt(date[2])).getTimeInMillis();
    }
    private int extractBalanceICA(String line){// finds balance in CSEK
        String balanceString=line.split(";")[balanceIndex];
        String[] denom=balanceString.split(",");// split into kr and öre, then parse separately and recombine
        return Integer.parseInt(denom[0].replaceAll(" ",""))*100+Integer.parseInt(denom[1].replaceAll(" kr",""));
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
        public int compareTo(Customer c){
            if(c.paid<paid){
                return -1;
            }
            else if(c.paid==paid){
                return 0;
            }
            return 1;
        }
        public void print(){// print methods for testing
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