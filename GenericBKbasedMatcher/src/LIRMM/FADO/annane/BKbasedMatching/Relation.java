package LIRMM.FADO.annane.BKbasedMatching;

public class Relation 
{
    /**
     * 
     * @param property: the ontology property that we want to explore
     * @param symbol of the property, we may use < for subClass property
     */
     public Relation(String property,String symbol,String abbreviation)
     {
    	 this.property=property;
    	 this.symbol=symbol;
    	 this.abbreviation=abbreviation;
     }
     
	String property;
	String symbol;
	String abbreviation;
	


	public String getProperty() {
		return property;
	}
	public String getSymbol() {
		return symbol;
	}
	public String getAbbreviation() {
		return abbreviation;
	}


}
