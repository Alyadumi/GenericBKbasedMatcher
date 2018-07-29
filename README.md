# GBM: Generic BK based Matcher

This project is the code of a Generic Background-knowledge based ontology Matcher (GBM). GBM has been implemented during my thesis that aimed to enhance ontology matching using external knowledge resources. GBM is the implementation of our Background-knowledge based ontology matching approach presented in [1]. It uses any direct matcher as a black box. In addition, it offers several parameters that allows to have various configurations according to the user needs.

We used this framework to participate in the OAEI 2017.5 compaign, with YAM-BIO, which is GBM with YAM++ [2] as a direct matcher and two biomedical ontologies as background knowledge DOID and UBERON. Our results are available here https://goo.gl/A496ug

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites
The direct matcher instanciated in the Parameters class has to implement a static function align which takes as input the URL of ontologies to align, and returns the URL of the generated alignment:

        URL alignment=align (URL ontology1, URL ontology2) 
        
The alignment should be stored in RDF format with the API alignment to be parsed correctly. Systems that have participated in the OAEI campaigns, may use GBM directly without any adaptation. Indeed, OAEI participants have to wrap their tools as SEALS packages, and the wrapping procedure includes the implementation of the function Align. [3]
        
Please, before running the code be sure that all libraries related to the used direct matcher are referenced. These libraries are available in the lib folder.

The framework offers two derivation strategies (Neo4j and specific_algo). When using the Neo4j strategy, the Neo4j graph database should be installed and variables driver and session in the Parameters class should be initialized.
DataSets for ML based selection

### Installing





[1] https://www.sciencedirect.com/science/article/pii/S1570826818300179?via%3Dihub.

[2] http://www.websemanticsjournal.org/index.php/ps/article/view/483.

[3] http://oaei.ontologymatching.org/2017/.
