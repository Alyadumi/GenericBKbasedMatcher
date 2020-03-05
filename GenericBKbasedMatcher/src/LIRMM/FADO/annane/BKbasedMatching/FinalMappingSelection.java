package LIRMM.FADO.annane.BKbasedMatching;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class FinalMappingSelection {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * This method selects the final mappings from the set of candidate mappings
	 * @param threshold: the min value that may have a selected mapping
	 * @return selected mappings in TreeSet format
	 * @throws NumberFormatException Scores are casted into double type which may rise this exception
	 * @throws IOException l'ensemble des mappings candidats sont dans un fichier path, l'ouverture de ce fichier peut générer cette exception
	 */
	public FinalMappingSelection() {
		
	}
	
	public TreeSet<String> selection(double threshold) throws NumberFormatException, IOException
	{
		TreeSet<String> finalMappings=new TreeSet<>();
		long debut =System.currentTimeMillis();
		File chemins=new File(Parameters.derivedCheminsPath);
		if(chemins.exists())
		{BufferedReader reader = new BufferedReader(new FileReader(Parameters.derivedCheminsPath)); 
		String line = null; 
		String value = null; 
	    int cpt=0;
	    Map<String,Map<String,MappingCandidate> > candidats=new HashMap<>();
		while ((line = reader.readLine()) != null) 
		{
			StringTokenizer lineParser = new StringTokenizer(line, ",");
			String uri1 = (String) lineParser.nextElement();
			String uri2 = (String) lineParser.nextElement();
			int pathLength=Integer.parseInt((String) lineParser.nextElement())-1;
			String path=(String) lineParser.nextElement();
			//String res=(String) lineParser.nextElement();
			//parser le chemin
			 lineParser = new StringTokenizer(path, "$$");
			 lineParser.nextToken();
			 Double avgScore=0.0, MultScore=1.0;
			 double score=0;
			 boolean subclass=false;
			 while(lineParser.hasMoreTokens())
			 {
				 String m=lineParser.nextToken();
				 if(!m.equals(""))
				 {
				    StringTokenizer details = new StringTokenizer(m, Parameters.separator);
					score=Double.parseDouble(details.nextToken());
					if(score==2.0)score=1.0;
					if(score==3.0){score=1.0;subclass=true;}
				 }
				 avgScore=(avgScore+score)/2;
				 MultScore=MultScore*score;
			 }
			 Map<String,MappingCandidate> liste;
			if (!candidats.keySet().contains(uri1))
			{
				liste =new HashMap<>();
				liste.put(uri2,new MappingCandidate(1,pathLength,avgScore,MultScore,null));
				candidats.put(uri1, liste);
			}
			else
			{
				liste=candidats.get(uri1);
				if(!liste.keySet().contains(uri2))
				{
					liste.put(uri2,new MappingCandidate(1,pathLength,avgScore,MultScore,null));
					candidats.put(uri1, liste);
				}
				else
				{
					MappingCandidate m=liste.get(uri2);
					liste.get(uri2).pathNumber++;
					if(liste.get(uri2).MaxAvg<avgScore)liste.get(uri2).MaxAvg=avgScore;
					if(liste.get(uri2).MaxMult<MultScore)liste.get(uri2).MaxMult=MultScore;
					if(liste.get(uri2).minPathLength>pathLength)liste.get(uri2).minPathLength=pathLength;
				}
			}
	}//endWhile
		//System.out.println(candidats.size());

		for (String uri1 : candidats.keySet()) {
			Map<String, MappingCandidate> liste = candidats.get(uri1);
		    double maxMultCandidatScore=0.0;
		    boolean stop =false;
		    MappingCandidate maxCandidate=null;
		    String uriCandidate=null;
			for (String uri2 : liste.keySet())
			{	
				MappingCandidate c = liste.get(uri2);
				if(c.MaxMult>=threshold)
				{
					if(c.minPathLength==1 && c.pathNumber>1)
					{
						finalMappings.add(uri1+','+uri2+','+c.MaxMult);
						stop=true;
					}
					if(c.MaxAvg>=1.0) 
					{
						finalMappings.add(uri1+','+uri2+','+c.MaxMult); 
						stop=true;
					}
					if(c.MaxMult>maxMultCandidatScore)
					{
						maxMultCandidatScore=c.MaxMult;
						maxCandidate=c;
						uriCandidate=uri2;
					}
				}
			}
			if(maxMultCandidatScore>=threshold)
			{
				if(!stop)
				{
				finalMappings.add(uri1+','+uriCandidate+','+maxMultCandidatScore);	
				}
			}
			
		}
		TreeSet<String> a = selection2(Parameters.derivedCheminsPath, threshold);
		for (String m : a) {
			StringTokenizer lineParser = new StringTokenizer(m, ",");
			String uri2=lineParser.nextToken();
			String uri1=lineParser.nextToken();
			String score=lineParser.nextToken();
			//String res=lineParser.nextToken();
			finalMappings.add(uri1+','+uri2+','+score);	
		}
		long time=System.currentTimeMillis()-debut;
		}
		else System.out.println("The derivation result is empty");
		return finalMappings;
	}
	/* *****************************************SELECTION2**************************************************** */
	public TreeSet<String> selection2(String chemins,double threshold) throws NumberFormatException, IOException
	{
		long debut =System.currentTimeMillis();
		BufferedReader reader = new BufferedReader(new FileReader(chemins)); 
		String line = null; 
		String value = null; 
	    int cpt=0;
	    Map<String,Map<String,MappingCandidate> > candidats=new HashMap<>();
		while ((line = reader.readLine()) != null) 
		{
			StringTokenizer lineParser = new StringTokenizer(line, ",");
			String uri2 = (String) lineParser.nextElement();
			String uri1 = (String) lineParser.nextElement();
			int pathLength=Integer.parseInt((String) lineParser.nextElement())-1;
			String path=(String) lineParser.nextElement();
			//String res=(String) lineParser.nextElement();
			//parser le chemin
			 lineParser = new StringTokenizer(path, "$$");
			 lineParser.nextToken();
			 Double avgScore=0.0, MultScore=1.0;
			 double score=0;
			 while(lineParser.hasMoreTokens())
			 {
				 String m=lineParser.nextToken();
				 if(!m.equals(""))
				 {
				    StringTokenizer details = new StringTokenizer(m, Parameters.separator);
					score=Double.parseDouble(details.nextToken());
					if(score==2.0)score=1.0;
				 }
				 avgScore=(avgScore+score)/2;
				 MultScore=MultScore*score;
			 }
			 Map<String,MappingCandidate> liste;
			if (!candidats.keySet().contains(uri1))
			{
				liste =new HashMap<>();
				liste.put(uri2,new MappingCandidate(1,pathLength,avgScore,MultScore,null));
				candidats.put(uri1, liste);
			}
			else
			{
				liste=candidats.get(uri1);
				if(!liste.keySet().contains(uri2))
				{
					liste.put(uri2,new MappingCandidate(1,pathLength,avgScore,MultScore,null));
					candidats.put(uri1, liste);
				}
				else
				{
					MappingCandidate m=liste.get(uri2);
					liste.get(uri2).pathNumber++;
					if(liste.get(uri2).MaxAvg<avgScore)liste.get(uri2).MaxAvg=avgScore;
					if(liste.get(uri2).MaxMult<MultScore)liste.get(uri2).MaxMult=MultScore;
					if(liste.get(uri2).minPathLength>pathLength)liste.get(uri2).minPathLength=pathLength;
				}
			}
	}//endWhile
		//System.out.println(candidats.size());
		TreeSet<String> finalMappings=new TreeSet<>();
		for (String uri1 : candidats.keySet()) {
			Map<String, MappingCandidate> liste = candidats.get(uri1);
		    double maxMultCandidatScore=0.0;
		    boolean stop =false;
		    MappingCandidate maxCandidate=null;
		    String uriCandidate=null;
			for (String uri2 : liste.keySet())
			{	
				MappingCandidate c = liste.get(uri2);
				if(c.MaxMult>=threshold)
				{
					if(c.minPathLength==1 && c.pathNumber>1){finalMappings.add(uri1+','+uri2+','+c.MaxMult);stop=true;}
					if(c.MaxAvg>=1.0) {finalMappings.add(uri1+','+uri2+','+c.MaxMult); stop=true;}
					if(c.MaxMult>maxMultCandidatScore)
					{
						maxMultCandidatScore=c.MaxMult;
						maxCandidate=c;
						uriCandidate=uri2;
					}
				}
			}
			if(maxMultCandidatScore>=threshold)
			{
				if(!stop)
				{
				finalMappings.add(uri1+','+uriCandidate+','+maxMultCandidatScore);	
				}
			}
			
		}
		long time=System.currentTimeMillis()-debut;

		return finalMappings;
	}


}
