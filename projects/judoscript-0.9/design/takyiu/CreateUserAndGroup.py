connect('system', 'weblogic', 't3://localhost:7001')
domain="JavaOneDomain"
cd("/")
cd("Servers/"+server)
target=cmo
cd("../..")
cd("SecurityConfiguration/"+domain)
cd("weblogic.security.providers.authentication.DefaultAuthenticator/Security:Name=myrealmDefaultAuthenticator")
cmo.createGroup("managers", "managers Group")
cmo.createUser("joe", "weblogic", "manager User")
cmo.createUser("mary", "weblogic", "employee User")
cmo.addMemberToGroup("managers", "joe")




