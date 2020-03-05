package LIRMM.FADO.annane.BKbasedMatching;

public class Mapping implements Comparable  {

	String uriSource;
	String ontologySource;
	String ontologyTarget;
	String uriTarget;
	String ontology;
	String code;
	Double score;
	String relation;
	String matcherName;
	public String type="";//child or father
	


	 public Mapping(String uri_source, String ontologySource, Double score,String relation, String matcherName) {
			this.uriSource = uri_source;
			//this.ontologySource = ontologySource;
			//*************************************
			this.ontology =ontologySource;
			this.code = uri_source;
			//**************************
			this.score = score;
			this.relation = relation;
			this.matcherName = matcherName;
			
		}
		
	 
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getOntology() {
		return ontology;
	}

	public void setOntology(String ontology) {
		this.ontology = ontology;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}


	
	public Mapping(String uri,String ontology, Double score)
	{
		this.code=uri;
		this.ontology=ontology;
		this.score=score;
	}
	
	public Mapping(String uri,String ontology, Double score, String type)
	{
		this.code=uri;
		this.ontology=ontology;
		this.score=score;
		this.type=type;
	}


	@Override
	public int compareTo(Object o) {
		Mapping map = (Mapping)o;
		// TODO Auto-generated method stub
		int res= score.compareTo(map.score);
		return res;
	}

}

