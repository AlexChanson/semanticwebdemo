import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Main {
    static String ressourceFolder = "src/main/resources/";
    static Model RDFS = ModelFactory.createRDFSModel(ModelFactory.createDefaultModel());

    public static void main(String[] args) throws Exception{

        Model model = ModelFactory.createDefaultModel();
        InputStream in = new FileInputStream(new File(ressourceFolder + "qalm.rdf"));
        model.read(in, "RDF/XML");
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
    public Model inferStuff(Model schema, Model model) {
        Model rdfsModel = ModelFactory.createRDFSModel(schema, model).difference(RDFS);
        rdfsModel.setNsPrefixes(schema);
        rdfsModel.setNsPrefixes(model);
        return rdfsModel;
    }
}
