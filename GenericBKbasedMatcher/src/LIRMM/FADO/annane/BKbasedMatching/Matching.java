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
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;


import OAEI2017.MatcherBridge;
import YAM_BIO_Matcher.A_Matching_Ontologies;

import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

public class Matching {
	

	URL source;
	URL target;
	String sourceOntologyURI;
	String targetOntologyURI;
	/**
	 * the matcher variable represents the direct matcher that will be used for anchoring. 
	 * It may be any direct matcher that implements an align function that takes as input the URL of the source and target ontologies and 
	 * returns the URL of the generated alignment. The generated alignment should be generated with the API alignment (the RDF format).
	 */
	MatcherBridge matcher=new MatcherBridge();
	TreeSet<String> resultAlignment;
	
	public static void main(String[] args) throws Exception {
	// TODO Auto-generated method stub
		File f=new File("Result/a.rdf");
		ComputeFScore(f.toURI().toURL(), C.ma_nci_Ref);
	}
	public Matching(URL source, URL target) throws URISyntaxException
	{
		this.source=source;
		this.target=target;
		sourceOntologyURI=JenaMethods.getOntologyUri(source);
		targetOntologyURI=JenaMethods.getOntologyUri(target);
	}
	
	public Matching(String source, String target) throws URISyntaxException, MalformedURLException
	{
		File sourceFile=new File(source);
		File targetFile=new File(target);
		this.source=sourceFile.toURI().toURL();
		this.target=targetFile.toURI().toURL();
		sourceOntologyURI=JenaMethods.getOntologyUri(this.source);
		targetOntologyURI=JenaMethods.getOntologyUri(this.target);
	}
	public void BkBasedMatching(TreeSet<String> URIs) throws Exception
	{
		URL res=null;
		BKbuilding buildBK=new BKbuilding();
		buildBK.sourceIRI=sourceOntologyURI;
		buildBK.source=source;
		Map<String, TreeSet<Noeud>> builtBk = buildBK.BuildEnrichedBK(URIs);
		System.out.println("la taille du BK selectionnée est de: "+builtBk.size() );
		
		BKuse useBK=new BKuse(source, target,URIs);
		useBK.sourceIRI=sourceOntologyURI;
		useBK.targetIRI=targetOntologyURI;
		useBK.target=target;
		useBK.source=source;
		useBK.BkOntologiesCodes=buildBK.BkOntologiesCodes;
		useBK.BKuseWithEnrichment(builtBk);
	}
	
	
	
	public URL BkBasedMatching(boolean ElcioMappings) throws Exception
	{
		URL res=null;
		
		A_Matching_Ontologies m=new A_Matching_Ontologies();
		Fichier f=new Fichier("direct.rdf");
		f.deleteFile();
		m.matchOntologies(source, target, "direct.rdf");
		TreeSet<String> directMappings = Fichier.loadOAEIAlignmentWithoutOntologies("direct.rdf");
		
		
		BKbuilding buildBK=new BKbuilding();
		buildBK.sourceIRI=sourceOntologyURI;
		buildBK.source=source;
		Map<String, TreeSet<Noeud>> builtBk = buildBK.BuildBKobo("C:\\Users\\annane\\Documents\\OP\\obo.txt");
		System.out.println("la taille du BK selectionnée est de: "+builtBk.size() );
		
		BKuse useBK=new BKuse(source, target,true);
		useBK.sourceIRI=sourceOntologyURI;
		useBK.targetIRI=targetOntologyURI;
		useBK.target=target;
		useBK.source=source;
		useBK.BkOntologiesCodes=buildBK.BkOntologiesCodes;
		useBK.BKexploitation(builtBk, true);
		resultAlignment= selection(C.thresholdSelection,true);

		
		for (String mapping : directMappings) {
			StringTokenizer s=new StringTokenizer(mapping,",");
			String uri1=s.nextToken();
			String uri2=s.nextToken();
            if(!resultAlignment.contains(uri1+','+uri2+','+1.0))
            {
            	resultAlignment.add(mapping);
            }
		}
		
		String resFile=C.ResultFolderPath+"res.rdf";
		Fichier fichierResultat=new Fichier(resFile);
		fichierResultat.deleteFile();
		URL resIndirect = fichierResultat.ecrire(getOAEIalignmentFormat());
		
		return resIndirect;
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
		Map<String, TreeSet<Noeud>> builtBk = buildBK.BuildBK();
		System.out.println("la taille du BK selectionnée est de: "+builtBk.size() );
		
		BKuse useBK=new BKuse(source, target);
		useBK.sourceIRI=sourceOntologyURI;
		useBK.targetIRI=targetOntologyURI;
		useBK.target=target;
		useBK.source=source;
		useBK.BkOntologiesCodes=buildBK.BkOntologiesCodes;
		useBK.BKexploitation(builtBk);
		
		resultAlignment= selection(C.thresholdSelection);
		String resFile=C.ResultFolderPath+"res.rdf";
		Fichier fichierResultat=new Fichier(resFile);
		fichierResultat.deleteFile();
		res=fichierResultat.ecrire(getOAEIalignmentFormat());
		
		LogMapRepair r=new LogMapRepair();
		Set<MappingObjectStr> repairedMappings = r.useLogMapRepair(source.toString(), target.toString(), res.getPath());		
		TreeSet<String> finalAlignment=new TreeSet<String>();
		resultAlignment.clear();
		for (MappingObjectStr mappingObjectStr : repairedMappings) {
		resultAlignment.add(mappingObjectStr.getIRIStrEnt1()+','+mappingObjectStr.getIRIStrEnt2()+','+mappingObjectStr.getConfidence());
		}
		
		resFile=C.ResultFolderPath+"res2.rdf";
		fichierResultat=new Fichier(resFile);
		fichierResultat.deleteFile();
		res=fichierResultat.ecrire(getOAEIalignmentFormat());
		
		
		/*resultAlignment=semanticVerification(source.toString(), target.toString(),fichierResultat.path);	
		resFile=C.ResultFolderPath+"resultat.rdf";
		fichierResultat=new Fichier(resFile);
		fichierResultat.deleteFile();
		res=fichierResultat.ecrire(getOAEIalignmentFormat());*/
		return res;
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
					System.out.println("Précision: " + df.format(precision));
					System.out.println("Rappel: " + df.format(recall));
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

	/**
	 * Cette fonction sélectionne les mapings finaux à partir de l'ensemble des mappings candidats
	 * @param threshold: la valeur minmale accepté pour un mapping candidat
	 * @return la liste des mappings sélectionnés sous fomre d'un TreeSet
	 * @throws NumberFormatException les scores sont parsés ont Double ce qui peut générer cette exception
	 * @throws IOException l'ensemble des mappings candidats sont dans un fichier path, l'ouverture de ce fichier peut générer cette exception
	 */
	public static TreeSet<String> selection(double threshold) throws NumberFormatException, IOException
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
			String uri1 = (String) lineParser.nextElement();
			String uri2 = (String) lineParser.nextElement();
			int pathLength=Integer.parseInt((String) lineParser.nextElement())-1;
			String path=(String) lineParser.nextElement();
			//String res=(String) lineParser.nextElement();
			//parser le chemin
			 lineParser = new StringTokenizer(path, "$$");
			 lineParser.nextToken();
			 Double avgScore=0.0, MultScore=1.0;
			 double score=0;
			 boolean subclass=false;
			 while(lineParser.hasMoreTokens())
			 {
				 String m=lineParser.nextToken();
				 if(!m.equals(""))
				 {
				    StringTokenizer details = new StringTokenizer(m, C.separator);
					score=Double.parseDouble(details.nextToken());
					if(score==2.0)score=1.0;
					if(score==3.0){score=1.0;subclass=true;}
				 }
				 avgScore=(avgScore+score)/2;
				 MultScore=MultScore*score;
			 }
			 Map<String,MappingCandidate> liste;
			if (!candidats.keySet().contains(uri1))
			{
				liste =new HashMap<>();
				liste.put(uri2,new MappingCandidate(1,pathLength,avgScore,MultScore,null));
				candidats.put(uri1, liste);
			}
			else
			{
				liste=candidats.get(uri1);
				if(!liste.keySet().contains(uri2))
				{
					liste.put(uri2,new MappingCandidate(1,pathLength,avgScore,MultScore,null));
					candidats.put(uri1, liste);
				}
				else
				{
					MappingCandidate m=liste.get(uri2);
					liste.get(uri2).pathNumber++;
					if(liste.get(uri2).MaxAvg<avgScore)liste.get(uri2).MaxAvg=avgScore;
					if(liste.get(uri2).MaxMult<MultScore)liste.get(uri2).MaxMult=MultScore;
					if(liste.get(uri2).minPathLength>pathLength)liste.get(uri2).minPathLength=pathLength;
				}
			}
	}//endWhile
		//System.out.println(candidats.size());

		for (String uri1 : candidats.keySet()) {
			Map<String, MappingCandidate> liste = candidats.get(uri1);
		    double maxMultCandidatScore=0.0;
		    boolean stop =false;
		    MappingCandidate maxCandidate=null;
		    String uriCandidate=null;
			for (String uri2 : liste.keySet())
			{	
				MappingCandidate c = liste.get(uri2);
				if(c.MaxMult>=threshold)
				{
					if(c.minPathLength==1 && c.pathNumber>1)
					{
						finalMappings.add(uri1+','+uri2+','+c.MaxMult);
						stop=true;
					}
					if(c.MaxAvg>=1.0) 
					{
						finalMappings.add(uri1+','+uri2+','+c.MaxMult); 
						stop=true;
					}
					if(c.MaxMult>maxMultCandidatScore)
					{
						maxMultCandidatScore=c.MaxMult;
						maxCandidate=c;
						uriCandidate=uri2;
					}
				}
			}
			if(maxMultCandidatScore>=threshold)
			{
				if(!stop)
				{
				finalMappings.add(uri1+','+uriCandidate+','+maxMultCandidatScore);	
				}
			}
			
		}
		TreeSet<String> a = selection2(C.derivedCheminsPath, threshold);
		for (String m : a) {
			StringTokenizer lineParser = new StringTokenizer(m, ",");
			String uri2=lineParser.nextToken();
			String uri1=lineParser.nextToken();
			String score=lineParser.nextToken();
			//String res=lineParser.nextToken();
			finalMappings.add(uri1+','+uri2+','+score);	
		}
		long time=System.currentTimeMillis()-debut;
		C.executionTime.add("selection "+(time)+"ms");}
		else System.out.println("The derivation result is empty");
		return finalMappings;
	}
//_______________________________________________________________________________________
	/* *****************************************SELECTION2**************************************************** */
	public static TreeSet<String> selection2(String chemins,double threshold) throws NumberFormatException, IOException
	{
		long debut =System.currentTimeMillis();
		BufferedReader reader = new BufferedReader(new FileReader(chemins)); 
		String line = null; 
		String value = null; 
	    int cpt=0;
	    Map<String,Map<String,MappingCandidate> > candidats=new HashMap<>();
		while ((line = reader.readLine()) != null) 
		{
			StringTokenizer lineParser = new StringTokenizer(line, ",");
			String uri2 = (String) lineParser.nextElement();
			String uri1 = (String) lineParser.nextElement();
			int pathLength=Integer.parseInt((String) lineParser.nextElement())-1;
			String path=(String) lineParser.nextElement();
			//String res=(String) lineParser.nextElement();
			//parser le chemin
			 lineParser = new StringTokenizer(path, "$$");
			 lineParser.nextToken();
			 Double avgScore=0.0, MultScore=1.0;
			 double score=0;
			 while(lineParser.hasMoreTokens())
			 {
				 String m=lineParser.nextToken();
				 if(!m.equals(""))
				 {
				    StringTokenizer details = new StringTokenizer(m, C.separator);
					score=Double.parseDouble(details.nextToken());
					if(score==2.0)score=1.0;
				 }
				 avgScore=(avgScore+score)/2;
				 MultScore=MultScore*score;
			 }
			 Map<String,MappingCandidate> liste;
			if (!candidats.keySet().contains(uri1))
			{
				liste =new HashMap<>();
				liste.put(uri2,new MappingCandidate(1,pathLength,avgScore,MultScore,null));
				candidats.put(uri1, liste);
			}
			else
			{
				liste=candidats.get(uri1);
				if(!liste.keySet().contains(uri2))
				{
					liste.put(uri2,new MappingCandidate(1,pathLength,avgScore,MultScore,null));
					candidats.put(uri1, liste);
				}
				else
				{
					MappingCandidate m=liste.get(uri2);
					liste.get(uri2).pathNumber++;
					if(liste.get(uri2).MaxAvg<avgScore)liste.get(uri2).MaxAvg=avgScore;
					if(liste.get(uri2).MaxMult<MultScore)liste.get(uri2).MaxMult=MultScore;
					if(liste.get(uri2).minPathLength>pathLength)liste.get(uri2).minPathLength=pathLength;
				}
			}
	}//endWhile
		//System.out.println(candidats.size());
		TreeSet<String> finalMappings=new TreeSet<>();
		for (String uri1 : candidats.keySet()) {
			Map<String, MappingCandidate> liste = candidats.get(uri1);
		    double maxMultCandidatScore=0.0;
		    boolean stop =false;
		    MappingCandidate maxCandidate=null;
		    String uriCandidate=null;
			for (String uri2 : liste.keySet())
			{	
				MappingCandidate c = liste.get(uri2);
				if(c.MaxMult>=threshold)
				{
					if(c.minPathLength==1 && c.pathNumber>1){finalMappings.add(uri1+','+uri2+','+c.MaxMult);stop=true;}
					if(c.MaxAvg>=1.0) {finalMappings.add(uri1+','+uri2+','+c.MaxMult); stop=true;}
					if(c.MaxMult>maxMultCandidatScore)
					{
						maxMultCandidatScore=c.MaxMult;
						maxCandidate=c;
						uriCandidate=uri2;
					}
				}
			}
			if(maxMultCandidatScore>=threshold)
			{
				if(!stop)
				{
				finalMappings.add(uri1+','+uriCandidate+','+maxMultCandidatScore);	
				}
			}
			
		}
		long time=System.currentTimeMillis()-debut;
		C.executionTime.add("selection "+(time)+"ms");
		return finalMappings;
	}
//_________________________________________________________________________________	
	public URL matchOntologies()
	{
		URL res;
		if (source!=null && target!=null)
			{
			res=matcher.align(source, target);
			}
		else throw new NullPointerException("source or target ontology URL is null");
		return res;
	}
	
	public URL matchOntologies(String resultPath) throws URISyntaxException, IOException
	{ 
		File destFile;
		URL res;
		if (source!=null && target!=null)
			{
			res=matcher.align(source, target);
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
		else throw new IOException("ERROR: the path of the source ontology is not correct");
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
		else throw new IOException("ERROR: the path of the target ontology is not correct");
	}
}
