import org.apache.jena.base.Sys;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.util.FileManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Main {
    static String ressourceFolder = "src/main/resources/";
    static Model RDFS = ModelFactory.createRDFSModel(ModelFactory.createDefaultModel());

    public static void main(String[] args) throws Exception{

        Model data = ModelFactory.createDefaultModel();
        InputStream in = new FileInputStream(new File(ressourceFolder + "bnb_dump.rdf"));
        data.read(in, "RDF/XML");

        Model foaf = ModelFactory.createDefaultModel();
        in = new FileInputStream(new File(ressourceFolder + "foaf.rdf"));
        foaf.read(in, "RDF/XML");

        Model schema = FileManager.get().loadModel("file:" + ressourceFolder + "blterms.owl");//ModelFactory.createDefaultModel();
        //in = new FileInputStream(new File(ressourceFolder + "blterms.owl"));
        //schema.read(in, "OWL/XML");

        Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
        reasoner = reasoner.bindSchema(schema.union(foaf));
        //Reasoner simplet = ReasonerRegistry.getRDFSReasoner();
        //simplet = simplet.bindSchema(foaf);
        InfModel infered = ModelFactory.createInfModel(reasoner, data);
        //infered = ModelFactory.createInfModel(reasoner, infered);

        String queryString = " select * where {?x rdf:type ?z} " ;
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


    /**
     * Create RDFS model from given schema and model.
     *
     * @param schema
     *            Schema
     * @param model
     *            Model
     * @return RDFS model
     */
    static public Model inferStuff(Model schema, Model model) {
        Model rdfsModel = ModelFactory.createRDFSModel(schema, model).difference(RDFS);
        rdfsModel.setNsPrefixes(schema);
        rdfsModel.setNsPrefixes(model);
        return rdfsModel;
    }
}
