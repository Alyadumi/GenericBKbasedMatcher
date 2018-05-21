package LIRMM.FADO.annane.BKbasedMatching;

import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.ox.krr.logmap2.LogMap2_RepairFacility;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;
import uk.ac.ox.krr.logmap2.oaei.reader.MappingsReaderManager;

public class LogMapRepair {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	OWLOntology onto1;
	OWLOntology onto2;

	OWLOntologyManager onto_manager;
		
	
	
	public Set<MappingObjectStr> useLogMapRepair(String onto1_iri,String onto2_iri,String input_mappings_file){
		Set<MappingObjectStr> repaired_mappings=null ;
		Set<MappingObjectStr> input_mappings;
		
		try{
			onto_manager = OWLManager.createOWLOntologyManager();
			
			onto1 = onto_manager.loadOntology(IRI.create(onto1_iri));
			onto2 = onto_manager.loadOntology(IRI.create(onto2_iri));
			
			
			//Input from a file (RDF OAEI Alignment format)
			//Mappings are also accepted as TXT or OWL with an special format
			MappingsReaderManager readermanager = new MappingsReaderManager(input_mappings_file, "RDF");
			input_mappings = readermanager.getMappingObjects();
						
			LogMap2_RepairFacility logmap2_repair = 
					new LogMap2_RepairFacility(
							onto1,				//Ontology1 
							onto2,				//Ontology2
							input_mappings,		//Input Mappings
							false,				//If the intersection or overlapping of the ontologies are extracted before the repair
							true);				//If the repair is performed in a two steps process (optimal) or in one cleaning step (more aggressive)
			
			
						
			//Set of mappings repaired by LogMap
			repaired_mappings = logmap2_repair.getCleanMappings();
			
			System.out.println("Num repaired mappings using LogMap: " + repaired_mappings.size());
			
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return repaired_mappings ;
		
	}

}
