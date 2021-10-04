connect('system', 'weblogic', 't3://localhost:7001')
pool="myPool"
server="managed"
cd("Servers/"+server)
target=cmo
cd("../..")
#start pool config
print 'Creating JDBCConnectionPool with name ' + pool
create(pool, "JDBCConnectionPool")
cd('JDBCConnectionPools/' + pool)
cmo.setDriverName("com.pointbase.jdbc.jdbcUniversalDriver")
cmo.setURL("jdbc:pointbase:server://localhost/demo")
props=makePropertiesObject("user=PBPUBLIC")
cmo.setProperties(props)
cmo.setPassword("PBPUBLIC")
cmo.setMaxCapacity(5)
cmo.addTarget(target)
print 'Done Configuring.'





