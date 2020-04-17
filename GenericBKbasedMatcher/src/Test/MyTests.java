package Test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import LIRMM.FADO.annane.BKbasedMatching.Fichier;
import LIRMM.FADO.annane.BKbasedMatching.Matching;
import LIRMM.FADO.annane.BKbasedMatching.Parameters;
import LIRMM.FADO.annane.BKbasedMatching.SemanticVerification;

public class MyTests {


	@Test
	public void semantic_verification_test() throws MalformedURLException
	{
		String sourcePath="Test\\OAEI_tracks\\Anatomy\\MA.owl";
		String targetPath="Test\\OAEI_tracks\\Anatomy\\NCI.owl";
		File sourceOntologyFile=new File(sourcePath);//source ontology
		File targetOntologyFile=new File(targetPath);//target ontology
		Parameters.sourceOntology=sourceOntologyFile.toURI().toURL();
		Parameters.targetOntology=targetOntologyFile.toURI().toURL();
		SemanticVerification s = new SemanticVerification();
		String alignment_path = "Test/semanticVerification/res.rdf";
		//String repaired_alignment_path =  "Test/semanticVerification/repaired_alignment.rdf";
		//Fichier repaired_alignment_file = new Fichier(repaired_alignment_path);
		TreeSet<String> repaired_alignment = s.LogMapRepair_SemanticVerification(alignment_path);
		System.out.println(repaired_alignment.size());
		Assert.assertTrue(repaired_alignment.size()>0);

	
	}
	
}
