package com.safety.recognition.cassandra;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.DropKeyspaceSpecification;
import org.springframework.data.cassandra.core.cql.keyspace.KeyspaceOption;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.core.mapping.SimpleUserTypeResolver;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

import java.util.Arrays;
import java.util.List;

@Configuration
@PropertySource(value = { "classpath:cassandra.properties" })
@EnableCassandraRepositories(basePackages = "com.safety.recognition.cassandra.repository")
public class CassandraConfig extends AbstractCassandraConfiguration {
    private static final Log LOGGER = LogFactory.getLog(CassandraConfig.class);

    @Autowired
    private Environment environment;

    @Override
    protected String getKeyspaceName() {
        return environment.getProperty("cassandra.keyspace");
    }

    @Override
    @Bean
    public CassandraClusterFactoryBean cluster() {
        final CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
        cluster.setJmxReportingEnabled(false);
        cluster.setContactPoints(environment.getProperty("cassandra.contactpoints"));
        cluster.setPort(Integer.parseInt(environment.getProperty("cassandra.port")));
        cluster.setKeyspaceCreations(getKeyspaceCreations());
        LOGGER.info("Cluster created with contact points [" + environment.getProperty("cassandra.contactpoints") + "] " + "& port [" + Integer.parseInt(environment.getProperty("cassandra.port")) + "].");
        return cluster;
    }

    @Bean
    public CassandraMappingContext mappingContext() throws ClassNotFoundException {
        CassandraMappingContext mappingContext= new CassandraMappingContext();
        mappingContext.setInitialEntitySet(getInitialEntitySet());
        mappingContext.setUserTypeResolver(new SimpleUserTypeResolver(cluster().getObject(), environment.getProperty("cassandra.keyspace")));
        return mappingContext;
    }

    @Override
    protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
        CreateKeyspaceSpecification specification = CreateKeyspaceSpecification
            .createKeyspace(environment.getProperty("cassandra.keyspace")).ifNotExists()
            .with(KeyspaceOption.DURABLE_WRITES, true).withSimpleReplication();
        return Arrays.asList(specification);
    }


    /*
     * Automatically configure a table if doesn't exist
     */
    @Override
    public SchemaAction getSchemaAction() {
        return SchemaAction.CREATE_IF_NOT_EXISTS;
    }


    /*
     * Get the entity package (where the entity class has the @Table annotation)
     */
    @Override
    public String[] getEntityBasePackages() {
        return new String[] { "com.safety.recognition.cassandra.model" };
    }

    @Override
    protected List<DropKeyspaceSpecification> getKeyspaceDrops() {
        return Arrays.asList(DropKeyspaceSpecification.dropKeyspace(environment.getProperty("cassandra.keyspace")));
    }


}
