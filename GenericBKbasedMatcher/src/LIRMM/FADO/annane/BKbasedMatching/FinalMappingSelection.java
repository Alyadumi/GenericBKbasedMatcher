package LIRMM.FADO.annane.BKbasedMatching;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

public class FinalMappingSelection {
	
	String derived_paths_path, 
		training_set_path = C.MLselectionFolderPath+"training_set.arff", 
		test_set_path = C.MLselectionFolderPath+"test_set.arff";
	ArrayList<String> index_candidate_mappings = new ArrayList<>();
	HashMap<String, ArrayList<Path>> mapping_candidates_paths;

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		// create if they don't exist folders that are required for the execution 
		FinalMappingSelection fms = new FinalMappingSelection();
		fms.MLBasedSelection();
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
				liste.put(uri2,new MappingCandidate(1,pathLength,avgScore,MultScore));
				candidats.put(uri1, liste);
			}
			else
			{
				liste=candidats.get(uri1);
				if(!liste.keySet().contains(uri2))
				{
					liste.put(uri2,new MappingCandidate(1,pathLength,avgScore,MultScore));
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
		long time = System.currentTimeMillis()-debut;
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
				liste.put(uri2,new MappingCandidate(1,pathLength,avgScore,MultScore));
				candidats.put(uri1, liste);
			}
			else
			{
				liste=candidats.get(uri1);
				if(!liste.keySet().contains(uri2))
				{
					liste.put(uri2,new MappingCandidate(1,pathLength,avgScore,MultScore));
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
     * 1. generate the test set
     * 2. generate the training set
     * 2. learning the selection model
     * 3. classifying candidate mappings 
     */
	
	/** generate the test set
	 * @throws IOException 
	 * 
	 */
	public void generateTestSet() throws IOException
	{
		Fichier test_set_file = new Fichier (C.MLselectionFolderPath+"test_set.arff");
		if (test_set_file.exists()) test_set_file.delete();
		test_set_file.ecrire(C.test_arff);
		this.derived_paths_path = C.derivedCheminsPath;
		this.loadMappingCandidatePaths();
		for (String cm:this.mapping_candidates_paths.keySet())
		{
			index_candidate_mappings.add(cm);
			String source_concept_uri = cm.substring(0, cm.indexOf(C.separator));
			String target_concept_uri = cm.substring(cm.indexOf(C.separator)+1);
			ArrayList<Path> paths = this.mapping_candidates_paths.get(cm);
			MappingCandidate map_cand = new MappingCandidate(source_concept_uri, target_concept_uri, paths);
			map_cand.computeAttributes();
			test_set_file.ecrire(map_cand.MaxMax.toString()+','+map_cand.MaxMin.toString()+','+map_cand.MaxAvg.toString()+','+
					                 map_cand.MinMax.toString()+','+map_cand.MinMin.toString()+','+map_cand.MinAvg.toString()+','+
					                 map_cand.MaxMult.toString()+','+map_cand.MinMult.toString()+','+map_cand.AvgMult.toString()+','+
					                 map_cand.MaxSum.toString()+','+map_cand.MinSum.toString()+','+map_cand.AvgSum.toString()+','+
					                 map_cand.AvgMax.toString()+','+map_cand.AvgMin.toString()+','+map_cand.AvgAvg.toString()+','+
					                 map_cand.MaxVar.toString()+','+map_cand.MinVar.toString()+','+map_cand.AvgVar.toString()+','+
					                 map_cand.MaxAvgPerVar.toString()+','+map_cand.MinAvgPerVar.toString()+','+map_cand.AvgAvgPerVar.toString()+','+
					                 String.valueOf(map_cand.maxPathLength)+','+String.valueOf(map_cand.minPathLength)+','+String.valueOf(map_cand.avgPathLength)+','+
					                 String.valueOf(map_cand.pathNumber)+','+
					                 String.valueOf(map_cand.direct_score)+','+
					                 String.valueOf(map_cand.MaxAvgManualMappingsNumber)+','+
                                     "? \r\n");			
		}
		
	}
	 /**
	  * generate the training set
	 * @throws Exception 
	  */
	public  void generateTrainingSet() throws Exception
	{
		Fichier datasetFolder = new Fichier(C.MLselectionDatasetsFolderPath);
		Fichier training_set_file = new Fichier (C.MLselectionFolderPath+"training_set.arff");
		if (training_set_file.exists())training_set_file.delete();
		training_set_file.ecrire(C.training_arff);
		if(datasetFolder != null && datasetFolder.exists())
		{
			File[]	sub_folders = datasetFolder.listFiles();
			for (File folder:sub_folders)
			{	System.out.println(folder.getName());
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
					this.derived_paths_path = matching.generateCandidateMappings();
					this.loadMappingCandidatePaths();					
					//annotate the candidate mappings with the reference alignments
					TreeSet<String> reference_alignment = Fichier.loadReferenceAlignment(reference_alignment_URL.getPath());
					TreeSet<String> reference_alignment_neutre = new TreeSet<>();
					// neutre mappings are specific to OAEI reference alignment, please comment the following instruction when using other datasets 
					reference_alignment_neutre = Fichier.loadReferenceAlignmentNeutre(reference_alignment_URL.getPath());
				 	boolean tag;
				 	//generate the training set file
					for (String cm:this.mapping_candidates_paths.keySet())
					{
						String source_concept_uri = cm.substring(0, cm.indexOf(C.separator));
						String target_concept_uri = cm.substring(cm.indexOf(C.separator)+1);
						ArrayList<Path> paths = this.mapping_candidates_paths.get(cm);
						MappingCandidate map_cand = new MappingCandidate(source_concept_uri, target_concept_uri, paths);
						//value of tag
						if(reference_alignment.contains(cm)||reference_alignment_neutre.contains(cm)||reference_alignment.contains(target_concept_uri+C.separator+source_concept_uri)||reference_alignment_neutre.contains(target_concept_uri+C.separator+source_concept_uri)) 
							{
								map_cand.annotation = true;
							} 
						else map_cand.annotation = false;
						map_cand.computeAttributes();
						training_set_file.ecrire(map_cand.MaxMax.toString()+','+map_cand.MaxMin.toString()+','+map_cand.MaxAvg.toString()+','+
								                 map_cand.MinMax.toString()+','+map_cand.MinMin.toString()+','+map_cand.MinAvg.toString()+','+
								                 map_cand.MaxMult.toString()+','+map_cand.MinMult.toString()+','+map_cand.AvgMult.toString()+','+
								                 map_cand.MaxSum.toString()+','+map_cand.MinSum.toString()+','+map_cand.AvgSum.toString()+','+
								                 map_cand.AvgMax.toString()+','+map_cand.AvgMin.toString()+','+map_cand.AvgAvg.toString()+','+
								                 map_cand.MaxVar.toString()+','+map_cand.MinVar.toString()+','+map_cand.AvgVar.toString()+','+
								                 map_cand.MaxAvgPerVar.toString()+','+map_cand.MinAvgPerVar.toString()+','+map_cand.AvgAvgPerVar.toString()+','+
								                 String.valueOf(map_cand.maxPathLength)+','+
								                 String.valueOf(map_cand.minPathLength)+','+
								                 String.valueOf(map_cand.avgPathLength)+','+
								                 String.valueOf(map_cand.pathNumber)   +','+
								                 String.valueOf(map_cand.direct_score)+','+
								                 String.valueOf(map_cand.MaxAvgManualMappingsNumber)+','+
								                 String.valueOf(map_cand.annotation)+"\r\n");
					}					
				}
			}
		}
		else new Exception("The dataset folder does not exist!").printStackTrace();
		
		//merge all the arff files to have the training set
	}
    /**
     * this function load the generated paths per candidate mapping
     * @throws IOException
     */
	public void loadMappingCandidatePaths() throws IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader(this.derived_paths_path)); 
	 	String line = null ;

	 	mapping_candidates_paths = new HashMap<>();
	 	while ((line = reader.readLine()) != null) 
	 	{
	 		Path p = new Path(line);
	 		String key = p.source_concept_uri+C.separator+p.target_concept_uri;
	 		if(!mapping_candidates_paths.keySet().contains(key))
	 		{
	 			ArrayList<Path> l = new ArrayList<>();
	 			mapping_candidates_paths.put(key, l);
	 		}
	 		mapping_candidates_paths.get(key).add(p);	
	 	}
	}
	
	
	 public   TreeSet<String> MLBasedSelection( ) throws Exception
	   {
		   System.out.println("ML based final mapping selection");
		  generateTestSet();
		 //  generateTrainingSet();
		   TreeSet<String> alignment = new TreeSet<>();
		   //load the training set
		   BufferedReader rt = new BufferedReader(new InputStreamReader(new FileInputStream(this.training_set_path)));
		   Instances data = new Instances(rt);
		   // setting class attribute 
		   if (data.classIndex() == -1)
		     data.setClassIndex(data.numAttributes() - 1);

		   RandomForest tree = new RandomForest();   // new instance of tree

		   tree.buildClassifier(data);   // learn the model (classifier)
		   
		   // load unlabeled data
		   Instances unlabeled = new Instances(
		                           new BufferedReader(
		                             new FileReader(this.test_set_path)));
		   // set class attribute
		   unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
		   // label instances
		   for (int i = 0; i < unlabeled.numInstances(); i++) {
		     double clsLabel = tree.classifyInstance(unlabeled.instance(i));
		     if(clsLabel == 0.0)
		     {
		    	 alignment.add(index_candidate_mappings.get(i).replace(C.separator, ",")+",1.0");
		     }
		    }
		  
		  return alignment;   
}

} //end class
