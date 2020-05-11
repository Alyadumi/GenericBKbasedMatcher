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
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;


import fr.inrialpes.exmo.align.parser.AlignmentParser;



public class BKuse {
	
	public static void main(String[] args) throws Exception {
		//load source and target ontologies
		File sourceOntologyFile=new File(C.mouse);//source ontology
		File targetOntologyFile=new File(C.human);//target ontology
		BKuse b = new BKuse(sourceOntologyFile.toURI().toURL(), targetOntologyFile.toURI().toURL());
		b.sourceIRI = "http://mouse.owl";
		b.targetIRI = "http://human.owl";
		String data_base_name = "neo4j";
		String password = "aminaamina";
		C.driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic(data_base_name, password )  );
		C.session= C.driver.session();
		b.Neo4Jderivation(C.ma_nci_Ref);
	}

	URL source;
	URL target;
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
	
	/**
	 * Constructor
	 * @param source: URL of the source ontology
	 * @param target: URL of the target ontology
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
			os.close();
		    //  Charger ontology cible 
			Model oc=JenaMethods.LoadOntologyModelWithJena(target);	
			targetUris=JenaMethods.loadOntologyUri(oc);
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
	 * Constructor that works with a list of source and target URIs
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
    
	/**
	 * BKexploitation: the main method that include all the bk exploitation process
	 * @param builtBK: the BK built from the previous step
	 * @throws Exception
	 */
	public  void BKexploitation(Map<String, TreeSet<Noeud>> builtBK) throws Exception
	{
		BkGraph=builtBK;
		Matching matching;
		//supprimer ce qui a etait dans le dossier
		Fichier fichier=new Fichier(C.directAlignmentFolderPath);
		fichier.deleteFile();

		long debut2 =System.currentTimeMillis();
		matching=new Matching(new File(C.BuiltBkPath).toURI().toURL(), target);
		matching.matchOntologies(C.directAlignmentFolderPath+"BK_target.rdf");	
		
		long time2 =System.currentTimeMillis()-debut2;
		C.executionTime.add("BK matching "+(time2)+"ms");
		

		matching=new Matching(source, target);
		URL res = matching.matchOntologies(C.directAlignmentFolderPath+"source_target.rdf" );
		//System.out.println("results before: "+Matching.ComputeFScore(res, C.t5_R));
		
		chargerBKMappingsFromBuiltBK(BkOntologiesCodes);
		
		if(C.derivationStrategy == C.derivationStrategies.specific_algo) derivationFunction();
		else
		{
				TreeSet<String> t1=new TreeSet<>();
				TreeSet<String> t2=new TreeSet<>();
				TreeSet<String> superClass=new TreeSet<>();
				TreeSet<String> subClass=new TreeSet<>();
				StringTokenizer lineParser;
				for ( String m:BkGraph.keySet()) {
					lineParser = new StringTokenizer(m, C.separator); 
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
				Fichier.deleteFile(C.BkFolderPath+"mappings.csv");
				Fichier.deleteFile(C.BkFolderPath+"obo.csv");
				Fichier.deleteFile(C.BkFolderPath+"subclass.csv");
				Fichier.deleteFile(C.BkFolderPath+"superclass.csv");
				Fichier f=new Fichier("");
				f.ecrire(C.BkFolderPath+"mappings.csv", "id1,o1,id2,o2,a"+Fichier.retourAlaLigne+Fichier.treeToString(t1));
				f.ecrire(C.BkFolderPath+"obo.csv", "id1,o1,id2,o2"+Fichier.retourAlaLigne+Fichier.treeToString(t2));
				f.ecrire(C.BkFolderPath+"subclass.csv", "id1,o1,id2,o2,a"+Fichier.retourAlaLigne+Fichier.treeToString(subClass));
				f.ecrire(C.BkFolderPath+"superclass.csv", "id1,o1,id2,o2"+Fichier.retourAlaLigne+Fichier.treeToString(superClass));
		
				BkGraph.clear();
		
				//delete what was in the folder
		        fichier.path=C.derivationResultFolderPath;
				fichier.deleteFile();
				
				loadMappingsToNeo4J(C.BkFolderPath);
				
				Neo4Jderivation(C.ma_nci_Ref);
				

		      if(C.BKselectionInternalExploration)
		      {
				//The concepts that have not been matched
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
	}
		
	/**
	 * BKuseWithEnrichment
	 */
	public  void BKuseWithEnrichment(Map<String, TreeSet<Noeud>> builtBK) throws Exception
	{
		System.out.println("********************************Matching with enrichment***************************");
		BkGraph=builtBK;
		Matching matching;
		long debut =System.currentTimeMillis();
		//supprimer ce qui a etait dans le dossier
		Fichier fichier=new Fichier(C.directAlignmentFolderPath);
		fichier.deleteFile();

		long debut2 =System.currentTimeMillis();
		matching=new Matching(new File(C.BuiltBkPath).toURI().toURL(), target);
		matching.matchOntologies(C.directAlignmentFolderPath+"BK_target.rdf");	
		
		long time2 =System.currentTimeMillis()-debut2;
		C.executionTime.add("BK matching "+(time2)+"ms");
		
		//System.out.println("results before: "+Matching.ComputeFScore(res, C.t5_R));
		
		chargerBKMappingsFromBuiltBK(BkOntologiesCodes);
		
		/* *********************************************************************** */
		TreeSet<String> t1=new TreeSet<>();
		TreeSet<String> t2=new TreeSet<>();
		TreeSet<String> superClass=new TreeSet<>();
		TreeSet<String> subClass=new TreeSet<>();
		StringTokenizer lineParser;
		for ( String m:BkGraph.keySet()) {
			lineParser = new StringTokenizer(m, C.separator); 
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
		Fichier.deleteFile(C.BkFolderPath+"mappings.csv");
		Fichier.deleteFile(C.BkFolderPath+"obo.csv");
		Fichier.deleteFile(C.BkFolderPath+"subclass.csv");
		Fichier.deleteFile(C.BkFolderPath+"superclass.csv");
		Fichier f=new Fichier("");
		f.ecrire(C.BkFolderPath+"mappings.csv", "id1,o1,id2,o2,a"+Fichier.retourAlaLigne+Fichier.treeToString(t1));
		f.ecrire(C.BkFolderPath+"obo.csv", "id1,o1,id2,o2"+Fichier.retourAlaLigne+Fichier.treeToString(t2));
		f.ecrire(C.BkFolderPath+"subclass.csv", "id1,o1,id2,o2,a"+Fichier.retourAlaLigne+Fichier.treeToString(subClass));
		f.ecrire(C.BkFolderPath+"superclass.csv", "id1,o1,id2,o2"+Fichier.retourAlaLigne+Fichier.treeToString(superClass));
		BkGraph.clear();
		/* *************************************************************** */	
		C.derivedCheminsPath=C.derivationResultFolderPath+"cheminEnriched.csv";
		f=new Fichier(C.derivedCheminsPath);
		f.deleteFile();
		
		loadMappingsToNeo4J(C.BkFolderPath);
		Neo4Jderivation(C.ma_nci_Ref);
	    //derivationFunction();
		long time=System.currentTimeMillis()-debut;
		C.executionTime.add("BKuse "+(time)+"ms");
	}
	
	/**
	 * load mappings to Neo4J database
	 * @param sourceFolder
	 * @throws IOException
	 */
	static public void loadMappingsToNeo4J(String sourceFolder) throws IOException
	{
		long debut=System.currentTimeMillis();
		//delete existing graph
		String query="MATCH p=()-[r]->() delete r";
		C.session.run(query);
		query="MATCH (n:concept) delete n";
		C.session.run(query);
		
		//charger mappings extracted by YAM++
		String destFolder = C.neo4j_import_folder;
		Fichier.deleteFile(destFolder+"mappings.csv");
		Fichier.deleteFile(destFolder+"obo.csv");
		File sourceFile=new File(sourceFolder+"mappings.csv");
		File destFile=new File(destFolder+"mappings.csv");
	    org.apache.commons.io.FileUtils.copyFile(sourceFile, destFile);
	    
		query="USING PERIODIC COMMIT 10000 "+
				 "LOAD CSV WITH HEADERS "+
				 "FROM \"file:///mappings.csv\" "+
				 "AS line "+
				 "merge (concept1:concept { id:line.id1,ontology:line.o1 }) "+
				 "merge (concept2:concept { id:line.id2,ontology:line.o2 }) "+
				 "CREATE (concept1)-[:equivalent{a:toFloat(line.a),b:toFloat(line.b),c:toInt(line.c),d:line.d}]->(concept2);";
	C.session.run(query);
	
	 sourceFile=new File(sourceFolder+"subclass.csv");
	 destFile=new File(destFolder+"subclass.csv");
    org.apache.commons.io.FileUtils.copyFile(sourceFile, destFile);
	
	query="USING PERIODIC COMMIT 10000 "+
			 "LOAD CSV WITH HEADERS "+
			 "FROM \"file:///subclass.csv\" "+
			 "AS line "+
			 "merge (concept1:concept { id:line.id1,ontology:line.o1 }) "+
			 "merge (concept2:concept { id:line.id2,ontology:line.o2 }) "+
			 "CREATE (concept1)-[:isA{a:toFloat(line.a),b:toFloat(line.b),c:toInt(line.c),d:line.d}]->(concept2);";
C.session.run(query);
	
	//charger OBO mappings
	sourceFile=new File(sourceFolder+"obo.csv");
    destFile=new File(destFolder+"obo.csv");
    org.apache.commons.io.FileUtils.copyFile(sourceFile, destFile);
	
	query="USING PERIODIC COMMIT 10000 "+
		  "LOAD CSV WITH HEADERS "+		
         "FROM \"file:///obo.csv\" "+
         "AS line "+
         "merge (concept1:concept { id:line.id1,ontology:line.o1 }) "+
         "merge (concept2:concept { id:line.id2,ontology:line.o2 }) "+
         "CREATE (concept1)-[:obo]->(concept2);";
	C.session.run(query);
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
		   String id=uriS;
		   String ontology= sourceIRI;

		    String	query="MATCH p=(n:concept{ontology:'"+ontology+"',id:'"+id+"'})-"
	   			+ "[r*"+C.stepMin+".."+C.stepMax+"]-(m:concept{ontology:'"+targetIRI+"'})"+
	   			"RETURN distinct m.id as target, p";
	   	    StatementResult result= null;
	   		result = C.session.run(query);
   			System.out.println(query);
	   		if(result!=null && result.hasNext())
	   		{

	   			System.out.println("yes "+uriS);
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
	   					for (Node noeud : p.nodes()) {
	   						//System.out.println(noeud.get("id").asString()+"#"+noeud.get("ontology").asString());
							nodes.add(noeud.get("id").asString()+C.separator+noeud.get("ontology").asString());
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
	   						path=path+scores.get(i)+C.separator+nodes.get(i)+"$$";
	   					}
	   					//System.out.println(path);
	   					if(ref.contains(id+','+uriTarget)||ref.contains(uriTarget+','+id))res="true";
	   					else if(refNeutre.contains(id+','+uriTarget)||refNeutre.contains(uriTarget+','+id))res="neutre";
	   					else res="false";
	   					Fichier f=new Fichier("");
	   					f.ecrire(C.derivedCheminsPath, id+','+uriTarget+','+nodes.size()+','+path+','+res+Fichier.retourAlaLigne);
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
		File repertoire = new File(C.directAlignmentFolderPath); 
		String[] alignments = repertoire.list();
		TreeSet<String> mappings=new TreeSet<>();
		for (String a : alignments) {
			Fichier fichier=new Fichier(C.directAlignmentFolderPath);
			if(a.equals("BK_target.rdf"))mappings.addAll(loadOAEIAlignmentWithBkUris(C.directAlignmentFolderPath+a));
			else mappings.addAll(fichier.loadOAEIAlignment(C.directAlignmentFolderPath+a));
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
			if(BkGraph.containsKey(ontologySource+C.separator+uri_source))BkGraph.get(ontologySource+C.separator+uri_source).add(map);
			else
			{
				TreeSet<Noeud> liste=new TreeSet<>();
				liste.add(map);
				BkGraph.put(ontologySource+C.separator+uri_source,liste);
			}
			 
			map= new Noeud(uri_source,ontologySource, Double.parseDouble(score));
			if(BkGraph.containsKey(ontologyTarget+C.separator+uri_target))BkGraph.get(ontologyTarget+C.separator+uri_target).add(map);
			else
			{
				TreeSet<Noeud> liste=new TreeSet<>();
				liste.add(map);
				BkGraph.put(ontologyTarget+C.separator+uri_target,liste);
			}
		}
		long time=System.currentTimeMillis()-debut;
		C.executionTime.add("chargerBKmappingsFromeBuiltBK "+(time)+"ms");
	}
	
	
	
	//*******************************************derivationFunction*******************************************
	public  void derivationFunction() throws Exception
	{
		System.out.println("I start derivation without Neo4J");
		long debut =System.currentTimeMillis();
		Fichier fichier=new Fichier(C.derivationResultFolderPath);
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
									TreeSet<Noeud> s = BkGraph.get(c.ontology+C.separator+c.code);
									
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
						long time=System.currentTimeMillis()-debut;
						C.executionTime.add("derivation "+(time)+"ms");
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
				conceptsPath=conceptsPath+"$$"+noeud.score+C.separator+noeud.code+C.separator+noeud.ontology;
			}
			//System.out.println(CN+","+CM);
			String res="false";
			CN=p.get(0).code;
			CM=p.get(p.size()-1).code;
			path=CN+","+CM+","+conceptsNumber+","+conceptsPath+","+"\r\n";
			//System.out.println(path);
			allPaths=allPaths+path;
		}
		Fichier fichier = new Fichier(C.derivedCheminsPath);
		fichier.ecrire(allPaths);
	}
	
	
}
