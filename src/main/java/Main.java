import org.apache.jena.atlas.json.io.JsonWriter;
import org.apache.jena.base.Sys;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;

import java.io.*;
import java.util.Iterator;

public class Main {
    static String ressourceFolder = "src/main/resources/";
    static Model RDFS = ModelFactory.createRDFSModel(ModelFactory.createDefaultModel());

    static Model data; // données de l'instance

    static Model infered; // objet instance faisant les inférences souhaitées

    static Model schema; // schéma principal

    static Model foaf; // schéma FOAF

    static Reasoner reasoner; // Raisonneur choisit

    static PrefixMapping prefixMapping; // préfixes utilisés

    public static void loadFileJavaDemo() throws FileNotFoundException {

        // Initialisation du modèle de graphe de l'instance
        data = ModelFactory.createDefaultModel();

        // Création d'un InputStream de Java pour lire le fichier RDF de la British National Library
        InputStream in = new FileInputStream(new File(ressourceFolder + "BNBLODB_sample.rdf"));

        // Charge le modèle depuis le flux
        data.read(in, "RDF/XML");

    }

    public static void loadFileDocumentManagerDemo() throws FileNotFoundException {
        foaf = ModelFactory.createDefaultModel();
        InputStream in = new FileInputStream(new File(ressourceFolder + "foaf.rdf"));
        foaf.read(in, "RDF/XML");
        
    	OntDocumentManager dm = new OntDocumentManager("file:" + ressourceFolder + "blterms.owl");
    	OntModelSpec modelSpec = new OntModelSpec( OntModelSpec.OWL_MEM );
    	modelSpec.setDocumentManager(dm);
        schema = ModelFactory.createOntologyModel(modelSpec);
        
        //Model schema = FileManager.get().loadModel("file:" + ressourceFolder + "blterms.owl");//ModelFactory.createDefaultModel();
        //in = new FileInputStream(new File(ressourceFolder + "blterms.owl"));
        //schema.read(in, "OWL/XML");
    }

    public static void mergeSchemasBindReasonerDemo() {
        reasoner = reasoner.bindSchema(schema.union(foaf));
    }

    public static void createInferenceModelDemo() {

        //reasoner = ReasonerRegistry.getOWLReasoner();          // calcule toutes les inférences OWL possibles
        reasoner = ReasonerRegistry.getTransitiveReasoner(); // calcule les inférences transitives simples

        // autres raisonneurs possibles:
        //reasoner = ReasonerRegistry.getRDFSReasoner();
        //reasoner = ReasonerRegistry.getRDFSSimpleReasoner();
        //reasoner = ReasonerRegistry.getOWLMiniReasoner();
        //reasoner = ReasonerRegistry.getOWLMicroReasoner();

        infered = ModelFactory.createInfModel(reasoner, data);
    }

    public static void setupPrefixesDemo() {
        prefixMapping = PrefixMapping.Factory.create();

        prefixMapping.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

        prefixMapping.setNsPrefix("blt", "http://www.bl.uk/schemas/bibliographic/blterms#");
        prefixMapping.setNsPrefix("bibo", "http://purl.org/ontology/bibo/");
        prefixMapping.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");

    }

    public static void addTriplets() {
    }

    public static void sparqlQueryDemo() {
        String queryString = " select distinct ?y where {?x ?y ?z} " ;
        Query query = QueryFactory.create(queryString) ;
        int c = 0;
        try (QueryExecution qexec = QueryExecutionFactory.create(query, infered)) {
            ResultSet results = qexec.execSelect() ;
            for ( int i = 0; results.hasNext() && (i < Integer.MAX_VALUE); ++i)
            {
                QuerySolution soln = results.nextSolution() ;
                RDFNode x = soln.get("x") ;       // Get a result variable by name.
                Resource r = soln.getResource("y") ; // Get a result variable - must be a resource
                Literal l = soln.getLiteral("z") ;   // Get a result variable - must be a literal
                System.out.printf("%s   |   %s   |   %s%n",x,r,l);
                c++;
            }
        }
        System.out.println(c);

        System.out.println("New\n");

        prefixMapping.getNsPrefixMap().forEach((k,v) -> System.out.println(k + ": "+ v));

        queryString = "select distinct ?c where {?x rdf:type ?c}";

        ParameterizedSparqlString sparqlString = new ParameterizedSparqlString(queryString, prefixMapping);

        query = sparqlString.asQuery();


        try (QueryExecution queryExecution = QueryExecutionFactory.create(query, infered)) {
            for (QuerySolution sol : execSelect(queryExecution)) {
                StringBuilder sb = new StringBuilder();

                Iterator<String> it = sol.varNames();

                while (it.hasNext()) {
                    String var = it.next();

                    sb.append(var);
                    sb.append(": ");
                    sb.append(sol.get(var).toString());
                    sb.append(" ,");

                }

                System.out.println(sb.toString());

            }


        }







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


    public static void main(String[] args) throws Exception{


        loadFileJavaDemo();
        loadFileDocumentManagerDemo();
        createInferenceModelDemo();
        mergeSchemasBindReasonerDemo();
        setupPrefixesDemo();
        addTriplets();
        sparqlQueryDemo();
        //writeModelSemanticWebFormat(ressourceFolder + "inf.ttl", infered);
        //writeModelClassicFormat(ressourceFolder + "inf.json", infered);

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

}
