
public abstract class FinancialData{
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
