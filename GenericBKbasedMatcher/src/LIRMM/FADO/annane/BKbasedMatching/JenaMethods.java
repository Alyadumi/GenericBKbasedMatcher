package LIRMM.FADO.annane.BKbasedMatching;


import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.hp.hpl.jena.vocabulary.OWL;


public class JenaMethods 
{
	public static QueryExecution qexec;
	
	public static void main(String[] args) throws Exception
	{
		File f=new File(C.ordo);
		System.out.println(getOntologyUri(f.toURI().toURL()));

		//retiredConcepts();
	}
	
	
	public static String getOntologyUri(Model model)
	{
		model.getResource(OWL.Ontology.getURI().toString());
		Map<String, String> prefixMap=model.getNsPrefixMap();
		String URI=prefixMap.get("");
		int length=URI.length();
		String c=URI.substring(length-1,length);
		if(c.equals("#") || c.equals("/"))
		{
			URI=URI.substring(0,length-1);
		}
		length=URI.length();
		c=URI.substring(length-4,length);
		/*if(c.equals(".owl"))
		{
			URI=URI.substring(0,length-4);
		}*/
		
		return URI;
	}
	
	public static TreeSet<String> loadOntologyElementsForSelection(Model ontology,String sourceIRI) throws Exception
	{
		TreeSet<String> ontologyUriCode = new TreeSet<String>();
		ResultSet res=ExecuteQuery(C.prefix+"select ?x where {?x a owl:Class}", ontology);
		String uri;
		while (res.hasNext()) 
		{
            uri=res.next().get("x").toString();

            if(uri.toLowerCase().contains(sourceIRI.toLowerCase()))
            	{
            		ontologyUriCode.add(sourceIRI+C.separator+uri);
            	}
		}		
		return ontologyUriCode;
	}
	

	
	public static TreeSet<String> loadOntologyCodes(Model ontology,String sourceIRI, boolean obo) throws Exception
	{
		TreeSet<String> ontologyUriCode = new TreeSet<String>();
		ResultSet res=ExecuteQuery(C.prefix+"select ?x where {?x a owl:Class}", ontology);
		String uri;
		while (res.hasNext()) 
		{
            uri=res.next().get("x").toString();
            String code=Fichier.getUriCode(uri);
            if(BKbuilding.codeInterface.containsKey(sourceIRI+C.separator+code))
            	{
            	String ontologyCode=BKbuilding.codeInterface.get(sourceIRI+C.separator+code);
            	StringTokenizer s=new StringTokenizer(ontologyCode,C.separator);
            	s.nextToken();
            	ontologyUriCode.add(s.nextToken());
            	}

		}		
		return ontologyUriCode;
	}
	/**
	 * This function is called when we select the subgraph with a set of uris instead of the complete ontology source
	 * @param Uris
	 * @param sourceIRI
	 * @return
	 * @throws Exception
	 */
	public static TreeSet<String> getFirstSelectedConcepts(TreeSet<String> Uris,String sourceIRI) throws Exception
	{
		TreeSet<String> ontologyUriCode = new TreeSet<String>();
		for (String uri : Uris) 
		{
       		ontologyUriCode.add(sourceIRI+C.separator+uri);  	
		}		
		return ontologyUriCode;
	}
/* ************************************************************* */	
	public static ArrayList<String> retiredConcepts()
	{
		ArrayList<String> a = new ArrayList<String>();
		Model nci=LoadOntologyModelWithJena(C.t2_nci);
		String query=C.prefix+"select distinct ?x where {?x  rdfs:subClassOf <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Retired_Concepts>}";
		ResultSet res = ExecuteQuery(query, nci);
		while(res.hasNext())
		{
			QuerySolution sol = res.next();
			String uri=sol.get("x").toString();
			String id=uri.substring(uri.indexOf("#")+1);
			a.add(id);
			//System.out.println(id);
			
		}
		return a;
	}
	
	/* ************************************************************** */
	
	public static ResultSet ExecuteQuery(String queryString, Model ontology) 
	{
		//System.out.println(queryString);
		Query query = QueryFactory.create(queryString);

		qexec = QueryExecutionFactory.create(query, ontology);
		ResultSet results = null;
		try {
			results = qexec.execSelect();

		} catch (Exception e) {
			e.printStackTrace();

		}

		return results;
	}
//***********************************************************************************************************
	public static String getOntologyUri(URL path) throws URISyntaxException
	{
		//search for the ontology declaration in XML format
			   if(path==null){
				   throw new NullPointerException("The path given to the getOntologyIRI function is null");}
			   	  String ontologyIri=null;
			   try{
				   	  File fXmlFile = new File(path.toURI());
			    	  DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			    	  DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			    	  Document document = dBuilder.parse(fXmlFile);
			    	  NodeList ontology = document.getElementsByTagName("owl:Ontology");
			    	  Element o=(Element)ontology.item(0);
			    	  if(o!=null){
				    	  ontologyIri=o.getAttribute("rdf:about");
			    	  }
			    	  else
			    	  {
			    		  NodeList node = document.getElementsByTagName("rdf:RDF");
				    	  Element e =(Element)node.item(0);
				    	  ontologyIri=e.getAttribute("xmlns");  
			    	  }
			   }

			   catch(Exception e)
			   {
						// TODO: handle exception
						   Model model =JenaMethods.LoadOntologyModelWithJena(path);
						  	model.getResource(OWL.Ontology.getURI().toString());
							Map<String, String> prefixMap=model.getNsPrefixMap();
							ontologyIri=prefixMap.get("");
							model.close();
				   
				

			   }

			   	if(ontologyIri!=null)
				{
				int length=ontologyIri.length();
				String c=ontologyIri.substring(length-1,length);
				if(c.equals("#") || c.equals("/"))
				{
					ontologyIri=ontologyIri.substring(0,length-1);
				}}
			   return ontologyIri;
	}
// ************************************************************************************************************
	public static Model LoadOntologyModelWithJena(URL owlFile) throws URISyntaxException 
	{
		Model model = ModelFactory.createDefaultModel();
		model.read(owlFile.toURI().toString());
		return model;
	}
// *******************************************************************************
	public static Model LoadOntologyModelWithJena(String owlFile) 
	{
		Model model = ModelFactory.createDefaultModel();
		model.read(owlFile);
		return model;
	}

//************************************************************************************
	public static TreeSet<String> loadOntologyUri(Model ontology)
	{
		TreeSet<String> ontologyUri = new TreeSet<String>();
		ResultSet res=ExecuteQuery(C.prefix+"select ?x where {?x a owl:Class}", ontology);
		String uri;
		while (res.hasNext()) 
		{
            uri=res.next().get("x").toString();
            if(uri.contains("http"))
            	{
            	ontologyUri.add(uri);
            	}
		}		
		return ontologyUri;
	}
	// *******************************************Extract relevant concepts*******************************************
	public static TreeSet<String> ExtractRelevantUri(TreeSet<String> list, Model ontology) {
		TreeSet<String> tree = new TreeSet<String>();

		for (String uri : list) {

			String query = C.prefix
					+ "select distinct ?y  where" + "{{ ?y rdfs:subClassOf <" + uri + "> . } " + // children-
					"UNION { ?y rdfs:subClassOf ?z . ?z  rdfs:subClassOf <" + uri + "> .}" + // children
																								// of
																								// children
					"UNION { <" + uri + "> rdfs:subClassOf ?y . }" + // parents
					"UNION { <" + uri + "> rdfs:subClassOf ?z . ?z  rdfs:subClassOf ?y . }" + // parent
																								// des
																								// parents
					"UNION { <" + uri + "> rdfs:subClassOf ?z . ?z  rdfs:subClassOf ?m . ?y rdfs:subClassOf ?m . }" + // uncles
																														// children
																														// of
																														// grand
																														// parents
					"UNION { <" + uri + "> rdfs:subClassOf ?z .  ?y rdfs:subClassOf ?z .} " + // siblings
					"UNION { <" + uri + "> rdfs:subClassOf ?z .  ?m rdfs:subClassOf ?z . ?y rdfs:subClassOf ?m .  }} ";// nephews
			ResultSet results = ExecuteQuery(query, ontology);
			//System.out.println(results.toString());
			if (results != null && results.hasNext()) {
				while (results.hasNext()) {
					String ur = results.nextSolution().get("y").toString();

					if (ur.contains("http"))
						tree.add(ur);

				}
			}
		}
		return tree;
	}



}
