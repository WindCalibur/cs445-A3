import java.io.IOException;

public class A3q4 {
	
	static double[][] f = new double[5][3];
	static double[][] b = new double[5][3];
	static double[][] emission = new double[3][4];
	static double[][] transition = new double[3][3];
	static int[] sequence = new int[4];
	static double prob;
	
	public A3q4() {
		// 1 = fair, 2 = loaded
		sequence[0] = 4;
		sequence[1] = 2;
		sequence[2] = 1;
		sequence[3] = 1;
		
		f[0][0] = 1.0;
		f[0][1] = 0;
		f[0][2] = 0;
		f[1][0] = 0;
		f[1][1] = 1.0/Math.pow(2, 3);
		f[1][2] = 1.0/Math.pow(2, 4);;
		f[2][0] = 0;
		f[2][1] = 1.0/Math.pow(2, 5);
		f[2][2] = 1.0/Math.pow(2, 6);
		f[3][0] = 0;
		f[3][1] = 1.0/Math.pow(2, 7);
		f[3][2] = 1.0/Math.pow(2, 7);
		f[4][0] = 0;
		f[4][1] = 9.0/Math.pow(2, 12);
		f[4][2] = 7.0/Math.pow(2, 11);
		
		prob = f[4][1] + f[4][2];
		
		// prob should be 5.6 x 10^3
		
		b[0][0] = 23.0/Math.pow(2,12);
		b[0][1] = 0;
		b[0][2] = 0;
		b[1][0] = 0;
		b[1][1] = 823.0/Math.pow(2, 15);
		b[1][2] = 649.0/Math.pow(2, 14);
		b[2][0] = 0;
		b[2][1] = 91.0/Math.pow(2, 10);
		b[2][2] = 93.0/Math.pow(2, 9);
		b[3][0] = 0;
		b[3][1] = 9.0/Math.pow(2, 5);
		b[3][2] = 7.0/Math.pow(2, 4);
		b[4][0] = 0;
		b[4][1] = 1.0;
		b[4][2] = 1.0;
		
		emission[0][0] = 0;
		emission[0][1] = 0;
		emission[0][2] = 0;
		emission[0][3] = 0;
		emission[1][0] = 0.25;
		emission[1][1] = 0.25;
		emission[1][2] = 0.25;
		emission[1][3] = 0.25;
		emission[2][0] = 0.5;
		emission[2][1] = 0.25;
		emission[2][2] = 0.125;
		emission[2][3] = 0.125;

		transition[0][0] = 0;
		transition[0][1] = 0.5;
		transition[0][2] = 0.5;
		transition[1][0] = 0;
		transition[1][1] = 0.875;
		transition[1][2] = 0.125;
		transition[2][0] = 0;
		transition[2][1] = 0.25;
		transition[2][2] = 0.75;
		
	}
	
	public void iterate() {
		double sumBF = 0;
		double sumBL = 0;
		double sumFF = 0;
		double sumFL = 0;
		double sumLF = 0;
		double sumLL = 0;
		
		for (int i=0; i < sequence.length; i++) {

			sumBF += f[i][0] * b[i+1][1] * emission[1][sequence[i]-1] * transition[0][1] / prob;
			sumBL += f[i][0] * b[i+1][2] * emission[2][sequence[i]-1] * transition[0][2] / prob;
			sumFF += f[i][1] * b[i+1][1] * emission[1][sequence[i]-1] * transition[1][1] / prob;
			sumFL += f[i][1] * b[i+1][2] * emission[2][sequence[i]-1] * transition[1][2] / prob;
			sumLF += f[i][2] * b[i+1][1] * emission[1][sequence[i]-1] * transition[2][1] / prob;
			sumLL += f[i][2] * b[i+1][2] * emission[2][sequence[i]-1] * transition[2][2] / prob;
			
		}
		System.out.println("A(lk) for 0 -> F = " + sumBF);
		System.out.println("A(lk) for 0 -> L = " + sumBL);
		System.out.println("A(lk) for F -> F = " + sumFF);
		System.out.println("A(lk) for F -> L = " + sumFL);
		System.out.println("A(lk) for L -> F = " + sumLF);
		System.out.println("A(lk) for L -> L = " + sumLL);
		
		
		transition[0][1] = sumBF / (sumBF + sumBL);
		transition[0][2] = sumBL / (sumBF + sumBL);
		transition[1][1] = sumFF / (sumFF + sumFL);
		transition[1][2] = sumFL / (sumFF + sumFL);
		transition[2][1] = sumLF / (sumLF + sumLL);
		transition[2][2] = sumLL / (sumLF + sumLL);
		
		System.out.println("a(0F) = " + transition[0][1]); 
		System.out.println("a(0L) = " + transition[0][2]); 
		System.out.println("a(FF) = " + transition[1][1]); 
		System.out.println("a(FL) = " + transition[1][2]); 
		System.out.println("a(LF) = " + transition[2][1]); 
		System.out.println("a(LL) = " + transition[2][2]); 
	}
	
	public void findProbability() {
		for (int i=1; i<sequence.length+1; i++) {
			f[i][1] = emission[1][sequence[i-1]-1]*(f[i-1][0]*transition[0][1] + f[i-1][1]*transition[1][1] + f[i-1][2]*transition[2][1]);
			f[i][2] = emission[2][sequence[i-1]-1]*(f[i-1][0]*transition[0][2] + f[i-1][1]*transition[1][2] + f[i-1][2]*transition[2][2]);
		}
		System.out.println("old probability = " + prob);
		prob = f[4][1] + f[4][2];
		System.out.println("new probability = " + prob);
	}
	
	public static void main(String[] args) throws IOException {
		A3q4 a3q4 = new A3q4();
		a3q4.iterate();
		a3q4.findProbability();
	}
	
}
