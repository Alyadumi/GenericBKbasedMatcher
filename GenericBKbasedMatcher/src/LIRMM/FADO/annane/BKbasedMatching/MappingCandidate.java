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
	int MaxAvgManualMappingsNumber =0;
	
	boolean annotation;// true or false according to the reference alignment
	
	public MappingCandidate(int numberPath,int pathLengthMin,Double avgMax, Double MaxMult, boolean res) {
		// TODO Auto-generated constructor stub
		this.pathNumber=numberPath;
		this.minPathLength=pathLengthMin;
		this.MaxAvg=avgMax;
		this.MaxMult=MaxMult;
		this.annotation=res;
	}
	
	public MappingCandidate(int numberPath,int pathLengthMin,Double avgMax, Double MaxMult) {
		// TODO Auto-generated constructor stub
		this.pathNumber=numberPath;
		this.minPathLength=pathLengthMin;
		this.MaxAvg=avgMax;
		this.MaxMult=MaxMult;
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
		
		MinMin = paths.get(0).minScores();
		MaxMin = paths.get(0).minScores();
		
		MinAvg = paths.get(0).avgScores();
		MaxAvg = paths.get(0).avgScores();
		
		MinMult = paths.get(0).multScores();
		MaxMult = paths.get(0).multScores();
		
		MinSum = paths.get(0).sumScores();
		MaxSum = paths.get(0).sumScores();
		
		MinVar = paths.get(0).varScores();
		MaxVar = paths.get(0).varScores();
		
		MinAvgPerVar = paths.get(0).avgPerVarScores();
		MaxAvgPerVar = paths.get(0).avgPerVarScores();
		
		MaxAvgManualMappingsNumber = paths.get(0).AvgManualMappings();
		
		
		for (Path p : paths)
		{
			if(p.length == 2)this.direct_score = p.scores.get(0);
			
			//path length
			if(minPathLength>p.length) minPathLength = p.length;
			if(maxPathLength<p.length) maxPathLength = p.length;
			avgPathLength = avgPathLength + p.length;
			
			//path max
			if(MinMax>p.maxScores())MinMax = p.maxScores();
			if(MaxMax<p.maxScores())MaxMax = p.maxScores();
			AvgMax = AvgMax + p.maxScores();
			
			//path min
			if(MinMin>p.minScores())MinMin = p.minScores();
			if(MaxMin<p.minScores())MaxMin = p.minScores();
			AvgMin = AvgMin + p.minScores();
			
			//path avg
			if(MinAvg>p.avgScores())MinAvg = p.avgScores();
			if(MaxAvg<p.avgScores())MaxAvg = p.avgScores();
			AvgAvg = AvgAvg + p.avgScores();
			
			//path mult
			if(MinMult>p.multScores())MinMult = p.multScores();
			if(MaxMult<p.multScores())MaxMult = p.multScores();
			AvgMult = AvgMult + p.multScores();
			
			//path sum
			if(MinSum>p.sumScores())MinSum = p.sumScores();
			if(MaxSum<p.sumScores())MaxSum = p.sumScores();
			AvgSum = AvgSum + p.sumScores();
			
			//path var
			if(MinVar>p.varScores())MinVar = p.varScores();
			if(MaxVar<p.varScores())MaxVar = p.varScores();
			AvgVar = AvgVar + p.varScores();
			
			//path avg/var
			if(MinAvgPerVar>p.avgPerVarScores())MinAvgPerVar = p.avgPerVarScores();
			if(MaxAvgPerVar<p.avgPerVarScores())MaxAvgPerVar = p.avgPerVarScores();
			AvgAvgPerVar = AvgAvgPerVar + p.avgPerVarScores();	
			
			//
			if(MaxAvgManualMappingsNumber<p.AvgManualMappings())MaxAvgManualMappingsNumber = p.AvgManualMappings();
		}
		pathNumber = paths.size();
		avgPathLength = avgPathLength/pathNumber;
		AvgMax = AvgMax / pathNumber;
		AvgMin = AvgMin / pathNumber;
		AvgAvg = AvgAvg / pathNumber;
		AvgMult = AvgMult / pathNumber;
		AvgSum = AvgSum / pathNumber;
		AvgVar = AvgVar / pathNumber;
		AvgAvgPerVar = AvgAvgPerVar / pathNumber;		
	}

}

