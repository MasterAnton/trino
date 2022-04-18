/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.plugin.clickhouse;

import com.google.common.collect.ImmutableMap;
import io.trino.plugin.jdbc.BaseJdbcConnectorTest;
import io.trino.testing.QueryRunner;
import io.trino.testing.sql.SqlExecutor;
import org.testng.annotations.Test;

import static io.trino.plugin.clickhouse.ClickHouseQueryRunner.createClickHouseQueryRunner;
import static io.trino.testing.sql.TestTable.randomTableSuffix;

public class TestClickHouseTruncateRWDate
        extends BaseJdbcConnectorTest
{
    private TestingClickHouseServer clickhouseServer;

    @Override
    protected SqlExecutor onRemoteDatabase()
    {
        return clickhouseServer::execute;
    }

    @Override
    protected QueryRunner createQueryRunner()
            throws Exception
    {
        this.clickhouseServer = closeAfterClass(new TestingClickHouseServer());
        return createClickHouseQueryRunner(
                clickhouseServer,
                ImmutableMap.of(),
                ImmutableMap.<String, String>builder()
                        .put("clickhouse.map-string-as-varchar", "true")
                        .buildOrThrow(),
                REQUIRED_TPCH_TABLES);
    }

    @Test
    public void testTruncateTable()
    {
        String tableName = "test_truncate_tabe_" + randomTableSuffix();
        assertUpdate("CREATE TABLE " + tableName + "(x int NOT NULL)");
        assertUpdate("INSERT INTO " + tableName + "(x) SELECT 123", 1);
        assertQuery("SELECT * FROM " + tableName, "VALUES (123)");
        assertQuery("SELECT count(*) FROM " + tableName, "VALUES (1)");
        assertUpdate("TRUNCATE TABLE " + tableName);
        assertQuery("SELECT count(*) FROM " + tableName, "VALUES (0)");
    }

    @Test
    public void testReadWriteDate()
    {
        String tableName = "test_date_tabe_" + randomTableSuffix();
        assertUpdate("CREATE TABLE " + tableName + "(id int NOT NULL, dateval Date)");

        assertUpdate("INSERT INTO " + tableName + "(id,dateval) VALUES (1,DATE '1970-01-01')", 1);
        assertQuery("SELECT dateval FROM " + tableName + " WHERE id = 1", "VALUES (DATE '1970-01-01')");

        assertUpdate("INSERT INTO " + tableName + "(id,dateval) VALUES (2,DATE '2106-02-07')", 1);
        assertQuery("SELECT dateval FROM " + tableName + " WHERE id = 2", "VALUES (DATE '2106-02-07')");

        assertQueryFails(
                "INSERT INTO " + tableName + "(id,dateval) VALUES (3,DATE '1969-12-31')",
                "Date must be between 1970-01-01 and 2106-02-07 in ClickHouse: 1969-12-31");

        assertQueryFails(
                "INSERT INTO " + tableName + "(id,dateval) VALUES (4,DATE '2106-02-08')",
                "Date must be between 1970-01-01 and 2106-02-07 in ClickHouse: 2106-02-08");
    }
}
