import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Main {
    static String ressourceFolder = "src/main/resources/";
    static Model RDFS = ModelFactory.createRDFSModel(ModelFactory.createDefaultModel());

    public static void main(String[] args) throws Exception{

        Model data = ModelFactory.createDefaultModel();
        InputStream in = new FileInputStream(new File(ressourceFolder + "qalm.rdf"));
        data.read(in, "RDF/XML");

        Model schema = ModelFactory.createDefaultModel();
        in = new FileInputStream(new File(ressourceFolder + "MerchantSiteOntology.rdfs"));
        schema.read(in, "RDFS/XML");

        Model infered = inferStuff(schema, data);

        String queryString = " select * where {?x ?y ?Z} " ;
        Query query = QueryFactory.create(queryString) ;
        try (QueryExecution qexec = QueryExecutionFactory.create(query, infered)) {
            ResultSet results = qexec.execSelect() ;
            for ( int i = 0; results.hasNext() && i < 10; ++i)
            {
                QuerySolution soln = results.nextSolution() ;
                RDFNode x = soln.get("varName") ;       // Get a result variable by name.
                Resource r = soln.getResource("VarR") ; // Get a result variable - must be a resource
                Literal l = soln.getLiteral("VarL") ;   // Get a result variable - must be a literal
            }
        }
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
