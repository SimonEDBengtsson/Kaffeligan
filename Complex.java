
public class Complex{
    double re,im;
    public Complex(){
        this(0,0);
    }
    public Complex(double re,double im){
        this.re=re;
        this.im=im;
    }
    public static Complex polar(double r,double ang){
        return new Complex(r*Math.cos(ang),r*Math.sin(ang));
    }
    public Complex add(Complex z){
        return new Complex(re+z.re,im+z.im);
    }
    public Complex scale(double c){
        return new Complex(re*c,im*c);
    }
    public Complex subtract(Complex z){
        return this.add(z.scale(-1));
    }
    public double abs(){
        return Math.sqrt(re*re+im*im);
    }
    public double arg(){// returns 0 for 0+0i
        if(re==0){
            if(im>=0){
                return 0;
            }
            else{
                return Math.PI/2;
            }
        }
        double ang=Math.atan(im/re);
        return re<0?ang+Math.PI:ang;
    }
    public Complex multiply(Complex z){
        return polar(abs()*z.abs(),arg()+z.arg());
    }
    public Complex invert(){
        return polar(1/abs(),-arg());
    }
    public Complex divide(Complex z){
        return multiply(z.invert());
    }
    public Complex euler(){// exp(this.euler())==this
        return new Complex(Math.log(abs()),arg());
    }
    public Complex power(Complex z){
        Complex exponent=euler().multiply(z);
        return polar(Math.pow(Math.E,exponent.re),exponent.im);
    }
    public static Complex[] dft(double[] x){// not fft, square time
        Complex[] X=new Complex[x.length];// the transform of x is X by mathematical convention
        for(int i=0;i<X.length;i++){
            double k=2*i*Math.PI/x.length;
            X[i]=new Complex();
            for(int j=0;j<x.length;j++){
                X[i]=X[i].add(polar(x[j],k*j));
            }
        }
        return X;
    }
    public static Complex[] dft(int[] x){
        double[] y=new double[x.length];
        for(int i=0;i<y.length;i++){
            y[i]=(double)x[i];
        }
        return dft(y);
    }
}