package LIRMM.FADO.annane.BKbasedMatching;

import java.util.ArrayList;

public class MappingCandidate {
    String source_concept_uri;
    String target_concept_uri;
    ArrayList<Path> paths;
    
	int pathNumber;
	int minPathLength, maxPathLength ;	
	double avgPathLength = 0.0;
	Double MaxMult, MinMult, AvgMult = 0.0;
	Double MaxSum, MinSum, AvgSum = 0.0;
	Double MaxAvg, MinAvg, AvgAvg = 0.0;
	Double MaxVar, MinVar, AvgVar = 0.0;
	Double MaxMin, MinMin, AvgMin = 0.0;
	Double MaxMax, MinMax, AvgMax = 0.0;
	Double MaxAvgPerVar, MinAvgPerVar, AvgAvgPerVar = 0.0;
	double direct_score = 0.0;
	double maximum_average_of_manual_mappings;
	
	
	
	
	
	
	String annotation;// true or false according to the reference alignment
	
	public MappingCandidate(int numberPath,int pathLengthMin,Double avgMax, Double MaxMult, String res) {
		// TODO Auto-generated constructor stub
		this.pathNumber=numberPath;
		this.minPathLength=pathLengthMin;
		this.MaxAvg=avgMax;
		this.MaxMult=MaxMult;
		this.annotation=res;
	}
	
	public MappingCandidate(String source_concept_uri, String target_concept_uri, ArrayList<Path> paths) {
		// TODO Auto-generated constructor stub
      this.source_concept_uri = source_concept_uri;
      this.target_concept_uri = target_concept_uri;
      this.paths = paths;
	}
	
	public void computeAttributes()
	{	minPathLength = paths.get(0).length; 
		maxPathLength = paths.get(0).length; 
		
		MinMax = paths.get(0).maxScores();
		MaxMax = paths.get(0).maxScores();
		
		
		
		
		for (Path p : paths)
		{
			if(p.length == 2)this.direct_score = p.scores.get(0);
			
			//path length
			if(minPathLength>p.length) minPathLength = p.length;
			if(maxPathLength<p.length) maxPathLength = p.length;
			avgPathLength = avgPathLength + p.length;
			
			//path mult
			if(MinMax>p.maxScores())MinMax = p.maxScores();
			if(MaxMax<p.maxScores())MaxMax = p.maxScores();
			AvgMax = AvgMax + p.maxScores();
		}
		

		pathNumber = paths.size();
		avgPathLength = avgPathLength/pathNumber;
		AvgMax = AvgMax / pathNumber;
		

		
		
	}

}

