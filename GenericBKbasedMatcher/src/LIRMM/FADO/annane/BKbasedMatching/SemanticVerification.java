package LIRMM.FADO.annane.BKbasedMatching;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import de.unima.alcomox.ExtractionProblem;
import de.unima.alcomox.Settings;
import de.unima.alcomox.exceptions.AlcomoException;
import de.unima.alcomox.mapping.Correspondence;
import de.unima.alcomox.mapping.Mapping;
import de.unima.alcomox.ontology.IOntology;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;


public class SemanticVerification {
	
	public static void main(String[] args) throws Exception {
	SemanticVerification s = new SemanticVerification();
	String alignment_path = "Test/semanticVerification/res.rdf";
	//String repaired_alignment_path =  "Test/semanticVerification/repaired_alignment.rdf";
	//Fichier repaired_alignment_file = new Fichier(repaired_alignment_path);
	
	String sourcePath="Test\\OAEI_tracks\\Anatomy\\MA.owl";
	String targetPath="Test\\OAEI_tracks\\Anatomy\\NCI.owl";
	File sourceOntologyFile=new File(sourcePath);//source ontology
	File targetOntologyFile=new File(targetPath);//target ontology
	Parameters.sourceOntology=sourceOntologyFile.toURI().toURL();
	Parameters.targetOntology=targetOntologyFile.toURI().toURL();
	TreeSet<String> repaired_alignment = s.semanticVerification(sourcePath, targetPath, alignment_path);
	}
	
	
	
	/**
	 * Semantic verification with ALcomo based on Hermit reasoner
	 * This code has been extracted from LogMapRepair
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
     
		
	/**
	 * LogMapRepair for semantic verification
	 */
	
	public TreeSet<String> LogMapRepair_SemanticVerification(String not_repaired_alignment_path)
	{
		    TreeSet<String> repaired_alignment = new TreeSet<>();
			LogMapRepair r=new LogMapRepair();
			Set<MappingObjectStr> repairedMappings = r.useLogMapRepair(Parameters.sourceOntology.toString(), Parameters.targetOntology.toString(), not_repaired_alignment_path);		
			
			for (MappingObjectStr mappingObjectStr : repairedMappings) 
			{
				repaired_alignment.add(mappingObjectStr.getIRIStrEnt1()+','+mappingObjectStr.getIRIStrEnt2()+','+mappingObjectStr.getConfidence());
			}
			return repaired_alignment;		
	}

}
