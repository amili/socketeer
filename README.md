socketeer
=========

A proxy with a twist

A project born out of the need to make a different and specific type of VPN.

It currently uses a STOMP tunneling, but is meant to use more than that.

Has frontends in a manner simillar to SOCKS4(a)/SOCKS5.



An example configuration:


socketeer.general.messagingserverenabled=true
socketeer.general.messagingserverport=61626

 #TODO
 #socketeer.general.sosisize=
 #socketeer.general.relaysize=

socketeer.sourcesink.0.clustername=c
socketeer.sourcesink.0.nodename=si
socketeer.sourcesink.0.channel=stomp:localhost:61626/sisotopic/user&pass
socketeer.sourcesink.0.isSpawningAndResolvingSink=true

socketeer.sourcesink.1.clustername=c
socketeer.sourcesink.1.nodename=so
socketeer.sourcesink.1.channel=stomp:localhost:61626/sisotopic/user&pass
socketeer.sourcesink.1.serverport=1234
socketeer.sourcesink.1.isSpawningAndResolvingSink=false
socketeer.sourcesink.1.peernode=si
socketeer.sourcesink.1.fixedHost=localhost
socketeer.sourcesink.1.fixedPort=80
socketeer.sourcesink.1.fixedType=0

 # access list
 #socketeer.sourcesink.1.accesslist=0
 #socketeer.accesslist.0.host=129.0.0.1

 # blacklist
 #socketeer.sourcesink.1.blacklist=0
 #socketeer.blacklist.0.cluster=c
 #socketeer.blacklist.0.node=si
 #socketeer.blacklist.0.host=localhost
 #socketeer.blacklist.0.port=80

 # whitelist
 #socketeer.sourcesink.1.whitelist=0
 #socketeer.whitelist.0.cluster=c
 #socketeer.whitelist.0.node=si
 #socketeer.whitelist.0.host=localhost
 #socketeer.whitelist.0.port=80

 # maplist
 #socketeer.sourcesink.1.maplist=0
 #socketeer.maplist.0.orghost=localhost
 #socketeer.maplist.0.orgport=80
 #socketeer.maplist.0.maphost=localhost
 #socketeer.maplist.0.mapport=90

 # proxy init
socketeer.sourcesink.1.socks4enabled=true
socketeer.sourcesink.1.socks5enabled=true
 #socketeer.sourcesink.1.httpenabled=true

 # TODO: content redirect
 #socketeer.sourcesink.1.contentredirect=0
socketeer.contentredirect.0.rulefirst=e
socketeer.contentredirect.0.ruleline=
socketeer.contentredirect.0.host=
socketeer.contentredirect.0.port=

 #TODO: planned, channelconfig (prefix,parameters)
socketeer.channel.0.port=
