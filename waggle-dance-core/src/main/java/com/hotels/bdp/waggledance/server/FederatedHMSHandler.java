/**
 * Copyright (C) 2016-2019 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hotels.bdp.waggledance.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.api.AbortTxnRequest;
import org.apache.hadoop.hive.metastore.api.AbortTxnsRequest;
import org.apache.hadoop.hive.metastore.api.AddDynamicPartitions;
import org.apache.hadoop.hive.metastore.api.AddForeignKeyRequest;
import org.apache.hadoop.hive.metastore.api.AddPartitionsRequest;
import org.apache.hadoop.hive.metastore.api.AddPartitionsResult;
import org.apache.hadoop.hive.metastore.api.AddPrimaryKeyRequest;
import org.apache.hadoop.hive.metastore.api.AggrStats;
import org.apache.hadoop.hive.metastore.api.AlreadyExistsException;
import org.apache.hadoop.hive.metastore.api.CacheFileMetadataRequest;
import org.apache.hadoop.hive.metastore.api.CacheFileMetadataResult;
import org.apache.hadoop.hive.metastore.api.CheckLockRequest;
import org.apache.hadoop.hive.metastore.api.ClearFileMetadataRequest;
import org.apache.hadoop.hive.metastore.api.ClearFileMetadataResult;
import org.apache.hadoop.hive.metastore.api.ColumnStatistics;
import org.apache.hadoop.hive.metastore.api.CommitTxnRequest;
import org.apache.hadoop.hive.metastore.api.CompactionRequest;
import org.apache.hadoop.hive.metastore.api.CompactionResponse;
import org.apache.hadoop.hive.metastore.api.ConfigValSecurityException;
import org.apache.hadoop.hive.metastore.api.CurrentNotificationEventId;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.DropConstraintRequest;
import org.apache.hadoop.hive.metastore.api.DropPartitionsRequest;
import org.apache.hadoop.hive.metastore.api.DropPartitionsResult;
import org.apache.hadoop.hive.metastore.api.EnvironmentContext;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.hive.metastore.api.FireEventRequest;
import org.apache.hadoop.hive.metastore.api.FireEventResponse;
import org.apache.hadoop.hive.metastore.api.ForeignKeysRequest;
import org.apache.hadoop.hive.metastore.api.ForeignKeysResponse;
import org.apache.hadoop.hive.metastore.api.Function;
import org.apache.hadoop.hive.metastore.api.GetAllFunctionsResponse;
import org.apache.hadoop.hive.metastore.api.GetFileMetadataByExprRequest;
import org.apache.hadoop.hive.metastore.api.GetFileMetadataByExprResult;
import org.apache.hadoop.hive.metastore.api.GetFileMetadataRequest;
import org.apache.hadoop.hive.metastore.api.GetFileMetadataResult;
import org.apache.hadoop.hive.metastore.api.GetOpenTxnsInfoResponse;
import org.apache.hadoop.hive.metastore.api.GetOpenTxnsResponse;
import org.apache.hadoop.hive.metastore.api.GetPrincipalsInRoleRequest;
import org.apache.hadoop.hive.metastore.api.GetPrincipalsInRoleResponse;
import org.apache.hadoop.hive.metastore.api.GetRoleGrantsForPrincipalRequest;
import org.apache.hadoop.hive.metastore.api.GetRoleGrantsForPrincipalResponse;
import org.apache.hadoop.hive.metastore.api.GetTableRequest;
import org.apache.hadoop.hive.metastore.api.GetTableResult;
import org.apache.hadoop.hive.metastore.api.GetTablesRequest;
import org.apache.hadoop.hive.metastore.api.GetTablesResult;
import org.apache.hadoop.hive.metastore.api.GrantRevokePrivilegeRequest;
import org.apache.hadoop.hive.metastore.api.GrantRevokePrivilegeResponse;
import org.apache.hadoop.hive.metastore.api.GrantRevokeRoleRequest;
import org.apache.hadoop.hive.metastore.api.GrantRevokeRoleResponse;
import org.apache.hadoop.hive.metastore.api.HeartbeatRequest;
import org.apache.hadoop.hive.metastore.api.HeartbeatTxnRangeRequest;
import org.apache.hadoop.hive.metastore.api.HeartbeatTxnRangeResponse;
import org.apache.hadoop.hive.metastore.api.HiveObjectPrivilege;
import org.apache.hadoop.hive.metastore.api.HiveObjectRef;
import org.apache.hadoop.hive.metastore.api.HiveObjectType;
import org.apache.hadoop.hive.metastore.api.Index;
import org.apache.hadoop.hive.metastore.api.InvalidInputException;
import org.apache.hadoop.hive.metastore.api.InvalidObjectException;
import org.apache.hadoop.hive.metastore.api.InvalidOperationException;
import org.apache.hadoop.hive.metastore.api.InvalidPartitionException;
import org.apache.hadoop.hive.metastore.api.LockComponent;
import org.apache.hadoop.hive.metastore.api.LockRequest;
import org.apache.hadoop.hive.metastore.api.LockResponse;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.NoSuchLockException;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.hadoop.hive.metastore.api.NoSuchTxnException;
import org.apache.hadoop.hive.metastore.api.NotificationEventRequest;
import org.apache.hadoop.hive.metastore.api.NotificationEventResponse;
import org.apache.hadoop.hive.metastore.api.OpenTxnRequest;
import org.apache.hadoop.hive.metastore.api.OpenTxnsResponse;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.PartitionEventType;
import org.apache.hadoop.hive.metastore.api.PartitionSpec;
import org.apache.hadoop.hive.metastore.api.PartitionsByExprRequest;
import org.apache.hadoop.hive.metastore.api.PartitionsByExprResult;
import org.apache.hadoop.hive.metastore.api.PartitionsStatsRequest;
import org.apache.hadoop.hive.metastore.api.PartitionsStatsResult;
import org.apache.hadoop.hive.metastore.api.PrimaryKeysRequest;
import org.apache.hadoop.hive.metastore.api.PrimaryKeysResponse;
import org.apache.hadoop.hive.metastore.api.PrincipalPrivilegeSet;
import org.apache.hadoop.hive.metastore.api.PrincipalType;
import org.apache.hadoop.hive.metastore.api.PrivilegeBag;
import org.apache.hadoop.hive.metastore.api.PutFileMetadataRequest;
import org.apache.hadoop.hive.metastore.api.PutFileMetadataResult;
import org.apache.hadoop.hive.metastore.api.Role;
import org.apache.hadoop.hive.metastore.api.SQLForeignKey;
import org.apache.hadoop.hive.metastore.api.SQLPrimaryKey;
import org.apache.hadoop.hive.metastore.api.SetPartitionsStatsRequest;
import org.apache.hadoop.hive.metastore.api.ShowCompactRequest;
import org.apache.hadoop.hive.metastore.api.ShowCompactResponse;
import org.apache.hadoop.hive.metastore.api.ShowLocksRequest;
import org.apache.hadoop.hive.metastore.api.ShowLocksResponse;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.metastore.api.TableMeta;
import org.apache.hadoop.hive.metastore.api.TableStatsRequest;
import org.apache.hadoop.hive.metastore.api.TableStatsResult;
import org.apache.hadoop.hive.metastore.api.ThriftHiveMetastore;
import org.apache.hadoop.hive.metastore.api.TxnAbortedException;
import org.apache.hadoop.hive.metastore.api.TxnOpenException;
import org.apache.hadoop.hive.metastore.api.Type;
import org.apache.hadoop.hive.metastore.api.UnknownDBException;
import org.apache.hadoop.hive.metastore.api.UnknownPartitionException;
import org.apache.hadoop.hive.metastore.api.UnknownTableException;
import org.apache.hadoop.hive.metastore.api.UnlockRequest;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.facebook.fb303.FacebookBase;
import com.facebook.fb303.fb_status;
import com.jcabi.aspects.Loggable;

import com.hotels.bdp.waggledance.mapping.model.DatabaseMapping;
import com.hotels.bdp.waggledance.mapping.service.MappingEventListener;
import com.hotels.bdp.waggledance.mapping.service.impl.NotifyingFederationService;
import com.hotels.bdp.waggledance.metrics.Monitored;

@Monitored
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class FederatedHMSHandler extends FacebookBase implements CloseableIHMSHandler {

  private static final Logger LOG = LoggerFactory.getLogger(FederatedHMSHandler.class);

  private static final String INVOCATION_LOG_NAME = "com.hotels.bdp.waggledance.server.invocation-log";
  private final MappingEventListener databaseMappingService;
  private final NotifyingFederationService notifyingFederationService;
  private Configuration conf;

  FederatedHMSHandler(
      MappingEventListener databaseMappingService,
      NotifyingFederationService notifyingFederationService) {
    super("waggle-dance-handler");
    this.databaseMappingService = databaseMappingService;
    this.notifyingFederationService = notifyingFederationService;
    this.notifyingFederationService.subscribe(databaseMappingService);
  }

  private ThriftHiveMetastore.Iface getPrimaryClient() throws TException {
    return databaseMappingService.primaryDatabaseMapping().getClient();
  }

  private DatabaseMapping checkWritePermissions(String databaseName) throws TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(databaseName);
    mapping.checkWritePermissions(databaseName);
    return mapping;
  }

  @Override
  public void close() throws IOException {
    shutdown();
  }

  @Override
  public void shutdown() {
    super.shutdown();
    try {
      notifyingFederationService.unsubscribe(databaseMappingService);
      databaseMappingService.close();
    } catch (IOException e) {
      LOG.warn("Error shutting down federated handler", e);
    }
  }

  //////////////////////////////

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public String getMetaConf(String key) throws MetaException, TException {
    return getPrimaryClient().getMetaConf(key);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void setMetaConf(String key, String value) throws MetaException, TException {
    getPrimaryClient().setMetaConf(key, value);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void create_database(Database database)
      throws AlreadyExistsException, InvalidObjectException, MetaException, TException {
    DatabaseMapping mapping = databaseMappingService.primaryDatabaseMapping();
    mapping.createDatabase(mapping.transformInboundDatabase(database));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public Database get_database(String name) throws NoSuchObjectException, MetaException, TException {
    LOG.info("Fetching database {}", name);
    DatabaseMapping mapping = databaseMappingService.databaseMapping(name);
    LOG.info("Mapping is '{}'", mapping.getDatabasePrefix());
    return mapping
        .transformOutboundDatabase(mapping.getClient().get_database(mapping.transformInboundDatabaseName(name)));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void drop_database(String name, boolean deleteData, boolean cascade)
      throws NoSuchObjectException, InvalidOperationException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(name);
    mapping.getClient().drop_database(mapping.transformInboundDatabaseName(name), deleteData, cascade);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<String> get_databases(String pattern) throws MetaException, TException {
    return databaseMappingService.getPanopticOperationHandler().getAllDatabases(pattern);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<String> get_all_databases() throws MetaException, TException {
    return databaseMappingService.getPanopticOperationHandler().getAllDatabases();
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void alter_database(String dbname, Database db) throws MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = checkWritePermissions(dbname);
    mapping.checkWritePermissions(db.getName());
    mapping
        .getClient()
        .alter_database(mapping.transformInboundDatabaseName(dbname), mapping.transformInboundDatabase(db));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public Type get_type(String name) throws MetaException, NoSuchObjectException, TException {
    return getPrimaryClient().get_type(name);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean create_type(Type type)
      throws AlreadyExistsException, InvalidObjectException, MetaException, TException {
    return getPrimaryClient().create_type(type);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean drop_type(String type) throws MetaException, NoSuchObjectException, TException {
    return getPrimaryClient().drop_type(type);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public Map<String, Type> get_type_all(String name) throws MetaException, TException {
    return getPrimaryClient().get_type_all(name);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<FieldSchema> get_fields(String db_name, String table_name)
      throws MetaException, UnknownTableException, UnknownDBException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    return mapping.getClient().get_fields(mapping.transformInboundDatabaseName(db_name), table_name);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<FieldSchema> get_schema(String db_name, String table_name)
      throws MetaException, UnknownTableException, UnknownDBException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    return mapping.getClient().get_schema(mapping.transformInboundDatabaseName(db_name), table_name);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void create_table(Table tbl)
      throws AlreadyExistsException, InvalidObjectException, MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = checkWritePermissions(tbl.getDbName());
    mapping.getClient().create_table(mapping.transformInboundTable(tbl));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void create_table_with_environment_context(Table tbl, EnvironmentContext environment_context)
      throws AlreadyExistsException, InvalidObjectException, MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = checkWritePermissions(tbl.getDbName());
    mapping.getClient().create_table_with_environment_context(mapping.transformInboundTable(tbl), environment_context);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void drop_table(String dbname, String name, boolean deleteData)
      throws NoSuchObjectException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(dbname);
    mapping.getClient().drop_table(mapping.transformInboundDatabaseName(dbname), name, deleteData);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void drop_table_with_environment_context(
      String dbname,
      String name,
      boolean deleteData,
      EnvironmentContext environment_context)
      throws NoSuchObjectException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(dbname);
    mapping
        .getClient()
        .drop_table_with_environment_context(mapping.transformInboundDatabaseName(dbname), name, deleteData,
            environment_context);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<String> get_tables(String db_name, String pattern) throws MetaException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    return mapping.getClient().get_tables(mapping.transformInboundDatabaseName(db_name), pattern);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<String> get_all_tables(String db_name) throws MetaException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    return mapping.getClient().get_all_tables(mapping.transformInboundDatabaseName(db_name));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public Table get_table(String dbname, String tbl_name) throws MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(dbname);
    return mapping
        .transformOutboundTable(mapping.getClient().get_table(mapping.transformInboundDatabaseName(dbname), tbl_name));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<Table> get_table_objects_by_name(String dbname, List<String> tbl_names)
      throws MetaException, InvalidOperationException, UnknownDBException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(dbname);
    List<Table> tables = mapping
        .getClient()
        .get_table_objects_by_name(mapping.transformInboundDatabaseName(dbname), tbl_names);
    List<Table> outboundTables = new ArrayList<>(tables.size());
    for (Table table : tables) {
      outboundTables.add(mapping.transformOutboundTable(table));
    }
    return outboundTables;
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<String> get_table_names_by_filter(String dbname, String filter, short max_tables)
      throws MetaException, InvalidOperationException, UnknownDBException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(dbname);
    return mapping
        .getClient()
        .get_table_names_by_filter(mapping.transformInboundDatabaseName(dbname), filter, max_tables);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void alter_table(String dbname, String tbl_name, Table new_tbl)
      throws InvalidOperationException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(dbname);
    mapping.checkWritePermissions(new_tbl.getDbName());
    mapping
        .getClient()
        .alter_table(mapping.transformInboundDatabaseName(dbname), tbl_name, mapping.transformInboundTable(new_tbl));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void alter_table_with_environment_context(
      String dbname,
      String tbl_name,
      Table new_tbl,
      EnvironmentContext environment_context)
      throws InvalidOperationException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(dbname);
    mapping.checkWritePermissions(new_tbl.getDbName());
    mapping
        .getClient()
        .alter_table_with_environment_context(mapping.transformInboundDatabaseName(dbname), tbl_name,
            mapping.transformInboundTable(new_tbl), environment_context);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public Partition add_partition(Partition new_part)
      throws InvalidObjectException, AlreadyExistsException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(new_part.getDbName());
    Partition result = mapping.getClient().add_partition(mapping.transformInboundPartition(new_part));
    return mapping.transformOutboundPartition(result);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public Partition add_partition_with_environment_context(Partition new_part, EnvironmentContext environment_context)
      throws InvalidObjectException, AlreadyExistsException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(new_part.getDbName());
    Partition result = mapping
        .getClient()
        .add_partition_with_environment_context(mapping.transformInboundPartition(new_part), environment_context);
    return mapping.transformOutboundPartition(result);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public int add_partitions(List<Partition> new_parts)
      throws InvalidObjectException, AlreadyExistsException, MetaException, TException {
    if (!new_parts.isEmpty()) {
      // Need to pick one mapping and use that for permissions and getting the client.
      // If the partitions added are for different databases in different clients that won't work with waggle-dance
      DatabaseMapping mapping = databaseMappingService.databaseMapping(new_parts.get(0).getDbName());
      for (Partition partition : new_parts) {
        mapping.checkWritePermissions(partition.getDbName());
      }
      return mapping.getClient().add_partitions(mapping.transformInboundPartitions(new_parts));
    }
    return 0;
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public int add_partitions_pspec(List<PartitionSpec> new_parts)
      throws InvalidObjectException, AlreadyExistsException, MetaException, TException {
    if (!new_parts.isEmpty()) {
      // Need to pick one mapping and use that for permissions and getting the client.
      // If the partitions added are for different databases in different clients that won't work with waggle-dance
      DatabaseMapping mapping = databaseMappingService.databaseMapping(new_parts.get(0).getDbName());
      for (PartitionSpec partitionSpec : new_parts) {
        mapping.checkWritePermissions(partitionSpec.getDbName());
      }
      return mapping.getClient().add_partitions_pspec(mapping.transformInboundPartitionSpecs(new_parts));
    }
    return 0;
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public Partition append_partition(String db_name, String tbl_name, List<String> part_vals)
      throws InvalidObjectException, AlreadyExistsException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(db_name);
    Partition result = mapping
        .getClient()
        .append_partition(mapping.transformInboundDatabaseName(db_name), tbl_name, part_vals);
    return mapping.transformOutboundPartition(result);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public AddPartitionsResult add_partitions_req(AddPartitionsRequest request)
      throws InvalidObjectException, AlreadyExistsException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(request.getDbName());
    for (Partition partition : request.getParts()) {
      mapping.checkWritePermissions(partition.getDbName());
    }
    AddPartitionsResult result = mapping
        .getClient()
        .add_partitions_req(mapping.transformInboundAddPartitionsRequest(request));
    return mapping.transformOutboundAddPartitionsResult(result);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public Partition append_partition_with_environment_context(
      String db_name,
      String tbl_name,
      List<String> part_vals,
      EnvironmentContext environment_context)
      throws InvalidObjectException, AlreadyExistsException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(db_name);
    Partition partition = mapping
        .getClient()
        .append_partition_with_environment_context(mapping.transformInboundDatabaseName(db_name), tbl_name, part_vals,
            environment_context);
    return mapping.transformOutboundPartition(partition);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public Partition append_partition_by_name(String db_name, String tbl_name, String part_name)
      throws InvalidObjectException, AlreadyExistsException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(db_name);
    Partition partition = mapping
        .getClient()
        .append_partition_by_name(mapping.transformInboundDatabaseName(db_name), tbl_name, part_name);
    return mapping.transformOutboundPartition(partition);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public Partition append_partition_by_name_with_environment_context(
      String db_name,
      String tbl_name,
      String part_name,
      EnvironmentContext environment_context)
      throws InvalidObjectException, AlreadyExistsException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(db_name);
    Partition partition = mapping
        .getClient()
        .append_partition_by_name_with_environment_context(mapping.transformInboundDatabaseName(db_name), tbl_name,
            part_name, environment_context);
    return mapping.transformOutboundPartition(partition);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean drop_partition(String db_name, String tbl_name, List<String> part_vals, boolean deleteData)
      throws NoSuchObjectException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(db_name);
    return mapping
        .getClient()
        .drop_partition(mapping.transformInboundDatabaseName(db_name), tbl_name, part_vals, deleteData);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean drop_partition_with_environment_context(
      String db_name,
      String tbl_name,
      List<String> part_vals,
      boolean deleteData,
      EnvironmentContext environment_context)
      throws NoSuchObjectException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(db_name);
    return mapping
        .getClient()
        .drop_partition_with_environment_context(mapping.transformInboundDatabaseName(db_name), tbl_name, part_vals,
            deleteData, environment_context);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean drop_partition_by_name(String db_name, String tbl_name, String part_name, boolean deleteData)
      throws NoSuchObjectException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(db_name);
    return mapping
        .getClient()
        .drop_partition_by_name(mapping.transformInboundDatabaseName(db_name), tbl_name, part_name, deleteData);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean drop_partition_by_name_with_environment_context(
      String db_name,
      String tbl_name,
      String part_name,
      boolean deleteData,
      EnvironmentContext environment_context)
      throws NoSuchObjectException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(db_name);
    return mapping
        .getClient()
        .drop_partition_by_name_with_environment_context(mapping.transformInboundDatabaseName(db_name), tbl_name,
            part_name, deleteData, environment_context);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public DropPartitionsResult drop_partitions_req(DropPartitionsRequest req)
      throws NoSuchObjectException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(req.getDbName());
    DropPartitionsResult result = mapping
        .getClient()
        .drop_partitions_req(mapping.transformInboundDropPartitionRequest(req));
    return mapping.transformOutboundDropPartitionsResult(result);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public Partition get_partition(String db_name, String tbl_name, List<String> part_vals)
      throws MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    return mapping
        .transformOutboundPartition(
            mapping.getClient().get_partition(mapping.transformInboundDatabaseName(db_name), tbl_name, part_vals));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public Partition exchange_partition(
      Map<String, String> partitionSpecs,
      String source_db,
      String source_table_name,
      String dest_db,
      String dest_table_name)
      throws MetaException, NoSuchObjectException, InvalidObjectException, InvalidInputException, TException {
    DatabaseMapping mapping = checkWritePermissions(source_db);
    mapping.checkWritePermissions(dest_db);
    Partition result = mapping
        .getClient()
        .exchange_partition(partitionSpecs, mapping.transformInboundDatabaseName(source_db), source_table_name,
            mapping.transformInboundDatabaseName(dest_db), dest_table_name);
    return mapping.transformOutboundPartition(result);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public Partition get_partition_with_auth(
      String db_name,
      String tbl_name,
      List<String> part_vals,
      String user_name,
      List<String> group_names)
      throws MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    Partition partition = mapping
        .getClient()
        .get_partition_with_auth(mapping.transformInboundDatabaseName(db_name), tbl_name, part_vals, user_name,
            group_names);
    return mapping.transformOutboundPartition(partition);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public Partition get_partition_by_name(String db_name, String tbl_name, String part_name)
      throws MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    Partition partition = mapping
        .getClient()
        .get_partition_by_name(mapping.transformInboundDatabaseName(db_name), tbl_name, part_name);
    return mapping.transformOutboundPartition(partition);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME, prepend=true)
  public List<Partition> get_partitions(String db_name, String tbl_name, short max_parts)
      throws NoSuchObjectException, MetaException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    List<Partition> partitions = mapping
        .getClient()
        .get_partitions(mapping.transformInboundDatabaseName(db_name), tbl_name, max_parts);
    return mapping.transformOutboundPartitions(partitions);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME, prepend=true)
  public List<Partition> get_partitions_with_auth(
      String db_name,
      String tbl_name,
      short max_parts,
      String user_name,
      List<String> group_names)
      throws NoSuchObjectException, MetaException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    List<Partition> partitions = mapping
        .getClient()
        .get_partitions_with_auth(mapping.transformInboundDatabaseName(db_name), tbl_name, max_parts, user_name,
            group_names);
    return mapping.transformOutboundPartitions(partitions);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME, prepend=true)
  public List<PartitionSpec> get_partitions_pspec(String db_name, String tbl_name, int max_parts)
      throws NoSuchObjectException, MetaException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    List<PartitionSpec> partitionSpecs = mapping
        .getClient()
        .get_partitions_pspec(mapping.transformInboundDatabaseName(db_name), tbl_name, max_parts);
    return mapping.transformOutboundPartitionSpecs(partitionSpecs);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<String> get_partition_names(String db_name, String tbl_name, short max_parts)
      throws MetaException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    return mapping.getClient().get_partition_names(mapping.transformInboundDatabaseName(db_name), tbl_name, max_parts);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME, prepend=true)
  public List<Partition> get_partitions_ps(String db_name, String tbl_name, List<String> part_vals, short max_parts)
      throws MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    List<Partition> partitions = mapping
        .getClient()
        .get_partitions_ps(mapping.transformInboundDatabaseName(db_name), tbl_name, part_vals, max_parts);
    return mapping.transformOutboundPartitions(partitions);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME, prepend=true)
  public List<Partition> get_partitions_ps_with_auth(
      String db_name,
      String tbl_name,
      List<String> part_vals,
      short max_parts,
      String user_name,
      List<String> group_names)
      throws NoSuchObjectException, MetaException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    List<Partition> partitions = mapping
        .getClient()
        .get_partitions_ps_with_auth(mapping.transformInboundDatabaseName(db_name), tbl_name, part_vals, max_parts,
            user_name, group_names);
    return mapping.transformOutboundPartitions(partitions);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<String> get_partition_names_ps(String db_name, String tbl_name, List<String> part_vals, short max_parts)
      throws MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    return mapping
        .getClient()
        .get_partition_names_ps(mapping.transformInboundDatabaseName(db_name), tbl_name, part_vals, max_parts);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME, prepend=true)
  public List<Partition> get_partitions_by_filter(String db_name, String tbl_name, String filter, short max_parts)
      throws MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    List<Partition> partitions = mapping
        .getClient()
        .get_partitions_by_filter(mapping.transformInboundDatabaseName(db_name), tbl_name, filter, max_parts);
    return mapping.transformOutboundPartitions(partitions);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME, prepend=true)
  public List<PartitionSpec> get_part_specs_by_filter(String db_name, String tbl_name, String filter, int max_parts)
      throws MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    List<PartitionSpec> partitionSpecs = mapping
        .getClient()
        .get_part_specs_by_filter(mapping.transformInboundDatabaseName(db_name), tbl_name, filter, max_parts);
    return mapping.transformOutboundPartitionSpecs(partitionSpecs);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public PartitionsByExprResult get_partitions_by_expr(PartitionsByExprRequest req)
      throws MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(req.getDbName());
    PartitionsByExprResult result = mapping
        .getClient()
        .get_partitions_by_expr(mapping.transformInboundPartitionsByExprRequest(req));
    return mapping.transformOutboundPartitionsByExprResult(result);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME, prepend=true)
  public List<Partition> get_partitions_by_names(String db_name, String tbl_name, List<String> names)
      throws MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    List<Partition> partitions = mapping
        .getClient()
        .get_partitions_by_names(mapping.transformInboundDatabaseName(db_name), tbl_name, names);
    return mapping.transformOutboundPartitions(partitions);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void alter_partition(String db_name, String tbl_name, Partition new_part)
      throws InvalidOperationException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(db_name);
    mapping.checkWritePermissions(new_part.getDbName());
    mapping
        .getClient()
        .alter_partition(mapping.transformInboundDatabaseName(db_name), tbl_name,
            mapping.transformInboundPartition(new_part));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void alter_partitions(String db_name, String tbl_name, List<Partition> new_parts)
      throws InvalidOperationException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(db_name);
    for (Partition newPart : new_parts) {
      mapping.checkWritePermissions(newPart.getDbName());
    }
    mapping
        .getClient()
        .alter_partitions(mapping.transformInboundDatabaseName(db_name), tbl_name,
            mapping.transformInboundPartitions(new_parts));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void alter_partition_with_environment_context(
      String db_name,
      String tbl_name,
      Partition new_part,
      EnvironmentContext environment_context)
      throws InvalidOperationException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(db_name);
    mapping.checkWritePermissions(new_part.getDbName());
    mapping
        .getClient()
        .alter_partition_with_environment_context(mapping.transformInboundDatabaseName(db_name), tbl_name,
            mapping.transformInboundPartition(new_part), environment_context);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void rename_partition(String db_name, String tbl_name, List<String> part_vals, Partition new_part)
      throws InvalidOperationException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(db_name);
    mapping.checkWritePermissions(new_part.getDbName());
    mapping.getClient().rename_partition(mapping.transformInboundDatabaseName(db_name), tbl_name, part_vals, new_part);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean partition_name_has_valid_characters(List<String> part_vals, boolean throw_exception)
      throws MetaException, TException {
    return getPrimaryClient().partition_name_has_valid_characters(part_vals, throw_exception);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public String get_config_value(String name, String defaultValue) throws ConfigValSecurityException, TException {
    return getPrimaryClient().get_config_value(name, defaultValue);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<String> partition_name_to_vals(String part_name) throws MetaException, TException {
    return getPrimaryClient().partition_name_to_vals(part_name);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public Map<String, String> partition_name_to_spec(String part_name) throws MetaException, TException {
    return getPrimaryClient().partition_name_to_spec(part_name);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void markPartitionForEvent(
      String db_name,
      String tbl_name,
      Map<String, String> part_vals,
      PartitionEventType eventType)
      throws MetaException, NoSuchObjectException, UnknownDBException, UnknownTableException, UnknownPartitionException,
      InvalidPartitionException, TException {
    DatabaseMapping mapping = checkWritePermissions(db_name);
    mapping
        .getClient()
        .markPartitionForEvent(mapping.transformInboundDatabaseName(db_name), tbl_name, part_vals, eventType);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean isPartitionMarkedForEvent(
      String db_name,
      String tbl_name,
      Map<String, String> part_vals,
      PartitionEventType eventType)
      throws MetaException, NoSuchObjectException, UnknownDBException, UnknownTableException, UnknownPartitionException,
      InvalidPartitionException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    return mapping
        .getClient()
        .isPartitionMarkedForEvent(mapping.transformInboundDatabaseName(db_name), tbl_name, part_vals, eventType);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public Index add_index(Index new_index, Table index_table)
      throws InvalidObjectException, AlreadyExistsException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(new_index.getDbName());
    mapping.checkWritePermissions(index_table.getDbName());
    Index result = mapping
        .getClient()
        .add_index(mapping.transformInboundIndex(new_index), mapping.transformInboundTable(index_table));
    return mapping.transformOutboundIndex(result);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void alter_index(String dbname, String base_tbl_name, String idx_name, Index new_idx)
      throws InvalidOperationException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(dbname);
    mapping.checkWritePermissions(new_idx.getDbName());
    mapping
        .getClient()
        .alter_index(mapping.transformInboundDatabaseName(dbname), base_tbl_name, idx_name,
            mapping.transformInboundIndex(new_idx));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean drop_index_by_name(String db_name, String tbl_name, String index_name, boolean deleteData)
      throws NoSuchObjectException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(db_name);
    return mapping
        .getClient()
        .drop_index_by_name(mapping.transformInboundDatabaseName(db_name), tbl_name, index_name, deleteData);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public Index get_index_by_name(String db_name, String tbl_name, String index_name)
      throws MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    return mapping
        .transformOutboundIndex(
            mapping.getClient().get_index_by_name(mapping.transformInboundDatabaseName(db_name), tbl_name, index_name));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<Index> get_indexes(String db_name, String tbl_name, short max_indexes)
      throws NoSuchObjectException, MetaException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    List<Index> indexes = mapping
        .getClient()
        .get_indexes(mapping.transformInboundDatabaseName(db_name), tbl_name, max_indexes);
    return mapping.transformOutboundIndexes(indexes);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<String> get_index_names(String db_name, String tbl_name, short max_indexes)
      throws MetaException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    return mapping.getClient().get_index_names(mapping.transformInboundDatabaseName(db_name), tbl_name, max_indexes);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean update_table_column_statistics(ColumnStatistics stats_obj)
      throws NoSuchObjectException, InvalidObjectException, MetaException, InvalidInputException, TException {
    DatabaseMapping mapping = checkWritePermissions(stats_obj.getStatsDesc().getDbName());
    return mapping.getClient().update_table_column_statistics(mapping.transformInboundColumnStatistics(stats_obj));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean update_partition_column_statistics(ColumnStatistics stats_obj)
      throws NoSuchObjectException, InvalidObjectException, MetaException, InvalidInputException, TException {
    DatabaseMapping mapping = checkWritePermissions(stats_obj.getStatsDesc().getDbName());
    return mapping.getClient().update_partition_column_statistics(mapping.transformInboundColumnStatistics(stats_obj));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public ColumnStatistics get_table_column_statistics(String db_name, String tbl_name, String col_name)
      throws NoSuchObjectException, MetaException, InvalidInputException, InvalidObjectException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    ColumnStatistics result = mapping
        .getClient()
        .get_table_column_statistics(mapping.transformInboundDatabaseName(db_name), tbl_name, col_name);
    return mapping.transformOutboundColumnStatistics(result);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public ColumnStatistics get_partition_column_statistics(
      String db_name,
      String tbl_name,
      String part_name,
      String col_name)
      throws NoSuchObjectException, MetaException, InvalidInputException, InvalidObjectException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    ColumnStatistics result = mapping
        .getClient()
        .get_partition_column_statistics(mapping.transformInboundDatabaseName(db_name), tbl_name, part_name, col_name);
    return mapping.transformOutboundColumnStatistics(result);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public TableStatsResult get_table_statistics_req(TableStatsRequest request)
      throws NoSuchObjectException, MetaException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(request.getDbName());
    return mapping.getClient().get_table_statistics_req(mapping.transformInboundTableStatsRequest(request));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public PartitionsStatsResult get_partitions_statistics_req(PartitionsStatsRequest request)
      throws NoSuchObjectException, MetaException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(request.getDbName());
    return mapping.getClient().get_partitions_statistics_req(mapping.transformInboundPartitionsStatsRequest(request));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public AggrStats get_aggr_stats_for(PartitionsStatsRequest request)
      throws NoSuchObjectException, MetaException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(request.getDbName());
    return mapping.getClient().get_aggr_stats_for(mapping.transformInboundPartitionsStatsRequest(request));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean set_aggr_stats_for(SetPartitionsStatsRequest request)
      throws NoSuchObjectException, InvalidObjectException, MetaException, InvalidInputException, TException {
    if (!request.getColStats().isEmpty()) {
      DatabaseMapping mapping = databaseMappingService
          .databaseMapping(request.getColStats().get(0).getStatsDesc().getDbName());
      for (ColumnStatistics stats : request.getColStats()) {
        mapping.checkWritePermissions(stats.getStatsDesc().getDbName());
      }
      return mapping.getClient().set_aggr_stats_for(mapping.transformInboundSetPartitionStatsRequest(request));
    }
    return false;
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean delete_partition_column_statistics(String db_name, String tbl_name, String part_name, String col_name)
      throws NoSuchObjectException, MetaException, InvalidObjectException, InvalidInputException, TException {
    DatabaseMapping mapping = checkWritePermissions(db_name);
    return mapping
        .getClient()
        .delete_partition_column_statistics(mapping.transformInboundDatabaseName(db_name), tbl_name, part_name,
            col_name);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean delete_table_column_statistics(String db_name, String tbl_name, String col_name)
      throws NoSuchObjectException, MetaException, InvalidObjectException, InvalidInputException, TException {
    DatabaseMapping mapping = checkWritePermissions(db_name);
    return mapping
        .getClient()
        .delete_table_column_statistics(mapping.transformInboundDatabaseName(db_name), tbl_name, col_name);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void create_function(Function func)
      throws AlreadyExistsException, InvalidObjectException, MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = checkWritePermissions(func.getDbName());
    mapping.getClient().create_function(mapping.transformInboundFunction(func));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void drop_function(String dbName, String funcName) throws NoSuchObjectException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(dbName);
    mapping.getClient().drop_function(mapping.transformInboundDatabaseName(dbName), funcName);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void alter_function(String dbName, String funcName, Function newFunc)
      throws InvalidOperationException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(dbName);
    mapping.checkWritePermissions(newFunc.getDbName());
    mapping
        .getClient()
        .alter_function(mapping.transformInboundDatabaseName(dbName), funcName,
            mapping.transformInboundFunction(newFunc));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<String> get_functions(String dbName, String pattern) throws MetaException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(dbName);
    return mapping.getClient().get_functions(mapping.transformInboundDatabaseName(dbName), pattern);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public Function get_function(String dbName, String funcName) throws MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(dbName);
    return mapping
        .transformOutboundFunction(
            mapping.getClient().get_function(mapping.transformInboundDatabaseName(dbName), funcName));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean create_role(Role role) throws MetaException, TException {
    return getPrimaryClient().create_role(role);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean drop_role(String role_name) throws MetaException, TException {
    return getPrimaryClient().drop_role(role_name);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<String> get_role_names() throws MetaException, TException {
    return getPrimaryClient().get_role_names();
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean grant_role(
      String role_name,
      String principal_name,
      PrincipalType principal_type,
      String grantor,
      PrincipalType grantorType,
      boolean grant_option)
      throws MetaException, TException {
    return getPrimaryClient().grant_role(role_name, principal_name, principal_type, grantor, grantorType, grant_option);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean revoke_role(String role_name, String principal_name, PrincipalType principal_type)
      throws MetaException, TException {
    return getPrimaryClient().revoke_role(role_name, principal_name, principal_type);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<Role> list_roles(String principal_name, PrincipalType principal_type) throws MetaException, TException {
    return getPrimaryClient().list_roles(principal_name, principal_type);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public GrantRevokeRoleResponse grant_revoke_role(GrantRevokeRoleRequest request) throws MetaException, TException {
    return getPrimaryClient().grant_revoke_role(request);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public GetPrincipalsInRoleResponse get_principals_in_role(GetPrincipalsInRoleRequest request)
      throws MetaException, TException {
    return getPrimaryClient().get_principals_in_role(request);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public GetRoleGrantsForPrincipalResponse get_role_grants_for_principal(GetRoleGrantsForPrincipalRequest request)
      throws MetaException, TException {
    return getPrimaryClient().get_role_grants_for_principal(request);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public PrincipalPrivilegeSet get_privilege_set(HiveObjectRef hiveObject, String user_name, List<String> group_names)
      throws MetaException, TException {
    DatabaseMapping mapping;
    if (hiveObject.getDbName() == null) {
      mapping = databaseMappingService.primaryDatabaseMapping();
    } else {
      mapping = databaseMappingService.databaseMapping(hiveObject.getDbName());
    }
    return mapping.getClient().get_privilege_set(mapping.transformInboundHiveObjectRef(hiveObject), user_name,
            group_names);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<HiveObjectPrivilege> list_privileges(
      String principal_name,
      PrincipalType principal_type,
      HiveObjectRef hiveObject)
      throws MetaException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(hiveObject.getDbName());
    List<HiveObjectPrivilege> privileges = mapping
        .getClient()
        .list_privileges(principal_name, principal_type, mapping.transformInboundHiveObjectRef(hiveObject));
    return mapping.transformOutboundHiveObjectPrivileges(privileges);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean grant_privileges(PrivilegeBag privileges) throws MetaException, TException {
    if (privileges.isSetPrivileges() && !privileges.getPrivileges().isEmpty()) {
      DatabaseMapping mapping = databaseMappingService
          .databaseMapping(privileges.getPrivileges().get(0).getHiveObject().getDbName());
      for (HiveObjectPrivilege privilege : privileges.getPrivileges()) {
        HiveObjectRef obj = privilege.getHiveObject();
        mapping.checkWritePermissions(obj.getDbName());
        if (obj.getObjectType() == HiveObjectType.DATABASE) {
          mapping.checkWritePermissions(obj.getObjectName());
        }
      }
      return mapping.getClient().grant_privileges(mapping.transformInboundPrivilegeBag(privileges));
    }
    return false;
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean revoke_privileges(PrivilegeBag privileges) throws MetaException, TException {
    if (privileges.isSetPrivileges() && !privileges.getPrivileges().isEmpty()) {
      DatabaseMapping mapping = databaseMappingService
          .databaseMapping(privileges.getPrivileges().get(0).getHiveObject().getDbName());
      for (HiveObjectPrivilege privilege : privileges.getPrivileges()) {
        HiveObjectRef obj = privilege.getHiveObject();
        mapping.checkWritePermissions(obj.getDbName());
        if (obj.getObjectType() == HiveObjectType.DATABASE) {
          mapping.checkWritePermissions(obj.getObjectName());
        }
      }
      return mapping.getClient().revoke_privileges(mapping.transformInboundPrivilegeBag(privileges));
    }
    return false;
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public GrantRevokePrivilegeResponse grant_revoke_privileges(GrantRevokePrivilegeRequest request)
      throws MetaException, TException {
    PrivilegeBag privilegesBag = request.getPrivileges();
    if (privilegesBag.isSetPrivileges() && !privilegesBag.getPrivileges().isEmpty()) {
      DatabaseMapping mapping = databaseMappingService
          .databaseMapping(privilegesBag.getPrivileges().get(0).getHiveObject().getDbName());
      for (HiveObjectPrivilege privilege : privilegesBag.getPrivileges()) {
        HiveObjectRef obj = privilege.getHiveObject();
        checkWritePermissions(obj.getDbName());
        if (obj.getObjectType() == HiveObjectType.DATABASE) {
          checkWritePermissions(obj.getObjectName());
        }
      }
      return mapping.getClient().grant_revoke_privileges(mapping.transformInboundGrantRevokePrivilegesRequest(request));
    }
    return getPrimaryClient().grant_revoke_privileges(request);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<String> set_ugi(String user_name, List<String> group_names) throws MetaException, TException {
    List<DatabaseMapping> mappings = databaseMappingService.getDatabaseMappings();
    return databaseMappingService.getPanopticOperationHandler().setUgi(user_name, group_names, mappings);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public String get_delegation_token(String token_owner, String renewer_kerberos_principal_name)
      throws MetaException, TException {
    return getPrimaryClient().get_delegation_token(token_owner, renewer_kerberos_principal_name);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public long renew_delegation_token(String token_str_form) throws MetaException, TException {
    return getPrimaryClient().renew_delegation_token(token_str_form);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void cancel_delegation_token(String token_str_form) throws MetaException, TException {
    getPrimaryClient().cancel_delegation_token(token_str_form);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public GetOpenTxnsResponse get_open_txns() throws TException {
    return getPrimaryClient().get_open_txns();
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public GetOpenTxnsInfoResponse get_open_txns_info() throws TException {
    return getPrimaryClient().get_open_txns_info();
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public OpenTxnsResponse open_txns(OpenTxnRequest rqst) throws TException {
    return getPrimaryClient().open_txns(rqst);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void abort_txn(AbortTxnRequest rqst) throws NoSuchTxnException, TException {
    getPrimaryClient().abort_txn(rqst);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void commit_txn(CommitTxnRequest rqst) throws NoSuchTxnException, TxnAbortedException, TException {
    getPrimaryClient().commit_txn(rqst);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public LockResponse lock(LockRequest rqst) throws NoSuchTxnException, TxnAbortedException, TException {
    DatabaseMapping mapping = databaseMappingService.primaryDatabaseMapping();
    List<LockComponent> components = rqst.getComponent();
    for (LockComponent component : components) {
      mapping.checkWritePermissions(component.getDbname());
    }
    return mapping.getClient().lock(mapping.transformInboundLockRequest(rqst));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public LockResponse check_lock(CheckLockRequest rqst)
      throws NoSuchTxnException, TxnAbortedException, NoSuchLockException, TException {
    return getPrimaryClient().check_lock(rqst);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void unlock(UnlockRequest rqst) throws NoSuchLockException, TxnOpenException, TException {
    getPrimaryClient().unlock(rqst);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public ShowLocksResponse show_locks(ShowLocksRequest rqst) throws TException {
    return getPrimaryClient().show_locks(rqst);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void heartbeat(HeartbeatRequest ids)
      throws NoSuchLockException, NoSuchTxnException, TxnAbortedException, TException {
    getPrimaryClient().heartbeat(ids);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public HeartbeatTxnRangeResponse heartbeat_txn_range(HeartbeatTxnRangeRequest txns) throws TException {
    return getPrimaryClient().heartbeat_txn_range(txns);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void compact(CompactionRequest rqst) throws TException {
    DatabaseMapping mapping = databaseMappingService.primaryDatabaseMapping();
    mapping.checkWritePermissions(rqst.getDbname());
    mapping.getClient().compact(mapping.transformInboundCompactionRequest(rqst));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public ShowCompactResponse show_compact(ShowCompactRequest rqst) throws TException {
    return getPrimaryClient().show_compact(rqst);
  }

  @Override
  public String getCpuProfile(int arg0) throws TException {
    return getPrimaryClient().getCpuProfile(arg0);
  }

  @Override
  public String getVersion() throws TException {
    return getPrimaryClient().getVersion();
  }

  @Override
  public fb_status getStatus() {
    try {
      return getPrimaryClient().getStatus();
    } catch (TException e) {
      LOG.error("Cannot getStatus() from client: ", e);
      return fb_status.DEAD;
    }
  }

  @Override
  public Configuration getConf() {
    return conf;
  }

  @Override
  public void setConf(Configuration conf) {
    this.conf = conf;
  }

  @Override
  public void init() throws MetaException {}

  // Hive 2.1.0 methods
  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void abort_txns(AbortTxnsRequest rqst) throws NoSuchTxnException, TException {
    getPrimaryClient().abort_txns(rqst);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void add_dynamic_partitions(AddDynamicPartitions rqst)
      throws NoSuchTxnException, TxnAbortedException, TException {
    DatabaseMapping mapping = checkWritePermissions(rqst.getDbname());
    mapping.getClient().add_dynamic_partitions(mapping.transformInboundAddDynamicPartitions(rqst));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void add_foreign_key(AddForeignKeyRequest req) throws NoSuchObjectException, MetaException, TException {
    getPrimaryClient().add_foreign_key(req);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public int add_master_key(String key) throws MetaException, TException {
    return getPrimaryClient().add_master_key(key);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void add_primary_key(AddPrimaryKeyRequest req) throws NoSuchObjectException, MetaException, TException {
    getPrimaryClient().add_primary_key(req);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean add_token(String token_identifier, String delegation_token) throws TException {
    return getPrimaryClient().add_token(token_identifier, delegation_token);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void alter_partitions_with_environment_context(
      String db_name,
      String tbl_name,
      List<Partition> new_parts,
      EnvironmentContext environment_context)
      throws InvalidOperationException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(db_name);
    mapping
        .getClient()
        .alter_partitions_with_environment_context(mapping.transformInboundDatabaseName(db_name), tbl_name, new_parts,
            environment_context);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void alter_table_with_cascade(String dbname, String tbl_name, Table new_tbl, boolean cascade)
      throws InvalidOperationException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(dbname);
    mapping
        .getClient()
        .alter_table_with_cascade(mapping.transformInboundDatabaseName(dbname), tbl_name, new_tbl, cascade);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public CacheFileMetadataResult cache_file_metadata(CacheFileMetadataRequest req) throws TException {
    DatabaseMapping mapping = databaseMappingService.primaryDatabaseMapping();
    mapping.checkWritePermissions(req.getDbName());
    return mapping.getClient().cache_file_metadata(mapping.transformInboundCacheFileMetadataRequest(req));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public ClearFileMetadataResult clear_file_metadata(ClearFileMetadataRequest req) throws TException {
    return getPrimaryClient().clear_file_metadata(req);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void create_table_with_constraints(Table tbl, List<SQLPrimaryKey> primaryKeys, List<SQLForeignKey> foreignKeys)
      throws AlreadyExistsException, InvalidObjectException, MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = checkWritePermissions(tbl.getDbName());
    mapping.getClient().create_table_with_constraints(mapping.transformInboundTable(tbl), primaryKeys, foreignKeys);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void drop_constraint(DropConstraintRequest req) throws NoSuchObjectException, MetaException, TException {
    DatabaseMapping mapping = checkWritePermissions(req.getDbname());
    mapping.getClient().drop_constraint(mapping.transformInboundDropConstraintRequest(req));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<Partition> exchange_partitions(
      Map<String, String> partitionSpecs,
      String source_db,
      String source_table_name,
      String dest_db,
      String dest_table_name)
      throws MetaException, NoSuchObjectException, InvalidObjectException, InvalidInputException, TException {
    DatabaseMapping mapping = checkWritePermissions(source_db);
    mapping.checkWritePermissions(dest_db);
    List<Partition> result = mapping
        .getClient()
        .exchange_partitions(partitionSpecs, mapping.transformInboundDatabaseName(source_db), source_table_name,
            mapping.transformInboundDatabaseName(dest_db), dest_table_name);
    return mapping.transformOutboundPartitions(result);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public FireEventResponse fire_listener_event(FireEventRequest rqst) throws TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(rqst.getDbName());
    return mapping.getClient().fire_listener_event(mapping.transformInboundFireEventRequest(rqst));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void flushCache() throws TException {
    getPrimaryClient().flushCache();
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public GetAllFunctionsResponse get_all_functions() throws MetaException, TException {
    return getPrimaryClient().get_all_functions();
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<String> get_all_token_identifiers() throws TException {
    return getPrimaryClient().get_all_token_identifiers();
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public CurrentNotificationEventId get_current_notificationEventId() throws TException {
    return getPrimaryClient().get_current_notificationEventId();
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<FieldSchema> get_fields_with_environment_context(
      String db_name,
      String table_name,
      EnvironmentContext environment_context)
      throws MetaException, UnknownTableException, UnknownDBException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    return mapping
        .getClient()
        .get_fields_with_environment_context(mapping.transformInboundDatabaseName(db_name), table_name,
            environment_context);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public GetFileMetadataResult get_file_metadata(GetFileMetadataRequest req) throws TException {
    return getPrimaryClient().get_file_metadata(req);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public GetFileMetadataByExprResult get_file_metadata_by_expr(GetFileMetadataByExprRequest req) throws TException {
    return getPrimaryClient().get_file_metadata_by_expr(req);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public ForeignKeysResponse get_foreign_keys(ForeignKeysRequest request)
      throws MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(request.getForeign_db_name());
    return mapping
        .transformOutboundForeignKeysResponse(
            mapping.getClient().get_foreign_keys(mapping.transformInboundForeignKeysRequest(request)));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<String> get_master_keys() throws TException {
    return getPrimaryClient().get_master_keys();
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public NotificationEventResponse get_next_notification(NotificationEventRequest rqst) throws TException {
    return getPrimaryClient().get_next_notification(rqst);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public int get_num_partitions_by_filter(String db_name, String tbl_name, String filter)
      throws MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    return mapping
        .getClient()
        .get_num_partitions_by_filter(mapping.transformInboundDatabaseName(db_name), tbl_name, filter);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public PrimaryKeysResponse get_primary_keys(PrimaryKeysRequest request)
      throws MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(request.getDb_name());
    return mapping
        .transformOutboundPrimaryKeysResponse(
            mapping.getClient().get_primary_keys(mapping.transformInboundPrimaryKeysRequest(request)));
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<FieldSchema> get_schema_with_environment_context(
      String db_name,
      String table_name,
      EnvironmentContext environment_context)
      throws MetaException, UnknownTableException, UnknownDBException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    return mapping
        .getClient()
        .get_schema_with_environment_context(mapping.transformInboundDatabaseName(db_name), table_name,
            environment_context);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<TableMeta> get_table_meta(String db_patterns, String tbl_patterns, List<String> tbl_types)
      throws MetaException, TException {
    return databaseMappingService.getPanopticOperationHandler()
        .getTableMeta(db_patterns, tbl_patterns, tbl_types);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public String get_token(String token_identifier) throws TException {
    return getPrimaryClient().get_token(token_identifier);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public PutFileMetadataResult put_file_metadata(PutFileMetadataRequest req) throws TException {
    return getPrimaryClient().put_file_metadata(req);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean remove_master_key(int key_seq) throws TException {
    return getPrimaryClient().remove_master_key(key_seq);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public boolean remove_token(String token_identifier) throws TException {
    return getPrimaryClient().remove_token(token_identifier);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public void update_master_key(int seq_number, String key) throws NoSuchObjectException, MetaException, TException {
    getPrimaryClient().update_master_key(seq_number, key);
  }

  // Hive 2.3.0 methods
  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public List<String> get_tables_by_type(String db_name, String pattern, String tableType)
      throws MetaException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(db_name);
    return mapping.getClient().get_tables_by_type(mapping.transformInboundDatabaseName(db_name), pattern, tableType);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public GetTableResult get_table_req(GetTableRequest req) throws MetaException, NoSuchObjectException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(req.getDbName());
    GetTableResult result = mapping.getClient().get_table_req(mapping.transformInboundGetTableRequest(req));
    return mapping.transformOutboundGetTableResult(result);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public GetTablesResult get_table_objects_by_name_req(GetTablesRequest req)
      throws MetaException, InvalidOperationException, UnknownDBException, TException {
    DatabaseMapping mapping = databaseMappingService.databaseMapping(req.getDbName());
    GetTablesResult result = mapping
        .getClient()
        .get_table_objects_by_name_req(mapping.transformInboundGetTablesRequest(req));
    return mapping.transformOutboundGetTablesResult(result);
  }

  @Override
  @Loggable(value = Loggable.DEBUG, skipResult = true, name = INVOCATION_LOG_NAME)
  public CompactionResponse compact2(CompactionRequest rqst) throws TException {
    DatabaseMapping mapping = databaseMappingService.primaryDatabaseMapping();
    mapping.checkWritePermissions(rqst.getDbname());
    return mapping.getClient().compact2(mapping.transformInboundCompactionRequest(rqst));
  }
}
