package LIRMM.FADO.annane.BKbasedMatching;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

public class FinalMappingSelection {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// create if they don't exist folders that are required for the execution 
		
		FinalMappingSelection.generateTrainingSet();

	}
	
	public FinalMappingSelection() {
		
	}
	
	/* ***************************************** Rule based selection **************************************************** */
	/**
	 * This method selects the final mappings from the set of candidate mappings
	 * @param threshold: the minimum score for a mapping candidate
	 * @return the list of selected mappings as a TreeSet
	 * @throws NumberFormatException scores are strings that will be parsed in double which may raise this exception
	 * @throws IOException: the set of mappin candidates are in a file 'chemins.csv'. Opening this file may raise this exception
	 */
	public TreeSet<String> selection(double threshold) throws NumberFormatException, IOException
	{
		TreeSet<String> finalMappings=new TreeSet<>();
		long debut =System.currentTimeMillis();
		File chemins=new File(C.derivedCheminsPath);
		if(chemins.exists())
		{BufferedReader reader = new BufferedReader(new FileReader(C.derivedCheminsPath)); 
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
				    StringTokenizer details = new StringTokenizer(m, C.separator);
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
		TreeSet<String> a = selection2(C.derivedCheminsPath, threshold);
		for (String m : a) {
			StringTokenizer lineParser = new StringTokenizer(m, ",");
			String uri2=lineParser.nextToken();
			String uri1=lineParser.nextToken();
			String score=lineParser.nextToken();
			//String res=lineParser.nextToken();
			finalMappings.add(uri1+','+uri2+','+score);	
		}
		long time=System.currentTimeMillis()-debut;
		C.executionTime.add("selection "+(time)+"ms");}
		else System.out.println("The derivation result is empty");
		return finalMappings;
	}

	/**
	 * selection2
	 * @param chemins: path of the file containing candidate mappings (paths)
	 * @param threshold
	 * @return the set of selected mappings among the candidate ones
	 * @throws NumberFormatException: parsing mapping scores may raise this exception
	 * @throws IOException: error while opening 'chemins' file may raise this exception
	 */
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
				    StringTokenizer details = new StringTokenizer(m, C.separator);
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
		C.executionTime.add("selection "+(time)+"ms");
		return finalMappings;
	}

	/* ***************************************** ML based selection **************************************************** */
    /**
     * the ML based selection requires several steps:
     * 1. generate the training set
     * 2. learning the selection model
     * 3. classifying candidate mappings 
     */
	
	 /**
	  * generate the training set
	 * @throws Exception 
	  */
	public static void generateTrainingSet() throws Exception
	{
		Fichier datasetFolder = new Fichier(C.MLselectionDatasetsFolderPath);
		if(datasetFolder != null && datasetFolder.exists())
		{
			File[]	sub_folders = datasetFolder.listFiles();
			for (File folder:sub_folders)
			{
				URL source_ontology_URL = null, target_ontology_URL = null, reference_alignment_URL =null;
				File[] files = folder.listFiles();
				for(File file: files)
				{
					if(file.getName().contains("source")) source_ontology_URL = file.toURI().toURL();
					else if (file.getName().contains("target"))   target_ontology_URL = file.toURI().toURL();
					else if(file.getName().contains("reference")) reference_alignment_URL = file.toURI().toURL();
				}
				if(source_ontology_URL == null || target_ontology_URL == null || reference_alignment_URL == null ) 
					new Exception("source, target or reference file are missing from "+folder.getName()).printStackTrace(); 
				else
				{
					C.sourceOntology = source_ontology_URL;
					C.targetOntology = target_ontology_URL;
					Matching matching = new Matching(C.sourceOntology, C.targetOntology);
					matching.returnCandidateMappings();
					//annotate the candidate mappings with the reference alignments
					//generate the arff file
					
				}
				
			}
		}
		else new Exception("The dataset folder does not exist!").printStackTrace();
		
		//merge all the arff files to have the training set
	}
	  /* public static  TreeSet<String> testWekaWithJava(String learn, String test,String indexInstancesPath) throws Exception
	   {
		   System.out.println("ML based final mapping selection");
		  // String folder="C://Users//annane//Desktop//today3//";
		   TreeSet<String> a = new TreeSet<>();
		   BufferedReader rt = new BufferedReader(new InputStreamReader(new FileInputStream(C.folder+learn+".arff")));
		   Instances data = new Instances(rt);
		   System.out.println("fichier de training est chargé ");
		   // setting class attribute if the data format does not provide this information
		   // For example, the XRFF format saves the class attribute information as well
		   if (data.classIndex() == -1)
		     data.setClassIndex(data.numAttributes() - 1);
		   //String[] options = new String[1];
		   //options[0] = "-U";            // unpruned tree
		   RandomForest tree = new RandomForest();   // new instance of tree
		   //tree.setOptions(options);     // set the options
		   tree.buildClassifier(data);   // build classifier

		   System.out.println("modèle appris");
		   
		   ArrayList<String> indexInstances=Fichier.loadInstances(indexInstancesPath);
		   System.out.println("les instances chargées: "+indexInstances.size());
		   // load unlabeled data
		   Instances unlabeled = new Instances(
		                           new BufferedReader(
		                             new FileReader(C.folder+test+".arff")));
		   
		   // set class attribute
		   unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
		   
		   // create copy
		   Instances labeled = new Instances(unlabeled); 
		  
		   // label instances
		   for (int i = 0; i < unlabeled.numInstances(); i++) {
		     double clsLabel = tree.classifyInstance(unlabeled.instance(i));
		    if(clsLabel==0)
		    {
		    	a.add(indexInstances.get(i));
		       //writer.println(indexInstances.get(i));
		       // writer.flush();
		      }
		   }
		   return a;
	   }*/
}
