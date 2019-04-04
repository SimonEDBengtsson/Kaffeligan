import java.awt.*;
public class TransactionSpectrum extends TransactionGraph{
    static double percentMargin=1.2;
    static int verticalLines=8;
    int[] amplitude;
    int maxAmplitude=0;
    public static void writeGraph(String outPath,FinancialData fd)throws Exception{// creates a png at "outPath"
        if(!outPath.matches(".*\\.png$")){
            throw new Exception("Filetype not supported");
        }
        TransactionGraph tg=new TransactionSpectrum(fd);
        java.awt.image.BufferedImage bi=new java.awt.image.BufferedImage(width,height,java.awt.image.BufferedImage.TYPE_INT_ARGB);
        tg.paintComponent(bi.getGraphics());// draw the JPanel onto it
        javax.imageio.ImageIO.write(bi,"png",new java.io.File(outPath));
    }
    public static void test()throws Throwable{
        javax.swing.JFrame frame=new javax.swing.JFrame();
        frame.add(new TransactionSpectrum(new ICAData("/home/simon/Downloads/Kontohandelser2019-04-01(1).csv")));
        frame.pack();
        frame.setVisible(true);
    }
    public TransactionSpectrum(FinancialData fd){
        this(fd,"",86400000L);
    }
    public TransactionSpectrum(FinancialData fd,String options,long period){
        super(fd,options,period);
        Complex[] ft=Complex.dft(pt.balanceChanges);
        amplitude=new int[ft.length];
        for(int i=0;i<amplitude.length;i++){
            amplitude[i]=(int)(ft[i].abs()/amplitude.length+0.5);
            maxAmplitude=amplitude[i]>maxAmplitude?amplitude[i]:maxAmplitude;
        }
    }
    @Override
    public void paintComponent(Graphics g){
        double fs=1.0/period;// mHz
        double xRatio=2.0*super.width/amplitude.length;// dft above fs/2 is useless
        double yRatio=super.height/(maxAmplitude*percentMargin);
        for(int i=0;i<verticalLines;i++){
            int x=(int)((i+1)*width/(verticalLines+1.0)+0.5);
            double f=x*fs/(amplitude.length*xRatio);
            long T=(long)(1/f+0.5);
            g.setColor(Color.LIGHT_GRAY);
            g.drawLine(x,0,x,height);
            g.setColor(Color.BLACK);
            g.drawString("T="+Civet.formatTime(T),x,g.getFontMetrics().getAscent());
        }
        g.setColor(Color.BLACK);
        for(int i=0;i<(amplitude.length+1)/2;i++){
            int x=(int)(xRatio*i+0.5);
            int y=height-(int)(yRatio*amplitude[i]+0.5);
            g.drawLine(x,height,x,y);
            double f=i*fs/amplitude.length;
            long T=(long)(1/f+0.5);// ms
        }
    }
}