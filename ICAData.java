import java.io.*;
import java.util.*;
public class ICAData extends FinancialData{// contains various data about incoming transactions
    public ICAData(String path)throws IOException{
        read(path);
    }
    public void read(String path)throws IOException{// Customer is a wrapper class for a name and paid amount with the Comparable interface
        BufferedReader in=new BufferedReader(new FileReader(path));// read in the csv file
        ArrayList<Customer> customers=new ArrayList<Customer>(3);
        ArrayList<Transaction> transactions=new ArrayList<Transaction>(3);
        String line=in.readLine();// this line is the header and can thus be discarded
        line=in.readLine();// the first line has the newest entry
        super.startDate=extractDate(line);
        super.startBalance=extractBalance(line);
        super.maxBalance=startBalance;// initialize to compare later
        super.minBalance=startBalance;
        String mem=line;
        do{
            transactions.add(readTransaction(line));
            int balance=extractBalance(line);
            super.maxBalance=maxBalance<balance?balance:maxBalance;// if "line"s balance is higher/lower than max/min, update
            super.minBalance=balance<minBalance?balance:minBalance;
            if(extractType(line).equals("Insättning")){// only deposits are counted as customers, need more speciftions for enum
                String name=extractName(line);
                int paid=extractPaid(line);
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
        super.endDate=extractDate(mem);// the last line has the newest entry
        super.endBalance=extractBalance(mem);
        super.netProfit=initialCapital-endBalance;
        super.customers=customers.toArray(new Customer[1]);// turn the arraylist into an array for sort to work
        Arrays.sort(super.customers);// the Comparable interface is implemented to put the highest paid in the beginning of the list
        super.transactions=transactions.toArray(new Transaction[1]);
        Arrays.sort(super.transactions);// needs flipping, .sort() uses quicksort so should be linear time
    }
    public Transaction readTransaction(String line){
        return new Transaction(extractDate(line),extractBalance(line));
    }
    public static String extractName(String line){// name of the person, designed for Swish payments
        String temp=line.split(";")[nameIndex];
        temp=temp.replaceAll("Swish\\s*","");// remove "Swish"
        temp=temp.replaceAll("([^,]+),([^,]+)","$2 $1");// put first- before last name
        temp=temp.replaceAll("^\\s*","");// remove leading spaces
        return temp;
    }
    public static String extractType(String line){
        return line.split(";")[typeIndex];
    }
    public static int extractPaid(String line){// finds amount paid in CSEK
        String temp=line.split(";")[paidIndex];
        return Integer.parseInt(temp.replaceAll("\\D",""));// remove all but digits
    }
    public static long extractDate(String line){// date in milliseconds since the epoch
        String[] date=line.split(";")[dateIndex].split("-");// yyyy-mm-dd;other;stuff;...
        return new GregorianCalendar(Integer.parseInt(date[0]),Integer.parseInt(date[1]),Integer.parseInt(date[2])).getTimeInMillis();
    }
    public static int extractBalance(String line){// finds balance in CSEK
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
}