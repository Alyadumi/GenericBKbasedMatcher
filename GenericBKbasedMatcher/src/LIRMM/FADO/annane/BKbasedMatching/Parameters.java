package LIRMM.FADO.annane.BKbasedMatching;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

import OAEI2017.MatcherBridge;

public class Parameters {
	
	/**
	 * External parameters
	 */
	public static String   alignmentsRepositoryFolderPath="BK"+File.separator+"alignmentsRepository"+File.separator;
    public static String   dataSetsFolderPath="BK"+File.separator+"DataSets"+File.separator;
	public static String   BKontologiesFolderPath="BK"+File.separator+"BKontologies"+File.separator;
	public static String   ExistingMappingsPath="BK"+File.separator+"ExistingMappings"+File.separator+"obo.csv";
    public static boolean  BKselectionInternalExploration=false;
    public static  int     derivationStrategy=derivationStrategies.specific_algo;
    public static  int     mappingSelectionStrategy=derivationStrategies.specific_algo;
    public static  int     derivationMaxPathLength=4;
	public static double   mappingSelectionThreshold=0.0;
	public static MatcherBridge matcher=new MatcherBridge();
	public static URL 		sourceOntology;
	public static URL 		targetOntology;
	public static boolean 	logMapRepair=true;
	
	/**
	 * Internal parameters
	 */
	
	public static String BkAlignmentsFolderPath="ProcessFolders"+File.separator+"BKalignments"+File.separator;
	public static String derivedCheminsPath="ProcessFolders"+File.separator+"derivationResult"+File.separator+"paths.csv";
	public static String derivationResultFolderPath="ProcessFolders"+File.separator+"derivationResult"+File.separator;
	public static String directAlignmentFolderPath="ProcessFolders"+File.separator+"directAlignments"+File.separator;
	public static String BkFolderPath="ProcessFolders"+File.separator+"BK"+File.separator;
	public static String BuiltBkPath=BkFolderPath+File.separator+"BK.owl";
	public static String ResultFolderPath= "ProcessFolders"+File.separator+"Result"+File.separator;
	public static String separator="¤";
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
    
	
       
    
    public class derivationStrategies {
    	  public static final int neo4j = 1;
    	  public static final int specific_algo = 2;
    }
    
    public class mappingSelectionStrategies {
  	  public static final int ML_based = 1;
  	  public static final int  Rule_based= 2;
  }
	
}
