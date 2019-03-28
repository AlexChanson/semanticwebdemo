import org.apache.jena.base.Sys;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.ontology.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;

import java.io.*;
import java.util.Iterator;
import java.util.Scanner;

public class Main {
    static String ressourceFolder = "src/main/resources/";
    static Model RDFS = ModelFactory.createRDFSModel(ModelFactory.createDefaultModel());

    static String ns = "http://monOntologie/";

    static Model data; // données de l'instance

    static OntModel bibo;

    static Model fullSchema;
    
    static Model infered; // objet instance faisant les inférences souhaitées

    static Model blterms; // schéma principal

    static OntModel foaf; // schéma FOAF

    static Reasoner reasoner; // Raisonneur choisit

    static PrefixMapping prefixMapping; // préfixes utilisés


    public static void main(String[] args) throws Exception{

        System.out.println("\nChargement de l'instance");
        loadFileJavaDemo();

        System.out.println("\nChargement des ontologies");
        block_enter();
        loadFileDocumentManagerDemo();

        System.out.println("\nCréation d'une classe et d'un individu");
        block_enter();
        addTriplets();

        System.out.println("\nCréation du modèle d'inférence");
        block_enter();
        createInferenceModelDemo();


        System.out.println("\nFabrication des préfixes");
        block_enter();
        setupPrefixesDemo();

        System.out.println("\nRequêtage de l'instance");
        sparqlQueryDemo();

        // écrire l'instance
        System.out.println("\nÉcriture de l'instance au format Turtle");
        block_enter();
        writeModelSemanticWebFormat(ressourceFolder + "inf.ttl", infered);

        System.out.println("\nÉcriture de l'instance au format json-ld");
        block_enter();
        writeModelClassicFormat(ressourceFolder + "inf.json", infered);


        System.out.println("\nÉcriture du nouveau schéma");
        block_enter();
        writeModelSemanticWebFormat(ressourceFolder+"nouveau_schema.ttl", fullSchema);

    }

    public static void loadFileJavaDemo() throws FileNotFoundException {

        // Initialisation du modèle de graphe de l'instance
        data = ModelFactory.createDefaultModel();

        // Création d'un InputStream de Java pour lire le fichier RDF de la British National Library
        InputStream in = new FileInputStream(new File(ressourceFolder + "BNBLODB_sample.rdf"));

        // Charge le modèle depuis le flux
        data.read(in, "RDF/XML");

    }

    public static void loadFileDocumentManagerDemo() throws FileNotFoundException {

        // chargement d'un OntModel depuis un fichier

        foaf = ModelFactory.createOntologyModel();
        InputStream in = new FileInputStream(new File(ressourceFolder + "foaf.rdf"));
        foaf.read(in, "RDF/XML");

        // chargement d'un Modèle en passant par le FileManager

        blterms = FileManager.get().loadModel("file:" + ressourceFolder + "blterms.owl");
        in = new FileInputStream(new File(ressourceFolder + "blterms.owl"));
        blterms.read(in, "OWL/XML");

        // chargement d'un OntModel depuis le OntDocumentManager
        
        OntDocumentManager ontDocumentManager = OntDocumentManager.getInstance();
    	OntModelSpec modelSpec_bibo = new OntModelSpec( OntModelSpec.OWL_MEM_TRANS_INF );

    	bibo = ModelFactory.createOntologyModel();
    	bibo.read("file:" + ressourceFolder + "bibo.rdf", "RDF/XML");

    	ontDocumentManager.addModel("test", bibo);

    	bibo = ontDocumentManager.getOntology("test2", modelSpec_bibo);

    	System.out.println("\nChargement via le OntDocumentManager\n\n Avec la mauvaise URI");
    	block_enter();

        System.out.println("\n");
    	System.out.println("Incorrectement chargé:");
    	System.out.println(bibo);
        System.out.println("\n");

        System.out.println("\nChargement avec le bon URI");
        block_enter();

        bibo = ontDocumentManager.getOntology("test", modelSpec_bibo);
        System.out.println("Correctement chargé:");
        System.out.println(bibo);
        System.out.println("\n");


    }



    public static void createInferenceModelDemo() {

        //reasoner = ReasonerRegistry.getOWLReasoner();          // calcule toutes les inférences OWL possibles
        reasoner = ReasonerRegistry.getTransitiveReasoner(); // calcule les inférences transitives simples

        // autres raisonneurs possibles:
        //reasoner = ReasonerRegistry.getRDFSReasoner();
        //reasoner = ReasonerRegistry.getRDFSSimpleReasoner();
        //reasoner = ReasonerRegistry.getOWLMiniReasoner();
        //reasoner = ReasonerRegistry.getOWLMicroReasoner();


        fullSchema = blterms.union(foaf).union(bibo);

        reasoner = reasoner.bindSchema(fullSchema);
        infered = ModelFactory.createInfModel(reasoner, data);
    }

    public static void setupPrefixesDemo() {
        prefixMapping = PrefixMapping.Factory.create();

        prefixMapping.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        prefixMapping.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        prefixMapping.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        prefixMapping.setNsPrefix("event", "http://purl.org/NET/c4dm/event.owl#");
        prefixMapping.setNsPrefix("dct", "http://purl.org/dc/terms/");

        prefixMapping.setNsPrefix("blt", "http://www.bl.uk/schemas/bibliographic/blterms#");
        prefixMapping.setNsPrefix("bibo", "http://purl.org/ontology/bibo/");
        prefixMapping.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");

        prefixMapping.setNsPrefix("mo", ns);

        System.out.println("\nPréfixes utilisés:");
        prefixMapping.getNsPrefixMap().forEach((k,v) -> System.out.println(k + ": "+ v));


    }

    public static void addTriplets() {



        OntClass ontClass = bibo.createClass(ns+"AudioBook");
        ontClass.addSuperClass(bibo.getOntResource("http://purl.org/ontology/bibo/Book"));

        ontClass.setLabel("Audio Book", "EN");
        ontClass.setLabel("Livre Audio", "FR");

        DatatypeProperty propertyLength = bibo.createDatatypeProperty(ns+"audioLength");
        propertyLength.addDomain(ontClass);
        propertyLength.addDomain(XSD.duration);

        Resource audioBook1 = data.createResource(ns+"audioBook1")
                .addProperty(RDF.type, ontClass)
                .addProperty(propertyLength, "1002s", XSDDatatype.XSDduration);

    }


    public static void sparqlQueryDemo() {

        System.out.println("\nRécupération des 100 premiers titres de livres");
        block_enter();
        sparql_query("select distinct ?title where { ?x rdf:type bibo:Book . ?x dct:title ?title  } limit 100", infered);

        System.out.println("\nRécupération des évènements sans inférences");
        block_enter();
    	sparql_query("select distinct ?x ?y where {?x ?y event:Event}", data);

        System.out.println("\nRécupération des évènements avec inférences");
        block_enter();
        sparql_query("select distinct ?x ?y where {?x ?y event:Event}", infered);

        System.out.println("\nChercher les audio books");
        block_enter();
        sparql_query("select ?x ?y ?z where { ?x rdf:type mo:AudioBook . ?x ?y ?z }", infered);

        System.out.println("\nSuperclasses de l'audio book");
        block_enter();
        sparql_query("select distinct ?x where { mo:audioBook1 rdf:type ?y . ?y rdfs:subClassOf ?x } ", infered);

    }

    public static void writeModelSemanticWebFormat(String path, Model model) {


        // écrire le résultat en Turtle
        try {
            File out = new File(path);
            FileOutputStream fos = new FileOutputStream(out);
            RDFDataMgr.write(fos, model, RDFFormat.TURTLE);
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void writeModelClassicFormat(String path, Model model) {

        // écrire le résultat en JSON-LD

        try {
            File out = new File(path);
            FileOutputStream fos = new FileOutputStream(out);
            RDFDataMgr.write(fos, model, RDFFormat.JSONLD);
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }


    public static Iterable<QuerySolution> execSelect(QueryExecution queryExecution) {
        return new Iterable<QuerySolution>() {
            @Override
            public Iterator<QuerySolution> iterator() {
                ResultSet resultSet = queryExecution.execSelect();
                return resultSet;
            }
        };
    }


    public static void block_enter() {
        Scanner sc = new Scanner(System.in);
        System.out.println("\nAppuyez sur entrée pour continuer...");
        sc.nextLine();
    }

    public static void sparql_query(String queryString, Model model) {

        ParameterizedSparqlString sparqlString = new ParameterizedSparqlString(queryString, prefixMapping);

        Query query = sparqlString.asQuery();
        System.out.println("--------------");
        System.out.println(queryString);
        int i = 0;
        try (QueryExecution queryExecution = QueryExecutionFactory.create(query, model)) {
        	for (QuerySolution sol : execSelect(queryExecution)) {
        		StringBuilder sb = new StringBuilder();
        		i++;
                Iterator<String> it = sol.varNames();

                while (it.hasNext()) {

                    String var = it.next();

                    sb.append(var);
                    sb.append(": ");
                    sb.append(sol.get(var).toString());
                    sb.append(", ");

                }

                System.out.println(sb.toString());

            }


        }
        System.out.println("nombre de résultats: "+i);
    }

}
