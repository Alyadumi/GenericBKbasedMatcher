package LIRMM.FADO.annane.BKbasedMatching;

import java.io.File;
import java.net.URL;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.GraphDatabase;
import LIRMM.FADO.annane.BKbasedMatching.C.derivationStrategies;




public class EXE {

	public static void main(String[] args) throws Exception {
		
		//set up external parameters
			
		C.derivationMaxPathLength = 4;
		C.derivationStrategy = derivationStrategies.specific_algo;
		
		if(C.derivationStrategy == derivationStrategies.neo4j)
		{
			String data_base_name = "neo4j";
			String password = "aminaamina";
			C.driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic(data_base_name, password )  );
			C.session= C.driver.session();
		}
		
		C.mappingSelectionStrategy = C.mappingSelectionStrategies.ML_based;
		
		C.semantic_verification = true;
		
		C.BKselectionInternalExploration = false ;
		

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
