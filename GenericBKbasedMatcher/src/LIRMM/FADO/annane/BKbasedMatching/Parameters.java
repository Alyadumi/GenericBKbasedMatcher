package LIRMM.FADO.annane.BKbasedMatching;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

//import uk.ac.ox.krr.logmap2.oaei.MatcherBridge;

import OAEI2017.MatcherBridge;








public class Parameters {
	
	/**
	 * External parameters
	 */
	public static URL 		sourceOntology;
	public static URL 		targetOntology;
    public static String   BKontologiesFolderPath="GenericBKbasedMatcher/BK"+File.separator+"BKontologies"+File.separator;
	public static String   ExistingMappingsPath="GenericBKbasedMatcher/BK"+File.separator+"ExistingMappings"+File.separator+"obo.csv";
	public static String   alignmentsRepositoryFolderPath="alignmentsRepository"+File.separator;
	public static MatcherBridge matcher=new MatcherBridge();
	
	
    public static boolean  BKselectionInternalExploration=false;
	public static ArrayList<Relation> BKselectionExplorationRelations;
	public static int BKselectionExplorationLength;
	
    public static  int     derivationStrategy;
    public static  int     derivationMaxPathLength;
    
    public static  int     mappingSelectionStrategy;
	public static double   mappingSelectionThreshold=0.0;
    public static String   dataSetsFolderPath="BK"+File.separator+"DataSets"+File.separator;
    
    public static boolean 	logMapRepair=true;
	

	
	/**
	 * Internal parameters
	 */
    public final static String matcherName="logMap";
	public static String BkAlignmentsFolderPath="ProcessFolders"+File.separator+"BKalignments"+File.separator;
	public final static String derivedCheminsPath="ProcessFolders"+File.separator+"derivationResult"+File.separator+"paths.csv";
	public final static String derivationResultFolderPath="ProcessFolders"+File.separator+"derivationResult"+File.separator;
	public static String directAlignmentFolderPath="ProcessFolders"+File.separator+"directAlignments"+File.separator;
	public final static String BkFolderPath="ProcessFolders"+File.separator+"BK"+File.separator;
	public final  static String BuiltBkPath=BkFolderPath+File.separator+"BK.owl";
	public static String ResultFolderPath= "ProcessFolders"+File.separator+"Result"+File.separator;
	public final static String separator="¤";
	public  static Driver driver;
	public static Session session;
	public static final String prefix = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
			+ "prefix skos: <http://www.w3.org/2004/02/skos/core#> "
			+ "PREFIX oboInOwl:<http://www.geneontology.org/formats/oboInOwl#> "
			+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
			+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ "PREFIX BIRNLEX:<http://bioontology.org/projects/ontologies/birnlex#> ";
	public static final String synonyms="oboInOwl:hasExactSynonym|oboInOwl:hasRelatedSynonym|skos:altLabel|<http://purl.bioontology.org/ontology/SYN#synonym>|<http://www.ebi.ac.uk/efo/alternative_term>|<http://purl.obolibrary.org/obo/synonym>|<http://scai.fraunhofer.de/CSEO#Synonym>|BIRNLEX:synonyms";
	public static final String prefLabs="rdfs:label|skos:prefLabel|BIRNLEX:preferred_label";
	public static String BK_manual_mappings_path = BkFolderPath+"manual_mappings.csv";
	public static String BK_automatic_mappings_path = BkFolderPath+"automatic_mappings.csv";
	public static String BK_target_by_classes_path = BkFolderPath+"target_by_classes.csv";
	public static String BK_target_classes_path = BkFolderPath+"target_classes.csv";
	public static String neo4j_import_folder="C:\\Users\\aannane\\Documents\\neo4j\\neo4jDatabases\\database-31fe5ea4-7db3-44d5-8638-ecb5f50c6600\\installation-3.5.14\\import\\";
	
       
    
    public class derivationStrategies {
    	  public static final int neo4j = 1;
    	  public static final int specific_algo = 2;
    }
    
    public class mappingSelectionStrategies {
  	  public static final int ML_based = 1;
  	  public static final int  Rule_based= 2;
  }
    

	
}
