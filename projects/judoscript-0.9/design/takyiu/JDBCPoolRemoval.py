connect('system', 'weblogic', 't3://localhost:7001')
pool="myPool"
server="managed"
cd("Servers/"+server)
target=cmo
cd("../..")
#start pool removal
print 'Untargeting JDBCConnectionPool with name ' + pool + '...'
cd('JDBCConnectionPools/' + pool)
cmo.removeTarget(target)
print 'Done un-targeting.'

print 'Removing JDBCConnectionPool with name ' + pool + '...'
cd("../..")
delete(pool, "JDBCConnectionPool")
print 'Done removing.'




