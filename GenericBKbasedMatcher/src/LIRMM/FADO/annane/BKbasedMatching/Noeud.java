package LIRMM.FADO.annane.BKbasedMatching;




public class Noeud implements Comparable {
	
	 String code;
	 String ontology;
	 Double score;
	 String source="";
	 public String type="";//child or father
	

	
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

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Noeud(String uri,String ontology, String source)
	{
		this.code=uri.replace(",", "_");
		this.ontology=ontology;
		this.source=source;
	}
	
	
	
	public Noeud(String uri,String ontology, Double score)
	{
		this.code=uri;
		this.ontology=ontology;
		this.score=score;
	}
	
	public Noeud(String uri,String ontology, Double score, String type)
	{
		this.code=uri;
		this.ontology=ontology;
		this.score=score;
		this.type=type;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		

	}


	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		Noeud n=(Noeud)o;
		if(this.code.equalsIgnoreCase(n.code)&&this.ontology.equalsIgnoreCase(n.ontology)&&this.score==n.score&&this.type.equals(n.type)&&this.source.equalsIgnoreCase(n.source))
		return 0;
		else return -1;
	}

}
