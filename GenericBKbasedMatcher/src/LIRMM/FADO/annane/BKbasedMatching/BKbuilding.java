package LIRMM.FADO.annane.BKbasedMatching;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.SKOS;
import org.semanticweb.owl.align.Alignment;

import eu.sealsproject.platform.res.tool.api.ToolBridgeException;
import eu.sealsproject.platform.res.tool.api.ToolException;
import fr.inrialpes.exmo.align.parser.AlignmentParser;




public class BKbuilding {

	public static String sourceIRI;
	Map<String,String> ExistingAlignments;
	String[] BkOntologies;
	Map<String,String> BkOntologiesCodes=new HashMap<String,String>();
	public static String sourceAcronym;
	public static boolean sourceNeedInterface=false; 
	public static HashMap<String, String > codeInterface=new HashMap<>();
	public static HashMap<String, String> sourceElements ;


	public static HashMap<String, String> ontologyAcronym;
	int code;
	
	public Map<String, String> getBkOntologiesCodes() {
		return BkOntologiesCodes;
	}
	

	Map<String, TreeSet<Mapping>> globalGraph=new HashMap<String, TreeSet<Mapping>>();
	Map<String, TreeSet<Mapping>> BkGraph=new HashMap<String, TreeSet<Mapping>>();


	public static void main(String[] args) throws FileNotFoundException, IOException, URISyntaxException {
		// TODO Auto-generated method stub
	
		

	}
	/**
	 * Constructor
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public BKbuilding() throws FileNotFoundException, IOException, URISyntaxException
	{
		globalGraph.clear();
		BkGraph.clear();
		ExistingAlignments = getExistingAlignments();
		File BkOntologiesFolder = new File(Parameters.BKontologiesFolderPath); 
		if(BkOntologiesFolder.exists())BkOntologies = BkOntologiesFolder.list(); 
		else System.out.println("BK ontologies folder does not exist");
		getOntologyCodes();
	}
	/**
	 * The main function
	 * @return
	 * @throws Exception
	 */
	public Map<String, TreeSet<Mapping>> BuildBK() throws Exception
	{
		long debut=System.currentTimeMillis();		
		Fichier folder=new Fichier(Parameters.BkAlignmentsFolderPath);
		folder.deleteFile();
		generateBkFromOneFolder();
		matchOntologyToBKontologies();
		chargerBKMappingsFromFolder();
		if(Parameters.ExistingMappingsPath!=null)chargerBK_Mappings();
		Model ontologySourceModel=JenaMethods.LoadOntologyModelWithJena(Parameters.sourceOntology);
		selectSubGraph(ontologySourceModel);
		createOwlFile2();
		return BkGraph;
	}
	
	
	static String getAcronym(String IRI)
	{
		String res;
		res=IRI.substring(IRI.lastIndexOf("/")+1);
		if(res.contains("."))res=res.substring(0, res.indexOf("."));
		res=res.toLowerCase();
		return res;
	}
	/**
	 * This function supposes that the global graph is already loaded and selects the subgraph for a set of uris
	 * @param URIs
	 * @return
	 * @throws Exception
	 */
	public Map<String, TreeSet<Mapping>> BuildEnrichedBK(TreeSet<String> URIs) throws Exception
	{
		chargerBKMappingsFromFolder();
		if(Parameters.ExistingMappingsPath!=null)chargerBK_Mappings();
		//Select the BK concepts related to the no matched source concepts directly or indirectly
		selectSubGraph(URIs);
		System.out.println("Taille initiale: "+BkGraph.size());
		enrichWithRelatedClasses();
		createOwlFile2();
		return BkGraph;
	}
	/**
	 * Select a sub graph from a set of uris
	 * @param URIs: the set of URIS that will help for the selection
	 * @throws Exception
	 */
	/* ***************************************************************************************** */
	void selectSubGraph(TreeSet<String> URIs) throws Exception
	{
	 TreeSet<String> sourceElements = JenaMethods.getFirstSelectedConcepts(URIs, sourceIRI);
	 TreeSet<String> untreated=new TreeSet<>(), treated=new TreeSet<>();
	 untreated.addAll(sourceElements);
	 TreeSet<Mapping> l;
	 String e,m; 
	 BkGraph.clear();
	 while (untreated.size()>0) 
	 {
		 e=untreated.first();
		 untreated.remove(e);
		 treated.add(e);
		 l = globalGraph.get(e);

		 if(l!=null)
		 {	
			 BkGraph.put(e,l);
			 for (Mapping Mapping : l) {
				m=Mapping.ontology+Parameters.separator+Mapping.code;
				if(!treated.contains(m))
				{
					untreated.add(m);
				}
			}
		 }
	}
	}
	
	
	public static HashMap<String,String> loadOntologyElementsForSelection(Model ontology,String acronym, boolean needInterface) throws Exception
	{
		HashMap<String,String>  ontologyUriCode = new HashMap<String,String> ();
		ResultSet res=JenaMethods.ExecuteQuery(Parameters.prefix+"select ?x where {?x a owl:Class}", ontology);
		String uri;
		while (res.hasNext()) 
		{
		
            uri=res.next().get("x").toString();
            if(uri.contains("http"))
            { 
            	String code=Fichier.getUriCode(uri,needInterface);
                if(needInterface)
                {
                    if(codeInterface.containsKey(acronym+Parameters.separator+code))
                	{
                     code=codeInterface.get(acronym+Parameters.separator+code);
                     ontologyUriCode.put(acronym+Parameters.separator+code,uri);
                	}
                }
                else
                {
                    ontologyUriCode.put(acronym+Parameters.separator+code,uri);	
                }

            }
		}	
		return ontologyUriCode;
	}
	/* ***************************************************************************************** */
	void selectSubGraph(Model ontologySource) throws Exception
	{
	 TreeSet<String> sourceElements = JenaMethods.loadOntologyElementsForSelection(ontologySource,sourceIRI);
	 TreeSet<String> untreated=new TreeSet<>(), treated=new TreeSet<>();
	 untreated.addAll(sourceElements);
	 TreeSet<Mapping> l;
	 String e,m; 
	 BkGraph.clear();
	 while (untreated.size()>0) 
	 {
		 e=untreated.first();
		 untreated.remove(e);
		 treated.add(e);
		 l = globalGraph.get(e);

		 if(l!=null)
		 {	
			 BkGraph.put(e,l);
			 for (Mapping Mapping : l) {
				m=Mapping.ontology+Parameters.separator+Mapping.code;
				if(!treated.contains(m))
				{
					untreated.add(m);
				}
			}
		 }
	}
	 System.out.println("La taille globale du graph: "+globalGraph.size());
	 System.out.println("La taille du graph BK: "+BkGraph.size());
	// globalGraph.clear();
	// enrichWithChildrenAndFathers();
	}
	/* *********************************************************************************************** */
	 void enrichWithRelatedClasses() throws MalformedURLException, URISyntaxException
	 {
			int cpt=0;
			Model ontology=null;
			ResultSet res;
			QuerySolution sol;	
			
			TreeSet<String> newConcepts=new TreeSet<>();
			HashMap<String, String> ontologyConcepts;
			HashMap<String, String> ontologyPath = localizeBKontologyPaths();
			newConcepts.addAll(BkGraph.keySet());

			for (String ontoURI : ontologyPath.keySet()) 
			{
				String path=ontologyPath.get(ontoURI);
				ontology=JenaMethods.LoadOntologyModelWithJena(path);
				for (Relation relation : Parameters.BKselectionExplorationRelations) 	
				{
					for(int j=1;j<=Parameters.BKselectionExplorationLength;j++)
					{
						System.out.println(j);
						//organize the selected concepts per ontology
						ontologyConcepts = categorizeConcepts(newConcepts);
						newConcepts=new TreeSet<>();

						/* ********************** looking for fathers *********************************** */
						String Query=Parameters.prefix+"SELECT ?c ?f  where {?c <"+relation.property+"> ?f} ";
						if(ontologyConcepts.get(ontoURI)!=null)
						{
							Query=Query+ " VALUES ?c  {"+ontologyConcepts.get(ontoURI)+"}";
							res = JenaMethods.ExecuteQuery(Query, ontology);
							if(res!=null && res.hasNext())
							{
								while(res.hasNext())
								{
									sol = res.next();
									String fatherURI=sol.get("f").toString();
									if(fatherURI.contains("http"))
									{
										String fatherConcept=ontoURI+Parameters.separator+fatherURI;
										String childURI=sol.get("c").toString();
										String childConcept=ontoURI+Parameters.separator+childURI;
										if(!BkGraph.containsKey(fatherConcept))
										{
											BkGraph.put(fatherConcept, new TreeSet<Mapping>());
											newConcepts.add(fatherConcept);
										}
										//Mapping child=new Mapping(childURI, ontoURI, 1.0,relation.getAbbreviation(),ontoURI);
										Mapping father=new Mapping(fatherURI, ontoURI,1.0,relation.getAbbreviation(),ontoURI);
										BkGraph.get(childConcept).add(father);
										//BkGraph.get(fatherConcept).add(child); 
									}
								}//endWhile	
							}//endIF

		      /* ********************** looking target classes *********************************** */
							Query=Parameters.prefix+"SELECT ?c ?f  where {?c <"+relation.getProperty()+"> ?f} ";
							Query=Query+ " VALUES ?f  {"+ontologyConcepts.get(ontoURI)+"}";
							res = JenaMethods.ExecuteQuery(Query, ontology);
							if(res!=null)
							{
							cpt=0;
								while(res.hasNext())
								{
									cpt++;
									sol = res.next();
									String childURI=sol.get("c").toString();
									if(childURI.contains("http"))
									{
										String childConcept=ontoURI+Parameters.separator+childURI;
										String fatherURI=sol.get("f").toString();
										String fatherConcept=ontoURI+Parameters.separator+fatherURI;
										if(!BkGraph.containsKey(childConcept))
										{
											BkGraph.put(childConcept, new TreeSet<Mapping>());
											newConcepts.add(childConcept);
										}
										//Mapping child=new Mapping(childURI, ontoURI, 1.0,relation.getAbbreviation(),ontoURI);
										Mapping father=new Mapping(fatherURI, ontoURI, 1.0,relation.getAbbreviation(),ontoURI);
										BkGraph.get(childConcept).add(father);
										//BkGraph.get(fatherConcept).add(child);
									}
								}	
							}//endIF
													
						}
						
					}
						System.out.println("after "+BkGraph.size()+" cpt: "+cpt);
				}
				ontology.close();
			}
	 }
	 /* ***************************************************** */
	 HashMap<String, String> localizeBKontologyPaths() throws MalformedURLException, URISyntaxException
	 {
		    
			HashMap<String, String> ontologyPath=new HashMap<>();	
			File BkOntologiesFolder =new File(Parameters.BKontologiesFolderPath);
			for (String filePath : BkOntologiesFolder.list()) 
			{
				File file=new File(Parameters.BKontologiesFolderPath+File.separator+filePath);
				String ontologyURI=JenaMethods.getOntologyUri(file.toURI().toURL());
				ontologyPath.put(ontologyURI, file.getAbsolutePath());
			}
			return ontologyPath;
	 }
	 /* ***************categorize concepts per ontology ************* */
	 HashMap<String, String> categorizeConcepts(TreeSet<String> newConcepts)
	 {
			HashMap<String, String> ontologyConcepts=new HashMap<>();
			
			String o,uri,values;
			
			for (String oc : newConcepts) 
			 {	 
				 o=oc.substring(0,oc.indexOf(Parameters.separator));
				 uri=oc.substring(oc.indexOf(Parameters.separator)+1);
				 if(!ontologyConcepts.keySet().contains(o))
				 {
					 values="<"+uri+"> ";
				 }
				 else
				 {
					 values=ontologyConcepts.get(o)+"<"+uri+"> "; 
				 }
				 ontologyConcepts.put(o,values);
			  }	
			return ontologyConcepts;
	 }

	/* ************************************************************************************************ */
	void createOwlFile2() throws Exception
	{
		long debut =System.currentTimeMillis();
		Model ontology=null;
		String requete1="", requete2;
		ResultSet res;
		String newUri;
		QuerySolution sol;
		
		//supprimer l'ancien builtBk s'il existe
		Fichier fichier=new Fichier(Parameters.BuiltBkPath);		
		fichier.deleteFile();
		

		//IRIFactory iriFactory = IRIFactory.semanticWebImplementation();
		//IRI iri = iriFactory.create("http://BK.rdf"); // always works
		// create an empty Model
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);;
		model.setNsPrefix("skos", SKOS.getURI());
		model.setNsPrefix("owl", OWL.NS);
		model.setNsPrefix("", "http://BK.owl");
		model.createOntology("http://BK.owl");
		
	
		TreeSet<String> conceptList=new TreeSet<>();
		conceptList.addAll(BkGraph.keySet());
		
		File BkOntologiesFolder =new File(Parameters.BKontologiesFolderPath);
		String values="";
		String o;
		for (String filePath : BkOntologiesFolder.list()) 
		{				String path=Parameters.BKontologiesFolderPath+filePath;
						File file=new File(path);
					 	String ontologyURI=JenaMethods.getOntologyUri(file.toURI().toURL());
					 	System.out.println("*****************************Nouvelle BK ontologie: "+ontologyURI);
						ontology=JenaMethods.LoadOntologyModelWithJena(file.toURI().toURL());
						 {	 //chercher tous les concepts de cet ontologies
							 String uri;
							 values="";
							 int numberConcept=0;
							 for (String oc : conceptList) 
							 {	 
								 o=oc.substring(0,oc.indexOf(Parameters.separator));
								 if(o.equalsIgnoreCase(ontologyURI))
								 {
									numberConcept++;
									uri=oc.substring(oc.indexOf(Parameters.separator)+1);
									values=values+"<"+uri+"> ";
								 }
							  }				
                    int cpt=0;
					 /* ****************************** Requete1: retrieve prefLabs ********************************* */
                    
					 requete1=Parameters.prefix+"SELECT ?x ?y  where {?x "+Parameters.prefLabs+" ?y} ";
					 requete1=requete1+ " VALUES ?x  {"+values+"}";
					 res = JenaMethods.ExecuteQuery(requete1, ontology);	
					 if(res!=null)
					 {
						 while(res.hasNext())
						 {
							 cpt++;
							 sol = res.next();
							 String codeU=sol.get("x").toString();
							 if(conceptList.contains(ontologyURI+Parameters.separator+codeU))
							 { 
								 String label=sol.get("y").toString();
								 newUri=codeU+"/"+BkOntologiesCodes.get(ontologyURI);
								 model.createClass(newUri).addProperty(SKOS.prefLabel, label);
							 }
							 else System.out.println("[CreateOwlFile] BE carfeul");
						 }
					 }
					 if(cpt!=numberConcept)System.out.println("[CreateOwlFile] numberConcept est: "+numberConcept+" trouvés est: "+cpt);
					 /* *************************** Requete2: retrieve synonyms ************************************ */
					 requete2=Parameters.prefix+"select distinct ?x ?y where {?x "+Parameters.synonyms+" ?y}";
					 requete2=requete2+ " VALUES ?x  {"+values+"}";
					 
					 res = JenaMethods.ExecuteQuery(requete2, ontology);	
					 if(res!=null)
					 {
						 while(res.hasNext())
						 {
						 cpt++;
						// System.out.println(cpt);
						 
						 sol = res.next();
						 String codeU=sol.get("x").toString();
						 if(conceptList.contains(ontologyURI+Parameters.separator+codeU))
						 { 
							 String label=sol.get("y").toString();
							 newUri=codeU+"/"+BkOntologiesCodes.get(ontologyURI);
							 model.createClass(newUri).addProperty(SKOS.altLabel, label);}
						 else System.out.println("[CreateOwlFile2] Be careful: "+ontologyURI+Parameters.separator+codeU);
						 }
					 }						 
				 ontology.close();
				 }
				}
					FileWriter out = null;
					try {
						  // XML format - long and verbose
						  out = new FileWriter( Parameters.BuiltBkPath);
						  model.write( out, "RDF/XML" );			 		
						}
					finally {
					  if (out != null) {
					    try {out.close();} 
					    catch (IOException ignore) {}
					  }
					}

	}
	/* ********* charger les mappings ******************** */
	  void chargerBK_Mappings() throws IOException
	{
		long debut =System.currentTimeMillis();
		String uri_source,ontologySource, uri_target,ontologyTarget,score,relation;
		File f = new File(Parameters.ExistingMappingsPath); 
		BufferedReader reader = new BufferedReader(new FileReader(f)); 
		String line = null; 
		while ((line = reader.readLine()) != null) 
		{
			try{
			//System.out.println("[chargerBK_Mappings]"+line);
			StringTokenizer lineParser = new StringTokenizer(line, ","); 
			uri_source=lineParser.nextElement().toString();
			ontologySource=lineParser.nextElement().toString();
			uri_target=lineParser.nextElement().toString();
			ontologyTarget=lineParser.nextElement().toString();
			score =lineParser.nextElement().toString();	
			relation =lineParser.nextElement().toString();
			String source=lineParser.nextElement().toString();
			Mapping map=new Mapping(uri_target,ontologyTarget, Double.parseDouble(score),relation,source);
			
			if(globalGraph.containsKey(ontologySource+Parameters.separator+uri_source))
				globalGraph.get(ontologySource+Parameters.separator+uri_source).add(map);
			else
			{
				TreeSet<Mapping> liste=new TreeSet<>();
				liste.add(map);
				globalGraph.put(ontologySource+Parameters.separator+uri_source,liste);
			}
			 
			map= new Mapping(uri_source,ontologySource, Double.parseDouble(score),relation,source);
			if(globalGraph.containsKey(ontologyTarget+Parameters.separator+uri_target))globalGraph.get(ontologyTarget+Parameters.separator+uri_target).add(map);
			else
			{
				TreeSet<Mapping> liste=new TreeSet<>();
				liste.add(map);
				globalGraph.put(ontologyTarget+Parameters.separator+uri_target,liste);
			}
		}
			catch(java.lang.NumberFormatException e)
			{}
			catch(java.util.NoSuchElementException e)
			{}
			}

	}
	
	/* ***************************************************************************** */
	 void chargerBKMappingsFromFolder() throws Exception
	{
		long debut =System.currentTimeMillis();
		TreeSet<String> mappings=new TreeSet<>();
		File BkAlignmentsFolder = new File(Parameters.BkAlignmentsFolderPath); 
		String[] BkAlignmentsList = BkAlignmentsFolder.list();
		for (String a : BkAlignmentsList) 
		{
			if(a.contains(".rdf"))
			{
				TreeSet<String> mappingsA = Fichier.loadOAEIAlignment(Parameters.BkAlignmentsFolderPath+a);
				mappings.addAll(Fichier.loadOAEIAlignment(Parameters.BkAlignmentsFolderPath+a));
			}
		}
		String uri_source,ontologySource, uri_target,ontologyTarget,score; 
		for (String line : mappings) 	
		{
			StringTokenizer lineParser = new StringTokenizer(line, ","); 
			uri_source=lineParser.nextElement().toString();
			ontologySource=lineParser.nextElement().toString();
			uri_target=lineParser.nextElement().toString();
			ontologyTarget=lineParser.nextElement().toString();
			score =lineParser.nextElement().toString();			
			Mapping map=new Mapping(uri_target,ontologyTarget, Double.parseDouble(score),"=",Parameters.matcherName);
			if(globalGraph.containsKey(ontologySource+Parameters.separator+uri_source))
				{
				globalGraph.get(ontologySource+Parameters.separator+uri_source).add(map);
				}
			else
			{
				TreeSet<Mapping> liste=new TreeSet<>();
				liste.add(map);
				globalGraph.put(ontologySource+Parameters.separator+uri_source,liste);
			}
			 
			map= new Mapping(uri_source,ontologySource, Double.parseDouble(score),"=",Parameters.matcherName);
			if(globalGraph.containsKey(ontologyTarget+Parameters.separator+uri_target))globalGraph.get(ontologyTarget+Parameters.separator+uri_target).add(map);
			else
			{
				TreeSet<Mapping> liste=new TreeSet<>();
				liste.add(map);
				globalGraph.put(ontologyTarget+Parameters.separator+uri_target,liste);
			}
		}
	}
	/**
	 * This function matches the source ontology to the BK ontologies
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws ToolBridgeException 
	 * @throws ToolException 
	 */
	    void matchOntologyToBKontologies() throws FileNotFoundException, IOException, URISyntaxException, ToolException, ToolBridgeException
	   {  
		  for(int j=0;j<BkOntologies.length;j++)
		  { 
			  
			File f=new File(Parameters.BKontologiesFolderPath+BkOntologies[j]);
			System.out.println(BkOntologies[j]);
			String targetIRI=JenaMethods.getOntologyUri(f.toURI().toURL());
			generateBKalignment(Parameters.sourceOntology, f.toURI().toURL(), sourceIRI, targetIRI);			
		  }
	   }
	    void getOntologyCodes() throws FileNotFoundException, IOException, URISyntaxException
	   {	code=1;	  
		  for(int j=0;j<BkOntologies.length;j++)
		  { 
			File f=new File(Parameters.BKontologiesFolderPath+BkOntologies[j]);
			String targetIRI=JenaMethods.getOntologyUri(f.toURI().toURL());
			
			//ajouter à la liste bkontologiescodes
			BkOntologiesCodes.put(targetIRI, "o"+code);
			BkOntologiesCodes.put("o"+code, targetIRI);
			code++;
			
		  }
	   }
	/**
	 * This funtion matches all ontologies within a given folder between each other
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws ToolBridgeException 
	 * @throws ToolException 
	 */
	 void generateBkFromOneFolder() throws FileNotFoundException, IOException, URISyntaxException, ToolException, ToolBridgeException
	{	  
		  for(int i=0;i<BkOntologies.length;i++)
		  { 
			for(int j=i+1;j<BkOntologies.length;j++)
			  { 
				File sourceFile=new File(Parameters.BKontologiesFolderPath+File.separator+BkOntologies[i]);
				String sourceIRI=JenaMethods.getOntologyUri(sourceFile.toURI().toURL());
				File targetFile=new File(Parameters.BKontologiesFolderPath+File.separator+BkOntologies[j]);
				String targetIRI=JenaMethods.getOntologyUri(targetFile.toURI().toURL());
				generateBKalignment(sourceFile.toURI().toURL(), targetFile.toURI().toURL(), sourceIRI, targetIRI);
			  }
		  }
	} 
	/**
	 * This function generates an alignment between two ontologies
	 * @param source The ontology source URL
	 * @param target The ontology target URL
	 * @param sourceIRI The source IRI
	 * @param targetIRI The target IRI
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws ToolBridgeException 
	 * @throws ToolException 
	 */
	void generateBKalignment(URL source,URL target, String sourceIRI, String targetIRI) throws IOException, URISyntaxException, ToolException, ToolBridgeException
	{
		if(ExistingAlignments.containsKey(sourceIRI+Parameters.separator+targetIRI))
		{
			  System.out.println("[generateBKalignment] super il existe");
			  String fileName=ExistingAlignments.get(sourceIRI+Parameters.separator+targetIRI);
			  File srcFile=new File (Parameters.alignmentsRepositoryFolderPath+fileName);
			  File destFile=new File(Parameters.BkAlignmentsFolderPath+fileName);
		      org.apache.commons.io.FileUtils.copyFile(srcFile, destFile);
		}
		else{
			Matching matching=new Matching(source,target);
			String alignmentName=getRandomName(Parameters.BkAlignmentsFolderPath);
			URL res=matching.matchOntologies(source, target,Parameters.BkAlignmentsFolderPath+File.separator+alignmentName+".rdf");		
			System.out.println("lurl du fichier resultat: "+res);

		}
		
		}
	/**
	 * 
	 * @param path the path of the alignment
	 * @return an array that includes two elements the first is the IRI of the source ontology and the second is the IRI of the target ontology
	 * @throws Exception the API alignment is used to parse alignments, it may throw parsing exceptions
	 */
      ArrayList<String> getAlignmentOntologies(String path) throws Exception
	{
		  AlignmentParser aparser = new AlignmentParser(0);
		  Alignment al = aparser.parse( new File( path ).toURI() );
		  String o1=al.getOntology1URI().toString();
		  String o2=al.getOntology2URI().toString();
		  ArrayList<String> l=new ArrayList<>();
		  l.add(o1);
		  l.add(o2);
		  return l;
	} 
      /**
       * generate a random file name that does not exist in the Folder
       * @param FolderPath
       * @return a random fileName
       */
String getRandomName(String FolderPath)
{
	  String randomName = RandomStringUtils.randomAlphabetic(10).toUpperCase();
      // Randomly generate the scenario name and check if a dir already got this name
      while (new File(FolderPath + File.separatorChar + randomName).exists()) {
        randomName = RandomStringUtils.randomAlphabetic(10).toUpperCase();
      }
      return randomName;
	}
/**
 * 
 * @return existing alignment list (o1¤o2,fileName.rdf)
 */
Map<String, String> getExistingAlignments()
{
	Map<String,String> existingAlignments=new HashMap<String,String>();
	String[] alignments=new File(Parameters.alignmentsRepositoryFolderPath).list();  
	if (alignments != null)
	  for (String a : alignments) 
	  {
		  if(a.contains(".rdf"))
		  try
		  {
			  ArrayList<String> l = getAlignmentOntologies(Parameters.alignmentsRepositoryFolderPath+a);
			  existingAlignments.put(l.get(0)+Parameters.separator+l.get(1), a );
		  }
		  catch(Exception e)
		  {
			  System.out.println("alignment: "+a+"that throws the exception");
			  e.printStackTrace();
		  }
	  }
	  return existingAlignments;
	}
public static void loadConcepts(String folderPath,String acronym) throws Exception{
	
		File folder=new File(folderPath); 
		String[] liste = folder.list();
		for (String fichier : liste) {
			String path=folderPath+File.separator+fichier;
			File f=new File(path);
			BufferedReader reader = new BufferedReader(new FileReader(f)); 
			String line;
			while((line=reader.readLine())!=null){
				StringTokenizer lineParser=new StringTokenizer(line, ",");
				String ontologyCode = lineParser.nextToken();
				String ontology= lineParser.nextToken();
				String prefLabCode=lineParser.nextToken();
				if(prefLabCode!=null)
				{
					if(ontology.equalsIgnoreCase(acronym))
					codeInterface.put(ontology+Parameters.separator+prefLabCode,ontologyCode);
					}
				else throw new Exception("prefLabCode VIDE");
			}
			reader.close();
		}


}

}
