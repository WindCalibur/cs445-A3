import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

public class A3q2 {
	
	static double[][] f = new double[256][256];
	static double[][] b = new double[256][256];
	final static Charset ENCODING = StandardCharsets.US_ASCII;
	final static String delims = "[ ]+";
	
	public A3q2() {
		
	}

	private void initForward(String matrix_name) throws IOException {
		for (int i=0; i<31; i++)
			for (int j=0; j<9; j++)
				f[i][j] = 0;
		Path path = Paths.get(matrix_name);
		try (Scanner scanner = new Scanner(path,ENCODING.name())) {
			String line;
			do {
				line = scanner.nextLine();
			} while (line.charAt(0)=='#');
			String[] letters = line.split(delims);
			int map[];
			map = new int[9];
			for (int j=1; j<=8; j++) {
				map[j] = letters[j].charAt(0);
				f[0][map[j]] = 1;
			}
			for (int i=0; i<30; i++) {
				String[] scores = scanner.nextLine().split(delims);
				int offset = 0;
				while (scores[offset].isEmpty())
					offset++;
				StringBuilder sb = new StringBuilder();
				String temp = "";
				if (scores[offset].length() == 2) {
					temp = sb.append(scores[offset].charAt(0)).append(scores[offset].charAt(1)).toString(); 
				} else {
					temp = sb.append(scores[offset].charAt(0)).toString();
				}
				
						
				int row = Integer.parseInt(temp);
						
				for (int j=1; j<=8; j++) {
					f[row][map[j]] = Double.parseDouble(scores[offset+j]);
				}
			}
		}
	}
	
	private void initBackwards(String matrix_name) throws IOException {
		for (int i=0; i<31; i++)
			for (int j=0; j<9; j++)
				b[i][j] = 0;
		Path path = Paths.get(matrix_name);
		try (Scanner scanner = new Scanner(path,ENCODING.name())) {
			String line;
			do {
				line = scanner.nextLine();
			} while (line.charAt(0)=='#');
			String[] letters = line.split(delims);
			int map[];
			map = new int[9];
			for (int j=1; j<=8; j++) {
				map[j] = letters[j].charAt(0);
				b[0][map[j]] = 1;
			}
			for (int i=0; i<30; i++) {
				String[] scores = scanner.nextLine().split(delims);
				int offset = 0;
				while (scores[offset].isEmpty())
					offset++;
				StringBuilder sb = new StringBuilder();
				String temp = "";
				if (scores[offset].length() == 2) {
					temp = sb.append(scores[offset].charAt(0)).append(scores[offset].charAt(1)).toString(); 
				} else {
					temp = sb.append(scores[offset].charAt(0)).toString();
				}
				
						
				int row = Integer.parseInt(temp);
						
				for (int j=1; j<=8; j++) {
					b[row][map[j]] = Double.parseDouble(scores[offset+j]);
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		A3q2 a3q2 = new A3q2();
		a3q2.initForward("forward.txt");
		a3q2.initBackwards("backwards.txt");
//		System.out.println(f[10]['t']);
		double prob = f[30]['A'] + f[30]['C'] + f[30]['G'] + f[30]['T'] + f[30]['a'] + f[30]['c'] + f[30]['g'] + f[30]['t'];
		System.out.println(prob);
		String states = "";
		for (int i=1; i <= 30; i++) {
			double Aplus = Math.log(f[i]['A']) + Math.log(b[i]['A']) - Math.log(prob);
			double Cplus = Math.log(f[i]['C']) + Math.log(b[i]['C']) - Math.log(prob);
			double Gplus = Math.log(f[i]['G']) + Math.log(b[i]['G']) - Math.log(prob);
			double Tplus = Math.log(f[i]['T']) + Math.log(b[i]['T']) - Math.log(prob);
			double Aminus = Math.log(f[i]['a']) + Math.log(b[i]['a']) - Math.log(prob);
			double Cminus = Math.log(f[i]['c']) + Math.log(b[i]['c']) - Math.log(prob);
			double Gminus = Math.log(f[i]['g']) + Math.log(b[i]['g']) - Math.log(prob);
			double Tminus = Math.log(f[i]['t']) + Math.log(b[i]['t']) - Math.log(prob);
			
			double max = Math.max(Math.max(Math.max(Aplus, Cplus), Math.max(Gplus, Tplus)), Math.max(Math.max(Aminus, Cminus), Math.max(Gminus, Tminus)));
			
			if (max == Aplus) {
				states = states + "A+, ";
			} else if (max == Cplus) {
				states = states + "C+, ";
			} else if (max == Gplus) {
				states = states + "G+, ";
			} else if (max == Tplus) {
				states = states + "T+, ";
			} else if (max == Aminus) {
				states = states + "A-, ";
			} else if (max == Cminus) {
				states = states + "C-, ";
			} else if (max == Gminus) {
				states = states + "G-, ";
			} else if (max == Tminus) {
				states = states + "T-, ";
			}
		}
		
		System.out.println(states);
		
	}
}
