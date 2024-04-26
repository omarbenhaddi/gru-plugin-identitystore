package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client;

import fr.paris.lutece.plugins.identitystore.AbstractIdentityStoreTestCase;
import fr.paris.lutece.plugins.identitystore.IdentityStoreTestContext;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ElasticClientTest extends AbstractIdentityStoreTestCase {
    private static final String IDENTITIES_ALIAS = "identities-alias";
    private static final String IDENTITIES_INDEX = "identities-index";
    protected ElasticsearchContainer elasticsearchContainer;
    protected String esUrl;
    protected ElasticClient elasticClient;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.startContainers();
        this.initConnection();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        this.shutDownContainers();
    }

    protected void startContainers( )
    {
        elasticsearchContainer = new ElasticsearchContainer(
                "docker.elastic.co/elasticsearch/elasticsearch:".concat( IdentityStoreTestContext.ELASTICSEARCH_VERSION ) )
                .withEnv( "xpack.security.enabled", "false" ).withNetworkAliases( "localhost" ).withReuse( false );
        elasticsearchContainer.start( );
        this.esUrl = "http://" + elasticsearchContainer.getHttpHostAddress( );
    }

    protected void shutDownContainers( )
    {
        if ( elasticsearchContainer != null && elasticsearchContainer.isRunning( ) )
        {
            elasticsearchContainer.stop( );
            elasticsearchContainer = null;
        }
    }

    @Override
    protected void preInitApplication() throws IOException {

    }

    private void initConnection() {
        this.elasticClient = new ElasticClient(this.esUrl);
    }

    public void testClient() throws ElasticClientException {
        final InputStream inputStream = this.getClass( ).getClassLoader( )
                .getResourceAsStream( "fr/paris/lutece/plugins/identitystore/service/indexer/elastic/index/model/internal/mappings.json" );
        final String mappings = new BufferedReader( new InputStreamReader( inputStream, StandardCharsets.UTF_8 ) ).lines( )
                .collect( Collectors.joining( "\n" ) );
        this.elasticClient.createMappings( IDENTITIES_INDEX, mappings );

        this.elasticClient.updateSettings( IDENTITIES_INDEX, "{ \"index.blocks.write\": true }" );
        this.elasticClient.updateSettings( IDENTITIES_INDEX, "{ \"index.blocks.write\": false }" );

        //this.elasticClient.addAliasOnIndex(IDENTITIES_INDEX, IDENTITIES_ALIAS);

        final String alias = this.elasticClient.getAlias(IDENTITIES_ALIAS);
        assertNotNull(alias);

        final boolean totoExists = this.elasticClient.aliasExists("toto");
        assertFalse("Alias toto ne devrait pas exister", totoExists);
        final boolean aliasExists = this.elasticClient.aliasExists(IDENTITIES_ALIAS);
        assertTrue("Alias " + IDENTITIES_ALIAS + " devrait exister", aliasExists);


    }
}