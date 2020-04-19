package LIRMM.FADO.annane.BKbasedMatching;

import java.io.File;
import java.net.URL;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.GraphDatabase;
import LIRMM.FADO.annane.BKbasedMatching.C.derivationStrategies;
import OAEI2017.MatcherBridge;

public class EXE {

	public static void main(String[] args) throws Exception {
		
		//set up external parameters
		
		C.matcher_name = "YAM++";
		C.matcher = new MatcherBridge();
		
		C.derivationMaxPathLength = 4;
		C.derivationStrategy = derivationStrategies.neo4j;
		if(C.derivationStrategy == derivationStrategies.neo4j)
		{
			String data_base_name = "neo4j";
			String password = "aminaamina";
			C.driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic(data_base_name, password )  );
			C.session= C.driver.session();
		}
		C.semantic_verification = false;
		
		C.BKselectionInternalExploration = false ;
		

		
		// create if they don't exist folders that are required for the execution 
		Fichier.returnFolder("ProcessingFolder");
		Fichier.returnFolder(C.BkAlignmentsFolderPath);
		Fichier.returnFolder(C.BkFolderPath);
		Fichier.returnFolder(C.derivationResultFolderPath);
		Fichier.returnFolder(C.ResultFolderPath);
		Fichier.returnFolder(C.directAlignmentFolderPath);
		Fichier.returnFolder(C.alignmentsRepositoryFolderPath);
		
		//load source and target ontologies
		File sourceOntologyFile=new File(C.mouse);//source ontology
		File targetOntologyFile=new File(C.human);//target ontology
		
		//match the ontologies
		if(sourceOntologyFile.exists()&&targetOntologyFile.exists())
		{
	    Matching matcher=new Matching(sourceOntologyFile.toURI().toURL(), targetOntologyFile.toURI().toURL());
	    URL res = matcher.BkBasedMatching();//res will contain the URL of the generated alignment
	    System.out.println(res);
	    matcher.ComputeFScore(res, C.ma_nci_Ref);
		}
		else
			System.out.println("One of the ontologies to align does not exist!");
	}

}
