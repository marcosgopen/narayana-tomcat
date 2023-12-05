package org.jboss.narayana.tomcat.jta.integration;
/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import org.apache.naming.NamingContext;
import org.apache.tomcat.dbcp.dbcp2.managed.BasicManagedDataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.jboss.narayana.tomcat.jta.TransactionalDataSourceFactory;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import jakarta.transaction.TransactionManager;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:zfeng@redhat.com>Zheng Feng</a>
 */
public class TestTransactionalDataSourceFactory extends AbstractUnitCase {

    @Test
    public void testProperties() throws Exception {
        final Reference ref = new Reference("javax.sql.XADataSource",
                TransactionalDataSourceFactory.class.getName(), null);
        final Properties properties = getTestProperties();
        for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
            ref.add(new StringRefAddr((String) entry.getKey(), (String) entry.getValue()));
        }
        final Context context = new NamingContext(null, "test");
        final TransactionManager tm = new TransactionManagerImple();
        final JdbcDataSource h2 = new JdbcDataSource();
        h2.setURL("jdbc:h2:mem:test");
        h2.setUser("sa");
        h2.setPassword("sa");

        context.bind("transactionManager", tm);
        context.bind("myDataSource", h2);

        final TransactionalDataSourceFactory factory = new TransactionalDataSourceFactory();
        final BasicManagedDataSource ds = (BasicManagedDataSource) factory.getObjectInstance(ref, null, context, null);
        assertNotNull(ds);
        checkDataSourceProperties(ds);
        checkConnectionPoolProperties(ds);
    }

    private Properties getTestProperties() {
        final Properties properties = new Properties();
        properties.setProperty("transactionManager", "transactionManager");
        properties.setProperty("xaDataSource", "myDataSource");
        properties.setProperty("maxTotal", "10");
        properties.setProperty("maxIdle", "8");
        properties.setProperty("minIdle", "0");
        properties.setProperty("maxWaitMillis", "500");
        properties.setProperty("initialSize", "5");
        properties.setProperty("defaultAutoCommit", "true");
        properties.setProperty("defaultReadOnly", "false");
        properties.setProperty("defaultTransactionIsolation", "READ_COMMITTED");
        properties.setProperty("defaultCatalog", "test");
        properties.setProperty("testOnBorrow", "true");
        properties.setProperty("testOnReturn", "false");
        properties.setProperty("username", "username");
        properties.setProperty("password", "password");
        properties.setProperty("validationQuery", "SELECT 1");
        properties.setProperty("validationQueryTimeout", "100");
        properties.setProperty("connectionInitSqls", "SELECT 1;SELECT 2");
        properties.setProperty("timeBetweenEvictionRunsMillis", "1000");
        properties.setProperty("minEvictableIdleTimeMillis", "2000");
        properties.setProperty("softMinEvictableIdleTimeMillis", "3000");
        properties.setProperty("numTestsPerEvictionRun", "2");
        properties.setProperty("testWhileIdle", "true");
        properties.setProperty("accessToUnderlyingConnectionAllowed", "true");
        properties.setProperty("removeAbandonedOnBorrow", "true");
        properties.setProperty("removeAbandonedOnMaintenance", "true");
        properties.setProperty("removeAbandonedTimeout", "3000");
        properties.setProperty("logAbandoned", "true");
        properties.setProperty("abandonedUsageTracking", "true");
        properties.setProperty("poolPreparedStatements", "true");
        properties.setProperty("maxOpenPreparedStatements", "10");
        properties.setProperty("lifo", "true");
        properties.setProperty("fastFailValidation", "true");
        properties.setProperty("disconnectionSqlCodes", "XXX,YYY");
        properties.setProperty("jmxName", "org.apache.commons.dbcp2:name=test");
        return properties;
    }

    private void checkDataSourceProperties(final BasicManagedDataSource ds) throws Exception {
        assertEquals(10, ds.getMaxTotal());
        assertEquals(8, ds.getMaxIdle());
        assertEquals(0, ds.getMinIdle());
        assertEquals(500, ds.getMaxWaitMillis());
        assertEquals(5, ds.getInitialSize());
        assertEquals(5, ds.getNumIdle());
        assertEquals(Boolean.TRUE, ds.getDefaultAutoCommit());
        assertEquals(Boolean.FALSE, ds.getDefaultReadOnly());
        assertEquals(Connection.TRANSACTION_READ_COMMITTED, ds.getDefaultTransactionIsolation());
        assertEquals("test", ds.getDefaultCatalog());
        assertTrue(ds.getTestOnBorrow());
        assertFalse(ds.getTestOnReturn());
        assertEquals("username", ds.getUsername());
        assertEquals("password", ds.getPassword());
        assertEquals("SELECT 1", ds.getValidationQuery());
        assertEquals(100, ds.getValidationQueryTimeout());
        assertEquals(2, ds.getConnectionInitSqls().size());
        assertEquals("SELECT 1", ds.getConnectionInitSqls().get(0));
        assertEquals("SELECT 2", ds.getConnectionInitSqls().get(1));
        assertEquals(1000, ds.getTimeBetweenEvictionRunsMillis());
        assertEquals(2000, ds.getMinEvictableIdleTimeMillis());
        assertEquals(3000, ds.getSoftMinEvictableIdleTimeMillis());
        assertEquals(2, ds.getNumTestsPerEvictionRun());
        assertTrue(ds.getTestWhileIdle());
        assertTrue(ds.isAccessToUnderlyingConnectionAllowed());
        assertTrue(ds.getRemoveAbandonedOnBorrow());
        assertTrue(ds.getRemoveAbandonedOnMaintenance());
        assertEquals(3000, ds.getRemoveAbandonedTimeout());
        assertTrue(ds.getLogAbandoned());
        assertTrue(ds.getAbandonedUsageTracking());
        assertTrue(ds.isPoolPreparedStatements());
        assertEquals(10, ds.getMaxOpenPreparedStatements());
        assertTrue(ds.getLifo());
        assertTrue(ds.getFastFailValidation());
        assertTrue(ds.getDisconnectionSqlCodes().contains("XXX"));
        assertTrue(ds.getDisconnectionSqlCodes().contains("YYY"));
        assertEquals("org.apache.commons.dbcp2:name=test", ds.getJmxName());
    }

    private void checkConnectionPoolProperties(final BasicManagedDataSource ds) {
        assertEquals(10, ds.getMaxTotal());
        assertEquals(8, ds.getMaxIdle());
        assertEquals(0, ds.getMinIdle());
        assertEquals(500, ds.getMaxWaitMillis());
        assertEquals(5, ds.getNumIdle());
        assertEquals(5, ds.getInitialSize());
        assertTrue(ds.getTestOnBorrow());
        assertFalse(ds.getTestOnReturn());
        assertEquals(1000, ds.getTimeBetweenEvictionRunsMillis());
        assertEquals(2000, ds.getMinEvictableIdleTimeMillis());
        assertEquals(3000, ds.getSoftMinEvictableIdleTimeMillis());
        assertEquals(2, ds.getNumTestsPerEvictionRun());
        assertTrue(ds.getTestWhileIdle());
        assertTrue(ds.getRemoveAbandonedOnBorrow());
        assertTrue(ds.getRemoveAbandonedOnMaintenance());
        assertEquals(3000, ds.getRemoveAbandonedTimeout());
        assertTrue(ds.getLogAbandoned());
        assertTrue(ds.getLifo());
    }
}
