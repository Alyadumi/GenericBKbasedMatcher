package LIRMM.FADO.annane.BKbasedMatching;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.NotFoundException;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;

//import OAEI2017.MatcherBridge;
//import YAM_BIO_Matcher.A_Matching_Ontologies;


import de.unima.alcomox.ExtractionProblem;
import de.unima.alcomox.Settings;
import de.unima.alcomox.exceptions.AlcomoException;
import de.unima.alcomox.mapping.Correspondence;
import de.unima.alcomox.mapping.Mapping;
import de.unima.alcomox.ontology.IOntology;
import eu.sealsproject.platform.res.tool.api.ToolBridgeException;
import eu.sealsproject.platform.res.tool.api.ToolException;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

public class Matching {
	

	URL source;
	URL target;
	String sourceOntologyURI;
	String targetOntologyURI;
	TreeSet<String> resultAlignment;
	
	public static void main(String[] args) throws Exception {
	// TODO Auto-generated method stub
		File f=new File("Result/a.rdf");
		ComputeFScore(f.toURI().toURL(), C.ma_nci_Ref);
	}
	
	
	
	public Matching(URL source, URL target) throws URISyntaxException
	{
		Fichier.returnFolder("ProcessingFolder");
		Fichier.returnFolder(C.BkAlignmentsFolderPath);
		Fichier.returnFolder(C.BkFolderPath);
		Fichier.returnFolder(C.derivationResultFolderPath);
		Fichier.returnFolder(C.ResultFolderPath);
		Fichier.returnFolder(C.directAlignmentFolderPath);
		Fichier.returnFolder(C.alignmentsRepositoryFolderPath);
		
		this.source=source;
		this.target=target;
		sourceOntologyURI=JenaMethods.getOntologyUri(source);
		targetOntologyURI=JenaMethods.getOntologyUri(target);
	}
	
	public Matching(String source, String target) throws URISyntaxException, MalformedURLException
	{
		Fichier.returnFolder("ProcessingFolder");
		Fichier.returnFolder(C.BkAlignmentsFolderPath);
		Fichier.returnFolder(C.BkFolderPath);
		Fichier.returnFolder(C.derivationResultFolderPath);
		Fichier.returnFolder(C.ResultFolderPath);
		Fichier.returnFolder(C.directAlignmentFolderPath);
		Fichier.returnFolder(C.alignmentsRepositoryFolderPath);
		
		
		File sourceFile=new File(source);
		File targetFile=new File(target);
		this.source=sourceFile.toURI().toURL();
		this.target=targetFile.toURI().toURL();
		sourceOntologyURI=JenaMethods.getOntologyUri(this.source);
		targetOntologyURI=JenaMethods.getOntologyUri(this.target);
	}
	
	public void BkBasedMatching(TreeSet<String> URIs) throws Exception
	{
		URL res = null;
		BKbuilding buildBK = new BKbuilding();
		buildBK.sourceIRI = sourceOntologyURI;
		buildBK.source=source;
		Map<String, TreeSet<Noeud>> builtBk = buildBK.BuildEnrichedBK(URIs);
		System.out.println("Size of the selected BK: "+builtBk.size() );
		
		BKuse useBK=new BKuse(source, target,URIs);
		useBK.sourceIRI=sourceOntologyURI;
		useBK.targetIRI=targetOntologyURI;
		useBK.target=target;
		useBK.source=source;
		useBK.BkOntologiesCodes=buildBK.BkOntologiesCodes;
		useBK.BKuseWithEnrichment(builtBk);
	}
	
	
	
	/**
	 * This function implement the whole BK based matching
	 * @return the URL of the file containing the resulted alignment
	 * @throws Exception
	 */
	public URL BkBasedMatching() throws Exception
	{
		URL res=null;
		BKbuilding buildBK=new BKbuilding();
		buildBK.sourceIRI=sourceOntologyURI;
		buildBK.source=source;
		
		//BK building or BK selection
		Map<String, TreeSet<Noeud>> builtBk = buildBK.BuildBK();
		System.out.println("The size of the selected BK is: "+builtBk.size() );
		
		//BK exploitation
		BKuse useBK=new BKuse(source, target);
		useBK.sourceIRI=sourceOntologyURI;
		useBK.targetIRI=targetOntologyURI;
		useBK.target=target;
		useBK.source=source;
		useBK.BkOntologiesCodes = buildBK.BkOntologiesCodes;
		useBK.BKexploitation(builtBk);
		
		//Final mapping selection
		FinalMappingSelection fms = new FinalMappingSelection();
		resultAlignment= fms.selection(C.thresholdSelection);
		String resFile=C.ResultFolderPath+"res.rdf";
		Fichier fichierResultat=new Fichier(resFile);
		fichierResultat.deleteFile();
		res=fichierResultat.ecrire(getOAEIalignmentFormat());
		
		if(C.semantic_verification)
		{
			LogMapRepair r=new LogMapRepair();
			Set<MappingObjectStr> repairedMappings = r.useLogMapRepair(source.toString(), target.toString(), resFile);		
			TreeSet<String> finalAlignment=new TreeSet<String>();
			resultAlignment.clear();
			for (MappingObjectStr mappingObjectStr : repairedMappings) {
			resultAlignment.add(mappingObjectStr.getIRIStrEnt1()+','+mappingObjectStr.getIRIStrEnt2()+','+mappingObjectStr.getConfidence());
			}
			
			resFile=C.ResultFolderPath+"res2.rdf";
			fichierResultat=new Fichier(resFile);
			fichierResultat.deleteFile();
			res=fichierResultat.ecrire(getOAEIalignmentFormat());
		}
		
		/*resultAlignment=semanticVerification(source.toString(), target.toString(),fichierResultat.path);	
		resFile=C.ResultFolderPath+"resultat.rdf";
		fichierResultat=new Fichier(resFile);
		fichierResultat.deleteFile();
		res=fichierResultat.ecrire(getOAEIalignmentFormat());*/
		return res;
	}
	/**
	 * 
	 */
	public String generateCandidateMappings() throws Exception
	{
		URL res=null;
		BKbuilding buildBK=new BKbuilding();
		buildBK.sourceIRI=sourceOntologyURI;
		buildBK.source=source;
		
		//BK building or BK selection
		Map<String, TreeSet<Noeud>> builtBk = buildBK.BuildBK();
		System.out.println("The size of the selected BK is: "+builtBk.size() );
		
		//BK exploitation
		BKuse useBK=new BKuse(source, target);
		useBK.sourceIRI=sourceOntologyURI;
		useBK.targetIRI=targetOntologyURI;
		useBK.target=target;
		useBK.source=source;
		useBK.BkOntologiesCodes = buildBK.BkOntologiesCodes;
		useBK.BKexploitation(builtBk);
		return C.derivedCheminsPath;
		}
	
	/**
	 * tester les performances du matcher sans BK
	 * @return
	 * @throws Exception
	 */
	public static String testAllWithoutBK() throws Exception
	{
		Fichier.deleteFile("scenarios");
		Fichier summaryResults=new Fichier("sammaryResults.csv");
		summaryResults.deleteFile();
		String resPath=null;
		String result="";
		String task;
		/* *****task1 */
		task="Anatomy,";

		File s=new File(C.mouse);
		File t=new File(C.human);
		Matching  bbm=new Matching(s.toURI().toURL(),t.toURI().toURL());
		URL resultFile = bbm.matchOntologies();
		result=result+task+ComputeFScore(resultFile, C.ma_nci_Ref)+Fichier.retourAlaLigne;
		summaryResults.ecrire(result);
		result="";
		/* *****task1 */
		task="task1,";

	    s=new File(C.t1_fma);
	    t=new File(C.t1_nci);
	    bbm=new Matching(s.toURI().toURL(),t.toURI().toURL());
		resultFile = bbm.matchOntologies();
		result=result+task+ComputeFScore(resultFile, C.t1_R)+Fichier.retourAlaLigne;
		summaryResults.ecrire(result);
		result="";
		/* *****task2 */
		task="task2,";
		s=new File(C.t2_fma);
		t=new File(C.t2_nci);
		bbm=new Matching(s.toURI().toURL(),t.toURI().toURL());
		resultFile = bbm.matchOntologies();
		result=result+task+ComputeFScore(resultFile, C.t1_R)+Fichier.retourAlaLigne;
		summaryResults.ecrire(result);
		result="";
		/* *****task3 */
		task="task3,";
		s=new File(C.t3_fma);
		t=new File(C.t3_snomed);
		bbm=new Matching(s.toURI().toURL(),t.toURI().toURL());
		resultFile = bbm.matchOntologies();
		result=result+task+ComputeFScore(resultFile, C.t3_R)+Fichier.retourAlaLigne;
		summaryResults.ecrire(result);
		result="";
		/* *****task4 */
		task="task4,";
		s=new File(C.t4_fma);
		t=new File(C.t4_snomed);
		bbm=new Matching(s.toURI().toURL(),t.toURI().toURL());
		resultFile =bbm.matchOntologies();
		result=result+task+ComputeFScore(resultFile, C.t3_R)+Fichier.retourAlaLigne;
		summaryResults.ecrire(result);
		result="";
		/* *****task5 */
		task="task5,";
		s=new File(C.t5_snomed);
		t=new File(C.t5_nci);
		bbm=new Matching(s.toURI().toURL(),t.toURI().toURL());
		resultFile = bbm.matchOntologies();
		result=result+task+ComputeFScore(resultFile, C.t5_R)+Fichier.retourAlaLigne;
		summaryResults.ecrire(result);
		result="";
		/* *****task6 */
		task="task6,";
		s=new File(C.t6_snomed);
		t=new File(C.t6_nci);
		bbm=new Matching(s.toURI().toURL(),t.toURI().toURL());
		resultFile = bbm.matchOntologies();
		result=result+task+ComputeFScore(resultFile, C.t5_R)+Fichier.retourAlaLigne;
		summaryResults.ecrire(result);
		result="";
		Fichier.deleteFile("scenarios");
		/* ******************************************************** */
		return summaryResults.path;
	}
	
	/**
	 * this function tests all OAEI biomedical benchmarks and write the results against the reference alignments
	 * @return the path of the final results
	 * @throws Exception
	 */
	public static String testAll() throws Exception
	{
		Fichier.deleteFile("scenarios");
		Fichier summaryResults=new Fichier("sammaryResults.csv");
		summaryResults.deleteFile();
		String resPath=null;
		String result="";
		String task;
		// *****Anatomy 
		task="Anatomy,";

		File s=new File(C.mouse);
		File t=new File(C.human);
		Matching  bbm=new Matching(s.toURI().toURL(),t.toURI().toURL());
		URL resultFile = bbm.BkBasedMatching();
		result=result+task+ComputeFScore(resultFile, C.ma_nci_Ref)+Fichier.retourAlaLigne;
		summaryResults.ecrire(result);
		result="";
		
		//* ****task1 
		task="task1,";

	    s=new File(C.t1_fma);
	    t=new File(C.t1_nci);
	    bbm=new Matching(s.toURI().toURL(),t.toURI().toURL());
		resultFile = bbm.BkBasedMatching();
		result=result+task+ComputeFScore(resultFile, C.t1_R)+Fichier.retourAlaLigne;
		summaryResults.ecrire(result);
		result="";
		 //* ****task2 
		task="task2,";
		s=new File(C.t2_fma);
		t=new File(C.t2_nci);
		bbm=new Matching(s.toURI().toURL(),t.toURI().toURL());
		resultFile = bbm.BkBasedMatching();
		result=result+task+ComputeFScore(resultFile, C.t1_R)+Fichier.retourAlaLigne;
		summaryResults.ecrire(result);
		result="";
		//* *****task3 
		task="task3,";
		s=new File(C.t3_fma);
		t=new File(C.t3_snomed);
		bbm=new Matching(s.toURI().toURL(),t.toURI().toURL());
		resultFile = bbm.BkBasedMatching();
		result=result+task+ComputeFScore(resultFile, C.t3_R)+Fichier.retourAlaLigne;
		summaryResults.ecrire(result);
		result="";
		//* *****task4 
		task="task4,";
		s=new File(C.t4_fma);
		t=new File(C.t4_snomed);
		bbm=new Matching(s.toURI().toURL(),t.toURI().toURL());
		resultFile =bbm.BkBasedMatching();
		result=result+task+ComputeFScore(resultFile, C.t3_R)+Fichier.retourAlaLigne;
		summaryResults.ecrire(result);
		result="";
		// *****task5 
		task="task5,";
		s=new File(C.t5_snomed);
		t=new File(C.t5_nci);
		bbm=new Matching(s.toURI().toURL(),t.toURI().toURL());
		resultFile = bbm.BkBasedMatching();
		result=result+task+ComputeFScore(resultFile, C.t5_R)+Fichier.retourAlaLigne;
		summaryResults.ecrire(result);
		result="";
		 //*****task6 
		task="task6,";
		s=new File(C.t6_snomed);
		t=new File(C.t6_nci);
		bbm=new Matching(s.toURI().toURL(),t.toURI().toURL());
		resultFile = bbm.BkBasedMatching();
		result=result+task+ComputeFScore(resultFile, C.t5_R)+Fichier.retourAlaLigne;
		summaryResults.ecrire(result);
		result="";
		Fichier.deleteFile("scenarios");

		return summaryResults.path;
	}

	/**
	 * Cette fonction calcule le FScore de l'alignement alignmentURL par rapport à l'alignement de référence reference
	 * @param alignmentURL
	 * @param reference
	 * @return
	 * @throws Exception
	 */
	public static String ComputeFScore(URL alignmentURL, String reference) throws Exception {
		long debut =System.currentTimeMillis();
		java.text.DecimalFormat df = new java.text.DecimalFormat("0.###");
		TreeSet<String> refAlign = Fichier.loadReferenceAlignment(reference);// l'alignement de
		TreeSet<String> refAlignNeutre = Fichier.loadReferenceAlignmentNeutre(reference);// l'alignement de	
		TreeSet<String>	a=Fichier.loadOAEIAlignment(alignmentURL);
		//System.out.println(refAlignNeutre.size()+" neutre mappings");
		double fscore = 0;
		double cpt = 0;
		double precision=0.0, correctMappings = 0,neutreMappings=0;
		double recall=0.0;
		if (refAlign.size()>0) 
		{
				    for (String mapping : a) 
				    {
				    	//System.out.println(mapping);
				    	StringTokenizer lineParser = new StringTokenizer(mapping, ",");
				    	String idS=lineParser.nextToken()
				    			,idT=lineParser.nextToken();
				    	if(refAlign.contains(idS+','+idT)||refAlign.contains(idT+','+idS))correctMappings++;	
				    	else if(refAlignNeutre.contains(idS+','+idT)||refAlignNeutre.contains(idT+','+idS))neutreMappings++;
					}	
				   // System.out.println(neutreMappings+" neutre");
					recall = correctMappings / refAlign.size();
					precision = correctMappings / (a.size()-neutreMappings);
					fscore=2 * precision * recall / (precision + recall);
					System.out.println("Precision: " + df.format(precision));
					System.out.println("Recall: " + df.format(recall));
					System.out.println("FScore: " + df.format(fscore));
					System.out.println("reference number : " +refAlign.size() );
					System.out.println("found number : " +a.size());
					System.out.println("correct mappings: "+correctMappings);
		}
		long time=System.currentTimeMillis()-debut;
		C.executionTime.add("computeFScore "+(time)+"ms");
		return df.format(precision)+','+df.format(recall)+","+df.format(fscore);
	}
	
	/**
	 * 
	 * @param threshold
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static TreeSet<String> selection(double threshold, boolean obo) throws NumberFormatException, IOException
	{
		TreeSet<String> finalMappings=new TreeSet<>();
		long debut =System.currentTimeMillis();
		File chemins=new File(C.derivedCheminsPath);
		if(chemins.exists())
		{BufferedReader reader = new BufferedReader(new FileReader(C.derivedCheminsPath)); 
		String line = null; 
		String value = null; 
	    int cpt=0;
	    Map<String,Map<String,MappingCandidate> > candidats=new HashMap<>();
		while ((line = reader.readLine()) != null) 
		{
			StringTokenizer lineParser = new StringTokenizer(line, ",");
			String code1= (String) lineParser.nextElement();
			String code2 = (String) lineParser.nextElement();
			String uri1=BKbuilding.sourceElements.get(BKbuilding.sourceAcronym+C.separator+code1);
			String uri2=BKuse.targetElements.get(BKuse.targetAcronym+C.separator+code2);
			int pathLength=Integer.parseInt((String) lineParser.nextElement())-1;
			String path=(String) lineParser.nextElement();
			double s=1.0/pathLength;
			finalMappings.add(uri1+','+uri2+','+1.0);
	}

		long time=System.currentTimeMillis()-debut;
		C.executionTime.add("selection "+(time)+"ms");}
		else System.out.println("The derivation result is empty");
		return finalMappings;
	}

	//_________________________________________________________________________________	
	public URL matchOntologies() throws ToolException, ToolBridgeException
	{
		URL res;
		if (source!=null && target!=null)
			{
			res= C.matcher.align(source, target);
			}
		else throw new NullPointerException("source or target ontology URL is null");
		return res;
	}
	
	public URL matchOntologies(String resultPath) throws URISyntaxException, IOException, ToolException, ToolBridgeException
	{ 
		File destFile;
		URL res;
		if (source!=null && target!=null)
			{
			res= C.matcher.align(source, target);
			File resFile=new File(res.toURI());
			destFile=new File(resultPath);
			//copy to the destination path
		    org.apache.commons.io.FileUtils.copyFile(resFile, destFile);
		    resFile.delete();
			}
		else throw new NullPointerException("source or target ontology URL is null");
		return destFile.toURI().toURL();
	}
	//_____________________________________________________________________________________________
	//**************converti l'arbre des mappings produits en un alignement qui respecte le format d'OAEI
    Alignment convertFromTree() throws URISyntaxException {
	    
	    Alignment alignments = new URIAlignment();

	    try {
	      Model o=JenaMethods.LoadOntologyModelWithJena(source);
	      sourceOntologyURI=JenaMethods.getOntologyUri(o);
	      o.close();
	      o=JenaMethods.LoadOntologyModelWithJena(target);
	      targetOntologyURI=JenaMethods.getOntologyUri(o);
	      o.close();
	      alignments.init(URI.create(sourceOntologyURI), URI.create(targetOntologyURI));
	      alignments.setFile1(source.toURI());
	      alignments.setFile2(target.toURI());

	      alignments.setLevel("0");
	      

	    } catch (AlignmentException e) {
	      e.printStackTrace();
	    }

	    if (resultAlignment != null && resultAlignment.size() > 0) {
	      for (String mapping : resultAlignment) {
	        try {
		      StringTokenizer lineParser = new StringTokenizer(mapping, ",");
	          URI entity1 = new URI(lineParser.nextToken());
	          URI entity2 = new URI(lineParser.nextToken());
	          double score = Double.parseDouble(lineParser.nextToken());

	          String relation = "=";

	          // add to alignment
	          alignments.addAlignCell(entity1, entity2, relation, score);
	        } catch (Exception e) {
	          // TODO Auto-generated catch block
	        	System.out.println(mapping);
	        	e.printStackTrace();
	        }
	      }
	    }

	    return alignments;
	  }
    //***********************************************************************************
    public  String getOAEIalignmentFormat() throws URISyntaxException {
	    Alignment alignment = this.convertFromTree();
	    try {
	      StringWriter swriter = new StringWriter();
	      PrintWriter writer = new PrintWriter(swriter);

	      // create an alignment visitor (renderer)
	      AlignmentVisitor renderer = new RDFRendererVisitor(writer);
	      renderer.init(new BasicParameters());

	      alignment.render(renderer);

	      writer.flush();
	      writer.close();

	      return swriter.toString();
	    } catch (Exception e) {
	      e.printStackTrace();
	    }

	    return null;
	  }
	//_____________________________________________________________________________________________
	public URL getSource() {
		return source;
	}
	public void setSource(URL source) {
		this.source = source;
	}
	public void setSource(String sourcePath) throws MalformedURLException {
		File f=new File(sourcePath);
		if (f.exists())this.source = f.toURI().toURL();
		else throw new NotFoundException("ERROR: Le chemin de l'ontologie source n'est pas bon");
	}
	public URL getTarget() {
		return target;
	}
	public void setTarget(URL target) {
		this.target = target;
	}
	public void setTarget(String targetPath) throws MalformedURLException
	{
		File f=new File(targetPath);
		if (f.exists())this.target = f.toURI().toURL();
		else throw new NotFoundException("ERROR: Le chemin de l'ontologie cible n'est pas bon");
	}

/**
 * Semantic verification with ALcomo based on Hermit reasoner
 */
	public TreeSet<String> semanticVerification(String ont1Path, String ont2Path, String alignPath) throws AlcomoException
	{
		TreeSet<String> a =new TreeSet<>();
		// we ant to use Pellet as reasoner (alternatively use HERMIT)
				Settings.BLACKBOX_REASONER = Settings.BlackBoxReasoner.HERMIT;
				
				// if you want to force to generate a one-to-one alignment add this line
				// by default its set to false
				Settings.ONE_TO_ONE = false;
				
				// load ontologies as IOntology (uses fast indexing for efficient reasoning)
				// formerly LocalOntology now IOntology is recommended
				IOntology sourceOnt = new IOntology(ont1Path);
				IOntology targetOnt = new IOntology(ont2Path);

				// load the mapping
				Mapping mapping = new Mapping(alignPath);
				mapping.applyThreshhold(0.3);
				System.out.println("thresholded input mapping has " + mapping.size() + " correspondences");
				
				// define diagnostic problem
				ExtractionProblem ep = new ExtractionProblem(
						ExtractionProblem.ENTITIES_CONCEPTSPROPERTIES,
						ExtractionProblem.METHOD_OPTIMAL,
						ExtractionProblem.REASONING_EFFICIENT
				);
				
				// attach ontologies and mapping to the problem
				ep.bindSourceOntology(sourceOnt);
				ep.bindTargetOntology(targetOnt);
				ep.bindMapping(mapping);
				
				// solve the problem
				ep.solve();
			
				Mapping extracted = ep.getExtractedMapping();
				for (Correspondence correspondence : extracted) {
					a.add(correspondence.getSourceEntityUri()+','+correspondence.getTargetEntityUri()+','+correspondence.getConfidence());
				}
				System.out.println("mapping reduced from " + mapping.size() + " to " + extracted.size() + " correspondences");
	return a;
	}

}
