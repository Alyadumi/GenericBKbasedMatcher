package LIRMM.FADO.annane.BKbasedMatching;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeSet;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.parser.AlignmentParser;







public class Fichier {
	
	
	
	 static String retourAlaLigne="\r\n";
	 public String path;
	 public Fichier(String path){
		 this.path=path;
	 }
	 
	 
	 
	 
		//*********************************************************************************************
		public static void main(String[] args) throws Exception {
			loadOAEIAlignment("C:\\Users\\annane\\workspace\\VLSOM\\res.rdf");
			//loadReferenceAlignment("C:\\Users\\annane\\Google Drive\\.1.Extension\\OAEI2016\\LargeBio\\with repairs\\oaei_FMA2NCI_UMLS_mappings_with_flagged_repairs.rdf");
		}
		
		 public static String getUriCode(String uri) throws Exception
		   {
			   String code=null;
			   if(uri==null)throw new Exception("uri donn� � la fonction getUriCode est null");
			   else
			   { 
				   if(uri.contains("#"))code=uri.substring(uri.indexOf("#")+1);
				   else if(uri.contains("/")) code =uri.substring(uri.lastIndexOf("/")+1);
				   else code=uri;
			    }			   
			   return code.replace(",", "_");
		   }
		 /**
		  * pour eviter les prb avec les id customise de l'OAEI
		  * @param uri
		  * @param needInterface
		  * @return
		  * @throws Exception
		  */
		 public static String getUriCode(String uri, boolean needInterface) throws Exception
		   {
			   String code=null;
			   if(uri==null)throw new Exception("uri donnee la fonction getUriCode est null");
			   else
			   { 
				   if(uri.contains("#"))code=uri.substring(uri.indexOf("#")+1);
				   else if(uri.contains("/")) code =uri.substring(uri.lastIndexOf("/")+1);
				   else code=uri;
			    }
			   
			   if(code.contains("_") && !needInterface)
			   {
				   code=code.substring(code.indexOf("_")+1);
			   }
			   
			   return code.replace(",", "_");
		   }
	 
	 
		/* **************** delete files ********************* */
		public  void deleteFile()
		{
			File file=new File(path);
			if(file.exists())
			{
				if(file.isDirectory())
				{
					String[] Children = file.list();
					for (String fileName : Children) 
					{
						File f=new File(path+File.separator+fileName);
						f.delete();
					}
				}
				else if(file.isFile())
				{
					file.delete();
				}
			}
		}
		//************************************************************
		public static void deleteFile(String path)
		{
			File file=new File(path);
			if(file.exists())
			{
				if(file.isDirectory())
				{
					String[] Children = file.list();
					for (String fileName : Children) 
					{
						File f=new File(path+File.separator+fileName);
						if(f.isDirectory()) deleteFile(path+File.separator+fileName);
					    f.delete();
					}
				}
				else if(file.isFile())
				{
					file.delete();
				}
			}
		}
	 
//*******************************************************************************************	 
		public static TreeSet<String> loadOAEIAlignment(String path) throws Exception
		{
			  TreeSet<String> m =new TreeSet<>();
			  AlignmentParser aparser = new AlignmentParser(0);
			  Alignment al = aparser.parse( new File( path ).toURI() );
			  String o1=al.getOntology1URI().toString();
			  String o2=al.getOntology2URI().toString();
		      for (Cell cell : al) 
		      {
		    	  String uri_source = cell.getObject1AsURI().toString();
		    	  String uri_target = cell.getObject2AsURI().toString();
		    	  double score = cell.getStrength();
				  m.add(uri_source+','+o1+','+uri_target+','+o2+','+score);
				//  System.out.println(uri_source+','+o1+','+uri_target+','+o2+','+score);
			}
			return m;
	  }
		
		//*******************************************************************************************	 
				public static TreeSet<String> loadOAEIAlignmentWithoutOntologies(String path) throws Exception
				{
					  TreeSet<String> m =new TreeSet<>();
					  AlignmentParser aparser = new AlignmentParser(0);
					  Alignment al = aparser.parse( new File( path ).toURI() );
					  String o1=al.getOntology1URI().toString();
					  String o2=al.getOntology2URI().toString();
				      for (Cell cell : al) 
				      {
				    	  String uri_source = cell.getObject1AsURI().toString();
				    	  String uri_target = cell.getObject2AsURI().toString();
				    	  double score = cell.getStrength();
						  m.add(uri_source+','+uri_target+','+score);
						//  System.out.println(uri_source+','+o1+','+uri_target+','+o2+','+score);
					}
					return m;
			  }
		//*******************************************************************************************	 
				public static TreeSet<String> loadOAEIAlignment(URL path) throws Exception
				{
					  TreeSet<String> m =new TreeSet<>();
					  AlignmentParser aparser = new AlignmentParser(0);
					  Alignment al = aparser.parse( new File( path.toURI() ).toURI() );
				      for (Cell cell : al) 
				      {
				    	  String uri_source = cell.getObject1AsURI().toString();
				    	  String uri_target = cell.getObject2AsURI().toString();
				    	  double score = cell.getStrength();
						  m.add(uri_source+','+uri_target+','+score);
						//  System.out.println(uri_source+','+uri_target+','+score);
					}
					return m;
			  }
		//*******************************************************************************************	 
		public static TreeSet<String> loadOAEIAlignment(String path,String acrS, String acrT) throws Exception
		{
			  TreeSet<String> m =new TreeSet<>();
			  AlignmentParser aparser = new AlignmentParser(0);
			  Alignment al = aparser.parse( new File( path ).toURI() );
		      for (Cell cell : al) 
		      {
		    	  String uri_source = cell.getObject1AsURI().toString();
		    	  String uri_target = cell.getObject2AsURI().toString();
		    	  double score = cell.getStrength();
				  m.add(uri_source+','+acrS+','+uri_target+','+acrT+','+score);
			}
			return m;
	  }

	//**************************************************neutre*******************
		public static TreeSet<String> loadReferenceAlignmentNeutre(String referencePath) throws AlignmentException
		{

			 TreeSet<String> m =new TreeSet<>();
			  AlignmentParser aparser = new AlignmentParser(0);
			  Alignment al = aparser.parse( new File( referencePath ).toURI() );
		      for (Cell cell : al) 
		      {
		    	  String uri_source = cell.getObject1AsURI().toString();
		    	  String uri_target = cell.getObject2AsURI().toString();
		    	  String relation = cell.getRelation().getRelation();
                  if(relation.equals("?"))
                  { 
                	  m.add(uri_source+','+uri_target);
                  }

			}
		
			return m;
		}
	// ***************************************charger l'alignement de r�f�rence*******************
			public static TreeSet<String> loadReferenceAlignment(String referencePath) throws AlignmentException
			{
				TreeSet<String> m =new TreeSet<>();
				  AlignmentParser aparser = new AlignmentParser(0);
				  Alignment al = aparser.parse( new File( referencePath ).toURI() );
			      for (Cell cell : al) 
			      {
			    	  String uri_source = cell.getObject1AsURI().toString();
			    	  String uri_target = cell.getObject2AsURI().toString();
			    	  String relation = cell.getRelation().getRelation();
	                  if(relation.equals("="))
	                  { 
	                	  m.add(uri_source+','+uri_target);
	                  }

				}
			
				return m;
			}
	 
	 //***********************************************************************************************
		public static String treeToString(TreeSet t)
		{
			return t.toString().replaceAll(", ", "\r\n").replace("[", "").replace("]", "").replace(';', ',');
		}
	//***************************************************************************************
		public static TreeSet<String> parseMappings(String path) throws FileNotFoundException, IOException { 
			File csvFile = new File(path); 
			TreeSet<String> mappings=new TreeSet<>();
			String id,ontology,uri;
			if (!csvFile.exists()) 
			throw new FileNotFoundException("Le fichier n'existe pas"); 
			StringTokenizer lineParser; 
			BufferedReader reader = new BufferedReader(new FileReader(csvFile)); 
			String line = null; 
			String value = null; 
			String mapping="";
			while ((line = reader.readLine()) != null)
			{ 
			 
				lineParser = new StringTokenizer(line, ","); 
			//	System.out.println(lineParser.countTokens());
			
				{ 
					ontology= (String) lineParser.nextElement(); //uri1
					id= (String) lineParser.nextElement(); 

					{
					mapping=ontology+','+id;
					ontology = (String) lineParser.nextElement(); 

					{
					id= (String) lineParser.nextElement(); 
					mapping=mapping+","+ontology+','+id;

				
					while(lineParser.hasMoreElements())
					{
						mapping=mapping+','+(String)lineParser.nextElement();

					}
					mappings.add(mapping);
					}
					}
					mapping="";
				} 

			} 
			return mappings;
			} 
		//****************************************************************************
			public static TreeSet<String> parseConcepts(String path) throws FileNotFoundException, IOException { 
				File csvFile = new File(path); 
				TreeSet<String> concepts=new TreeSet<>();
				String id,ontology,uri;
				if (!csvFile.exists()) 
				throw new FileNotFoundException("Le fichier n'existe pas"); 
				StringTokenizer lineParser; 
				BufferedReader reader = new BufferedReader(new FileReader(csvFile)); 
				String line = null; 
				String value = null; 
				try{
				while ((line = reader.readLine()) != null)
				{ 
					lineParser = new StringTokenizer(line, ","); 
				
					{ 
						ontology = (String) lineParser.nextElement(); //ontology
						id=(String) lineParser.nextElement();//id
						 concepts.add(id+","+ontology);
						
						ontology = (String) lineParser.nextElement(); //uri2 
						id=(String) lineParser.nextElement();
						concepts.add(id+","+ontology);
					} 

				} }
				catch( java.util.NoSuchElementException e)
				{
					System.out.println(path);
				}
				return concepts;
				} 
//*****************************************************************************************************************
			public static TreeSet<String> parseConceptsFromYAMFiles(String path, String sourceAcronym, String targetAcronym) throws Exception { 
				TreeSet<String> concepts=new TreeSet<>();
				TreeSet<String> yamMappings=loadOAEIAlignment(path);
				for (String mapping : yamMappings) 
				{
					String idS=mapping.substring(0,mapping.indexOf(','))
						  ,idT=mapping.substring(mapping.indexOf(',')+1, mapping.lastIndexOf(','));
					concepts.add(idS+','+sourceAcronym);
					concepts.add(idT+','+targetAcronym);
				}
				return concepts;
				} 
//****************************************************************************************************************************************************
			public static TreeSet<String> parseMappingsFromYAMFiles(String path, String sourceAcronym, String targetAcronym) throws Exception { 
				TreeSet<String> mappings=new TreeSet<>();
				TreeSet<String> yamMappings=loadOAEIAlignment(path);
				for (String mapping : yamMappings) 
				{
					String idS=mapping.substring(0,mapping.indexOf(','))
						  ,idT=mapping.substring(mapping.indexOf(',')+1, mapping.lastIndexOf(','))
						  ,score=mapping.substring(mapping.lastIndexOf(',')+1);
					if(score.equalsIgnoreCase("0"))throw new Exception("un score de zero a ete detecte: "+idS+" "+idT);
					mappings.add(idS+','+sourceAcronym+','+idT+','+targetAcronym+','+score);
				}
				return mappings;
				} 
////********************************************************************************************************************************
//			public static void parseMappingsFromOAEIalignmentsFolder(String folderPathSource,String folderPathDestination) throws Exception { 
//				
//				  File repertoire1 = new File(folderPathSource); 
//				  String[] children = repertoire1.list(); 
//		
//				for(int i=0;i<children.length;i++)
//				  {
//			
//					String path=folderPathSource+children[i];
//					TreeSet<String> mappings=new TreeSet<>();
//					TreeSet<String> yamMappings=loadOAEIAlignment(path);
//					String sourceAcronym,targetAcronym;
//					System.out.println(children[i]);;
//					sourceAcronym=children[i].substring(0, children[i].indexOf('-'));
//					targetAcronym=children[i].substring(children[i].indexOf('-')+1, children[i].indexOf('.'));
//					for (String mapping : yamMappings) 
//					{
//						String idS=mapping.substring(0,mapping.indexOf(','))
//							  ,idT=mapping.substring(mapping.indexOf(',')+1, mapping.lastIndexOf(','))
//							  ,score=mapping.substring(mapping.lastIndexOf(',')+1);
//						if(score.equalsIgnoreCase("0."))
//						{
//							System.out.println(mapping);
//							System.out.println(score);
//						}
//						mappings.add(idS+','+sourceAcronym+','+idT+','+targetAcronym+','+score);
//					}
//					ecrire(treeToString(mappings));
//				  }
//				} 
//****************************************************************************************************************************************************
			/**
			 * This function allows to extract mappings from alignments produced by YAM++   
			 * @param FolderPath: the folder that contains the YAM++ alignments
			 * @param conceptsFileName : The name of the file that will contains the different concepts concerned by the alignments
			 * @param mappingsFileName :The name of the file that will contains all the mappings extracted from the alignments
			 * @throws Exception 
			 */
			public  void fileForNeo4jFromYamFiles(String FolderPath, String conceptsFileName,String mappingsFileName) throws Exception
			   {
				   		  FilenameFilter javaFilter = new FilenameFilter() { 
						  public boolean accept(File arg0, String arg1) { 
						  return arg1.endsWith(".rdf"); 
						  } 
						  }; 

						  File repertoire = new File(FolderPath); 
						  String[] children = repertoire.list(javaFilter); 
						  TreeSet<String> allConcepts=new TreeSet<>();
						  TreeSet<String> allMappings=new TreeSet<>();
						for(int i=0;i<children.length;i++)
						  { 
							System.out.println(children[i]);
							String file=children[i];
							String acroS=file.substring(0,file.indexOf('_')).toUpperCase();
							String acroT=file.substring(file.indexOf('_')+1,file.indexOf('.')).toUpperCase();
							allConcepts.addAll(parseConceptsFromYAMFiles(FolderPath+children[i],acroS,acroT));
							allMappings.addAll(parseMappingsFromYAMFiles(FolderPath+children[i],acroS,acroT));

						  } 
						 ecrire("id,ontology\r\n"+treeToString(allConcepts));
						 ecrire(  "id1,o1,id2,o2,a\r\n"+treeToString(allMappings));
			   }

//****************************************************************************************************************************************************
	   public void fileForNeo4j(String FolderPath, String conceptsFileName,String mappingsFileName) throws FileNotFoundException, IOException
	   {
		   		  FilenameFilter javaFilter = new FilenameFilter() { 
				  public boolean accept(File arg0, String arg1) { 
				  return arg1.endsWith(".csv"); 
				  } 
				  }; 

				  File repertoire = new File(FolderPath); 
				  String[] children = repertoire.list(javaFilter); 
				  TreeSet<String> allConcepts=new TreeSet<>();
				  TreeSet<String> allMappings=new TreeSet<>();
				for(int i=0;i<children.length;i++)
				  { 
					System.out.println(children[i]+ "  "+i);
					allConcepts.addAll(parseConcepts(FolderPath+children[i]));
					allMappings.addAll(parseMappings(FolderPath+children[i]));

				  } 
				 ecrire( "id,ontology\r\n"+treeToString(allConcepts));
				 ecrire(  "o1,id1,o2,id2,a,b,c,d\r\n"+treeToString(allMappings));
	   }

//****************************************************************************	
	public  URL ecrire ( String text){
	try
			{
		        PrintWriter ecrir ;
				ecrir = new PrintWriter(new FileWriter(path,true));
				ecrir.print(text);
				ecrir.flush();
				ecrir.close();
				return new File(path).toURI().toURL();
			}//try
			catch (IOException a)
			{
				
				a.printStackTrace();;
			}
	return null;
		}//ecrire
//***********************************************************************
	public  void ecrire ( String chemin, String text)
	{
      path=chemin;
      ecrire(text);
	}//ecrire

	}