package LIRMM.FADO.annane.BKbasedMatching;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import org.apache.jena.rdf.model.Model;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;


import fr.inrialpes.exmo.align.parser.AlignmentParser;



public class BKuse {

	URL source=Parameters.sourceOntology;
	URL target=Parameters.targetOntology;
	public static String  targetAcronym, sourceAcronym;
	String sourceIRI, targetIRI;
	TreeSet<String> sourceUris;
	TreeSet<String> matchedSourceUris=new TreeSet<>();
	TreeSet<String> targetUris;
	boolean needInterface=false;
	Map<String, TreeSet<Noeud>> BkGraph;
	ArrayList<ArrayList<Noeud>> paths=new ArrayList<ArrayList<Noeud>>();
	Map<String,String> BkOntologiesCodes;
	public static HashMap<String, String> targetElements ;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
/**
 * Constructor
 * @param source
 * @param target
 * @throws URISyntaxException
 */
	public BKuse(URL source, URL target) throws URISyntaxException
	{
		if(source!=null && target !=null)
		{
			this.source=source;
			this.target=target;
			//  Charger ontology source 	
			Model os=JenaMethods.LoadOntologyModelWithJena(source);	
			sourceUris=JenaMethods.loadOntologyUri(os);
		    //  Charger ontology cible 
			Model oc=JenaMethods.LoadOntologyModelWithJena(target);	
			targetUris=JenaMethods.loadOntologyUri(oc);
		}
		else
		{
			throw new NullPointerException("source or target ontology URL is null");
		}
	}
	
	
	public BKuse(URL source, URL target,boolean ElcioMappings) throws Exception
	{
		if(source!=null && target !=null)
		{
			
			this.source=source;
			this.target=target;
			//  Charger ontology source 
			sourceIRI=BKbuilding.sourceIRI;
			sourceUris=new TreeSet<>();
			sourceUris.addAll(BKbuilding.sourceElements.keySet());
		    //  Charger ontology cible 
			Model oc=JenaMethods.LoadOntologyModelWithJena(target);	
			targetIRI=JenaMethods.getOntologyUri(oc);
		
			if(BKbuilding.ontologyAcronym.containsKey(targetIRI))
			{
				if(!targetIRI.equals("http://mouse.owl")&&!targetIRI.equals("http://human.owl"))
					{needInterface=true;}
				targetAcronym=BKbuilding.ontologyAcronym.get(targetIRI);
				BKbuilding.codeInterface.clear();
				BKbuilding.loadConcepts("C:\\Users\\annane\\Desktop\\concepts\\concepts\\",targetAcronym);
			}
			else targetAcronym=BKbuilding.getAcronym(targetIRI);			
			targetUris=new TreeSet<>();
            targetElements = BKbuilding.loadOntologyElementsForSelection(oc, targetAcronym, needInterface);
            targetUris.addAll(targetElements.keySet());

			oc.close();
 
		}
		else
		{
			throw new NullPointerException("source or target ontology URL is null");
		}
	}
	/**
	 * Constructor
	 * @param BKuse
	 * @throws URISyntaxException
	 */
	public BKuse(URL source, URL target,TreeSet<String> URIs) throws URISyntaxException
	{
		if(source!=null && target !=null)
		{
			this.source=source;
			this.target=target;
			//  SourceIRIS	
			sourceUris=URIs;
		    //  Charger ontology cible 
			Model oc=JenaMethods.LoadOntologyModelWithJena(target);	
			targetUris=JenaMethods.loadOntologyUri(oc);
		}
		else
		{
			throw new NullPointerException("source or target ontology URL is null");
		}
	}
	/**
	 * Constructeur qui fonctionne avec des listes duris source et cible
	 */
	public BKuse(String sourceAcronym, String targetAcronym,TreeSet<String> URIs,TreeSet<String> URIt) throws URISyntaxException
	{
		if(sourceAcronym!=null && targetAcronym !=null)
		{
			this.sourceAcronym=sourceAcronym;
			this.targetAcronym=targetAcronym;
			//  SourceIRIS	
			sourceUris=URIs;
		    //  Charger ontology cible 
			targetUris=URIt;
		}
		else
		{
			throw new NullPointerException("source or target ontology URL is null");
		}
	}
//___________________________________________________________________________________________
	
	public  void BKexploitation(Map<String, TreeSet<Noeud>> builtBK) throws Exception
	{
		BkGraph=builtBK;
		Matching matching;
		long debut =System.currentTimeMillis();
		//supprimer ce qui a etait dans le dossier
		Fichier fichier=new Fichier(Parameters.directAlignmentFolderPath);
		fichier.deleteFile();//Delete the old alignements

		URL BBK=new File(Parameters.BuiltBkPath).toURI().toURL();
		//Anchoring the target ontology to the built BK
		matching=new Matching(BBK, target);
		matching.matchOntologies(BBK,target,Parameters.directAlignmentFolderPath+"BK_target.rdf");	
		
		//Matching source and target ontologies directly
		matching=new Matching(source, target);
		URL res = matching.matchOntologies(source,target,Parameters.directAlignmentFolderPath+"source_target.rdf" );
		
		//Load all BK mappings
		chargerBKMappingsFromBuiltBK(BkOntologiesCodes);
		
		if(Parameters.derivationStrategy==Parameters.derivationStrategies.specific_algo)
			derivationFunction();
		else if(Parameters.derivationStrategy==Parameters.derivationStrategies.neo4j)
		{
			TreeSet<String> automatic_mappings=new TreeSet<>();
			TreeSet<String> manual_mappings=new TreeSet<>();
			StringTokenizer lineParser;
			for ( String m:BkGraph.keySet()) {
				lineParser = new StringTokenizer(m, Parameters.separator); 
				String os=lineParser.nextToken();
				String cs=lineParser.nextToken();
				for (Noeud n : BkGraph.get(m)) 
				{
				    String m1=cs+','+os+','+n.code+','+n.ontology+','+n.score;
					String m2=n.code+','+n.ontology+','+cs+','+os+','+n.score;
					if(n.score<2)
					{
					//equivalence edges, we do not need the direction if a equals b so b equals a
					if(!automatic_mappings.contains(m1)&&!automatic_mappings.contains(m2))
						{
								automatic_mappings.add(m1);
						}
						
					}
					else
					{
						if(!manual_mappings.contains(m1) && !manual_mappings.contains(m2))
							manual_mappings.add(m1);
					}
				}
			}
			Fichier.deleteFile(Parameters.BK_automatic_mappings_path);
			Fichier.deleteFile(Parameters.BK_manual_mappings_path);
			Fichier.deleteFile(Parameters.BK_target_classes_path);
			Fichier.deleteFile(Parameters.BK_target_by_classes_path);
			Fichier f=new Fichier("");
			f.ecrire(Parameters.BK_automatic_mappings_path, "id1,o1,id2,o2,a"+Fichier.retourAlaLigne+Fichier.treeToString(automatic_mappings));
			File hh=new File(Parameters.BK_automatic_mappings_path);
			if(hh.exists())System.out.println(hh.getAbsolutePath());
			f.ecrire(Parameters.BK_manual_mappings_path, "id1,o1,id2,o2"+Fichier.retourAlaLigne+Fichier.treeToString(manual_mappings));
			BkGraph.clear();

			//load the generated mappings to Neo4j
			loadMappingsToNeo4J(Parameters.BkFolderPath);
			
			//Delete the old derivation result file
	        fichier.path=Parameters.derivationResultFolderPath;
			fichier.deleteFile();
			Neo4Jderivation();
			if(Parameters.BKselectionInternalExploration)
			{
				//les concepts non matcheé
				TreeSet<String> notMatchedConcepts=new TreeSet<>();

			    for (String uri : sourceUris) {
					if(!matchedSourceUris.contains(uri))
						notMatchedConcepts.add(uri);
				}

				if(notMatchedConcepts.size()>0)
				{
					Matching m=new Matching(source, target);
					m.BkBasedMatching(notMatchedConcepts);
				}
				else
				{
					System.out.println("Pas de enriched");
				}	
			}			
		}
		else
			throw new NullPointerException("Please, specify the derivation strategy parameter.");

	}
	
	
	
	
	public  void BKexploitation(Map<String, TreeSet<Noeud>> builtBK, boolean ElcioMappings) throws Exception
	{
		BkGraph=builtBK;
		Matching matching;
		long debut =System.currentTimeMillis();
		//supprimer ce qui a etait dans le dossier
		Fichier fichier=new Fichier(Parameters.directAlignmentFolderPath);
		fichier.deleteFile();

		long debut2 =System.currentTimeMillis();
		
		TreeSet<String> t1=new TreeSet<>();

		StringTokenizer lineParser;
		for ( String m:BkGraph.keySet()) {
			lineParser = new StringTokenizer(m, Parameters.separator); 
			String os=lineParser.nextToken();
			String cs=lineParser.nextToken();
			for (Noeud n : BkGraph.get(m)) 
			{
	            
			    String m1=cs+','+os+','+n.code+','+n.ontology+','+n.score;
				String m2=n.code+','+n.ontology+','+cs+','+os+','+n.score;
				if(n.score<2)
				{
					
					if(!t1.contains(m1) && !t1.contains(m2))
					{
				
						t1.add(m1);

					}
					
				}
			
			}
		}
		Fichier.deleteFile(Parameters.BkFolderPath+"mappings.csv");

		Fichier f=new Fichier("");
		f.ecrire(Parameters.BkFolderPath+"mappings.csv", "id1,o1,id2,o2,a"+Fichier.retourAlaLigne+Fichier.treeToString(t1));

		BkGraph.clear();

		//supprimer ce qui �tait dans le dossier
        fichier.path=Parameters.derivationResultFolderPath;
		fichier.deleteFile();
		
		loadMappingsToNeo4J(Parameters.BkFolderPath);
		
		if(Parameters.derivationStrategy==Parameters.derivationStrategies.neo4j)
		 Neo4Jderivation();
		else derivationFunction();
	}
	/**
	 * 
	 */
	public  void BKuseWithEnrichment(Map<String, TreeSet<Noeud>> builtBK) throws Exception
	{
		System.out.println("********************************Matching with enrichment***************************");
		BkGraph=builtBK;
		Matching matching;
		long debut =System.currentTimeMillis();
		//supprimer ce qui a etait dans le dossier
		Fichier fichier=new Fichier(Parameters.directAlignmentFolderPath);
		fichier.deleteFile();
		URL BBK=new File(Parameters.BuiltBkPath).toURI().toURL();
		matching=new Matching(new File(Parameters.BuiltBkPath).toURI().toURL(), target);
		matching.matchOntologies(BBK,target,Parameters.directAlignmentFolderPath+"BK_target.rdf");	
		//System.out.println("results before: "+Matching.ComputeFScore(res, C.t5_R));
		
		chargerBKMappingsFromBuiltBK(BkOntologiesCodes);
		
		/* *********************************************************************** */
		TreeSet<String> t1=new TreeSet<>();
		TreeSet<String> t2=new TreeSet<>();
		TreeSet<String> superClass=new TreeSet<>();
		TreeSet<String> subClass=new TreeSet<>();
		StringTokenizer lineParser;
		for ( String m:BkGraph.keySet()) {
			lineParser = new StringTokenizer(m, Parameters.separator); 
			String os=lineParser.nextToken();
			String cs=lineParser.nextToken();
			for (Noeud n : BkGraph.get(m)) 
			{
			    String m1=cs+','+os+','+n.code+','+n.ontology+','+n.score;
				String m2=n.code+','+n.ontology+','+cs+','+os+','+n.score;
				if(n.score<2)
				{
					
				//	if(!t1.contains(m1) && !t1.contains(m2)&&!superClass.contains(m1)&&!subClass.contains(m1))
					{
						if(n.type.equals(""))
						{t1.add(m1);t1.add(m2);}
						else
						{
							if(n.type.equals("child"))
							{
								subClass.add(m1);
							}
							else
							{
								superClass.add(m1);
							}
						}
					}
					
				}
				else
				{
					//if(!t2.contains(m1) && !t2.contains(m2))
					{
						t2.add(m1);
						t2.add(m2);
					}
				}
			}
		}
		Fichier.deleteFile(Parameters.BkFolderPath+"mappings.csv");
		Fichier.deleteFile(Parameters.BkFolderPath+"obo.csv");
		Fichier.deleteFile(Parameters.BkFolderPath+"subclass.csv");
		Fichier.deleteFile(Parameters.BkFolderPath+"superclass.csv");
		Fichier f=new Fichier("");
		f.ecrire(Parameters.BkFolderPath+"mappings.csv", "id1,o1,id2,o2,a"+Fichier.retourAlaLigne+Fichier.treeToString(t1));
		f.ecrire(Parameters.BkFolderPath+"obo.csv", "id1,o1,id2,o2"+Fichier.retourAlaLigne+Fichier.treeToString(t2));
		f.ecrire(Parameters.BkFolderPath+"subclass.csv", "id1,o1,id2,o2,a"+Fichier.retourAlaLigne+Fichier.treeToString(subClass));
		f.ecrire(Parameters.BkFolderPath+"superclass.csv", "id1,o1,id2,o2"+Fichier.retourAlaLigne+Fichier.treeToString(superClass));
		BkGraph.clear();
		/* *************************************************************** */	
		Parameters.derivedCheminsPath=Parameters.derivationResultFolderPath+"cheminEnriched.csv";
		f=new Fichier(Parameters.derivedCheminsPath);
		f.deleteFile();
		
		
		if(Parameters.derivationStrategy==Parameters.derivationStrategies.neo4j)
		{
			loadMappingsToNeo4J(Parameters.BkFolderPath);
			Neo4Jderivation();
		}
		else 
			derivationFunction();
	}
	//*******************************************************************************************	
	/*
	 * ********************************* loadMappingsToNeo4J
	 */
	static public void loadMappingsToNeo4J(String sourceFolder) throws IOException
	{
		long debut=System.currentTimeMillis();
		//delete existing graph
		String query="MATCH p=()-[r]->() delete r";
		Parameters.session.run(query);
		query="MATCH (n:concept) delete n";
		Parameters.session.run(query);
		
		//charger mappings extracted by YAM++
		

		String destFolder=Parameters.neo4j_import_folder;
		Fichier.deleteFile(destFolder+"automatic_mappings.csv");
		Fichier.deleteFile(destFolder+"manualMappings.csv");
		Fichier.deleteFile(destFolder+"subclass.csv");
		
		File sourceFile=new File(Parameters.BK_automatic_mappings_path);
		File destFile=new File(destFolder+"automatic_mappings.csv");
	    org.apache.commons.io.FileUtils.copyFile(sourceFile, destFile);
	    
		query="USING PERIODIC COMMIT 10000 "+
				 "LOAD CSV WITH HEADERS "+
				 "FROM \"file:///automatic_mappings.csv\" "+
				 "AS line "+
				 "merge (concept1:concept { id:line.id1,ontology:line.o1 }) "+
				 "merge (concept2:concept { id:line.id2,ontology:line.o2 }) "+
				 "CREATE (concept1)-[:equivalent{a:toFloat(line.a),b:toFloat(line.b),c:toInt(line.c),d:line.d}]->(concept2);";
	Parameters.session.run(query);
	
	//Load manual mappings
	sourceFile=new File(Parameters.BK_manual_mappings_path);
    destFile=new File(destFolder+"manualMappings.csv");
    org.apache.commons.io.FileUtils.copyFile(sourceFile, destFile);
	
	query="USING PERIODIC COMMIT 10000 "+
		  "LOAD CSV WITH HEADERS "+		
         "FROM \"file:///manualMappings.csv\" "+
         "AS line "+
         "merge (concept1:concept { id:line.id1,ontology:line.o1 }) "+
         "merge (concept2:concept { id:line.id2,ontology:line.o2 }) "+
         "CREATE (concept1)-[:obo]->(concept2);";
	Parameters.session.run(query);
	
	File f=new File(Parameters.BK_target_classes_path);
	if(f.exists())
	{sourceFile=new File(Parameters.BK_target_classes_path);
	destFile=new File(destFolder+"subclass.csv");
    org.apache.commons.io.FileUtils.copyFile(sourceFile, destFile);
	
	query="USING PERIODIC COMMIT 10000 "+
			 "LOAD CSV WITH HEADERS "+
			 "FROM \"file:///subclass.csv\" "+
			 "AS line "+
			 "merge (concept1:concept { id:line.id1,ontology:line.o1 }) "+
			 "merge (concept2:concept { id:line.id2,ontology:line.o2 }) "+
			 "CREATE (concept1)-[:isA{a:toFloat(line.a),b:toFloat(line.b),c:toInt(line.c),d:line.d}]->(concept2);";
    Parameters.session.run(query);}
	

	}
	
	/*
	 * *****************************Derivation with Neo4J
	 */ 
	   public  void Neo4Jderivation() throws Exception
	   {
		 String path;
		 	   	
	   	System.out.println("****************** Derivation with Neo4j is started");
	   for (String uriS : sourceUris) 
	   {
		   String id=uriS;
		   String ontology=sourceIRI;

		   String	query="MATCH p=(n:concept{ontology:'"+ontology+"',id:'"+id+"'})-"
	   			+ "[r*1.."+Parameters.derivationMaxPathLength+"]-(m:concept{ontology:'"+targetIRI+"'})"+
	   			"RETURN distinct m.id as target, p";
	   		StatementResult result= null;
	   		result=Parameters.session.run(query);

	   		if(result!=null && result.hasNext())
	   		{
	   			while(result.hasNext())
	   			{	path="";					
	   				Record record=result.next();
	   				Path p=record.get("p").asPath();
	   				ArrayList<String> nodes=null;
	   				ArrayList<Double> scores=null;
	   				String uriTarget=record.get("target").asString();
	   				if(targetUris.contains(uriTarget))
	   				{ 	
	   					this.matchedSourceUris.add(uriS);
	   					nodes=new ArrayList<>();
	   				    scores=new ArrayList<>();
	   					for (Node noeud : p.nodes()) 
	   					{
	   						//System.out.println(noeud.get("id").asString()+"#"+noeud.get("ontology").asString());
							nodes.add(noeud.get("id").asString()+Parameters.separator+noeud.get("ontology").asString());
						}
		   				//System.out.println(nodes.size()+" nodes");
	   					scores.add(null);
	   					for (Relationship r : p.relationships()) 
	   					{
							if(r.hasType("obo"))scores.add(2.0);
							else if(r.hasType("isA"))scores.add(3.0);
							else scores.add(r.get("a").asDouble());
						}
	   					path="$$";
		   			//	System.out.println(nodes.size()+" nodes");
	   					for(int i=0;i<nodes.size();i++)
	   					{
	   						path=path+scores.get(i)+Parameters.separator+nodes.get(i)+"$$";
	   					}
	   					//System.out.println(path);

	   					Fichier f=new Fichier("");
	   					f.ecrire(Parameters.derivedCheminsPath, id+','+uriTarget+','+nodes.size()+','+path+','+Fichier.retourAlaLigne);
	   			    }
	   				
	   		}//end while	   		
	   	}
	   	
	   }
	   }
	/*
	 * *****************************Derivation with Neo4J
	 */ 
	   public  void Neo4Jderivation(String referencePath) throws Exception
	   {
		 long debut=System.currentTimeMillis();
		 String codeCible;
		 int NodesNumber;
		 String path;
		 String res;

	   	TreeSet<String> ref,refNeutre;
	   	ref=Fichier.loadReferenceAlignment(referencePath);
	   	refNeutre=Fichier.loadReferenceAlignmentNeutre(referencePath);
	   	
	   	System.out.println("******************************* Derivation with Neo4j is started");
	   for (String uriS : sourceUris) 
	   {
		   String id=uriS.substring(uriS.indexOf(Parameters.separator)+1);
		   String ontology=uriS.substring(0,uriS.indexOf(Parameters.separator));

		   String	query="MATCH p=(n:concept{ontology:'"+ontology+"',id:'"+id+"'})-"
	   			+ "[r*1.."+Parameters.derivationMaxPathLength+"]-(m:concept{ontology:'"+targetAcronym+"'})"+
	   			"RETURN distinct m.id as target, p";
	   	//	System.out.println(query);

	   		StatementResult result= null;
	   		result=Parameters.session.run(query);

	   		if(result!=null && result.hasNext())
	   		{
	   			while(result.hasNext())
	   			{	path="";					
	   				Record record=result.next();
	   				Path p=record.get("p").asPath();
	   				ArrayList<String> nodes=null;
	   				ArrayList<Double> scores=null;
	   				String uriTarget=record.get("target").asString();
	   				if(targetUris.contains(targetAcronym+Parameters.separator+uriTarget))
	   				{ 	

	   					this.matchedSourceUris.add(uriS);
	   					nodes=new ArrayList<>();
	   				    scores=new ArrayList<>();
	   					for (Node noeud : p.nodes()) {
	   						//System.out.println(noeud.get("id").asString()+"#"+noeud.get("ontology").asString());
							nodes.add(noeud.get("id").asString()+Parameters.separator+noeud.get("ontology").asString());
						}
		   				//System.out.println(nodes.size()+" nodes");
	   					scores.add(null);
	   					for (Relationship r : p.relationships()) {
							if(r.hasType("obo"))scores.add(2.0);
							else if(r.hasType("isA"))scores.add(3.0);
							else scores.add(r.get("a").asDouble());
						}
	   					path="$$";
		   			//	System.out.println(nodes.size()+" nodes");
	   					for(int i=0;i<nodes.size();i++)
	   					{
	   						path=path+scores.get(i)+Parameters.separator+nodes.get(i)+"$$";
	   					}
	   					//System.out.println(path);
	   					if(ref.contains(id+','+uriTarget)||ref.contains(uriTarget+','+id))res="true";
	   					else if(refNeutre.contains(id+','+uriTarget)||refNeutre.contains(uriTarget+','+id))res="neutre";
	   					else res="false";
	   					Fichier f=new Fichier("");
	   					f.ecrire(Parameters.derivedCheminsPath, id+','+uriTarget+','+nodes.size()+','+path+','+res+Fichier.retourAlaLigne);
	   			    }
	   				
	   		}//end while	   		
	   	}
	   	
	   }
		long time=System.currentTimeMillis()-debut;
	   }
	/**
	 * the concept uri of the built BK are customized as follows http:BK.rdf/ontology source acronym/original uri
	 * this is why we need a customized parser to get the mappings between the built BK and the target ontology	
	 */
	 TreeSet<String> loadOAEIAlignmentWithBkUris(String path) throws Exception
			{
				  TreeSet<String> m =new TreeSet<>();
				  AlignmentParser aparser = new AlignmentParser(0);
				  Alignment al = aparser.parse( new File( path ).toURI() );
				  String o2=al.getOntology2URI().toString();
			      for (Cell cell : al) 
			      {
			    	  String uri_source = cell.getObject1AsURI().toString();
			    	  String codeOntologySource=uri_source.substring(uri_source.lastIndexOf("/")+1);
			    	  String o1=BkOntologiesCodes.get(codeOntologySource);
			    	  uri_source=uri_source.substring(0,uri_source.lastIndexOf("/"));
			    	  String uri_target = cell.getObject2AsURI().toString();
			    	  double score = cell.getStrength();
					  m.add(uri_source+','+o1+','+uri_target+','+o2+','+score);
				}
				return m;
		  }
//___________________________________________________________________________________________________________	
	public void chargerBKMappingsFromBuiltBK(Map<String, String> BkOntologiesCodes) throws Exception
	{
		long debut =System.currentTimeMillis();
		File repertoire = new File(Parameters.directAlignmentFolderPath); 
		String[] alignments = repertoire.list();
		TreeSet<String> mappings=new TreeSet<>();
		for (String a : alignments) {
			Fichier fichier=new Fichier(Parameters.directAlignmentFolderPath);
			if(a.equals("BK_target.rdf"))mappings.addAll(loadOAEIAlignmentWithBkUris(Parameters.directAlignmentFolderPath+a));
			else mappings.addAll(fichier.loadOAEIAlignment(Parameters.directAlignmentFolderPath+a));
		}
		
		String uri_source,ontologySource, uri_target,ontologyTarget,score;


	    int cpt=0;
	    
		for (String line : mappings) 	
		{
			
			StringTokenizer lineParser = new StringTokenizer(line, ","); 
			uri_source=lineParser.nextElement().toString();
			ontologySource=lineParser.nextElement().toString();
			uri_target=lineParser.nextElement().toString();
			ontologyTarget=lineParser.nextElement().toString();
			score =lineParser.nextElement().toString();
					
			Noeud map=new Noeud(uri_target,ontologyTarget, Double.parseDouble(score));
			//System.out.println(uri_source+ontologySource);
			if(BkGraph.containsKey(ontologySource+Parameters.separator+uri_source))BkGraph.get(ontologySource+Parameters.separator+uri_source).add(map);
			else
			{
				TreeSet<Noeud> liste=new TreeSet<>();
				liste.add(map);
				BkGraph.put(ontologySource+Parameters.separator+uri_source,liste);
			}
			 
			map= new Noeud(uri_source,ontologySource, Double.parseDouble(score));
			if(BkGraph.containsKey(ontologyTarget+Parameters.separator+uri_target))BkGraph.get(ontologyTarget+Parameters.separator+uri_target).add(map);
			else
			{
				TreeSet<Noeud> liste=new TreeSet<>();
				liste.add(map);
				BkGraph.put(ontologyTarget+Parameters.separator+uri_target,liste);
			}
		}

	}
	
	
	//*******************************************derivationFunction*******************************************
	public  void derivationFunction(boolean MappingElcio) throws Exception
	{
		System.out.println("I start derivation with obo mappings");
		long debut =System.currentTimeMillis();
		Fichier fichier=new Fichier(Parameters.derivationResultFolderPath);
		fichier.deleteFile();
		ArrayList<String> treatedConcepts=new ArrayList<String>();
		ArrayList<Noeud> liste;
		ArrayList<Noeud> listePrime;
		ArrayList<ArrayList<Noeud>> myGraph=new ArrayList<ArrayList<Noeud>>();
		

	//  la grande boucle 
	//				Fichier f=new Fichier("bkGraph.txt");
		//			f.ecrire(BkGraph.toString());
		for (String ontologyCode : sourceUris) 
						{
							myGraph.clear();
							treatedConcepts.clear();
							paths.clear();
							
							StringTokenizer kk=new StringTokenizer(ontologyCode,Parameters.separator);
							String ontology=kk.nextToken();
							String uri=kk.nextToken();
							
							Noeud c=new Noeud(uri,ontology, "");
							//System.out.println(uri+' '+acroS);
							ArrayList<Noeud> l=new ArrayList<Noeud>();
							l.add(c);
							myGraph.add(l);
							boolean stop =false;
							int cpt=0;
							while (myGraph.size()>0  && cpt<100 && !stop) {
								liste=myGraph.get(0);
								myGraph.remove(0);
								c=liste.get(liste.size()-1);
								if(!treatedConcepts.contains(c.code+c.ontology))
								{
									treatedConcepts.add(c.code+c.ontology);
									TreeSet<Noeud> s = BkGraph.get(c.ontology+Parameters.separator+c.code);
									
									if(s!=null)
									{
										for (Noeud targetNode : s) 
									    {
										
											if(!sourceUris.contains(targetNode.ontology+Parameters.separator+targetNode.code))
											{

												listePrime=(ArrayList<Noeud>) liste.clone();
												listePrime.add(targetNode);
												if(targetUris.contains(targetNode.ontology+Parameters.separator+targetNode.code))
												{
													System.out.println("I found another one!");
													paths.add(listePrime);
													stop=true;
												}
												else 
												{
													cpt++;
													myGraph.add(listePrime);
												}
											 }
									    }//end for
									}	
								}	
							}//endwhile
							if(paths.size()>0)writePaths();
						}
	}
	
	//*******************************************derivationFunction*******************************************
	public  void derivationFunction() throws Exception
	{
		System.out.println("I start derivation");
		long debut =System.currentTimeMillis();
		Fichier fichier=new Fichier(Parameters.derivationResultFolderPath);
		fichier.deleteFile();
		ArrayList<String> treatedConcepts=new ArrayList<String>();
		ArrayList<Noeud> liste;
		ArrayList<Noeud> listePrime;
		ArrayList<ArrayList<Noeud>> myGraph=new ArrayList<ArrayList<Noeud>>();
		

	//  la grande boucle 
	//				Fichier f=new Fichier("bkGraph.txt");
		//			f.ecrire(BkGraph.toString());
		for (String uri : sourceUris) 
						{
							myGraph.clear();
							treatedConcepts.clear();
							paths.clear();
							
							Noeud c=new Noeud(uri,sourceIRI, "");
							//System.out.println(uri+' '+acroS);
							ArrayList<Noeud> l=new ArrayList<Noeud>();
							l.add(c);
							myGraph.add(l);
							while (myGraph.size()>0) {
								liste=myGraph.get(0);
								myGraph.remove(0);
								c=liste.get(liste.size()-1);
								if(!treatedConcepts.contains(c.code+c.ontology))
								{
									treatedConcepts.add(c.code+c.ontology);
									TreeSet<Noeud> s = BkGraph.get(c.ontology+Parameters.separator+c.code);
									
									if(s!=null)
									{
										for (Noeud targetNode : s) 
									    {
										
											if(!sourceUris.contains(targetNode.code))
											{
												listePrime=(ArrayList<Noeud>) liste.clone();
												listePrime.add(targetNode);
												if(targetUris.contains(targetNode.code))
												{
													paths.add(listePrime);
												}
												else 
												{
													myGraph.add(listePrime);
												}
											 }
									    }//end for
									}	
								}	
							}//endwhile
							if(paths.size()>0)writePaths();
						}

	}
	
	
	//***************************************   WritePaths
	public  void writePaths()
	{
		String allPaths="";
		String path = "";
		String CN;
		String CM;
		String conceptsPath="";
		int conceptsNumber;
	
		for (ArrayList<Noeud> p : paths) 
		{
			conceptsPath="";
			conceptsNumber=p.size();
			for (Noeud noeud : p)
			{
				//sourcesPath=sourcesPath+noeud.source;
				conceptsPath=conceptsPath+"$$"+noeud.score+Parameters.separator+noeud.code+Parameters.separator+noeud.ontology;
			}
			//System.out.println(CN+","+CM);
			String res="false";
			CN=p.get(0).code;
			CM=p.get(p.size()-1).code;
			path=CN+","+CM+","+conceptsNumber+","+conceptsPath+","+"\r\n";
			//System.out.println(path);
			allPaths=allPaths+path;
		}
		Fichier fichier=new Fichier(Parameters.derivedCheminsPath);
		fichier.ecrire(allPaths);
	}
	

}
