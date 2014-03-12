openfire-roomservice-plugin
===========================

Openfire plugin to allows administration of rooms via HTTP requests.

Configuration page included into plugin like userservice plugin.
The service address is [hostname]/plugins/roomservice/roomService
Parameters:
* jid [add (full jid), add2 (just jid_name)]
* secret
* subdomain [add, add2]
* type
* description [add, add2]
* roomname [add, add2]
* jidresource [add2]
* jiddomain [add2]
* roomnaturalname [add2]
* subject [add2]
* maxusers [add2]


Supported type: 'add', add2 (add extended), 'delete', 'createMultiUserChatService' and 'removeMultiUserChatService' (updateMultiUserChatService -> hidden)

Example
http://10.10.1.32:9090/plugins/roomService/roomservice?type=createMultiUserChatService&secret=pWLz65KU&subdomain=demo1&description=this+is+a+demo+testing

http://10.10.1.32:9090/plugins/roomService/roomservice?type=add2&roomname=entryname&subdomain=hpc&secret=ESbSemyLQ%40Gk9CxwGmC&jid=hp.hpc&jidresource=chat1&jiddomain=chat1&description=this+room+is+created+by+default&roomnaturalname=Entry+Name&subject=test+room&maxusers=10

Reference http://www.igniterealtime.org/projects/openfire/plugins/roomservice/readme.html

Remember to configure plugin in Openfire admin console.
