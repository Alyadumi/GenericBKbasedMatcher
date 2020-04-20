package LIRMM.FADO.annane.BKbasedMatching;


import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

//import uk.ac.ox.krr.logmap_lite.MatcherBridge; //LogMap_lite

import uk.ac.ox.krr.logmap2.oaei.MatcherBridge; //LogMap

//import OAEI2017.MatcherBridge; //YAM++





public class C {
	
	/**
	 * External parameters
	 */
	public static URL 		sourceOntology;
	public static URL 		targetOntology;
    public static final String   BKontologiesFolderPath="GenericBKbasedMatcher/BK"+File.separator+"BKontologies"+File.separator;
	public static final String   ExistingMappingsPath="GenericBKbasedMatcher/BK"+File.separator+"ExistingMappings"+File.separator+"obo.csv";
	
	public final static MatcherBridge matcher = new MatcherBridge();
	public final static String matcher_name = "LogMap";
	
	public static boolean semantic_verification = false;
	
	public final static String   alignmentsRepositoryFolderPath="alignmentsRepository"+File.separator + matcher_name +File.separator;
	
		
    public static boolean  BKselectionInternalExploration = false;

	public static int BKselectionExplorationLength = 1;
	
    public static  int     derivationStrategy = derivationStrategies.specific_algo;
    public static  int     derivationMaxPathLength = 4;
    
    public static  int     mappingSelectionStrategy;
	public static double   mappingSelectionThreshold = 0.0;
    public static String   dataSetsFolderPath="BK"+File.separator+"DataSets"+File.separator;
    
    
	
	/**
	 * Internal parameters
	 */
    public final static String MLselectionDatasetsFolderPath = "ML_selection/ML_datasets"+File.separator;
    public final static String BkAlignmentsFolderPath="ProcessingFolder/BKalignments"+File.separator;
	public final static String ResultFolderPath= "ProcessingFolder/Result"+File.separator;
	public final static String BkFolderPath="ProcessingFolder/BK"+File.separator;
	public final static String BuiltBkPath=BkFolderPath+File.separator+"BK.owl";
	public final static String directAlignmentFolderPath="ProcessingFolder/directAlignments"+File.separator;
	public final static String derivationResultFolderPath="ProcessingFolder/derivationResult"+File.separator;
	public static String derivedCheminsPath=derivationResultFolderPath+File.separator+"chemins.csv";
	public final static String separator="¤";
	public static Driver driver;
	public static Session session;
	public final static  String prefix = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
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
	public final static String neo4j_import_folder="C:\\Users\\aannane\\Documents\\neo4j\\neo4jDatabases\\database-31fe5ea4-7db3-44d5-8638-ecb5f50c6600\\installation-3.5.14\\import\\";
	
    
	
	public static String separatorBkAlignmentFiles="___";

	public final static String BkOntologiesFolderPath="BKontologies"+File.separator;
		
	public static ArrayList<String> executionTime = new ArrayList<>();

	
    public static double mappingSelectionPourcentage = 0.0;
	public static  String folder= "C:/Users/annane/workspace/AutomaticBKbasedMatching/Anatomy Alignments/";
	public static final String resFolder=folder;
			//"C:/Users/annane/workspace/Matching/EXP_LogMap/BK/BK6/";

	public static final int stepMin=1,stepMax=3;
	
	public static Double threshold=0.9;
	public static Double thresholdSelection=0.00;
	
	   public static final double scoreOboMappings=1,scoreYamMappings=0.5;
	   public static final int maxSyn=2;
	   public static final String t1_R="E:\\Amina documents\\MyDocuments YAM-BIO\\.1.Extension\\OAEI2016\\LargeBio\\with repairs\\oaei_FMA2NCI_UMLS_mappings_with_flagged_repairs.rdf";
	   public static final String t3_R="E:\\Amina documents\\MyDocuments YAM-BIO\\.1.Extension\\OAEI2016\\LargeBio\\with repairs\\oaei_FMA2SNOMED_UMLS_mappings_with_flagged_repairs.rdf";
	   public static final String t5_R="E:\\Amina documents\\MyDocuments YAM-BIO\\.1.Extension\\OAEI2016\\LargeBio\\with repairs\\oaei_SNOMED2NCI_UMLS_mappings_with_flagged_repairs.rdf";
	   public static final String subGraphFolder="C:/Users/annane/workspace/Matching/EXP_LogMap/Global BK with YAM 38/subgraph/";
	



   
   public static final String t1_fma="E:\\Amina documents\\MyDocuments YAM-BIO\\.1.Extension\\OAEI2016\\LargeBio\\T1\\FMA.owl";
   public static final String t1_nci="E:\\Amina documents\\MyDocuments YAM-BIO\\.1.Extension\\OAEI2016\\LargeBio\\T1\\NCI.owl";
   public static final String t3_fma="E:\\Amina documents\\MyDocuments YAM-BIO\\.1.Extension\\OAEI2016\\LargeBio/T3/FMA.owl";
   public static final String t3_snomed="E:\\Amina documents\\MyDocuments YAM-BIO\\.1.Extension\\OAEI2016\\LargeBio/T3/SNOMED.owl";
   public static final String t5_nci="E:\\Amina documents\\MyDocuments YAM-BIO\\.1.Extension\\OAEI2016\\LargeBio/T5/NCI.owl";
   public static final String t5_snomed="E:\\Amina documents\\MyDocuments YAM-BIO\\.1.Extension\\OAEI2016\\LargeBio/T5/SNOMED.owl";

   public static final String t2_fma="E:\\Amina documents\\MyDocuments YAM-BIO\\.1.Extension\\OAEI2016\\LargeBio/T2/FMA.owl";
   public static final String t2_nci="E:\\Amina documents\\MyDocuments YAM-BIO\\.1.Extension\\OAEI2016\\LargeBio/T2/NCI.owl";
   public static final String t4_fma="E:\\Amina documents\\MyDocuments YAM-BIO\\.1.Extension\\OAEI2016\\LargeBio/T4/FMA.owl";
   public static final String t4_snomed="E:\\Amina documents\\MyDocuments YAM-BIO\\.1.Extension\\OAEI2016\\LargeBio/T4/SNOMED.owl";
   public static final String t6_snomed="E:\\Amina documents\\MyDocuments YAM-BIO\\.1.Extension\\OAEI2016\\LargeBio/T6/SNOMED.owl";
   public static final String t6_nci="E:\\Amina documents\\MyDocuments YAM-BIO\\.1.Extension\\OAEI2016\\LargeBio/T6/NCI.owl";
   

   public static final String BKfolder="C:/Users/annane/Google Drive/.1.Extension/BK ontologies/";
   
   


	
	public static final String ma_nci_Ref="E:/Amina documents/MyDocuments YAM-BIO/JWS/bridges/Nouveau dossier\\Anatomy\\reference.rdf";
	public static final String mouse="E:/Amina documents/MyDocuments YAM-BIO/JWS/bridges/Nouveau dossier\\Anatomy\\MA.owl";
	public static final String human="E:\\Amina documents\\MyDocuments YAM-BIO\\JWS\\bridges\\Nouveau dossier\\Anatomy\\NCI.owl";
	
	

	public static final String oboFolder="C:/Users/annane/workspace/MyProject/logMapOntologies/obo/";
	public static final String mapCodeDoid="C:/Users/annane/workspace/Matching/Resources/doid.xrefs";
	public static final String mapCodeUberon="C:/Users/annane/workspace/Matching/Resources/uberon.xrefs";
	public static final String mapCode="C:/Users/annane/workspace/Matching/Resources/UberonDoid.txt";
	public static String artff=
"@attribute aMaxMax numeric \r\n"+
"@attribute aMaxMin numeric \r\n"+
"@attribute aMaxAvg numeric \r\n"+

"@attribute aMinMax numeric \r\n"+
"@attribute aMinMin numeric \r\n"+
"@attribute aMinAvg numeric \r\n"+

"@attribute aMMax numeric \r\n"+
"@attribute aMMin numeric \r\n"+
"@attribute aMAvg numeric \r\n"+

"@attribute aPMax numeric \r\n"+
"@attribute aPMin numeric \r\n"+
"@attribute aPAvg numeric \r\n"+

"@attribute aAMax numeric \r\n"+
"@attribute aAMin numeric \r\n"+
"@attribute aAAvg numeric \r\n"+

"@attribute aVMax numeric \r\n"+
"@attribute aVMin numeric \r\n"+
"@attribute aVAvg numeric \r\n"+

"@attribute aMSVMax numeric \r\n"+
"@attribute aMSVMin numeric \r\n"+
"@attribute aMSVAvg numeric \r\n"+

"@attribute LMax numeric \r\n"+
"@attribute LMin numeric \r\n"+
"@attribute LAvg numeric \r\n"+
"@attribute PN numeric \r\n"+
"@attribute DS numeric \r\n"+
"@attribute PLab numeric \r\n"+
"@attribute obo numeric \r\n"+
"@attribute class {true, false} \r\n"+
"@data \r\n";
	
	//*************************************************************************
		public static  ArrayList<String> generatePoss(String word){
	        ArrayList<String> al = new ArrayList<String>();
	        //System.out.println("word "+word);
	        String firstLetter = word.substring(0, 1);
	        al.add(firstLetter);
	        if (word.length()==1) return al;
	        else{
	            ArrayList<String> possibilities = generatePoss(word.substring(1));
	            for (String poss:possibilities){
	                al.add(poss);
	                al.add(firstLetter+poss);
	            }
	            return al;
	        }
	}

		 public static String getUriCode(String uri) throws Exception
		   {
			   String code=null;
			   if(uri==null)throw new Exception("URI given to the getUriCode function is null");
			   else
			   { 
				   if(uri.contains("#"))code=uri.substring(uri.indexOf("#")+1);
				   else if(uri.contains("/")) code =uri.substring(uri.lastIndexOf("/")+1);
				   else code=uri;
			    }
			   
			   return code;
		   }


		   public class derivationStrategies {
		    	  public static final int neo4j = 1;
		    	  public static final int specific_algo = 2;
		    }
		    
		    public class mappingSelectionStrategies {
		  	  public static final int ML_based = 1;
		  	  public static final int  Rule_based= 2;
		  }
}