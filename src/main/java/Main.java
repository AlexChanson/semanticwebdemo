import org.apache.jena.base.Sys;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.FileManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Main {
    static String ressourceFolder = "src/main/resources/";
    static Model RDFS = ModelFactory.createRDFSModel(ModelFactory.createDefaultModel());


    static Model data;

    static Model infered;

    static Model schema;

    static Model foaf;

    static Reasoner reasoner;

    static PrefixMapping prefixMapping;

    public static void loadFileJavaDemo() throws FileNotFoundException {

        // Initialisation du modèle de graphe de l'instance
        data = ModelFactory.createDefaultModel();

        // Création d'un InputStream de Java pour lire le fichier RDF de la British National Library
        InputStream in = new FileInputStream(new File(ressourceFolder + "bnb_dump.rdf"));

        // Charge le modèle depuis le flux
        data.read(in, "RDF/XML");

    }

    public static void loadFileDocumentManagerDemo() throws FileNotFoundException {
        foaf = ModelFactory.createDefaultModel();
        InputStream in = new FileInputStream(new File(ressourceFolder + "foaf.rdf"));
        foaf.read(in, "RDF/XML");

        Model schema = FileManager.get().loadModel("file:" + ressourceFolder + "blterms.owl");//ModelFactory.createDefaultModel();
        //in = new FileInputStream(new File(ressourceFolder + "blterms.owl"));
        //schema.read(in, "OWL/XML");
    }

    public static void mergeSchemasBindReasonerDemo() {

        reasoner = reasoner.bindSchema(schema.union(foaf));
    }

    public static void createInferenceModelDemo() {

        reasoner = ReasonerRegistry.getOWLReasoner();          // calcule toutes les inférences OWL possibles
        //reasoner = ReasonerRegistry.getTransitiveReasoner(); // calcule les inférences transitives simples
        //reasoner = ReasonerRegistry.getRDFSReasoner();

        infered = ModelFactory.createInfModel(reasoner, data);
    }

    public static void setupPrefixesDemo() {
        prefixMapping = PrefixMapping.Factory.create();
    }

    public static void addTriplets() {

    }

    public static void sparqlQueryDemo() {
        String queryString = " select * where {?x :type ?z} " ;
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
    }

    public static void writeModelSemanticWebFormat() {

    }

    public static void writeModelClassicFormat() {

    }


    public static void main(String[] args) throws Exception{


        loadFileJavaDemo();
        loadFileDocumentManagerDemo();
        mergeSchemasBindReasonerDemo();
        createInferenceModelDemo();
        setupPrefixesDemo();
        addTriplets();
        sparqlQueryDemo();
        writeModelSemanticWebFormat();
        writeModelClassicFormat();

    }

}
