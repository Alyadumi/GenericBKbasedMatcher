package LIRMM.FADO.annane.BKbasedMatching;

public class MappingCandidate {

	int pathNumber;
	int minPathLength;
	Double MaxAvg;
	Double MaxMult;
	String res;
	
	public MappingCandidate(int numberPath,int pathLengthMin,Double avgMax, Double MaxMult, String res) {
		// TODO Auto-generated constructor stub
		this.pathNumber=numberPath;
		this.minPathLength=pathLengthMin;
		this.MaxAvg=avgMax;
		this.MaxMult=MaxMult;
		this.res=res;
	}

}

