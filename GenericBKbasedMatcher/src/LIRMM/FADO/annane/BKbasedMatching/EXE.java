package LIRMM.FADO.annane.BKbasedMatching;

import java.io.File;

import java.net.URL;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

public class EXE {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		String sourcePath="C:\\Users\\annane\\Google Drive\\.1.Extension\\OAEI2016\\LargeBio\\T1\\FMA.owl";;
		String targetPath="C:/Users/annane/Google Drive/.1.Extension/OAEI2016/LargeBio/T1/NCI.owl";
		String referenceAlignment="C:\\Users\\annane\\Google Drive\\.1.Extension\\OAEI2016\\LargeBio\\with repairs\\oaei_FMA2NCI_UMLS_mappings_with_flagged_repairs.rdf";;
		
		
		//Parameters
		
		Parameters.driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic("neo4j", "aminaamina")  );
		Parameters.session=Parameters.driver.session();
		File sourceOntologyFile=new File(sourcePath);//source ontology
		File targetOntologyFile=new File(targetPath);//target ontology
		Parameters.sourceOntology=sourceOntologyFile.toURI().toURL();
		Parameters.targetOntology=targetOntologyFile.toURI().toURL();
		if(sourceOntologyFile.exists()&&targetOntologyFile.exists())
		{
	    Matching matcher=new Matching(sourceOntologyFile.toURI().toURL(), targetOntologyFile.toURI().toURL());
	    URL res=matcher.BkBasedMatching();//res will contain the URL of the generated alignment
	    System.out.println("the generated alignment is here: "+res);
	    matcher.ComputeFScore(res, referenceAlignment);
		}
		else
			System.out.println("One of the ontologies to align does not exist!");
	}

}
