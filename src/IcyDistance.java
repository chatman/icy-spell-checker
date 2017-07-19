import java.util.Arrays;
import java.util.List;


public class IcyDistance
{

	static double SUBSTITUTION (char s, char t, int index) {
		char keyboard[][] = {"qwertyuiop".toCharArray(), "asdfghjkl;".toCharArray(), "zxcvbnm,./".toCharArray()};
		List<Character> vowels = Arrays.asList('a', 'e', 'i', 'o', 'u'); 
		for (int r=0; r<3; r++)
			for (int c=0; c<keyboard[r].length; c++)
				if (keyboard[r][c]==s && (
						(c>=1 						&& keyboard[r][c-1]==t) ||
						(c<keyboard[r].length-1 	&& keyboard[r][c+1]==t) ||
						(r>=1 						&& keyboard[r-1][c]==t) ||
						(r<=1					 	&& keyboard[r+1][c]==t) ))
					return 0.8;
		if (vowels.contains(s) && vowels.contains(t))
			return 0.8;
		if (index==0)
			return 1.2;
		return 1;
	}
	public static double damlev(String s, String t, double[] workspace)
	{
		double DELETION = 1.0;
		double INSERTION = 1.1;
		double TRANSPOSITION = 0.8;
		//double SUBSTITUTION = 1.2;

		int lenS = s.length();
		int lenT = t.length();
		int lenS1 = lenS + 1;
		int lenT1 = lenT + 1;
		if (lenT1 == 1)
			return lenS1 - 1;
		if (lenS1 == 1)
			return lenT1 - 1;
		double[] dl = workspace;
		int dlIndex = 0;
		int sPrevIndex = 0, tPrevIndex = 0, rowBefore = 0;
		double min = 0, cost = 0, tmp = 0;
		int tri = lenS1 + 2;
		// start row with constant
		dlIndex = 0;
		for (tmp = 0; tmp < lenT1; tmp++)
		{
			dl[dlIndex] = tmp;
			dlIndex += lenS1;
		}
		for (int sIndex = 0; sIndex < lenS; sIndex++)
		{
			dlIndex = sIndex + 1;
			dl[dlIndex] = dlIndex; // start column with constant
			for (int tIndex = 0; tIndex < lenT; tIndex++)
			{
				rowBefore = dlIndex;
				dlIndex += lenS1;
				//deletion
				min = dl[rowBefore] + DELETION;
				// insertion
				tmp = dl[dlIndex - 1] + INSERTION;
				if (tmp < min)
					min = tmp;
				cost = TRANSPOSITION;
				if (s.charAt(sIndex) == t.charAt(tIndex))
					cost = 0;
				//if (cost!=0)
					//System.out.println("Deletion/Insertion at: "+sIndex);

				if (sIndex > 0 && tIndex > 0)
				{
					if (s.charAt(sIndex) == t.charAt(tPrevIndex) && s.charAt(sPrevIndex) == t.charAt(tIndex))
					{
						//System.out.println("Transposition of: "+s.charAt(sIndex)+s.charAt(sPrevIndex)+", "+t.charAt(tIndex)+t.charAt(tPrevIndex));
						tmp = dl[rowBefore - tri] + cost;
						// transposition
						if (tmp < min)
							min = tmp;
					}
				}

				char sChar = sIndex<s.length()?s.charAt(sIndex):'\0';
				char tChar = tIndex<t.length()?t.charAt(tIndex):'\0';
				if (cost!=0) cost = SUBSTITUTION(sChar, tChar, sIndex);
				//if (cost!=0) System.out.println("Subs of: "+s.charAt(sIndex)+", "+t.charAt(tIndex));
				// substitution
				tmp = dl[rowBefore - 1] + cost; 
				if (tmp < min)
					min = tmp;
				dl[dlIndex] = min;

				if (DEBUG) {
					System.out.println("sPrevIndex=" + sPrevIndex + ", tPrevIndex=" + tPrevIndex + ", sIndex=" + sIndex + ", tIndex=" + tIndex);
					System.out.println("'" + s + "', '" + t + "'");
					for (int v = 0; v < lenT1; v++)
					{
						for (int w = 0; w < lenS1; w++)
							System.out.print(dl[v * lenS1 + w] + " ");
						System.out.println();
					}
				}    
				tPrevIndex = tIndex;
			}
			sPrevIndex = sIndex;
		}
		return dl[dlIndex];
	}
	
	static boolean DEBUG = false;

	public static double[] getWorkspace(int sl, int tl)
	{
		return new double[(sl + 1) * (tl + 1)];
	}
	private final static double[] ZERO_LENGTH_INT_ARRAY = new double[0];

	static DoubleMeta dm = new DoubleMeta();
	public static double getDistance (String s, String t)
	{
		double value;
		if (s != null && t != null)
			value = damlev(s, t, getWorkspace(s.length(), t.length()));
		else
			value = damlev(s, t, ZERO_LENGTH_INT_ARRAY);
		if (dm.transform(s).equals(dm.transform(t))) {
			value*=0.9;
		} else {
			//System.out.println("S: "+dm.transform(s)+", T: "+dm.transform(t));
		}
		value/=t.length();
		return value;
	}

	public static void main(String[] args) throws Exception
	{
		//System.out.println(damlev("pooland", "pooliand"));
		
		//System.out.println(damlev("12xy45", "12yx45"));
		System.out.println(getDistance("off", "of"));
		System.out.println(getDistance("o", "of"));
		System.out.println(getDistance("portland", "portland"));
		System.out.println(getDistance("phoenix", "pheonix"));

	}
}