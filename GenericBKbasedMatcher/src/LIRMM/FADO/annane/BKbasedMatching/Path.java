package LIRMM.FADO.annane.BKbasedMatching;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Path {
	
	public static void main(String[] args) throws Exception {
		Path p = new Path("a,b,3,$$null¤a¤ao$$3¤b¤bo,");
		System.out.println(p.avgScores());
	}
	
	/**
	 * Constructor
	 * @param derived_path
	 */
	String source_concept_uri = null;
	String target_concept_uri = null;
	int length = 0;// number of nodes
	ArrayList<Double> scores = new ArrayList<>();
	ArrayList<Noeud>  nodes = new ArrayList<>();
	ArrayList<String> relations = new ArrayList<>();
	ArrayList<String> origin = new ArrayList<>(); //manual or automatic
	int number_manual_mapping = 0;
	
	
	public Path(String derived_path)
	{
		if(derived_path.length()>0)
		{
			String[] elements = derived_path.split(",");
			source_concept_uri = elements[0];
			target_concept_uri = elements[1];
			length = Integer.parseInt(elements[2]);
			String path = elements[3];
			StringTokenizer lineParser = new StringTokenizer(path, "$$");

		    while (lineParser.hasMoreTokens())
			{
		    	String e = lineParser.nextToken();
				String node_elements[] = e.split(C.separator);
				if(node_elements.length>0) {
					String score = node_elements[0];
					String concept_uri = node_elements[1];
					String ontology_uri = node_elements[2];
					double double_score;
					if(score.equalsIgnoreCase("null")) double_score = 0.0;
					else 
						{
							double_score = Double.parseDouble(score);
							this.scores.add(double_score);
						}
					Noeud node = new Noeud(concept_uri, ontology_uri, double_score);
					this.nodes.add(node);
				}
			}
			
			
		}
	}

	
	
	public double sumScores()
	{
		double res = 0.0;
		for (double s:this.scores)
		{
			res = res + s;
		}
		return res;
	}
	
	public double avgScores()
	{

		return (this.sumScores()/(this.length-1));
	}

	public double maxScores()
	{
		double res = 0.0;
		for (double s : this.scores)
		{
			if(res<s)res=s;
		}
		return res;
	}
	
	public double minScores()
	{
		double res = 1000;// random initialisation with a value greater than any mapping score
		for (double s : this.scores)
		{
			if(res>s)res = s;
		}
		return res;
	}

	public double multScores()
	{
		double res = 1.0;
		for(double s:scores)
		{
			res = res * s;
		}
		return res;
	}
	
	public int AvgManualMappings()
	{
		int res = 0;
		for(double s:scores)
		{
			if(res == 2.0) res++;
		}
		return res/(length-1);
	}

	public double varScores()
	{
		double avg = this.avgScores();
		double y = 0.0;
		for (double s:scores)
		{
			y = y + Math.pow((s-avg),2);
		}
		return y/(length-1);
	}
	
	public double avgPerVarScores() {
		double avg = avgScores();
		double var = varScores();
		return avg/var;
	}
}
