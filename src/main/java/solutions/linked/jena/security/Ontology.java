/* CVS $Id: $ */
package solutions.linked.jena.security; 
import org.apache.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from .ontology.ttl 
 * @author Auto-generated by schemagen on 20 May 2019 19:34 
 */
public class Ontology {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static final Model M_MODEL = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "https://linked.solutions/fuseki-oidc/ontology#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     * @return namespace as String
     * @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = M_MODEL.createResource( NS );
    
    /** <p>Points to an Ant-pattern matching the IRIs of information resources to which 
     *  access is being granted.</p>
     */
    public static final Property accessTo = M_MODEL.createProperty( "https://linked.solutions/fuseki-oidc/ontology#accessTo" );
    
    /** <p>The username of the person or social entity to being given the right</p> */
    public static final Property agentUserName = M_MODEL.createProperty( "https://linked.solutions/fuseki-oidc/ontology#agentUserName" );
    
    /** <p>Points to the assembler model used to provide the security graph</p> */
    public static final Property securityBaseModel = M_MODEL.createProperty( "https://linked.solutions/fuseki-oidc/ontology#securityBaseModel" );
    
    /** <p>The name of the graph that contains the access descriptions for the dataset</p> */
    public static final Property securityGraphName = M_MODEL.createProperty( "https://linked.solutions/fuseki-oidc/ontology#securityGraphName" );
    
}
