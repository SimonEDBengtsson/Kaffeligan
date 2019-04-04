import javax.swing.*;
public class TransactionGraph extends JPanel{
    static int width=GUI.imageWidth,height=GUI.imageHeight;
    FinancialData.Transaction[] transactions;
    public TransactionGraph(FinancialData.Transaction[] transactions){
        this.transactions=transactions;
    }
}
