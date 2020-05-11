package LIRMM.FADO.annane.BKbasedMatching;

public class Mapping {
	
	String source_concept_uri;
	String source_ontology_uri;
	String target_concept_uri;
	String target_ontology_uri;
	String relation_symbole;
	String matcher;
	Path path;
	boolean manual_mapping = false;
	
	double score = 0.0;
	
	public Mapping(String source_concept_uri, String target_concept_uri, double score )
	{
		this.source_concept_uri = source_concept_uri;
		this.target_concept_uri = target_concept_uri;
		this.score = score;
	}
	
	public Mapping(String source_concept_uri, String target_concept_uri, double score, Path path )
	{
		this.source_concept_uri = source_concept_uri;
		this.target_concept_uri = target_concept_uri;
		this.score = score;
		this.path = path;
	}
	
	public Mapping(String source_concept_uri, String source_ontology_uri, String target_concept_uri, String target_ontology_uri, double score )
	{
		this.source_concept_uri = source_concept_uri;
		this.source_ontology_uri = source_ontology_uri;
		this.target_concept_uri = target_concept_uri;
		this.target_ontology_uri = target_ontology_uri;
		this.score = score;
	}

}
