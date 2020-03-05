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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.jena.rdf.model.Model;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;

import eu.sealsproject.platform.res.tool.api.ToolBridgeException;
import eu.sealsproject.platform.res.tool.api.ToolException;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

public class Matching {
	
	String sourceOntologyURI;
	String targetOntologyURI;
	/**
	 * the matcher variable represents the direct matcher that will be used for anchoring. 
	 * It may be any direct matcher that implements an align function that takes as input the URL of the source and target ontologies and 
	 * returns the URL of the generated alignment. The generated alignment should be generated with the API alignment (the RDF format).
	 */
	
	TreeSet<String> resultAlignment;
	

	public Matching(URL source, URL target) throws URISyntaxException
	{
		sourceOntologyURI=JenaMethods.getOntologyUri(source);
		targetOntologyURI=JenaMethods.getOntologyUri(target);
	}
	
	public Matching(String source, String target) throws URISyntaxException, MalformedURLException
	{
		sourceOntologyURI=JenaMethods.getOntologyUri(Parameters.sourceOntology);
		targetOntologyURI=JenaMethods.getOntologyUri(Parameters.targetOntology);
	}
	
	public void BkBasedMatching(TreeSet<String> URIs) throws Exception
	{
		BKbuilding buildBK=new BKbuilding();
		buildBK.sourceIRI=sourceOntologyURI;
		Map<String, TreeSet<Mapping>> builtBk = buildBK.BuildEnrichedBK(URIs);		
		BKuse useBK=new BKuse(Parameters.sourceOntology, Parameters.targetOntology,URIs);
		useBK.sourceIRI=sourceOntologyURI;
		useBK.targetIRI=targetOntologyURI;
		useBK.BkOntologiesCodes=buildBK.BkOntologiesCodes;
		useBK.BKuseWithEnrichment(builtBk);
	}
	
	
	

	/**
	 * This function implements the whole BK based matching
	 * @return the URL of the file containing the resulted alignment
	 * @throws Exception
	 */
	public URL BkBasedMatching() throws Exception
	{
		URL res=null;
		BKbuilding buildBK=new BKbuilding();
		buildBK.sourceIRI=sourceOntologyURI;
		Map<String, TreeSet<Mapping>> builtBk = buildBK.BuildBK();
		System.out.println("la taille du BK selectionnée est de: "+builtBk.size() );
		
		BKuse useBK=new BKuse(Parameters.sourceOntology, Parameters.targetOntology);
		useBK.sourceIRI=sourceOntologyURI;
		useBK.targetIRI=targetOntologyURI;
		useBK.BkOntologiesCodes=buildBK.BkOntologiesCodes;
		useBK.BKexploitation(builtBk);
		
		resultAlignment= selection(Parameters.mappingSelectionThreshold, Parameters.derivedCheminsPath);
		String resFile=Parameters.ResultFolderPath+"res.rdf";
		Fichier fichierResultat=new Fichier(resFile);
		fichierResultat.deleteFile();
		res=fichierResultat.ecrire(getOAEIalignmentFormat());
		
		
		//Repairing the generated alignment with LogMapRepair module if Parameters.logMapRepair is true
		if(Parameters.logMapRepair)
		{
			LogMapRepair r=new LogMapRepair();
			Set<MappingObjectStr> repairedMappings = r.useLogMapRepair(Parameters.sourceOntology.toString(), Parameters.targetOntology.toString(), res.getPath());		
			resultAlignment.clear();
			for (MappingObjectStr mappingObjectStr : repairedMappings) 
			{
			resultAlignment.add(mappingObjectStr.getIRIStrEnt1()+','+mappingObjectStr.getIRIStrEnt2()+','+mappingObjectStr.getConfidence());
			}
			resFile=Parameters.ResultFolderPath+"res2.rdf";
			fichierResultat=new Fichier(resFile);
			fichierResultat.deleteFile();
			res=fichierResultat.ecrire(getOAEIalignmentFormat());
		}
		
		return res;
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

		return df.format(precision)+','+df.format(recall)+","+df.format(fscore);
	}
	




	/**
	 * Cette fonction sélectionne les mapings finaux à partir de l'ensemble des mappings candidats
	 * @param threshold: la valeur minmale accepté pour un mapping candidat
	 * @return la liste des mappings sélectionnés sous fomre d'un TreeSet
	 * @throws NumberFormatException les scores sont parsés ont Double ce qui peut générer cette exception
	 * @throws IOException l'ensemble des mappings candidats sont dans un fichier path, l'ouverture de ce fichier peut générer cette exception
	 */
	public static TreeSet<String> selection(double threshold, String derivedPaths) throws NumberFormatException, IOException
	{
		TreeSet<String> finalMappings=new TreeSet<>();
		long debut =System.currentTimeMillis();
		File chemins=new File(derivedPaths);
		if(chemins.exists())
		{
			BufferedReader reader = new BufferedReader(new FileReader(derivedPaths)); 
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
				Boolean subclass=false;
				while(lineParser.hasMoreTokens())
			 {
				 String m=lineParser.nextToken();
				 if(!m.equals(""))
				 {
				    StringTokenizer details = new StringTokenizer(m, Parameters.separator);
					score=Double.parseDouble(details.nextToken());
					if(score==2.0)score=1.0;
					if(score==3.0)
					{
						score=1.0;
						subclass=true;
					}
				 }
				 avgScore=(avgScore+score)/2;
				 MultScore=MultScore*score;
			 }
			 String relation;
			 if(subclass)relation ="subClassOf";
			 else relation = "=";
			 Map<String,MappingCandidate> liste;
			if (!candidats.keySet().contains(uri1))
			{
				liste =new HashMap<>();
				liste.put(uri2,new MappingCandidate(1,pathLength,avgScore,MultScore,relation));
				candidats.put(uri1, liste);
			}
			else
			{
				liste=candidats.get(uri1);
				if(!liste.keySet().contains(uri2))
				{
					liste.put(uri2,new MappingCandidate(1,pathLength,avgScore,MultScore,relation));
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
		    String uriCandidate=null, maxRelation=null;
			for (String uri2 : liste.keySet())
			{	
				MappingCandidate c = liste.get(uri2);
				if(c.MaxMult>=threshold)
				{
					if(c.minPathLength==1 && c.pathNumber>1)
					{
						finalMappings.add(uri1+','+uri2+','+c.MaxMult+','+c.relation);
						stop=true;
					}
					if(c.MaxAvg>=1.0) 
					{
						finalMappings.add(uri1+','+uri2+','+c.MaxMult+','+c.relation); 
						stop=true;
					}
					if(c.MaxMult>maxMultCandidatScore)
					{
						maxMultCandidatScore=c.MaxMult;
						maxCandidate=c;
						maxRelation=c.relation;
						uriCandidate=uri2;
					}
				}
			}
			if(maxMultCandidatScore>=threshold)
			{
				if(!stop)
				{
				finalMappings.add(uri1+','+uriCandidate+','+maxMultCandidatScore+','+maxRelation);	
				}
			}
			
		}
		TreeSet<String> a = selection2(derivedPaths, threshold);
		for (String m : a) {
			StringTokenizer lineParser = new StringTokenizer(m, ",");
			String uri2=lineParser.nextToken();
			String uri1=lineParser.nextToken();
			String score=lineParser.nextToken();
			String relation=lineParser.nextToken();
			finalMappings.add(uri1+','+uri2+','+score+','+relation);	
		}
      }
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
			 boolean subClass=false;
			 while(lineParser.hasMoreTokens())
			 {
				 String m=lineParser.nextToken();
				 if(!m.equals(""))
				 {
				    StringTokenizer details = new StringTokenizer(m, Parameters.separator);
					score=Double.parseDouble(details.nextToken());
					if(score==2.0)score=1.0;
					else if(score==3.0)
					{
						subClass=true;
						score=1.0;
						}
				 }
				 avgScore=(avgScore+score)/2;
				 MultScore=MultScore*score;
			 }
			 String relation;
			 if(subClass)relation="subClassOf";
			 else relation="=";
			 Map<String,MappingCandidate> liste;
			if (!candidats.keySet().contains(uri1))
			{
				liste =new HashMap<>();
				liste.put(uri2,new MappingCandidate(1,pathLength,avgScore,MultScore,relation));
				candidats.put(uri1, liste);
			}
			else
			{
				liste=candidats.get(uri1);
				if(!liste.keySet().contains(uri2))
				{
					liste.put(uri2,new MappingCandidate(1,pathLength,avgScore,MultScore,relation));
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
		    String maxRelation = null;
		    String uriCandidate=null;
			for (String uri2 : liste.keySet())
			{	
				MappingCandidate c = liste.get(uri2);
				if(c.MaxMult>=threshold)
				{
					if(c.minPathLength==1 && c.pathNumber>1)
					{
						finalMappings.add(uri1+','+uri2+','+c.MaxMult+','+c.relation);
						stop=true;
					}
					if(c.MaxAvg>=1.0) 
					{
						finalMappings.add(uri1+','+uri2+','+c.MaxMult+','+c.relation); 
						stop=true;
					}
					if(c.MaxMult>maxMultCandidatScore)
					{
						maxMultCandidatScore=c.MaxMult;
						maxCandidate=c;
						maxRelation=c.relation;
						uriCandidate=uri2;
					}
				}
			}
			if(maxMultCandidatScore>=threshold)
			{
				if(!stop)
				{
				finalMappings.add(uri1+','+uriCandidate+','+maxMultCandidatScore+','+maxRelation);	
				}
			}
			
		}

		return finalMappings;
	}
//_________________________________________________________________________________	
	public URL matchOntologies() throws ToolException, ToolBridgeException
	{
		URL res;
		if (Parameters.sourceOntology!=null && Parameters.targetOntology!=null)
			{
			res=Parameters.matcher.align(Parameters.sourceOntology, Parameters.targetOntology);
			}
		else throw new NullPointerException("source or target ontology URL is null");
		return res;
	}
	
	public URL matchOntologies(URL source, URL target,String resultPath) throws URISyntaxException, IOException, ToolException, ToolBridgeException
	{ 
		File destFile;
		URL res;
		if (source!=null && target!=null)
			{
			res=Parameters.matcher.align(source,target);
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
	      Model o=JenaMethods.LoadOntologyModelWithJena(Parameters.sourceOntology);
	      sourceOntologyURI=JenaMethods.getOntologyUri(o);
	      o.close();
	      o=JenaMethods.LoadOntologyModelWithJena(Parameters.targetOntology);
	      targetOntologyURI=JenaMethods.getOntologyUri(o);
	      o.close();
	      alignments.init(URI.create(sourceOntologyURI), URI.create(targetOntologyURI));
	      alignments.setFile1(Parameters.sourceOntology.toURI());
	      alignments.setFile2(Parameters.targetOntology.toURI());

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
	          String relation = lineParser.nextToken();

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

}