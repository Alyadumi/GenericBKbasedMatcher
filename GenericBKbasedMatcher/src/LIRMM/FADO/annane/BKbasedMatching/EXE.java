package LIRMM.FADO.annane.BKbasedMatching;

import java.io.File;

import java.net.URL;

public class EXE {

	public static void main(String[] args) throws Exception {
		
		// create the necessary folders
		Fichier.returnFolder("ProcessingFolder");
		Fichier.returnFolder(C.BkAlignmentsFolderPath);
		Fichier.returnFolder(C.BkFolderPath);
		Fichier.returnFolder(C.derivationResultFolderPath);
		Fichier.returnFolder(C.ResultFolderPath);
		Fichier.returnFolder(C.directAlignmentFolderPath);
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
