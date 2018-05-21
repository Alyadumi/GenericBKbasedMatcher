package LIRMM.FADO.annane.BKbasedMatching;

import java.io.File;

import java.net.URL;

public class EXE {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		File sourceOntologyFile=new File(C.t1_fma);//source ontology
		File targetOntologyFile=new File(C.t1_nci);//target ontology
		if(sourceOntologyFile.exists()&&targetOntologyFile.exists())
		{
	    Matching matcher=new Matching(sourceOntologyFile.toURI().toURL(), targetOntologyFile.toURI().toURL());
	    URL res=matcher.BkBasedMatching();//res will contain the URL of the generated alignment
	    System.out.println(res);
	    matcher.ComputeFScore(res, C.t1_R);
		}
		else
			System.out.println("One of the ontologies to align does not exist!");
	}

}
