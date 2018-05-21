package LIRMM.FADO.annane.BKbasedMatching;


import java.io.File;
import java.util.ArrayList;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;




public class C {
	
	public  static Driver driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic("neo4j", "aminaamina")  );
	public static Session session=driver.session();
	public static String separatorBkAlignmentFiles="___";
	public static String separator="¤";
	public static String BkOntologiesFolderPath="C:\\Users\\annane\\workspace\\AutomaticBKbasedMatching\\BK_ontologies\\BK1\\";
	public static String BkAlignmentsFolderPath="BKalignments"+File.separator;
	public static String alignmentsRepositoryFolderPath="alignmentsRepository"+File.separator;

	public static String BkFolderPath="BK"+File.separator;
	public static String oboFilePath=alignmentsRepositoryFolderPath+"obo.csv";
	public static String directAlignmentFolderPath="directAlignments"+File.separator;
	public static String derivationResultFolderPath="derivationResult"+File.separator;
	public static String derivedCheminsPath=derivationResultFolderPath+File.separator+"chemins.csv";
	public static ArrayList<String> executionTime = new ArrayList<>();
	public static String BuiltBkPath=BkFolderPath+File.separator+"BK.owl";
	public static  String ResultFolderPath= "Result"+File.separator;
	
	public static final String synonyms="oboInOwl:hasExactSynonym|oboInOwl:hasRelatedSynonym|skos:altLabel|<http://purl.bioontology.org/ontology/SYN#synonym>|<http://www.ebi.ac.uk/efo/alternative_term>|<http://purl.obolibrary.org/obo/synonym>|<http://scai.fraunhofer.de/CSEO#Synonym>|BIRNLEX:synonyms";
	public static final String prefLabs="rdfs:label|skos:prefLabel|BIRNLEX:preferred_label";
    public static double mappingSelectionPourcentage=0.0;
	public static  String folder= "C:/Users/annane/workspace/AutomaticBKbasedMatching/Anatomy Alignments/";
	public static final String resFolder=folder;
			//"C:/Users/annane/workspace/Matching/EXP_LogMap/BK/BK6/";

	public static final int stepMin=1,stepMax=3;
	
	public static Double threshold=0.9;
	public static Double thresholdSelection=0.00;
	
	   public static final double scoreOboMappings=1,scoreYamMappings=0.5;
	   public static final int maxSyn=2;
	   public static final String t1_R="C:\\Users\\annane\\Google Drive\\.1.Extension\\OAEI2016\\LargeBio\\with repairs\\oaei_FMA2NCI_UMLS_mappings_with_flagged_repairs.rdf";
	   public static final String t3_R="C:\\Users\\annane\\Google Drive\\.1.Extension\\OAEI2016\\LargeBio\\with repairs\\oaei_FMA2SNOMED_UMLS_mappings_with_flagged_repairs.rdf";
	   public static final String t5_R="C:\\Users\\annane\\Google Drive\\.1.Extension\\OAEI2016\\LargeBio\\with repairs\\oaei_SNOMED2NCI_UMLS_mappings_with_flagged_repairs.rdf";
	   public static final String subGraphFolder="C:/Users/annane/workspace/Matching/EXP_LogMap/Global BK with YAM 38/subgraph/";
	
		public static final String prefix = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>"
				+ "prefix skos: <http://www.w3.org/2004/02/skos/core#> "
				+ "PREFIX oboInOwl:<http://www.geneontology.org/formats/oboInOwl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX BIRNLEX:<http://bioontology.org/projects/ontologies/birnlex#> ";

   public static final String t2_fma="C:/Users/annane/Google Drive/.1.Extension/OAEI2016/LargeBio/T2/FMA.owl";
   public static final String t2_nci="C:/Users/annane/Google Drive/.1.Extension/OAEI2016/LargeBio/T2/NCI.owl";
   public static final String t4_fma="C:/Users/annane/Google Drive/.1.Extension/OAEI2016/LargeBio/T4/FMA.owl";
   public static final String t4_snomed="C:/Users/annane/Google Drive/.1.Extension/OAEI2016/LargeBio/T4/SNOMED.owl";
   public static final String t6_snomed="C:/Users/annane/Google Drive/.1.Extension/OAEI2016/LargeBio/T6/SNOMED.owl";
   public static final String t6_nci="C:/Users/annane/Google Drive/.1.Extension/OAEI2016/LargeBio/T6/NCI.owl";
   public static final String HP="C:\\Users\\annane\\Google Drive\\theseAmina\\Mes travaux\\5.OAEI2017\\Phenotype_ontologies\\hp.owl";
   public static final String MP="C:\\Users\\annane\\Google Drive\\theseAmina\\Mes travaux\\5.OAEI2017\\Phenotype_ontologies\\mp.owl";
   public static final String doid="C:\\Users\\annane\\Google Drive\\theseAmina\\Mes travaux\\5.OAEI2017\\Phenotype_ontologies\\doid.owl";
   public static final String ordo="C:\\Users\\annane\\Google Drive\\theseAmina\\Mes travaux\\5.OAEI2017\\Phenotype_ontologies\\ordo.owl";
   
   public static final String MESH="C:\\Users\\annane\\Google Drive\\theseAmina\\Mes travaux\\5.OAEI2017\\Phenotype_ontologies\\mesh.owl";
   public static final String omim="C:\\Users\\annane\\Google Drive\\theseAmina\\Mes travaux\\5.OAEI2017\\Phenotype_ontologies\\omim.owl";
   public static final String R_hp_omim="C:\\Users\\annane\\Google Drive\\theseAmina\\Mes travaux\\5.OAEI2017\\Phenotype_ontologies\\HP-OMIM\\HP_OMIM.owl";
	 public static final String R_hp_mp="C:\\Users\\annane\\Google Drive\\theseAmina\\Mes travaux\\5.OAEI2017\\Phenotype_ontologies\\HP-MP\\HP_MP.owl"; 
	public static final String t1_fma="C:\\Users\\annane\\Google Drive\\.1.Extension\\OAEI2016\\LargeBio\\T1\\FMA.owl";
   public static final String t1_nci="C:/Users/annane/Google Drive/.1.Extension/OAEI2016/LargeBio/T1/NCI.owl";
   public static final String t3_fma="C:/Users/annane/Google Drive/.1.Extension/OAEI2016/LargeBio/T3/FMA.owl";
   public static final String t3_snomed="C:/Users/annane/Google Drive/.1.Extension/OAEI2016/LargeBio/T3/SNOMED.owl";
   public static final String t5_nci="C:/Users/annane/Google Drive/.1.Extension/OAEI2016/LargeBio/T5/NCI.owl";
   public static final String t5_snomed="C:/Users/annane/Google Drive/.1.Extension/OAEI2016/LargeBio/T5/SNOMED.owl";


   public static final String BKfolder="C:/Users/annane/Google Drive/.1.Extension/BK ontologies/";
   
   

	
	
	public static final String ma_nci_Ref="C:\\Users\\annane\\Google Drive\\theseAmina\\Mes travaux\\5.OAEI2017\\Anatomy\\reference.rdf";
	public static final String mouse="C:\\Users\\annane\\Google Drive\\theseAmina\\Mes travaux\\5.OAEI2017\\Anatomy\\ma.owl";
	public static final String human="C:\\Users\\annane\\Google Drive\\theseAmina\\Mes travaux\\5.OAEI2017\\Anatomy\\nci.owl";

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
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}