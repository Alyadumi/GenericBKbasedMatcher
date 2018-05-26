package LIRMM.FADO.annane.BKbasedMatching;

public class MappingCandidate {

	int pathNumber;
	int minPathLength;
	Double MaxAvg;
	Double MaxMult;
	String res;
	String relation;
	

	
	public MappingCandidate(int numberPath,int pathLengthMin,Double avgMax, Double MaxMult, String relation) {
		// TODO Auto-generated constructor stub
		this.pathNumber=numberPath;
		this.minPathLength=pathLengthMin;
		this.MaxAvg=avgMax;
		this.MaxMult=MaxMult;
		this.relation=relation;
	}

}

